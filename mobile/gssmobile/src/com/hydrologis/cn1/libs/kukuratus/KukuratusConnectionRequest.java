/** *****************************************************************************
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
 * ****************************************************************************
 */
package com.hydrologis.cn1.libs.kukuratus;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.ui.CN;
import com.codename1.ui.util.Resources;
import com.hydrologis.cn1.libs.HyDialogs;
import com.hydrologis.cn1.libs.HyLog;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Connection request class in line with kukuratus servers.
 *
 * @author hydrologis
 */
public abstract class KukuratusConnectionRequest extends ConnectionRequest {
    
    public static final int HTTP_OK = 200;
    
    @Override
    public void readResponse(InputStream input) throws IOException {
        int responseCode = getResponseCode();
        if (responseCode == HTTP_OK) {
            readOkResponse(input);
        } else {
            InputStreamReader reader = new InputStreamReader(input);
            JSONParser parser = new JSONParser();
            Map<String, Object> response = parser.parseJSON(reader);
            if (response != null) {
                Object message = response.get("message");
                if (message instanceof String) {
                    String msg = (String) message;
                    CN.callSerially(() -> {
                        HyDialogs.showWarningDialog(msg);
                    });
                }
            }
        }
    }
    
    public abstract void readOkResponse(InputStream input) throws IOException;

    @Override
    protected void handleErrorResponseCode(int code, String message) {
        // Errors handled in readResponse
        
        
//        if (code != 200) {
//            CN.callSerially(() -> {
//                if (message != null) {
//                    try {
//                        KukuratusStatus status = KukuratusStatus.fromJsonString(message);
//                        HyDialogs.showWarningDialog(status.getMessage());
//                    } catch (IOException ex) {
//                        HyDialogs.showErrorDialog(message);
//                    }
//                } else {
//                    String msg = "An error occurred.";
//                    HyDialogs.showWarningDialog(msg);
//                }
//            });
//        }
    }
    
    @Override
    protected void readHeaders(Object connection) throws IOException {
        if (HyLog.DO_DEBUG) {
            String[] headerNames = getHeaderFieldNames(connection);
            for (String headerName : headerNames) {
                if (headerName == null) {
                    continue;
                }
                String[] values = getHeaders(connection, headerName);
                if (values.length == 1) {
                    HyLog.d("header: " + headerName + " = " + values[0]);
                } else {
                    HyLog.d("header: " + headerName + " with values:");
                    for (String value : values) {
                        HyLog.d(" --> " + value);
                    }
                }
            }
        } else {
            super.readHeaders(connection);
        }
        
    }

    /**
     * Perform a get method.
     *
     * @param url the url to do the get on.
     * @param auth the optional authorization string.
     */
    public void prepareGet(String url, String auth) {
        setPost(false);
        setHttpMethod("GET");
        if (auth != null) {
            addRequestHeader("Authorization", auth);
        }
        addRequestHeader("Connection", "keep-alive");
        setUrl(url);
    }

    /**
     * Perform a get with progress dialog.
     *
     * @param url the url to do the get on.
     * @param auth the optional authorization string.
     * @param theme the theme to get the icon from.
     * @param progressTitle the title of the dialog.
     */
    public void doGetWithProgress(String url, String auth, Resources theme, String progressTitle) {
        HyLog.d("GET: " + url);
        prepareGet(url, auth);
        KukuratusInfiniteDownloadProgressDialog prog = new KukuratusInfiniteDownloadProgressDialog();
        prog.showInfiniteBlockingWithTitle(progressTitle, theme, this);
        NetworkManager.getInstance().addToQueue(this);
    }
    
}
