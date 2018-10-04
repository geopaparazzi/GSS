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

import com.codename1.io.Preferences;
import com.codename1.ui.Display;

/**
 *
 * @author hydrologis
 */
public class HyUtilities {

    public static String CUSTOM_UDID = "GSS_CUSTOM_UDID";
    public static String MIMETYPE_BYTEARRAY = "application/octet-stream";

    public static String getUdid() {
        String udid = Display.getInstance().getUdid();
        if (udid == null) {
            udid = Preferences.get(CUSTOM_UDID, null);
            if (udid == null || udid.length() == 0) {
                return null;
            }
        }
        return udid;
    }


}
