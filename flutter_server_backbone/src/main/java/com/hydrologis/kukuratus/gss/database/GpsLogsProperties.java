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
package com.hydrologis.kukuratus.gss.database;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The gps log properties table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogsproperties")
public class GpsLogsProperties {
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String COLOR_FIELD_NAME = "color"; //$NON-NLS-1$
    public static final String WIDTH_FIELD_NAME = "width"; //$NON-NLS-1$
    public static final String GPSLOGS_FIELD_NAME = "gpslogsid"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = COLOR_FIELD_NAME, canBeNull = false)
    public String color;

    @DatabaseField(columnName = WIDTH_FIELD_NAME, canBeNull = false)
    public float width;

    @DatabaseField(columnName = GPSLOGS_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpsLogs.gpslogFKColumnDefinition)
    public GpsLogs gpsLog;

    GpsLogsProperties() {
    }

    public GpsLogsProperties( long id ) {
        this.id = id;
    }

    public GpsLogsProperties( String color, float width, GpsLogs gpsLog ) {
        this.color = color;
        this.width = width;
        this.gpsLog = gpsLog;
    }

}
