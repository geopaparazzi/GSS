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
package com.hydrologis.gssmobile.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public class SyncData {
    public static final String MEDIA = "Media";
    public static final String GPS_LOGS = "Gps Logs";
    public static final String NOTES = "Notes";

    public final List<Object> rootsList = Arrays.asList(NOTES, GPS_LOGS, MEDIA);

    public final HashMap<String, List<?>> type2ListMap = new HashMap<>();
    
}
