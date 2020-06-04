package com.hydrologis.kukuratus.gss;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.secure;
import static spark.Spark.staticFiles;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.validation.constraints.Null;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.gss.database.Forms;
import com.hydrologis.kukuratus.gss.database.GpapProject;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.GpsLogsData;
import com.hydrologis.kukuratus.gss.database.ImageData;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.hydrologis.kukuratus.servlets.ServletUtils;
import com.hydrologis.kukuratus.tiles.ITilesGenerator;
import com.hydrologis.kukuratus.tiles.MapsforgeTilesGenerator;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;

/**
 * Deploy:
 * 
 * - export as runnable jar (will use embedded jetty, which is anyways necessary
 * for websockets) - run with java -jar xxx.jar
 * 
 * To load static pages, the folder public, that resides in the
 * src/main/resources folder, needs to be copied to the folder into which the
 * runnable jar is exported and used.
 * 
 * 
 * @author hydrologis
 *
 */
public class GssServer implements Vars {

    private List<Class<?>> tableClasses = Arrays.asList(//
            GpapProject.class, //
            GpapUsers.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class, //
            Forms.class);

    private ITilesGenerator mapsforgeTilesGenerator;

    public void start() throws Exception {
        start(null, null);
    }

    public void start(String keyStorePath, String keyStorePassword) throws Exception {
        KukuratusWorkspace workspace = KukuratusWorkspace.getInstance();
        File dataFolder = workspace.getDataFolder();
        File dbFile = new File(dataFolder, "gss_database.mv.db"); //$NON-NLS-1$
        if (!dbFile.exists()) {
            KukuratusLogger.logInfo(this, "No database present in folder, creating one."); //$NON-NLS-1$
        }

        // TODO handle dbs
        ASpatialDb db = EDb.H2GIS.getSpatialDb();
        db.open(dbFile.getAbsolutePath());
        DatabaseHandler.init(db);
        createTables();

        /*
         * THE SERVER
         */
        if (keyStorePath != null && keyStorePassword != null) {
            KukuratusLogger.logInfo(this, "Using ssl with keystore: " + keyStorePath); //$NON-NLS-1$
            secure(keyStorePath, keyStorePassword, null, null);
        }

        port(WEBAPP_PORT);
        staticFiles.location("/public");

        // TODO Cache/Expire time -> default is no caching
        // staticFiles.expireTime(600); // ten minutes

        // ENABLE CORS START
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
        // ENABLE CORS END

        // ROUTES START
        activateMapsforge();

        GssServerApi.addCheckRoute();
        GssServerApi.addTilesRoute(mapsforgeTilesGenerator);
        GssServerApi.addUploadRoute();
        GssServerApi.addGetDataRoute();
        GssServerApi.addGetDataByTypeRoute();
        GssServerApi.addGetImagedataRoute();
        GssServerApi.addLoginRoute();
        GssServerApi.addUserSettingsRoute();
        GssServerApi.addListByTypeRoute();
        GssServerApi.addUpdateByTypeRoute();
        GssServerApi.addDeleteByTypeRoute();

        get("/", (req, res) -> {
            res.redirect("index.html");
            return null;
        });
        // ROUTES END
    }

    private void activateMapsforge() {
        try {
            File dataFolder = KukuratusWorkspace.getInstance().getDataFolder();
            File[] mapfiles = dataFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".map"); //$NON-NLS-1$
                }
            });

            float factor = 1f;
            int tile = 256;
            mapsforgeTilesGenerator = new MapsforgeTilesGenerator("mapsforge", mapfiles, tile, factor, null); //$NON-NLS-1$
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private void createTables() throws Exception {
        DatabaseHandler dbHandler = DatabaseHandler.instance();
        for (Class<?> tClass : tableClasses) {
            String tableName = DatabaseHandler.getTableName(tClass);
            if (dbHandler.getDb().hasTable(tableName)) {
                KukuratusLogger.logDebug(this, "Table exists already: " + tableName); //$NON-NLS-1$
                continue;
            }
            KukuratusLogger.logDebug(this, "Creating table: " + tableName); //$NON-NLS-1$
            dbHandler.createTableIfNotExists(tClass);
            KukuratusLogger.logDebug(this, "Done creating table: " + tableName); //$NON-NLS-1$
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.err.println("The workspace folder needs to be supplied as argument.");
            System.exit(1);
        }

        String workspacePath = args[0];
        File workspaceFolder = new File(workspacePath);
        if (!workspaceFolder.exists()) {
            System.err.println("The workspace folder needs to exist.");
            System.exit(1);
        }
        KukuratusWorkspace.setWorkspaceFolderPath(workspacePath);

        String pwd = null;
        String keyStorePath = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (new File(arg).exists()) {
                // assuming it is the keystore
                keyStorePath = arg;
            } else {
                pwd = arg;
            }
        }

        System.out.println("****************************************************");
        System.out.println("* Launching with parameters:");
        System.out.println("* \tWORKSPACE FOLDER: " + workspacePath);
        System.out.println("* \tSSL KEYSTORE FILE: " + (keyStorePath != null ? keyStorePath : " - nv - "));
        System.out.println("* \tMOBILE PWD: " + (pwd != null ? pwd : " - nv - "));
        System.out.println("****************************************************");

        if (pwd != null) {
            // pwd was supplied
            ServletUtils.MOBILE_UPLOAD_PWD = pwd;
        } else {
            ServletUtils.MOBILE_UPLOAD_PWD = "gss_Master_Survey_Forever_2018";
        }

        String keyStorePassword = null;
        if (keyStorePath != null) {
            // ask for the password at startup
            System.out.println("Please enter the keystore password and press return:");
            try (Scanner in = new Scanner(System.in)) {
                keyStorePassword = in.nextLine();
                if (keyStorePassword.trim().length() == 0) {
                    System.out.println("Disabling keystore use due to empty password.");
                    keyStorePassword = null;
                    keyStorePath = null;
                }
            }
        }

        GssServer gssServer = new GssServer();
        gssServer.start(keyStorePath, keyStorePassword);
    }

}