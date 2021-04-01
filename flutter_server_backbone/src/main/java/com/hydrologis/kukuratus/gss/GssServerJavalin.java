package com.hydrologis.kukuratus.gss;

import java.io.Console;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.gss.database.Forms;
import com.hydrologis.kukuratus.gss.database.GpapProject;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.GpsLogsData;
import com.hydrologis.kukuratus.gss.database.ImageData;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.hydrologis.kukuratus.registry.RegistryHandler;
import com.hydrologis.kukuratus.servlets.ServletUtils;
import com.hydrologis.kukuratus.tiles.ITilesGenerator;
import com.hydrologis.kukuratus.tiles.MapsforgeTilesGenerator;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.log.Logger;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class GssServerJavalin implements Vars {
    private List<Class< ? >> tableClasses = Arrays.asList(//
            GpapProject.class, //
            GpapUsers.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class, //
            Forms.class);

    private ITilesGenerator mapsforgeTilesGenerator;

    public void start( ASpatialDb db ) throws Exception {
        start(db, null, null);
    }

    public void start( ASpatialDb db, String keyStorePath, String keyStorePassword ) throws Exception {

        Javalin app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
            config.server(() -> {
                Server server = new Server();
                if (keyStorePath != null && keyStorePassword != null) {
                    SslContextFactory sslContextFactory = new SslContextFactory.Server();
                    sslContextFactory.setKeyStorePath(keyStorePath);
                    sslContextFactory.setKeyStorePassword(keyStorePassword);
                    ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
                    sslConnector.setPort(443);
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(WEBAPP_PORT);
                    server.setConnectors(new Connector[]{sslConnector, connector});
                } else {
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(WEBAPP_PORT);
                    server.setConnectors(new Connector[]{connector});
                }
                return server;
            });
            config.addStaticFiles("public", Location.EXTERNAL);
            if (keyStorePath != null && keyStorePassword != null) {
                config.enforceSsl = true;
            }
        }).start(WEBAPP_PORT);

        activateMapsforge();
        // ROUTES START
        // app.before(ctx -> {
        // KukuratusLogger.logDebug(this, ctx.req.getPathInfo());
        // });
        GssServerApiJavalin.addCheckRoute(app);
        GssServerApiJavalin.addTilesRoute(app, mapsforgeTilesGenerator);
        GssServerApiJavalin.addClientUploadRoute(app);
        GssServerApiJavalin.addClientGetBaseDataRoute(app);
        GssServerApiJavalin.addClientGetFormsRoute(app);
        GssServerApiJavalin.addClientProjectDataUploadRoute(app);
        GssServerApiJavalin.addGetDataRoute(app);
        GssServerApiJavalin.addGetDataByTypeRoute(app);
        GssServerApiJavalin.addGetImagedataRoute(app);
        GssServerApiJavalin.addLoginRoute(app);
        GssServerApiJavalin.addUserSettingsRoute(app);
        GssServerApiJavalin.addListByTypeRoute(app);
        GssServerApiJavalin.addUpdateByTypeRoute(app);
        GssServerApiJavalin.addDeleteByTypeRoute(app);

        app.get("/", ctx -> ctx.redirect("index.html"));
        // ROUTES END

        DatabaseHandler.init(db);
        createTables();

    }

    private void activateMapsforge() {
        try {
            File dataFolder = KukuratusWorkspace.getInstance().getDataFolder();
            File[] mapfiles = dataFolder.listFiles(new FilenameFilter(){
                @Override
                public boolean accept( File dir, String name ) {
                    return name.endsWith(".map"); //$NON-NLS-1$
                }
            });

            if (mapfiles != null && mapfiles.length > 0) {
                float factor = 1f;
                int tile = 256;
                mapsforgeTilesGenerator = new MapsforgeTilesGenerator("mapsforge", mapfiles, tile, factor, null); //$NON-NLS-1$
            }
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
        }
    }

    private void createTables() throws Exception {
        DatabaseHandler dbHandler = DatabaseHandler.instance();
        for( Class< ? > tClass : tableClasses ) {
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

    public static void main( String[] args ) throws Exception {

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

        String mobilePwd = null;
        String keyStorePath = null;
        String postgresUrl = null;
        for( int i = 1; i < args.length; i++ ) {
            String arg = args[i];
            if (new File(arg).exists()) {
                // assuming it is the keystore
                keyStorePath = arg;
            } else if (arg.contains("jdbc:postgresql")) {
                // jdbc:postgresql://localhost:5432/test
                postgresUrl = arg.replaceFirst(EDb.POSTGIS.getJdbcPrefix(), "");
            } else {
                mobilePwd = arg;
            }
        }

        Console console = System.console();
        if (console == null) {
            System.err.println("WARNING: no console available. PostGIS mode won't work.");
        }
        ASpatialDb db = null;
        if (postgresUrl != null) {
            db = EDb.POSTGIS.getSpatialDb();
            String user;
            String pwd;
            if (postgresUrl.endsWith("test")) {
                user = "test";
                pwd = "test";
            } else {
                user = console.readLine("Please enter the postgresql username: ");
                char[] pwdChars = console.readPassword("Please enter the postgresql password: ");
                pwd = new String(pwdChars);
            }
            db.setCredentials(user, pwd);
            db.open(postgresUrl);
        } else {
            KukuratusWorkspace workspace = KukuratusWorkspace.getInstance();
            File dataFolder = workspace.getDataFolder();
            File dbFile = new File(dataFolder, "gss_database.mv.db"); //$NON-NLS-1$
            if (!dbFile.exists()) {
                KukuratusLogger.logInfo("main", "No database present in folder, creating one."); //$NON-NLS-1$
            }
            db = EDb.H2GIS.getSpatialDb();
            db.open(dbFile.getAbsolutePath());
        }
        // set registry and logger on current db
        Logger.INSTANCE.init(db);
        KukuratusLogger.logger = Logger.INSTANCE;
        RegistryHandler.INSTANCE.initWithDb(db);

        System.out.println("****************************************************");
        System.out.println("* Launching with parameters:");
        System.out.println("* \tLAUNCH FOLDER: " + new File(".").getAbsolutePath());
        System.out.println("* \tWORKSPACE FOLDER: " + workspacePath);
        System.out.println("* \tSSL KEYSTORE FILE: " + (keyStorePath != null ? keyStorePath : " - nv - "));
        System.out.println("* \tMOBILE PWD: " + (mobilePwd != null ? mobilePwd : " - nv - "));
        if (postgresUrl != null) {
            System.out.println("* \tDATABASE: " + postgresUrl);
        } else {
            System.out.println("* \tDATABASE: " + db.getDatabasePath());
        }
        System.out.println("****************************************************");

        if (mobilePwd != null) {
            // pwd was supplied
            ServletUtils.MOBILE_UPLOAD_PWD = mobilePwd;
        } else {
            ServletUtils.MOBILE_UPLOAD_PWD = "gss_Master_Survey_Forever_2018";
        }

        String keyStorePassword = null;
        if (keyStorePath != null) {
            // ask for the password at startup
            if (console != null) {
                char[] pwdChars = console.readPassword("Please enter the keystore password: ");
                keyStorePassword = new String(pwdChars);
            } else {
                try (Scanner in = new Scanner(System.in)) {
                    System.out.print("Please enter the keystore password: ");
                    keyStorePassword = in.nextLine();
                }
            }
            if (keyStorePassword.trim().length() == 0) {
                System.out.println("Disabling keystore use due to empty password.");
                keyStorePassword = null;
                keyStorePath = null;
            }
        }

        GssServerJavalin gssServer = new GssServerJavalin();
        gssServer.start(db, keyStorePath, keyStorePassword);
    }
}