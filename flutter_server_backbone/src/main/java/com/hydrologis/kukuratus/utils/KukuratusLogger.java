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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;

import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;

/**
 * A simple logger, to be properly implemented.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class KukuratusLogger {

    public static Logger logger = null;
    static {
        try {
            logger = KukuratusWorkspace.getInstance().getLogDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final boolean LOG_INFO = true;
    public static final boolean LOG_ACCESS = true;
    public static final boolean LOG_DEBUG = true;
    public static final boolean LOG_ERROR = true;

    private static final String SEP = ":: "; //$NON-NLS-1$

    private static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

    public static void logInfo( Object owner, String msg ) {
        if (LOG_INFO) {
            final String _msg = toMessage(owner, msg);
            try {
                if (logger != null)
                    logger.insert(EMessageType.INFO, owner.toString(), _msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logAccess( Object owner, String msg ) {
        if (LOG_ACCESS) {
            final String _msg = toMessage(owner, msg);
            try {
                if (logger != null)
                    logger.insert(EMessageType.ACCESS, owner.toString(), _msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logDebug( Object owner, String msg ) {
        if (LOG_DEBUG) {
            final String _msg = toMessage(owner, msg);
            try {
                if (logger != null)
                    logger.insert(EMessageType.DEBUG, owner.toString(), _msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logError( Object owner, String msg, Throwable e ) {
        if (LOG_ERROR) {
            final String _msg = toMessage(owner, msg);
            if (logger != null)
                logger.insertError(owner.toString(), _msg, e);
        }
    }

    public static void logError( Object owner, Throwable e ) {
        logError(owner, null, e);
    }

    private static String toMessage( Object owner, String msg ) {
        if (msg == null)
            msg = ""; //$NON-NLS-1$
        String newMsg = f.format(new Date()) + SEP;
        if (owner instanceof String) {
            newMsg = newMsg + owner + SEP;
        } else {
            newMsg = newMsg + owner.getClass().getSimpleName() + SEP;
        }
        newMsg = newMsg + msg;
        return newMsg;
    }

}
