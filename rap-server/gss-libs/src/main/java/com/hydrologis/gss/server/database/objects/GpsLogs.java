/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server.database.objects;
import com.hydrologis.gss.server.database.ISpatialTable;
import com.hydrologis.gss.server.database.ormlite.LineStringTypeH2GIS;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.LineString;

/**
 * The gps log table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogs")
public class GpsLogs implements ISpatialTable {
    public static final String ID_FIELD_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String STARTTS_FIELD_NAME = "startts";
    public static final String ENDTS_FIELD_NAME = "endts";
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid";

    public static final String gpslogFKColumnDefinition = "long references gpslogs(id) on delete cascade";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = STARTTS_FIELD_NAME, canBeNull = false)
    public long startTs;

    @DatabaseField(columnName = ENDTS_FIELD_NAME, canBeNull = false)
    public long endTs;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = LineStringTypeH2GIS.class)
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

}
