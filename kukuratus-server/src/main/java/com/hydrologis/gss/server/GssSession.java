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
package com.hydrologis.gss.server;

import com.vaadin.server.VaadinSession;

public class GssSession {

    public static final String KEY_LOADED_GPAPUSERS = "GSS_KEY_LOADED_GPAPUSERS";
    public static final String KEY_LAST_MAP_POSITION = "GSS_KEY_LAST_MAP_POSITION";

    public static String[] getLoadedGpapUsers() {
        return (String[]) VaadinSession.getCurrent().getAttribute(KEY_LOADED_GPAPUSERS);
    }

    public static void setLoadedGpapUsers( String[] loadedGpspUsers ) {
        VaadinSession.getCurrent().setAttribute(KEY_LOADED_GPAPUSERS, loadedGpspUsers);
    }

    public static void setLastMapPosition( double[] lonLatZoom ) {
        VaadinSession.getCurrent().setAttribute(KEY_LAST_MAP_POSITION, lonLatZoom);
    }

    public static double[] getLastMapPosition() {
        return (double[]) VaadinSession.getCurrent().getAttribute(KEY_LAST_MAP_POSITION);
    }
}
