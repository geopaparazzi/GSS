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
package com.hydrologis.gss.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.servlets.Status;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.NetworkUtilities;
import com.j256.ormlite.dao.Dao;

public class ServletUtils {
    private static boolean DEBUG = true;
    private static final String NO_PERMISSION = "No permission! Contact your system administrator.";

    public static String canProceed( HttpServletRequest request, HttpServletResponse response, String tag ) throws Exception {
        String tagPart = "";
        if (tag != null)
            tagPart = " (" + tag + ")";
        String authHeader = request.getHeader("Authorization");
        String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
        if (userPwd == null || !userPwd[1].equals("gss_Master_Survey_Forever_2018")) {
            Status errStatus = new Status(Status.CODE_403_FORBIDDEN, NO_PERMISSION);
            errStatus.sendTo(response);
            return null;
        }
        String deviceId = userPwd[0];

        DatabaseHandler dbHandler = SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get();
        Dao<GpapUsers, ? > usersDao = dbHandler.getDao(GpapUsers.class);
        GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId).queryForFirst();
        if (gpapUser == null) {
            debug("Connection from: " + deviceId + tagPart + " NO PERMISSION ERROR");
            Status errStatus = new Status(Status.CODE_403_FORBIDDEN, NO_PERMISSION);
            errStatus.sendTo(response);
            return null;
        }

        debug("Connection from: " + gpapUser.name + tagPart);
        return deviceId;
    }

    public static void sendJsonString( HttpServletResponse response, String jsonToSend ) throws IOException {
        response.setHeader("Content-Type", "application/json");
        PrintWriter writer = response.getWriter();
//        char[] chars = jsonToSend.toCharArray();
//        int length = jsonToSend.getBytes().length;
//        setContentLength(response, length);
//        writer.print(chars);
        writer.print(jsonToSend);
        writer.flush();
    }

    public static void setContentLength( HttpServletResponse response, long length ) {
        if (length <= Integer.MAX_VALUE) {
            response.setContentLength((int) length);
        } else {
            response.addHeader("Content-Length", Long.toString(length));
        }
    }

    public static void debug( String msg ) {
        if (DEBUG) {
            KukuratusLogger.logDebug("ServletUtils", msg);
        }
    }

    public static void printHeaders( HttpServletRequest request, HttpServletResponse response ) {
        if (DEBUG) {
            Enumeration<String> headerNames = request.getHeaderNames();
            KukuratusLogger.logDebug("ServletUtils", "REQUEST HEADERS");
            while( headerNames.hasMoreElements() ) {
                String headerName = headerNames.nextElement();
                if (headerName != null) {
                    String header = request.getHeader(headerName);
                    KukuratusLogger.logDebug("ServletUtils", "\t#--> " + headerName + "=" + header);
                }
            }
            Collection<String> headerNames1 = response.getHeaderNames();
            KukuratusLogger.logDebug("ServletUtils", "RESPONSE HEADERS");
            for( String headerName : headerNames1 ) {
                if (headerName != null) {
                    String header = request.getHeader(headerName);
                    KukuratusLogger.logDebug("ServletUtils", "\t*--> " + headerName + "=" + header);
                }
            }
        }
    }

}
