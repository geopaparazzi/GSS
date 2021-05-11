package com.hydrologis.kukuratus.gss;

import java.io.Console;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.log.Logger;

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
        GssServerApiJavalin.addLogRoute(app);

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
        Options options = new Options();
        options.addOption("w", "workspace", true, "The path to the workspace.");
        options.addOption("s", "ssl", true, "The optional path to the keystore file for ssl.");
        options.addOption("sp", "ssl_pwd", true,
                "The optional password for the keystore file. Mandatory if the keystore file is defined.");
        options.addOption("mp", "mobilepwd", true, "The password used by mobile devices to connect (defaults to testPwd).");
        options.addOption("p", "psql_url", true, "The optional url to enable postgis database use (disables H2GIS).");
        options.addOption("pu", "psql_user", true,
                "The optional postgis user (defaults to test). Mandatory if the url is defined.");
        options.addOption("pp", "psql_pwd", true,
                "The optional postgis password (defaults to test). Mandatory if the url is defined.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String mobilePwd = null;
        String keyStorePath = null;
        String keyStorePwd = null;
        String postgresUrl = null;
        String postgresUser = null;
        String postgresPwd = null;
        String workspacePath = null;
        if (!cmd.hasOption("w")) {
            System.err.println("The workspace folder needs to be supplied as argument.");
            help(options);
            System.exit(1);
        } else {
            workspacePath = cmd.getOptionValue("w");
            File workspaceFolder = new File(workspacePath);
            if (!workspaceFolder.exists()) {
                System.err.println("The workspace folder needs to exist.");
                System.exit(1);
            }
            KukuratusWorkspace.setWorkspaceFolderPath(workspacePath);
        }

        if (cmd.hasOption("mp")) {
            mobilePwd = cmd.getOptionValue("mp");
        }
        if (cmd.hasOption("p")) {
            if (!cmd.hasOption("pu") || !cmd.hasOption("pp")) {
                help(options);
                System.exit(1);
            } else {
                postgresUrl = cmd.getOptionValue("p");
                postgresUser = cmd.getOptionValue("pu");
                postgresPwd = cmd.getOptionValue("pp");
            }
        }
        if (cmd.hasOption("s")) {
            if (!cmd.hasOption("sp")) {
                help(options);
                System.exit(1);
            } else {
                keyStorePath = cmd.getOptionValue("s");
                keyStorePwd = cmd.getOptionValue("sp");
                if(!new File(keyStorePath).exists()) {
                    System.err.println("Ignoring keystore file that does not exist.");
                    keyStorePath = null;
                }
            }
        }

        // Variables can be set from environment, if not previously set
        if (postgresUrl == null) {
            String postgresUrlTmp = System.getenv("POSTGRES_URL");
            String postgresUserTmp = System.getenv("POSTGRES_USER");
            String postgresPwdTmp = System.getenv("POSTGRES_PASSWORD");
            if (postgresUrlTmp != null && postgresUser != null && postgresPwd != null) {
                postgresUrl = postgresUrlTmp;
                postgresUser = postgresUserTmp;
                postgresPwd = postgresPwdTmp;
            }
        }

        if (mobilePwd == null) {
            String mobilePwdTmp = System.getenv("MOBILE_PASSWORD");
            if (mobilePwdTmp != null) {
                mobilePwd = mobilePwdTmp;
            }
        }
        // end reading environment

        ASpatialDb db = null;
        if (postgresUrl != null) {
            db = EDb.POSTGIS.getSpatialDb();
            db.setCredentials(postgresUser, postgresPwd);
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

        GssServerJavalin gssServer = new GssServerJavalin();
        gssServer.start(db, keyStorePath, keyStorePwd);
    }

    private static void help( Options options ) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("gss ", options);
    }
}