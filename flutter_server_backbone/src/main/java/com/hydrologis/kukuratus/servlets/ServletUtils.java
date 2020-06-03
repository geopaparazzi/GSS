/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.kukuratus.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.registry.RegistryHandler;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.utils.KukuratusSession;
import com.hydrologis.kukuratus.utils.KukuratusStatus;
import com.hydrologis.kukuratus.utils.Messages;
import com.hydrologis.kukuratus.utils.NetworkUtilities;
import com.j256.ormlite.dao.Dao;

import spark.Request;
import spark.Response;

public class ServletUtils {
    public static String MOBILE_UPLOAD_PWD = ""; // set this at startup
    private static boolean DEBUG = true;
    private static final String NO_PERMISSION = Messages.getString("ServletUtils.no_permission"); //$NON-NLS-1$

    public static final int MAX_UPLOAD_SIZE = 50 * 1024 * 1024; // 50MB
    public static String tmpDir;
    static {
        tmpDir = System.getProperty("java.io.tmpdir");
    }

    public static Object canProceed(Request request, Response response, String tag) throws Exception {
        String tagPart = ""; //$NON-NLS-1$
        if (tag != null)
            tagPart = " (" + tag + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        String ipAddress = NetworkUtilities.getIpAddress(request.raw());

        String authHeader = request.headers("Authorization"); //$NON-NLS-1$
        String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
        if (userPwd == null || !userPwd[1].equals(MOBILE_UPLOAD_PWD)) { // $NON-NLS-1$
            String msg = ""; //$NON-NLS-1$
            if (userPwd != null && userPwd[0] != null && userPwd[1] != null) {
                msg = Messages.getString("ServletUtils.permission_denied") + ipAddress //$NON-NLS-1$
                        + Messages.getString("ServletUtils.for_user") + userPwd[0] //$NON-NLS-1$
                        + Messages.getString("ServletUtils.with_pwd") //$NON-NLS-1$
                        + userPwd[1];
            } else {
                msg = Messages.getString("ServletUtils.permission_denied") + ipAddress; //$NON-NLS-1$
            }
            logAccess(msg);
            KukuratusStatus errStatus = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, NO_PERMISSION,
                    new RuntimeException());
            return errStatus;
        }
        String deviceId = userPwd[0];

        DatabaseHandler dbHandler = DatabaseHandler.instance();
        Dao<GpapUsers, ?> usersDao = dbHandler.getDao(GpapUsers.class);
        GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId).queryForFirst();
        if (gpapUser == null) {
            String limitStr = RegistryHandler.INSTANCE
                    .getGlobalSettingByKey(KukuratusSession.KEY_AUTOMATIC_REGISTRATION, "0"); //$NON-NLS-1$
            long limit = Long.parseLong(limitStr);
            long now = System.currentTimeMillis();
            double deltaMinutes = (now - limit) / 1000.0;
            if (deltaMinutes < KukuratusSession.timerSeconds) {
                // register the device automatically
                gpapUser = new GpapUsers(deviceId, deviceId, "", 1); //$NON-NLS-1$//$NON-NLS-2$
                usersDao.create(gpapUser);
            } else {
                logAccess(Messages.getString("ServletUtils.permission_denied") + ipAddress
                // $NON-NLS-1$
                        + Messages.getString("ServletUtils.for_device") + deviceId + tagPart
                        // $NON-NLS-1$
                        + Messages.getString("ServletUtils.no_permission_error")); //$NON-NLS-1$
                KukuratusStatus errStatus = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, NO_PERMISSION,
                        new RuntimeException());
                return errStatus;
            }
        }

        debug("Connection from: " + gpapUser.name + tagPart); //$NON-NLS-1$
        return deviceId;
    }

    public static void sendJsonString(HttpServletResponse response, String jsonToSend) throws IOException {
        response.setHeader("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
        PrintWriter writer = response.getWriter();
        writer.print(jsonToSend);
        writer.flush();
    }

    public static void setContentLength(HttpServletResponse response, long length) {
        if (length <= Integer.MAX_VALUE) {
            response.setContentLength((int) length);
        } else {
            response.addHeader("Content-Length", Long.toString(length)); //$NON-NLS-1$
        }
    }

    public static void debug(String msg) {
        if (DEBUG) {
            KukuratusLogger.logDebug("ServletUtils", msg); //$NON-NLS-1$
        }
    }

    public static void logAccess(String msg) {
        KukuratusLogger.logAccess("ServletUtils", msg); //$NON-NLS-1$
    }

    public static void printHeaders(HttpServletRequest request, HttpServletResponse response) {
        if (DEBUG) {
            Enumeration<String> headerNames = request.getHeaderNames();
            KukuratusLogger.logDebug("ServletUtils", "REQUEST HEADERS"); //$NON-NLS-1$ //$NON-NLS-2$
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (headerName != null) {
                    String header = request.getHeader(headerName);
                    KukuratusLogger.logDebug("ServletUtils", "\t#--> " + headerName + "=" + header); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            Collection<String> headerNames1 = response.getHeaderNames();
            KukuratusLogger.logDebug("ServletUtils", "RESPONSE HEADERS"); //$NON-NLS-1$ //$NON-NLS-2$
            for (String headerName : headerNames1) {
                if (headerName != null) {
                    String header = request.getHeader(headerName);
                    KukuratusLogger.logDebug("ServletUtils", "\t*--> " + headerName + "=" + header); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
        }
    }

}
