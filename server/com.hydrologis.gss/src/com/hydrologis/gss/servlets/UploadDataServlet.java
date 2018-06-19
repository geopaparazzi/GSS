package com.hydrologis.gss.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
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
public class UploadDataServlet extends HttpServlet {
    private static final String TAG = UploadDataServlet.class.getSimpleName();
    private static final String NO_PERMISSION = "No permission! Contact your system administrator.";
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

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
            Devices uploadingDevice = SensitDatabaseUtilities.getDeviceByUniqueId(devicesQB, userPwd[0]);
            if (uploadingDevice == null) {
                throw new ServletException(NO_PERMISSION);
            }

            ipAddress = NetworkUtilities.getIpAddress(request);
            userStr = uploadingDevice.uniqueid;
            logDb.insert(EMessageType.ACCESS, TAG, "Upload connection from machine '" + userStr + "' at ip:" + ipAddress);

            ServletInputStream inputStream = request.getInputStream();

            // try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int read = 0;
                final byte[] bytes = new byte[1024];
                while( (read = inputStream.read(bytes)) != -1 ) {
                    baos.write(bytes, 0, read);
                }
                byte[] byteArray = baos.toByteArray();
                if (byteArray.length > 0) {

                    String errMsg = ph.putUploadedData(db, uploadingDevice, byteArray);
                    if (errMsg != null) {
                        String msg = "Data not inserted due to problems in retrieved data: " + errMsg;
                        String className = this.getClass().getSimpleName();
                        ServletUtilities.sendErrorStatus(response, new RuntimeException(), msg, className);
                        return;
                    }

                }
                /*
                 * if everything went well return ok status
                 */

                byte[] okStatusBytes = ph.getOkStatusBytes("Data properly synchronized.");
                response.setHeader("Content-Type", NetworkUtilities.CONTENTTYPE);
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(okStatusBytes);
                outputStream.flush();
            }

            logDb.insert(EMessageType.ACCESS, TAG,
                    "Upload connection from '" + userStr + "' at ip:" + ipAddress + " completed properly.");

        } catch (Exception ex) {
            try {
                logDb.insertError(TAG, "Upload connection from '" + userStr + "' at ip:" + ipAddress + " errored with:\n", ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*
             * if there are problems, return some information.
             */
            String msg = "An error occurred while uploading data to the server.";
            String className = this.getClass().getSimpleName();
            ServletUtilities.sendErrorStatus(response, ex, msg, className);
        }
    }

}