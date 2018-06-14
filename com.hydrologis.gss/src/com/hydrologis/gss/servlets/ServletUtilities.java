package com.hydrologis.gss.servlets;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.gasleaksensors.libs.SensitContextV1;

import eu.hydrologis.stage.libs.utils.NetworkUtilities;

public class ServletUtilities {
    /**
     * Send an error status.
     *  
     * @param response the response to send to.
     * @param ex the exception.
     * @param msg the message.
     * @param className the name of the launching class.
     * @throws IOException
     */
    public static void sendErrorStatus( HttpServletResponse response, Exception ex, String msg, String className )
            throws IOException {

        try {
            byte[] errorStatusBytes = SensitContextV1.instance().getProtocolHandler().getErrorStatusBytes(ex, msg, className);
            response.setHeader("Content-Type", NetworkUtilities.CONTENTTYPE);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(errorStatusBytes);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
