package com.hydrologis.gss;

import java.io.File;

import org.h2.tools.Server;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.h2gis.H2GisServer;

import com.hydrologis.gss.server.database.DatabaseHandler;

import eu.hydrologis.stage.libs.log.StageLogger;

public class GssDbProvider {

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

            databaseFile = GssContext.instance().getDatabaseFile();

            EDb dbToUse = null;
            if (databaseFile.getName().endsWith(EDb.SPATIALITE.getExtension())) {
                dbToUse = EDb.SPATIALITE;
            } else if (databaseFile.getName().endsWith(EDb.H2GIS.getExtension())) {
                dbToUse = EDb.H2GIS;
            } else {
                throw new IllegalArgumentException("Can't understand db file: " + databaseFile);
            }

            String exists = "non existing";
            if (databaseFile.exists()) {
                exists = "existing";
            }
            String dburl = databaseFile.getAbsolutePath();
            StageLogger.logDebug(this, "Connecting to " + exists + " database: " + dburl);

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
        } catch (Exception e1) {
            StageLogger.logError(this, e1);
        }

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
