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
package com.hydrologis.kukuratus.utils;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

/**
 * Utilities for netwrok handling.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public class NetworkUtilities {
    public static final String CONTENTTYPE = "application/x-protobuf"; //$NON-NLS-1$

    public static String getIpAddress( HttpServletRequest request ) {
        String ip = request.getHeader("X-Forwarded-For"); //$NON-NLS-1$
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { //$NON-NLS-1$
            ip = request.getHeader("Proxy-Client-IP"); //$NON-NLS-1$
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { //$NON-NLS-1$
            ip = request.getHeader("WL-Proxy-Client-IP"); //$NON-NLS-1$
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { //$NON-NLS-1$
            ip = request.getHeader("HTTP_CLIENT_IP"); //$NON-NLS-1$
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { //$NON-NLS-1$
            ip = request.getHeader("HTTP_X_FORWARDED_FOR"); //$NON-NLS-1$
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { //$NON-NLS-1$
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String[] getUserPwdWithBasicAuthentication( String authHeader ) {
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) { //$NON-NLS-1$
                    String credentials;
                    try {
                        Decoder decoder = Base64.getDecoder();
                        String nextToken = st.nextToken();
                        credentials = new String(decoder.decode(nextToken), "UTF-8"); //$NON-NLS-1$
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        KukuratusLogger.logError("NetworkUtilities.getUserPwdWithBasicAuthentication()",
                                "For authHeader: " + authHeader, e);
                        return null;
                    }
                    int p = credentials.indexOf(":"); //$NON-NLS-1$
                    if (p != -1) {
                        String login = credentials.substring(0, p).trim();
                        String password = credentials.substring(p + 1).trim();

                        return new String[]{login, password};
                    } else {
                        KukuratusLogger.logError("NetworkUtilities.getUserPwdWithBasicAuthentication()",
                                "For authHeader: " + authHeader + " no colon found.", new RuntimeException());
                        return null;
                    }
                }
            }
        }
        KukuratusLogger.logError("NetworkUtilities.getUserPwdWithBasicAuthentication()", "authHeader is null",
                new RuntimeException());
        return null;
    }
    
    public static ReturnMessages sendByteArrayPostHttpurlconnection( String urlStr, byte[] byteArray, String user,
            String password ) throws Exception {

        String userPassword = user + ":" + password; //$NON-NLS-1$
        URL url = new URL(urlStr);
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setUseCaches(false);
        uc.setDoOutput(true);
        uc.setRequestMethod("POST"); //$NON-NLS-1$
        uc.setRequestProperty("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
        uc.setRequestProperty("Content-length", "" + byteArray.length); //$NON-NLS-1$ //$NON-NLS-2$
        uc.setRequestProperty("Cache-Control", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        uc.setRequestProperty("Content-Type", "application/octet-stream"); //$NON-NLS-1$ //$NON-NLS-2$

        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        uc.setRequestProperty("Authorization", "Basic " + encoding); //$NON-NLS-1$ //$NON-NLS-2$
        uc.setConnectTimeout(5000);
        uc.connect();

        DataOutputStream request = new DataOutputStream(uc.getOutputStream());
        request.write(byteArray);
        request.flush();

        int responseCode = uc.getResponseCode();
        byte[] outByteArray = new byte[0];
        if (200 <= responseCode && uc.getResponseCode() <= 299) {
            outByteArray = IOUtils.toByteArray(uc.getInputStream());
        }
        ReturnMessages returnMessage = getMessageForCode(responseCode);
        returnMessage.setOptionalBytes(outByteArray);

        return returnMessage;
    }

    /**
     * Get a default message for an HTTP code.
     *
     * @param responseCode     the http code.
     * @return the return message.
     */
    public static ReturnMessages getMessageForCode( int responseCode ) {
        switch( responseCode ) {
        case HttpURLConnection.HTTP_OK:
            return ReturnMessages.HTTP_OK;
        case HttpURLConnection.HTTP_FORBIDDEN:
            return ReturnMessages.HTTP_FORBIDDEN;
        case HttpURLConnection.HTTP_UNAUTHORIZED:
            return ReturnMessages.HTTP_UNAUTHORIZED;
        case HttpURLConnection.HTTP_NOT_FOUND:
            return ReturnMessages.HTTP_NOT_FOUND;
        default:
            return ReturnMessages.UNKNOWN;
        }
    }

    public static String getB64Auth( String login, String pass ) {
        String source = login + ":" + pass; //$NON-NLS-1$
        byte[] byteArray = Base64.getEncoder().encode(source.getBytes());
        String encodedString = new String(byteArray);
        String ret = "Basic " + encodedString; //$NON-NLS-1$
        return ret;
    }

    /**
     * Send a GET request for a byte array.
     *
     * @param urlStr            the url.
     * @param requestParameters request parameters or <code>null</code>.
     * @param user              user or <code>null</code>.
     * @param password          password or <code>null</code>.
     * @return the fetched byte array.
     * @throws Exception if something goes wrong.
     */
    public static ReturnMessages sendByteArrayGetRequestHttpurlconnection( String urlStr, String requestParameters, String user,
            String password ) throws Exception {
        if (requestParameters != null && requestParameters.length() > 0) {
            urlStr += "?" + requestParameters; //$NON-NLS-1$
        }
        URL url = new URL(urlStr);
        String userPassword = user + ":" + password; //$NON-NLS-1$
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setRequestProperty("Authorization", "Basic " + encoding); //$NON-NLS-1$ //$NON-NLS-2$
        uc.setConnectTimeout(3000);
        uc.connect();
        int responseCode = uc.getResponseCode();
        byte[] byteArray = new byte[0];
        if (200 <= responseCode && uc.getResponseCode() <= 299) {
            byteArray = IOUtils.toByteArray(uc.getInputStream());
        }
        ReturnMessages returnMessage = getMessageForCode(responseCode);
        returnMessage.setOptionalBytes(byteArray);
        return returnMessage;
    }

}
