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
package com.hydrologis.gssmobile.utils;

import com.codename1.util.Base64;
import com.hydrologis.cn1.libs.HyUtilities;

/**
 *
 * @author hydrologis
 */
public class GssUtilities {
    
    public static final int DEFAULT_BYTE_ARRAY_READ = 8192;

    public static final String MASTER_GSS_PASSWORD = "gss_Master_Survey_Forever_2018";

    public static final int MPR_TIMEOUT = 5 * 60 * 1000; // 5 minutes timeout

    public static final String LAST_DB_PATH = "GSS_LAST_DB_PATH";
    public static final String SERVER_URL = "GSS_SERVER_URL";

    public static final String SYNCH_PATH = "/upload";
    public static final String DATA_DOWNLOAD_PATH = "/datadownload";
    public static final String TAGS_DOWNLOAD_PATH = "/tagsdownload";

    public static final String DATA_DOWNLOAD_BASEMAP = "basemaps";
    public static final String DATA_DOWNLOAD_OVERLAYS = "overlays";
    public static final String DATA_DOWNLOAD_PROJECTS = "projects";
    public static final String DATA_DOWNLOAD_NAME = "name";

    public static final String TAGS_DOWNLOAD_TAGS = "tags";
    public static final String TAGS_DOWNLOAD_TAG = "tag";
    public static final String TAGS_DOWNLOAD_NAME = "name";

//    public static String NATIVE_BROWSER_USE = "GSS_NATIVE_BROWSER_USE";
    public static final float ICON_SIZE = 4;
    public static final float BIG_ICON_SIZE = 8;
    public static final String YES = "Yes";
    public static final String NO = "No";

    public static String getAuthHeader() {
        String authCode = HyUtilities.getUdid() + ":" + GssUtilities.MASTER_GSS_PASSWORD;
        String authHeader = "Basic " + Base64.encode(authCode.getBytes());
        return authHeader;
    }

}
