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
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import com.hydrologis.kukuratus.database.ISpatialTable;
import com.hydrologis.kukuratus.database.ormlite.KukuratusLineStringType;
import com.hydrologis.kukuratus.utils.export.KmlRepresenter;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The gps log table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogs")
public class GpsLogs implements ISpatialTable, KmlRepresenter {
    private static final long serialVersionUID = 1L;
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String NAME_FIELD_NAME = "name"; //$NON-NLS-1$
    public static final String STARTTS_FIELD_NAME = "startts"; //$NON-NLS-1$
    public static final String ENDTS_FIELD_NAME = "endts"; //$NON-NLS-1$
    public static final String UPLOADTIMESTAMP_FIELD_NAME = "uploadts"; //$NON-NLS-1$
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid"; //$NON-NLS-1$
    public static final String GPAPPROJECT_FIELD_NAME = "gpapprojectid"; //$NON-NLS-1$
    public static final String COLOR_FIELD_NAME = "color"; //$NON-NLS-1$
    public static final String WIDTH_FIELD_NAME = "width"; //$NON-NLS-1$

    public static final String gpslogFKColumnDefinition = "bigint references gpslogs(id) on delete cascade"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = STARTTS_FIELD_NAME, canBeNull = false)
    public long startTs;

    @DatabaseField(columnName = ENDTS_FIELD_NAME, canBeNull = false)
    public long endTs;

    @DatabaseField(columnName = UPLOADTIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long uploadTimestamp;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = KukuratusLineStringType.class)
    public LineString the_geom;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    @DatabaseField(columnName = GPAPPROJECT_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapProject.projectsFKColumnDefinition)
    public GpapProject gpapProject;

    @DatabaseField(columnName = COLOR_FIELD_NAME, canBeNull = false)
    public String color;

    @DatabaseField(columnName = WIDTH_FIELD_NAME, canBeNull = false)
    public float width;

    GpsLogs() {
    }

    public GpsLogs( long id ) {
        this.id = id;
    }

    public GpsLogs( String name, long startTs, long endTs, LineString the_geom, String color, float width,
            GpapUsers gpapUser, GpapProject gpapProject, long uploadTimestamp ) {
        this.name = name;
        this.startTs = startTs;
        this.endTs = endTs;
        this.the_geom = the_geom;
        this.gpapUser = gpapUser;
        this.color = color;
        this.width = width;
        this.gpapProject = gpapProject;
        this.uploadTimestamp = uploadTimestamp;
        the_geom.setSRID(ISpatialTable.SRID);
    }

    public String toKmlString() {
        String hexColor = "#FF0000"; //$NON-NLS-1$ ;
        if (color.startsWith("#")) { //$NON-NLS-1$
            hexColor = color;
        }

        String name = makeXmlSafe(this.name);
        StringBuilder sB = new StringBuilder();
        sB.append("<Placemark>\n"); //$NON-NLS-1$
        sB.append("<name>" + name + "</name>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sB.append("<visibility>1</visibility>\n"); //$NON-NLS-1$
        sB.append("<LineString>\n"); //$NON-NLS-1$
        sB.append("<tessellate>1</tessellate>\n"); //$NON-NLS-1$
        sB.append("<coordinates>\n"); //$NON-NLS-1$
        Coordinate[] coords = ((LineString) the_geom).getCoordinates();
        for( int i = 0; i < coords.length; i++ ) {
            double lon = coords[i].x;
            double lat = coords[i].y;
            sB.append(lon).append(",").append(lat).append(",1 \n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sB.append("</coordinates>\n"); //$NON-NLS-1$
        sB.append("</LineString>\n"); //$NON-NLS-1$
        sB.append("<Style>\n"); //$NON-NLS-1$
        sB.append("<LineStyle>\n"); //$NON-NLS-1$

        String aabbggrr = "#FF" + new StringBuilder(hexColor.substring(1)).reverse().toString(); //$NON-NLS-1$
        sB.append("<color>").append(aabbggrr).append("</color>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sB.append("<width>").append(width).append("</width>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sB.append("</LineStyle>\n"); //$NON-NLS-1$
        sB.append("</Style>\n"); //$NON-NLS-1$
        sB.append("</Placemark>\n"); //$NON-NLS-1$

        return sB.toString();
    }

    public boolean hasImages() {
        return false;
    }

    @Override
    public List<String> getImageIds() {
        return Collections.emptyList();
    }
}
