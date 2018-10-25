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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.h2.tools.Server;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.h2gis.H2GisDb;
import org.hortonmachine.dbs.h2gis.H2GisServer;
import org.hortonmachine.dbs.postgis.PostgisDb;
import org.hortonmachine.dbs.spatialite.hm.SpatialiteDb;
import org.hortonmachine.dbs.utils.EGeometryType;

import com.hydrologis.gss.server.database.objects.Forms;
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
import org.locationtech.jts.geom.Point;

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
    public static String USERNAME = "gss";
    public static final String PWD = "gss";

    private static boolean doPostgis = false;
    private static final String PG_URL = "localhost:5432/gss";

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
            GpsLogsProperties.class, //
            Forms.class);

    public synchronized void init( String... parameters ) {
        if (db != null) {
            return;
        }
        try {

            EDb dbToUse = null;
            if (doPostgis) {
                dbToUse = EDb.POSTGIS;
                dburl = PG_URL;
                KukuratusLogger.logDebug(this, "Connecting to database: " + dburl);
            } else {
                USERNAME = null;
                databaseFile = getDatabaseFile();
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
            }

            db = dbToUse.getSpatialDb();
            if (USERNAME != null)
                db.setCredentials(USERNAME, PWD);
            db.open(dburl);

            databaseHandler = DatabaseHandler.instance(db);

//            if (!dbExistedAlready) {
            createTables(databaseHandler);
//            }
        } catch (Exception e1) {
            KukuratusLogger.logError(this, e1);
        }
    }

    private void createTables( DatabaseHandler dbHandler ) throws Exception {
        for( Class< ? > tClass : tableClasses ) {
            String tableName = DatabaseHandler.getTableName(tClass);
            if (dbHandler.getDb().hasTable(tableName)) {
                KukuratusLogger.logDebug(this, "Table exists already: " + tableName);
                continue;
            }
            KukuratusLogger.logDebug(this, "Creating table: " + tableName);
            dbHandler.createTableIfNotExists(tClass);
            KukuratusLogger.logDebug(this, "Done creating table: " + tableName);
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
        throw new IOException("Can't find main database file. Check your workspace startup configuration. ");
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
