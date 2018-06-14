package com.hydrologis.gss.servlets;

import java.io.IOException;

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
 * Admin data download servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class DownloadAdminServlet extends HttpServlet {
    private static final String TAG = DownloadAdminServlet.class.getSimpleName();
    private static final String NO_PERMISSION = "No permission! Contact your system administrator.";
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        IDbProvider runningWorkDbProvider = GssContext.instance().getRunningWorkDbProvider();
        ASpatialDb db = runningWorkDbProvider.getDb();
        Logger logDb = StageWorkspace.getInstance().getLogDb();
        String ipAddress = "unknown";
        String userStr = "unknown";
        try {

            String authHeader = request.getHeader("Authorization");
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            if (userPwd == null) {
                throw new ServletException(NO_PERMISSION);
            }
            userStr = userPwd[0];
            String pwdStr = userPwd[1];

            ITransferProtocolHandler protocolHandler = GssContext.instance().getProtocolHandler();

            DatabaseHandler databaseHandler = runningWorkDbProvider.getDatabaseHandler();
            QueryBuilder<Devices, ? > devicesQB = databaseHandler.getDao(Devices.class).queryBuilder();
            Devices downloadingDevice = SensitDatabaseUtilities.getDeviceByUniqueId(devicesQB, userPwd[0]);

            if (downloadingDevice == null || pwdStr == null || !pwdStr.equals(protocolHandler.getAdminPassword())) {
                throw new ServletException(NO_PERMISSION);
            }

            ipAddress = NetworkUtilities.getIpAddress(request);
            logDb.insert(EMessageType.ACCESS, TAG, "Download Admin connection from " + userStr + " at ip:" + ipAddress);

            byte[] adminData = protocolHandler.getAdminData(db);

            response.setHeader("Content-Type", NetworkUtilities.CONTENTTYPE);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(adminData);
            outputStream.flush();

            logDb.insert(EMessageType.ACCESS, TAG,
                    "Download Admin connection from " + userStr + " at ip:" + ipAddress + " completed properly.");

        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Download Admin connection from " + userStr + " at ip:" + ipAddress + " errored with:\n",
                        ex);
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