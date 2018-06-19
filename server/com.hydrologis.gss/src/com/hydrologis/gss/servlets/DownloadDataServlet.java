package com.hydrologis.gss.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;

import com.gasleaksensors.databases.v2.core.handlers.DatabaseHandler;
import com.gasleaksensors.databases.v2.core.objects.Devices;
import com.gasleaksensors.libsV2.IDbProvider;
import com.gasleaksensors.libsV2.ITransferProtocolHandler;
import com.gasleaksensors.libsV2.SensitContextV2;
import com.hydrologis.gss.utils.SensitDatabaseUtilities;
import com.j256.ormlite.stmt.QueryBuilder;

import eu.hydrologis.stage.libs.utils.NetworkUtilities;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * Download servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class DownloadDataServlet extends HttpServlet {

    private static final String TAG = DownloadDataServlet.class.getSimpleName();
    private static final String NO_PERMISSION = "No permission! Contact your system administrator.";
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        Logger logDb = StageWorkspace.getInstance().getLogDb();
        String ipAddress = "unknown";
        String userStr = "unknown";
        try {
            
            IDbProvider dbp = GssContext.instance().getRunningWorkDbProvider();
            DatabaseHandler databaseHandler = dbp.getDatabaseHandler();
            ASpatialDb db = dbp.getDb();
            QueryBuilder<Devices, ? > devicesQB = databaseHandler.getDao(Devices.class).queryBuilder();

            
            ITransferProtocolHandler ph = GssContext.instance().getProtocolHandler();

            String authHeader = request.getHeader("Authorization");
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            if (userPwd == null) {
                throw new ServletException(NO_PERMISSION);
            }
            userStr = userPwd[0];

            Devices downloadingDevice = SensitDatabaseUtilities.getDeviceByUniqueId(devicesQB, userPwd[0]);
            if (downloadingDevice == null) {
                throw new ServletException(NO_PERMISSION);
            }

            ipAddress = NetworkUtilities.getIpAddress(request);
            logDb.insert(EMessageType.ACCESS, TAG, "Download connection from " + userStr + " at ip:" + ipAddress);

            HashMap<Long, Devices> lastTs2Device = new HashMap<>();
            byte[] data = ph.getDownloadDataForDevices(db, downloadingDevice, lastTs2Device);
            response.setHeader("Content-Type", NetworkUtilities.CONTENTTYPE);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(data);
            outputStream.flush();

            logDb.insert(EMessageType.ACCESS, TAG,
                    "Download connection from " + userStr + " at ip:" + ipAddress + " completed properly.");

            /*
             * if everything went well, the last gpspoint downloaded has to be inserted
             * for each other user. 
             */
            for( Entry<Long, Devices> deviceEntries : lastTs2Device.entrySet() ) {
                long lastTs = deviceEntries.getKey();
                Devices device = deviceEntries.getValue();
                if (true) {
                    throw new RuntimeException("TODO");
                }
//                SensitDatabaseUtilities.updateLastDownloadedWorkTimestampForMachine(downloadConnectionsHandler, downloadingDevice.getId(),
//                        device.getId(), lastTs);
            }

        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Download connection from " + userStr + " at ip:" + ipAddress + " errored with:\n", ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*
             * if there are problems, return some information.
             */
            String msg = "An error occurred while gathering the data from the server.";
            String className = this.getClass().getSimpleName();

            ServletUtilities.sendErrorStatus(response, ex, msg, className);
        }
    }

}