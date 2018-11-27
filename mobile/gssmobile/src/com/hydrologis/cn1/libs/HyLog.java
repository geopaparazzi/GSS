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
package com.hydrologis.cn1.libs;

import com.codename1.io.Log;

/**
 *
 * @author hydrologis
 */
public class HyLog {
    public static final boolean DO_DEBUG = true; // disable in production


    /**
     * Constant indicating the logging level Debug is the default and the lowest
     * level followed by info, warning and error
     */
    public static final int DEBUG = 1;

    /**
     * Constant indicating the logging level Debug is the default and the lowest
     * level followed by info, warning and error
     */
    public static final int INFO = 2;

    /**
     * Constant indicating the logging level Debug is the default and the lowest
     * level followed by info, warning and error
     */
    public static final int WARNING = 3;

    /**
     * Constant indicating the logging level Debug is the default and the lowest
     * level followed by info, warning and error
     */
    public static final int ERROR = 4;
    
    public static void p(String message) {
        Log.p(message);
    }
    
    public static void w(String message) {
        Log.p(message, WARNING);
    }

    public static void e(Throwable t) {
        Log.e(t);
    }

    /**
     * Default println method invokes the print instance method, uses given
     * level
     *
     * @param text the text to print
     * @param level one of DEBUG, INFO, WARNING, ERROR
     */
    public static void p(String text, int level) {
        Log.p(text, level);
    }

    public static void d(String text) {
        if (DO_DEBUG) {
            Log.p(text, WARNING);
        }
    }

    /**
     * Sends the current log to the cloud. Notice that this method is
     * synchronous and returns only when the sending completes
     */
    public static void sendLog() {
        Log.sendLog();
    }

    /**
     * Sends the current log to the cloud and returns immediately
     */
    public static void sendLogAsync() {
        Log.sendLogAsync();
    }

    /**
     * Binds pro based crash protection logic that will send out an email in
     * case of an exception thrown on the EDT
     *
     * @param consumeError true will hide the error from the user, false will
     * leave the builtin logic that defaults to showing an error dialog to the
     * user
     */
    public static void bindCrashProtection(final boolean consumeError) {
        Log.bindCrashProtection(consumeError);
    }
}
