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
import java.util.Optional;

import org.h2.tools.Server;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.h2gis.H2GisServer;

import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;

public enum GssDbProvider {
    INSTANCE;
    
    private static final String DB_PREFIX = "gss_database";
    public static final String USERNAME = "gss";
    public static final String PWD = "gss";

    private ASpatialDb db;
    private File databaseFile;

    private static Server server;
    private DatabaseHandler databaseHandler;

    private synchronized void init() {
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

            String dburl = databaseFile.getAbsolutePath();
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

            if (dbToUse == EDb.H2GIS) {
                if (server == null) {
                    server = H2GisServer.startTcpServerMode("9092", false, null, true,
                            databaseFile.getParentFile().getAbsolutePath());
                }
                if (server.isRunning(false)) {
                    dburl = "tcp://localhost:9092/" + databaseFile.getAbsolutePath();
                }
            }

            db = dbToUse.getSpatialDb();
            // db.setCredentials(USERNAME, PWD);
            db.open(dburl);

            databaseHandler = DatabaseHandler.instance(db);

            if (!dbExistedAlready) {
                databaseHandler.createTables();
            }
        } catch (Exception e1) {
            KukuratusLogger.logError(this, e1);
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
        init();
        return db;
    }

    public DatabaseHandler getDatabaseHandler() {
        init();
        return databaseHandler;
    }

}
