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

import com.codename1.io.JSONParser;
import com.codename1.util.regex.StringReader;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author hydrologis
 */
public class KukuratusStatus {

    public static int CODE_200_OK = 200;
    public static int CODE_403_FORBIDDEN = 403;
    public static int CODE_404_NOTFOUND = 404;
    public static int CODE_500_INTERNAL_SERVER_ERROR = 500;

    private int code;
    private String message;
    private Throwable throwable;
    private boolean isError;

    /**
     * Constructor for success status.
     *
     * @param code the http code.
     * @param message the human readable message.
     */
    public KukuratusStatus(int code, String message) {
        this.code = code;
        this.message = message;
        isError = false;
    }

    /**
     * Constructor for error status.
     *
     * @param code the http code.
     * @param message the human readable message.
     * @param throwable the error.
     */
    public KukuratusStatus(int code, String message, Throwable throwable) {
        this.code = code;
        this.message = message;
        this.throwable = throwable;
        isError = true;
    }

    public String getMessage() {
        return message;
    }

    public static KukuratusStatus fromJsonString(String json) throws IOException {
        JSONParser parser = new JSONParser();
        Map<String, Object> response = parser.parseJSON(new StringReader(json));

        Object codeObj = response.get("code");
        Object messageObj = response.get("message");
//        Object traceObj = response.get("trace");
//        Object timestampObj = response.get("timestamp");

        KukuratusStatus ks = new KukuratusStatus(CODE_200_OK, "");
        if (codeObj instanceof Number) {
            Number codeNum = (Number) codeObj;
            int code = codeNum.intValue();

            if (messageObj instanceof String) {
                String messageStr = (String) messageObj;
                ks = new KukuratusStatus(code, messageStr);
            }
        }
        return ks;
    }

}
