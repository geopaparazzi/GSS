package com.hydrologis.kukuratus.gss;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.secure;
import static spark.Spark.staticFiles;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.gss.database.Forms;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.GpsLogsData;
import com.hydrologis.kukuratus.gss.database.ImageData;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.hydrologis.kukuratus.registry.RegistryHandler;
import com.hydrologis.kukuratus.registry.Settings;
import com.hydrologis.kukuratus.registry.User;
import com.hydrologis.kukuratus.tiles.ITilesGenerator;
import com.hydrologis.kukuratus.tiles.MapsforgeTilesGenerator;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.utils.KukuratusStatus;
import com.hydrologis.kukuratus.utils.NetworkUtilities;
import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;

import spark.Request;

/**
 * Deploy:
 * 
 * - export as runnable jar (will use embedded jetty, which is anyways necessary for websockets)
 * - run with java -jar xxx.jar
 * 
 * To load static pages, the folder public, that resides in the src/main/resources folder, needs
 * to be copied to the folder into which the runnable jar is exported and used. 
 * 
 * 
 * @author hydrologis
 *
 */
public class GssServer implements Vars {

    private List<Class< ? >> tableClasses = Arrays.asList(//
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

    public void start( String keyStorePath, String keyStorePassword ) throws Exception {
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
        options("/*", ( request, response ) -> {
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

        before(( request, response ) -> response.header("Access-Control-Allow-Origin", "*"));
        // ENABLE CORS END

        // ROUTES START
        activateMapsforge();

        get("/check", ( req, res ) -> {
            return "It works. " + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS);
        });

        get("/tiles/:source/:z/:x/:y", ( req, res ) -> {
            String source = req.params(":source");
            if (source.equals("mapsforge")) {
                String x = req.params(":x");
                String y = req.params(":y");
                String z = req.params(":z");
                int xTile = Integer.parseInt(x);
                int yTile = Integer.parseInt(y);
                int zoom = Integer.parseInt(z);
                try {
                    HttpServletResponse raw = res.raw();
                    raw.setContentType("image/png");
                    res.header("Content-Disposition", "attachment; filename=image.png");
                    ServletOutputStream outputStream = raw.getOutputStream();
                    mapsforgeTilesGenerator.getTile(xTile, yTile, zoom, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    return raw;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // halt();
                }
                return res;
            }
            return res;
        });

//        get("/data/:sectorid/:from/:to", ( req, res ) -> {
//            try {
//                String sectorId = req.params(":sectorid");
//                int id = Integer.parseInt(sectorId);
//                String from = req.params(":from");
//                String to = req.params(":to");
//                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//                long fromDate = dateFormatter.parse(from + " 00:00:00").getTime();
//                long toDate = dateFormatter.parse(to + " 23:59:59").getTime();
//
//                String sql = "select  type_id,timestamp, value from measures where sectorid=" + id + " and timestamp between "
//                        + fromDate + " and " + toDate + " group by  type_id, timestamp order by  type_id, timestamp";
//                JSONObject root = new JSONObject();
//                JSONArray temperatureArray = new JSONArray();
//                JSONArray humidityArray = new JSONArray();
//                root.put(TEMPERATURE_ID, temperatureArray);
//                root.put(HUMIDITY_ID, humidityArray);
//                String json = db.execOnConnection(connection -> {
//                    try (IHMStatement stmt = connection.createStatement(); IHMResultSet rs = stmt.executeQuery(sql)) {
//                        while( rs.next() ) {
//                            int type = rs.getInt(1);
//                            long ts = rs.getLong(2);
//                            double value = rs.getDouble(3);
//
//                            if (type == 0) {
//                                JSONObject valueObj = new JSONObject();
//                                valueObj.put("ts", ts);
//                                valueObj.put("v", value);
//                                temperatureArray.put(valueObj);
//                            } else {
//                                JSONObject valueObj = new JSONObject();
//                                valueObj.put("ts", ts);
//                                valueObj.put("v", value);
//                                humidityArray.put(valueObj);
//                            }
//                        }
//                        return root.toString();
//                    }
//                });
//                return json;
//            } catch (Exception e) {
//                Logger.INSTANCE.insertError("get status", "error", e);
//            }
//            return "{ERROR}";
//        });

        post("/data", ( req, res ) -> {
            KukuratusLogger.logDebug("GssServer#post(/data", "Received request from " + req.raw().getRemoteAddr());
            if (hasPermission(req)) {
                String surveyors = req.queryParams(SURVEYORS);

                JSONObject root = new JSONObject();
                Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);

                List<GpapUsers> users;
                if (surveyors != null) {
                    String[] surveyorsArray = surveyors.split(";");
                    users = userDao.queryBuilder().where().in(GpapUsers.NAME_FIELD_NAME, Arrays.asList(surveyorsArray)).query();
                } else {
                    users = userDao.queryForAll();
                }

                // TODO parameterize users, from and to
                Long from = null;
                Long to = null;

                Dao<GpsLogs, ? > logsDao = DatabaseHandler.instance().getDao(GpsLogs.class);
                GssDatabaseUtilities.getLogs(root, logsDao, users, from, to);

                Dao<Notes, ? > notesDao = DatabaseHandler.instance().getDao(Notes.class);

                // simple notes
                GssDatabaseUtilities.getNotes(root, notesDao, users, from, to, false);
                // form notes
                GssDatabaseUtilities.getNotes(root, notesDao, users, from, to, true);

                Dao<Images, ? > imagesDao = DatabaseHandler.instance().getDao(Images.class);
                GssDatabaseUtilities.getImages(root, imagesDao, users, from, to);

                return root.toString();
            } else {
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
                res.status(ks.getCode());
                return ks.toJson();
            }
        });

        // get data from the server by type and the primary key id
        get("/data/:type/:id", ( req, res ) -> {
//            if (hasPermission(req)) {
            String type = req.params(":type");
            String id = req.params(":id");
            KukuratusLogger.logDebug("GssServer#get(/data/" + type + "/" + id,
                    "Received request from " + req.raw().getRemoteAddr());

            try {
                // images or notes are requested
                if (type.equals(GssDatabaseUtilities.IMAGES)) {
                    if (id != null) {
                        long idLong = Long.parseLong(id);
                        ImageData idObj = new ImageData(idLong);
                        Dao<ImageData, ? > dao = DatabaseHandler.instance().getDao(ImageData.class);
                        ImageData result = dao.queryForSameId(idObj);
                        return result.data;
                    }
                }
                return "";
            } catch (Exception e) {
                KukuratusLogger.logError("GssServer#get(/data/:type/:id", e);
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, "ERROR", e);
                res.status(ks.getCode());
                return ks.toJson();
            }
//            } else {
//                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
//                res.status(ks.getCode());
//                return ks.toJson();
//            }
        });

        // get an image by the original project id and the userid
        get("/imagedata/:userid/:originalid", ( req, res ) -> {
//            if (!hasPermission(req)) {
            String userId = req.params(":userid");
            String originalImageDataId = req.params(":originalid");
            KukuratusLogger.logDebug("GssServer#get(/imagedata/" + userId + "/" + originalImageDataId,
                    "Received request from " + req.raw().getRemoteAddr());

            try {
                long userIdLong = Long.parseLong(userId);
                long originalImageDataIdLong = Long.parseLong(originalImageDataId);
                Dao<ImageData, ? > dao = DatabaseHandler.instance().getDao(ImageData.class);
                ImageData imageData = dao.queryBuilder().where().eq(ImageData.ORIGINALID_FIELD_NAME, originalImageDataIdLong)
                        .and().eq(ImageData.GPAPUSER_FIELD_NAME, userIdLong).queryForFirst();
                return imageData.data;
            } catch (Exception e) {
                KukuratusLogger.logError("GssServer#get(/imagedata/:userid/:originalid", e);
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, "ERROR", e);
                res.status(ks.getCode());
                return ks.toJson();
            }
//            } else {
//                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
//                res.status(ks.getCode());
//                return ks.toJson();
//            }
        });

        get("/login", ( req, res ) -> {
            String authHeader = req.headers(AUTHORIZATION); // $NON-NLS-1$
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
            if (user != null) {
                KukuratusLogger.logDebug("GssServer#post(/login for " + user.getUniqueName(),
                        "Received request from " + req.raw().getRemoteAddr());
                boolean admin = RegistryHandler.INSTANCE.isAdmin(user);
                JSONObject response = new JSONObject();
                response.put(KEY_HASPERMISSION, true);
                response.put(KEY_ISADMIN, admin);

                // also get last used map background and map position
                String baseMap = RegistryHandler.INSTANCE.getSettingByKey(KEY_BASEMAP, "Mapsforge", user.getUniqueName());
                response.put(KEY_BASEMAP, baseMap);
                String xyz = RegistryHandler.INSTANCE.getSettingByKey(KEY_MAPCENTER, "0.0;0.0;6", user.getUniqueName());
                response.put(KEY_MAPCENTER, xyz);
                res.status(KukuratusStatus.CODE_200_OK);
                return response.toString();
            }
            JSONObject response = new JSONObject();
            response.put(KEY_HASPERMISSION, false);
            return response.toString();
        });

        post("/usersettings", ( req, res ) -> {
            String authHeader = req.headers(AUTHORIZATION); // $NON-NLS-1$
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
            if (user != null) {
                KukuratusLogger.logDebug("GssServer#post(/usersettings for " + user.getUniqueName(),
                        "Received request from " + req.raw().getRemoteAddr());
                String baseMap = req.queryParams(KEY_BASEMAP);
                if (baseMap != null) {
                    Settings s = new Settings(KEY_BASEMAP, baseMap, user.getUniqueName());
                    RegistryHandler.INSTANCE.insertOrUpdateSetting(s);
                }
                String mapCenter = req.queryParams(KEY_MAPCENTER);
                if (mapCenter != null) {
                    Settings s = new Settings(KEY_MAPCENTER, mapCenter, user.getUniqueName());
                    RegistryHandler.INSTANCE.insertOrUpdateSetting(s);
                }
                return "OK";
            } else {
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
                res.status(ks.getCode());
                return ks.toJson();
            }
        });

        get("/list/:type", ( req, res ) -> {
            KukuratusLogger.logDebug("GssServer#get(/list/:type", "Received request from " + req.raw().getRemoteAddr());
            if (hasPermission(req)) {
                String type = req.params(":type");
                if (type.equals(SURVEYORS)) {
                    Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                    List<GpapUsers> users = userDao.queryForAll();
                    JSONObject root = new JSONObject();
                    JSONArray usersArray = new JSONArray();
                    root.put(SURVEYORS, usersArray);
                    for( GpapUsers gpapUsers : users ) {
                        usersArray.put(gpapUsers.name);
                    }
                    return root.toString();
                }

                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND, "Type not recugnised.");
                res.status(ks.getCode());
                return ks.toJson();
            } else {
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
                res.status(ks.getCode());
                return ks.toJson();
            }
        });

        get("/", ( req, res ) -> {
            res.redirect("index.html");
            return null;
        });
        // ROUTES END
    }

    private boolean hasPermission( Request req ) throws Exception {
        try {
            String authHeader = req.headers(AUTHORIZATION); // $NON-NLS-1$
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
            return user != null;
        } catch (Exception e) {
            KukuratusLogger.logError(this, e);
            return false;
        }
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

            float factor = 1f;
            int tile = 256;
            mapsforgeTilesGenerator = new MapsforgeTilesGenerator("mapsforge", mapfiles, tile, factor, null); //$NON-NLS-1$
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

//        args = new String[]{"/home/hydrologis/TMP/TESTGSS/"}; // TODO remove after testing

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

        String keyStorePath = null;
        String keyStorePassword = null;
        if (args.length == 2) {
            File keyStorInfo = new File(args[1]);
            if (keyStorInfo.exists()) {
                keyStorePath = keyStorInfo.getAbsolutePath();
                // ask for the password at startup
                System.out.println("Please enter the keystore passord and press return:");
                try (Scanner in = new Scanner(System.in)) {
                    keyStorePassword = in.nextLine();
                    if (keyStorePassword.trim().length() == 0) {
                        System.out.println("Disabling keystore use due to empty password.");
                        keyStorePassword = null;
                        keyStorePath = null;
                    }
                }

            } else {
                System.err.println("Keystore file doesn't exist.");
                System.exit(1);
            }
        }

        GssServer lampServer = new GssServer();
        lampServer.start(keyStorePath, keyStorePassword);
    }

}