/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server.database;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.h2gis.H2GisDb;
import org.hortonmachine.dbs.spatialite.hm.SpatialiteDb;

import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.GpsLogsData;
import com.hydrologis.gss.server.database.objects.GpsLogsProperties;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.database.ISpatialTable;
import com.j256.ormlite.table.TableUtils;

public class GssDatabaseHandler extends DatabaseHandler {

    private List<Class< ? >> tableClasses = Arrays.asList(//
            GpapUsers.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class, //
            GpsLogsProperties.class);

    public static GssDatabaseHandler instance( ASpatialDb db ) throws Exception {
        GssDatabaseHandler dbHandler = new GssDatabaseHandler(db);
        db.accept(dbHandler);
        return dbHandler;
    }

    public List<Class< ? >> getTableClasses() {
        return tableClasses;
    }

    private GssDatabaseHandler( ASpatialDb db ) throws SQLException {
        super(db);
    }

    public void createTables() throws Exception {
        for( Class< ? > tClass : tableClasses ) {
            System.out.println("Create if not exists: " + getTableName(tClass));
            TableUtils.createTableIfNotExists(connectionSource, tClass);
            if (ISpatialTable.class.isAssignableFrom(tClass)) {
                if (db instanceof H2GisDb) {
                    H2GisDb h2gisDb = (H2GisDb) db;
                    String tableName = getTableName(tClass);
                    h2gisDb.addSrid(tableName, TABLES_EPSG, ISpatialTable.GEOM_FIELD_NAME);
                    h2gisDb.createSpatialIndex(tableName, ISpatialTable.GEOM_FIELD_NAME);
                } else if (db instanceof SpatialiteDb) {
                    // SpatialiteDb spatialiteDb = (SpatialiteDb) db;
                    String tableName = getTableName(tClass);
                    String geometryType = getGeometryType(tClass);
                    db.execOnConnection(conn -> {
                        // SELECT RecoverGeometryColumn('pipespieces', 'the_geom', 4326,
                        // 'LINESTRING', 'XY')
                        String sql = "SELECT RecoverGeometryColumn('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "', "
                                + TABLES_EPSG + ", '" + geometryType + "', 'XY')";
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }
                        // SELECT CreateSpatialIndex('pipespieces','the_geom');
                        sql = "SELECT CreateSpatialIndex('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "')";
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }

                        return null;
                    });
                }
            }
        }
    }
}
