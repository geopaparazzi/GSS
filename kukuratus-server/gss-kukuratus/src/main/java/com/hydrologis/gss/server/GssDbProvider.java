/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.h2.tools.Server;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.h2gis.H2GisDb;
import org.hortonmachine.dbs.h2gis.H2GisServer;
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
import com.hydrologis.kukuratus.libs.spi.DbProvider;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;

/**
 * The db provider as loaded from the SPI. It has to be instanced and initialized only once.
 * It has basically a singleton behavior.
 * 
 * 
 * @author Antonello Andrea (www.hydrologis.com)
 */
public class GssDbProvider implements DbProvider {

    private static final String TCP_LOCALHOST = "tcp://localhost:9092";
    private static final String TCP_PASSWORD = null;
    private static final String DB_PREFIX = "gss_database";
    public static final String USERNAME = "gss";
    public static final String PWD = "gss";

    private ASpatialDb db;
    private File databaseFile;

    private static boolean doServer = true;
    private static Server server;
    private DatabaseHandler databaseHandler;
    private String dburl;

    private List<Class< ? >> tableClasses = Arrays.asList(//
            GpapUsers.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class, //
            GpsLogsProperties.class);

    public synchronized void init() {
        if (db != null) {
            return;
        }
        try {

            databaseFile = getDatabaseFile();

            EDb dbToUse = null;
            if (databaseFile.getName().endsWith(EDb.SPATIALITE.getExtension())) {
                dbToUse = EDb.SPATIALITE;
            } else if (databaseFile.getName().endsWith(EDb.H2GIS.getExtension())) {
                dbToUse = EDb.H2GIS;
            } else {
                throw new IllegalArgumentException("Can't understand db file: " + databaseFile);
            }

            dburl = databaseFile.getAbsolutePath();
            boolean dbExistedAlready = databaseFile.exists();
            if (!dbExistedAlready) {
                KukuratusLogger.logDebug(this, "Create database (wasn't existing): " + dburl);
                try (ASpatialDb tmpDb = dbToUse.getSpatialDb()) {
                    // db.setCredentials(USERNAME, PWD);
                    tmpDb.open(dburl);
                }
            } else {
                KukuratusLogger.logDebug(this, "Connecting to database: " + dburl);
            }

            if (doServer && dbToUse == EDb.H2GIS) {
                if (server == null) {
                    server = H2GisServer.startTcpServerMode("9092", false, TCP_PASSWORD, true,
                            databaseFile.getParentFile().getAbsolutePath());
                }
                if (server.isRunning(false)) {
                    dburl = TCP_LOCALHOST + "/" + databaseFile.getAbsolutePath();
                }
            }

            db = dbToUse.getSpatialDb();
            // db.setCredentials(USERNAME, PWD);
            db.open(dburl);

            databaseHandler = DatabaseHandler.instance(db);

            if (!dbExistedAlready) {
                createTables(databaseHandler);
            }
        } catch (Exception e1) {
            KukuratusLogger.logError(this, e1);
        }
    }

    private void createTables( DatabaseHandler dbHandler ) throws Exception {
        for( Class< ? > tClass : tableClasses ) {
            System.out.println("Create if not exists: " + DatabaseHandler.getTableName(tClass));
            dbHandler.createTableIfNotExists(tClass);
            if (ISpatialTable.class.isAssignableFrom(tClass)) {
                if (db instanceof H2GisDb) {
                    H2GisDb h2gisDb = (H2GisDb) db;
                    String tableName = DatabaseHandler.getTableName(tClass);
                    h2gisDb.addSrid(tableName, DatabaseHandler.TABLES_EPSG, ISpatialTable.GEOM_FIELD_NAME);
                    h2gisDb.createSpatialIndex(tableName, ISpatialTable.GEOM_FIELD_NAME);
                } else if (db instanceof SpatialiteDb) {
                    // SpatialiteDb spatialiteDb = (SpatialiteDb) db;
                    String tableName = DatabaseHandler.getTableName(tClass);
                    String geometryType = DatabaseHandler.getGeometryType(tClass);
                    db.execOnConnection(conn -> {
                        // SELECT RecoverGeometryColumn('pipespieces', 'the_geom', 4326,
                        // 'LINESTRING', 'XY')
                        String sql = "SELECT RecoverGeometryColumn('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "', "
                                + DatabaseHandler.TABLES_EPSG + ", '" + geometryType + "', 'XY')";
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

    public File getDatabaseFile() throws IOException {
        Optional<File> globalDataFolder = KukuratusWorkspace.getInstance().getGlobalDataFolder();
        if (globalDataFolder.isPresent()) {
            File dataFolderFile = globalDataFolder.get();
            File[] databaseFiles = dataFolderFile.listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return name.startsWith(DB_PREFIX)
                            && (name.endsWith(EDb.SPATIALITE.getExtension()) || name.endsWith(EDb.H2GIS.getExtension()));
                }
            });
            if (databaseFiles != null && databaseFiles.length == 1) {
                return databaseFiles[0];
            }

            // create a new database
            KukuratusLogger.logInfo(this, "No database present in folder, creating one.");
            File dbFile = new File(dataFolderFile, "gss_database.mv.db");
            return dbFile;
        }
        throw new IOException("Can't find main database file in: " + globalDataFolder.get().getAbsolutePath());
    }

    public ASpatialDb getDb() {
        return db;
    }

    public Optional<DatabaseHandler> getDatabaseHandler() {
        return Optional.ofNullable(databaseHandler);
    }

    public void close() {
        try {
            if (doServer && server != null && server.isRunning(true)) {
                Server.shutdownTcpServer(TCP_LOCALHOST, TCP_PASSWORD, true, false);
                server.stop();
            }
            if (databaseHandler != null) {
                databaseHandler.close();
            }
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

}
