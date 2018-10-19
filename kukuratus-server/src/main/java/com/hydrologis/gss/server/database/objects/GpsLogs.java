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
package com.hydrologis.gss.server.database.objects;
import java.util.Collections;
import java.util.List;

import com.hydrologis.kukuratus.libs.database.ISpatialTable;
import com.hydrologis.kukuratus.libs.database.ormlite.KukuratusLineStringType;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.export.KmlRepresenter;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * The gps log table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogs")
public class GpsLogs implements ISpatialTable, KmlRepresenter {
    private static final long serialVersionUID = 1L;
    public static final String ID_FIELD_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String STARTTS_FIELD_NAME = "startts";
    public static final String ENDTS_FIELD_NAME = "endts";
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid";

    public static final String gpslogFKColumnDefinition = "bigint references gpslogs(id) on delete cascade";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = STARTTS_FIELD_NAME, canBeNull = false)
    public long startTs;

    @DatabaseField(columnName = ENDTS_FIELD_NAME, canBeNull = false)
    public long endTs;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = KukuratusLineStringType.class)
    public LineString the_geom;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    GpsLogs() {
    }

    public GpsLogs( long id ) {
        this.id = id;
    }

    public GpsLogs( String name, long startTs, long endTs, LineString the_geom, GpapUsers gpapUser ) {
        this.name = name;
        this.startTs = startTs;
        this.endTs = endTs;
        this.the_geom = the_geom;
        this.gpapUser = gpapUser;
        the_geom.setSRID(ISpatialTable.SRID);
    }

    public String toKmlString() {
        String hexColor = "#FF0000";
        float width = 3;
        try {
            Dao<GpsLogsProperties, ? > logPropDAO = SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get()
                    .getDao(GpsLogsProperties.class);

            GpsLogsProperties props = logPropDAO.queryBuilder().where().eq(GpsLogsProperties.GPSLOGS_FIELD_NAME, this)
                    .queryForFirst();
            hexColor = props.color;
            if (!hexColor.startsWith("#")) {
                hexColor = "#FF0000";
            }
            width = props.width;
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }

        String name = makeXmlSafe(this.name);
        StringBuilder sB = new StringBuilder();
        sB.append("<Placemark>\n");
        sB.append("<name>" + name + "</name>\n");
        sB.append("<visibility>1</visibility>\n");
        sB.append("<LineString>\n");
        sB.append("<tessellate>1</tessellate>\n");
        sB.append("<coordinates>\n");
        Coordinate[] coords = ((LineString) the_geom).getCoordinates();
        for( int i = 0; i < coords.length; i++ ) {
            double lon = coords[i].x;
            double lat = coords[i].y;
            sB.append(lon).append(",").append(lat).append(",1 \n");
        }
        sB.append("</coordinates>\n");
        sB.append("</LineString>\n");
        sB.append("<Style>\n");
        sB.append("<LineStyle>\n");

        String aabbggrr = "#FF" + new StringBuilder(hexColor.substring(1)).reverse().toString();
        sB.append("<color>").append(aabbggrr).append("</color>\n");
        sB.append("<width>").append(width).append("</width>\n");
        sB.append("</LineStyle>\n");
        sB.append("</Style>\n");
        sB.append("</Placemark>\n");

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
