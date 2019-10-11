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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * A simple status class used in kukuratus.
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public class KukuratusStatus {
    public static int CODE_200_OK = 200;
    public static int CODE_403_FORBIDDEN = 403;
    public static int CODE_404_NOTFOUND = 404;
    public static int CODE_500_INTERNAL_SERVER_ERROR = 500;

    private int code;
    private String message;
    private Throwable throwable;

    /**
     * Constructor for success status.
     * 
     * @param code the http code.
     * @param message the human readable message.
     */
    public KukuratusStatus( int code, String message ) {
        this.code = code;
        this.message = message;
    }

    /**
     * Constructor for error status.
     * 
     * @param code the http code.
     * @param message the human readable message.
     * @param throwable the error.
     */
    public KukuratusStatus( int code, String message, Throwable throwable ) {
        this.code = code;
        this.message = message;
        this.throwable = throwable;
    }

    public String toJson() {
        JSONObject root = new JSONObject();
        root.put("code", code); //$NON-NLS-1$
        root.put("message", message); //$NON-NLS-1$
        if (throwable != null)
            root.put("trace", throwable.getMessage()); //$NON-NLS-1$
        root.put("timestamp", new DateTime().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS)); //$NON-NLS-1$
        return root.toString(2);
    }

    public void sendTo( HttpServletResponse response ) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json"); //$NON-NLS-1$
        PrintWriter out = response.getWriter();
        out.print(toJson());
        out.flush();
    }

}
