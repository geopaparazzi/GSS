package com.hydrologis.kukuratus.gss;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.objects.QueryResult;
import org.hortonmachine.dbs.log.EMessageType;
import org.hortonmachine.dbs.log.Logger;
import org.hortonmachine.dbs.log.Message;
import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.StringUtilities;
import org.hortonmachine.gears.utils.files.FileUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.hortonmachine.gears.utils.images.ImageUtilities;
import org.hortonmachine.gears.utils.time.UtcTimeUtilities;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.gss.database.Forms;
import com.hydrologis.kukuratus.gss.database.GpapProject;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.GpsLogsData;
import com.hydrologis.kukuratus.gss.database.ImageData;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.hydrologis.kukuratus.registry.Group;
import com.hydrologis.kukuratus.registry.RegistryHandler;
import com.hydrologis.kukuratus.registry.Settings;
import com.hydrologis.kukuratus.registry.User;
import com.hydrologis.kukuratus.servlets.ServletUtils;
import com.hydrologis.kukuratus.tiles.ITilesGenerator;
import com.hydrologis.kukuratus.utils.FormStatus;
import com.hydrologis.kukuratus.utils.KukuratusLogger;
import com.hydrologis.kukuratus.utils.KukuratusSession;
import com.hydrologis.kukuratus.utils.KukuratusStatus;
import com.hydrologis.kukuratus.utils.Messages;
import com.hydrologis.kukuratus.utils.NetworkUtilities;
import com.hydrologis.kukuratus.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

public class GssServerApiJavalin implements Vars {

    static final String ROUTES_GET_DBINFO = "/dbinfo";

    static final String ROUTES_LOG = "/log/:type/:limit";

    static final String ROUTES_DELETE_TYPE = "/delete/:type";
    static final String ROUTES_UPDATE_TYPE = "/update/:type";
    static final String ROUTES_LIST_TYPE = "/list/:type";
    /**
     * Get survey data.
     */
    static final String ROUTES_GETDATA = "/data";
    /**
     * Get base data as background maps or datasets.
     */
    static final String ROUTES_GET_BASEDATA = "/datadownload";
    /**
     * Get forms route.
     */
    static final String ROUTES_GET_FORMS = "/tagsdownload";
    /**
     * Get single data items by type and id.
     */
    static final String ROUTES_GETDATA_BY_TYPE = "/data/:type/:id";
    static final String ROUTES_GET_IMAGEDATA = "/imagedata/:dataid/:size";
    // static final String ROUTES_GET_IMAGEDATA = "/imagedata/:userid/:dataid";
    static final String ROUTES_UPLOAD = "/upload";
    static final String ROUTES_PROJECTDATA_UPLOAD = "/dataupload";
    static final String ROUTES_LOGIN = "/login";
    static final String ROUTES_USERSETTINGS = "/usersettings";
    static final String ROUTES_USERSETTINGS_BY_TYPE = "/usersettings/:type";
    static final String ROUTES_TILES_SOURCE_Z_X_Y = "/tiles/:source/:z/:x/:y";

    static final String NOTE_OBJID = "note";
    static final String IMAGE_OBJID = "image";
    static final String LOG_OBJID = "gpslog";
    static final String TYPE_KEY = "type";
    static final String IMAGE_COUNT_KEY = "imgcount";

    static final String PROJECT_NAME = "PROJECT_NAME";

    static final String TABLE_NOTES = "notes";
    static final String TABLE_NOTESEXT = "notesext";
    static final String NOTES_COLUMN_ID = "_id";
    static final String NOTES_COLUMN_LON = "lon";
    static final String NOTES_COLUMN_LAT = "lat";
    static final String NOTES_COLUMN_ALTIM = "altim";
    static final String NOTES_COLUMN_TS = "ts";
    static final String NOTES_COLUMN_DESCRIPTION = "description";
    static final String NOTES_COLUMN_TEXT = "text";
    static final String NOTES_COLUMN_FORM = "form";
    static final String NOTES_COLUMN_ISDIRTY = "isdirty";
    static final String NOTES_COLUMN_STYLE = "style";

    static final String TABLE_IMAGES = "images";
    static final String TABLE_IMAGE_DATA = "imagedata";
    static final String IMAGES_COLUMN_ID = "_id";
    static final String IMAGES_COLUMN_LON = "lon";
    static final String IMAGES_COLUMN_LAT = "lat";
    static final String IMAGES_COLUMN_ALTIM = "altim";
    static final String IMAGES_COLUMN_TS = "ts";
    static final String IMAGES_COLUMN_AZIM = "azim";
    static final String IMAGES_COLUMN_TEXT = "text";
    static final String IMAGES_COLUMN_ISDIRTY = "isdirty";
    static final String IMAGES_COLUMN_NOTE_ID = "note_id";
    static final String IMAGES_COLUMN_IMAGEDATA_ID = "imagedata_id";
    static final String IMAGESDATA_COLUMN_ID = "_id";
    static final String IMAGESDATA_COLUMN_IMAGE = "data";
    static final String IMAGESDATA_COLUMN_THUMBNAIL = "thumbnail";

    static final String TABLE_GPSLOGS = "gpslogs";
    static final String TABLE_GPSLOG_DATA = "gpslogsdata";
    static final String TABLE_GPSLOG_PROPERTIES = "gpslogsproperties";
    static final String LOGS_COLUMN_ID = "_id";
    static final String LOGS_COLUMN_STARTTS = "startts";
    static final String LOGS_COLUMN_ENDTS = "endts";
    static final String LOGS_COLUMN_LENGTHM = "lengthm";
    static final String LOGS_COLUMN_ISDIRTY = "isdirty";
    static final String LOGS_COLUMN_TEXT = "text";
    static final String LOGSPROP_COLUMN_ID = "_id";
    static final String LOGSPROP_COLUMN_VISIBLE = "visible";
    static final String LOGSPROP_COLUMN_WIDTH = "width";
    static final String LOGSPROP_COLUMN_COLOR = "color";
    static final String LOGSPROP_COLUMN_LOGID = "logid";
    static final String LOGSDATA_COLUMN_ID = "_id";
    static final String LOGSDATA_COLUMN_LON = "lon";
    static final String LOGSDATA_COLUMN_LAT = "lat";
    static final String LOGSDATA_COLUMN_ALTIM = "altim";
    static final String LOGSDATA_COLUMN_TS = "ts";
    static final String LOGSDATA_COLUMN_LOGID = "logid";
    static final String LOGSDATA_COLUMN_LON_FILTERED = "filtered_lon";
    static final String LOGSDATA_COLUMN_LAT_FILTERED = "filtered_lat";
    static final String LOGSDATA_COLUMN_ACCURACY_FILTERED = "filtered_accuracy";

    private static User hasPermission( Context ctx ) throws Exception {
        try {
            String authHeader = ctx.req.getHeader(AUTHORIZATION); // $NON-NLS-1$
//            KukuratusLogger.logDebug("HAS_PERMISSION", "Checking Auth: " + authHeader);
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
//            KukuratusLogger.logDebug("HAS_PERMISSION", "Checked and found user: " + user);
            return user;
        } catch (Exception e) {
            KukuratusLogger.logError("GssServerApi#hasPermission", e);
            return null;
        }
    }

    private static boolean hasPermissionDoubleCheck( Context ctx, String tag ) throws Exception {
        try {
            String authHeader = ctx.req.getHeader(AUTHORIZATION);
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
            if (user != null) {
                return true;
            }
            Object procObj = ServletUtils.canProceed(ctx, tag);
            if (!(procObj instanceof KukuratusStatus)) {
                return true;
            }
        } catch (Exception e) {
            KukuratusLogger.logError("GssServerApi#hasPermissionDoubleCheck", e);
        }
        return false;
    }

    public static void addCheckRoute( Javalin app ) {
        app.get("/check", ctx -> {
            ctx.result("It works. " + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS));
        });
    }

    public static void addDbinfoRoute( Javalin app ) {
        app.get(ROUTES_GET_DBINFO, ctx -> {
            User validUser = hasPermission(ctx);
            KukuratusLogger.logAccess(ROUTES_GET_DBINFO, getRequestLogString(ctx, validUser));
            if (validUser != null) {
                DatabaseHandler dbHandler = DatabaseHandler.instance();
                String[] dbInfo = dbHandler.getDb().getDbInfo();
                String dbInfoStr = StringUtilities.joinStrings("\n", dbInfo);
                ctx.result(dbInfoStr);
            } else {
                sendNoPermission(ctx);
            }
        });
    }

    public static void addTilesRoute( Javalin app, ITilesGenerator mapsforgeTilesGenerator ) {

        app.get(ROUTES_TILES_SOURCE_Z_X_Y, ctx -> {
            if (mapsforgeTilesGenerator == null) {
                // serve empty images
                HttpServletResponse raw = ctx.res;
                raw.setContentType("image/png");
                raw.addHeader("Content-Disposition", "attachment; filename=image.png");
                ServletOutputStream outputStream = raw.getOutputStream();

                BufferedImage transparent = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = transparent.createGraphics();
                g.setColor(new Color(0, 0, 0, 0));
                g.fillRect(0, 0, 256, 256);
                g.dispose();
                ImageIO.write(transparent, "png", outputStream);

                outputStream.flush();
                outputStream.close();
            } else {
                String source = ctx.pathParam(":source");
                if (source.equals("mapsforge")) {
                    String x = ctx.pathParam(":x");
                    String y = ctx.pathParam(":y");
                    String z = ctx.pathParam(":z");
                    int xTile = Integer.parseInt(x);
                    int yTile = Integer.parseInt(y);
                    int zoom = Integer.parseInt(z);
                    try {
                        HttpServletResponse raw = ctx.res;
                        raw.setContentType("image/png");
                        raw.addHeader("Content-Disposition", "attachment; filename=image.png");
                        ServletOutputStream outputStream = raw.getOutputStream();
                        mapsforgeTilesGenerator.getTile(xTile, yTile, zoom, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public static void addClientUploadRoute( Javalin app ) {
        app.post(ROUTES_UPLOAD, ctx -> {
            KukuratusLogger.logDebug(ROUTES_UPLOAD, getRequestLogString(ctx, null));

            Object procObj = ServletUtils.canProceed(ctx, ROUTES_UPLOAD);
            if (procObj instanceof KukuratusStatus) {
                KukuratusStatus ks = (KukuratusStatus) procObj;
                ctx.status(ks.getCode());
                ctx.result(ks.toJson());
            } else if (procObj instanceof String) {
                String deviceId = (String) procObj;

                DatabaseHandler dbHandler = DatabaseHandler.instance();
                GeometryFactory gf = GeometryUtilities.gf();
                Dao<GpapUsers, ? > usersDao = dbHandler.getDao(GpapUsers.class);
                GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId).queryForFirst();

                Dao<GpapProject, ? > projectDao = dbHandler.getDao(GpapProject.class);

                Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);

                Dao<GpsLogs, ? > logsDao = dbHandler.getDao(GpsLogs.class);
                Dao<GpsLogsData, ? > logsDataDao = dbHandler.getDao(GpsLogsData.class);

                Dao<ImageData, ? > imageDataDao = dbHandler.getDao(ImageData.class);
                Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);

                HashMap<String, Object> partData = new HashMap<String, Object>();
                Map<String, List<String>> formParamMap = ctx.formParamMap();
                Set<String> set = formParamMap.keySet();
                for( String key : set ) {
                    List<String> list = formParamMap.get(key);
                    if (list.size() > 0) {
                        partData.put(key, list.get(0));
                    }
                }

                String type = (String) partData.get(TYPE_KEY);
                String projectName = (String) partData.get(PROJECT_NAME);

                GpapProject project = projectDao.queryBuilder().where().eq(GpapProject.NAME_FIELD_NAME, projectName)
                        .queryForFirst();
                if (project == null) {
                    project = new GpapProject(projectName);
                    projectDao.create(project);
                }

                switch( type ) {
                case NOTE_OBJID:
                    String text = getString(partData, NOTES_COLUMN_TEXT, "- nv -");
                    String descr = getString(partData, NOTES_COLUMN_DESCRIPTION, "");
                    long ts = getLong(partData, NOTES_COLUMN_TS, 0);

                    double lon = getDouble(partData, NOTES_COLUMN_LON, 0);
                    double lat = getDouble(partData, NOTES_COLUMN_LAT, 0);
                    Coordinate coordinate = new Coordinate(lon, lat);
                    Point point = gf.createPoint(coordinate);
                    Envelope env = new Envelope(coordinate);
                    env.expandBy(0.000001);
                    ASpatialDb db = dbHandler.getDb();
                    String versionsSql = "id in (select  max(" + Notes.ID_FIELD_NAME + ")  from " + Notes.TABLE_NAME
                            + " group by ST_asText(" + Notes.GEOM_FIELD_NAME + ") )";

                    QueryResult tableRecords = db.getTableRecordsMapIn(Notes.TABLE_NAME, env, 1, -1, versionsSql);
                    long previousId = -1;
                    if (tableRecords.data.size() > 0 && tableRecords.geometryIndex != -1) {
                        int indexOf = getIndexIgnoreCase(tableRecords.names, Notes.ID_FIELD_NAME);
                        if (indexOf != -1) {
                            Object[] objects = tableRecords.data.get(0);
                            // TODO here it would be best to find the nearest
                            Object idObj = objects[indexOf];
                            if (idObj instanceof Long) {
                                previousId = (Long) idObj;
                            }
                        }
                    }

                    double altim = getDouble(partData, NOTES_COLUMN_ALTIM, 0);
                    String form = getString(partData, NOTES_COLUMN_FORM, null);

                    String marker = getString(partData, Notes.NOTESEXT_COLUMN_MARKER, "mapMarker");
                    double size = getDouble(partData, Notes.NOTESEXT_COLUMN_SIZE, 10);
                    double rotation = getDouble(partData, Notes.NOTESEXT_COLUMN_ROTATION, 0);
                    String color = getString(partData, Notes.NOTESEXT_COLUMN_COLOR, "#FFFFFF");
                    double accuracy = getDouble(partData, Notes.NOTESEXT_COLUMN_ACCURACY, 0);
                    double heading = getDouble(partData, Notes.NOTESEXT_COLUMN_HEADING, 0);
                    double speed = getDouble(partData, Notes.NOTESEXT_COLUMN_SPEED, 0);
                    double speedaccuracy = getDouble(partData, Notes.NOTESEXT_COLUMN_SPEEDACCURACY, 0);

                    Notes serverNote = new Notes(point, altim, ts, descr, text, form, marker, size, rotation, color, accuracy,
                            heading, speed, speedaccuracy, gpapUser, project, previousId, System.currentTimeMillis());
                    notesDao.create(serverNote);
                    if (form != null) {
                        List<String> imageIds = Utilities.getImageIds(form);
                        if (!imageIds.isEmpty()) {
                            HashMap<String, String> oldId2NewMap = new HashMap<>();
                            for( String imageIdString : imageIds ) {
                                UploadedFile imageFile = ctx
                                        .uploadedFile(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_IMAGE + "_" + imageIdString);
                                UploadedFile thumbFile = ctx
                                        .uploadedFile(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_THUMBNAIL + "_" + imageIdString);

                                byte[] imageData = getByteArray(imageFile.getContent()); // (byte[])
                                                                                         // partData.get(
                                // TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_IMAGE + "_" +
                                // imageIdString);
                                byte[] thumbData = getByteArray(thumbFile.getContent());
                                // (byte[]) partData.get(
                                // TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_THUMBNAIL + "_" +
                                // imageIdString);
                                ImageData imgData = new ImageData(imageData, gpapUser);
                                imageDataDao.create(imgData);
                                Point imgPoint = gf.createPoint(new Coordinate(lon, lat));
                                Images img = new Images(imgPoint, altim, ts, -1, imageFile.getFilename(), serverNote, imgData,
                                        gpapUser, thumbData, project, System.currentTimeMillis());
                                imagesDao.create(img);

                                oldId2NewMap.put(imageIdString, String.valueOf(img.id));
                            }
                            form = GssDatabaseUtilities.updateImagesIds(form, oldId2NewMap);

                            serverNote.form = form;
                            notesDao.update(serverNote);
                        }
                    }

                    ServletUtils.debug("Uploaded note: " + serverNote.text);
                    break;
                case IMAGE_OBJID:
                    // long imgId = getLong(partData, NOTES_COLUMN_ID, -1);
                    String imgText = getString(partData, IMAGES_COLUMN_TEXT, "- nv -");
                    long imgTs = getLong(partData, IMAGES_COLUMN_TS, 0);
                    double imgLon = getDouble(partData, IMAGES_COLUMN_LON, 0);
                    double imgLat = getDouble(partData, IMAGES_COLUMN_LAT, 0);
                    double imgAltim = getDouble(partData, IMAGES_COLUMN_ALTIM, -1);

                    UploadedFile imageFile = ctx.uploadedFile(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_IMAGE);
                    UploadedFile thumbFile = ctx.uploadedFile(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_THUMBNAIL);
                    byte[] imageData = getByteArray(imageFile.getContent());
                    byte[] thumbnailData = getByteArray(thumbFile.getContent());

                    // byte[] imageData = (byte[]) partData.get(TABLE_IMAGE_DATA + "_" +
                    // IMAGESDATA_COLUMN_IMAGE);
                    // byte[] thumbnailData = (byte[]) partData
                    // .get(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_THUMBNAIL);

                    ImageData imgData = new ImageData(imageData, gpapUser);
                    imageDataDao.create(imgData);
                    Point imgPoint = gf.createPoint(new Coordinate(imgLon, imgLat));
                    Images img = new Images(imgPoint, imgAltim, imgTs, -1, imgText, null, imgData, gpapUser, thumbnailData,
                            project, System.currentTimeMillis());
                    imagesDao.create(img);

                    ServletUtils.debug("Uploaded image: " + imgText);
                    break;
                case LOG_OBJID:
                    String logText = getString(partData, LOGS_COLUMN_TEXT, "- nv -");
                    long startts = getLong(partData, LOGS_COLUMN_STARTTS, 0);
                    long endts = getLong(partData, LOGS_COLUMN_ENDTS, 0);
                    float width = getFloat(partData, LOGSPROP_COLUMN_WIDTH, 3);
                    String logColor = getString(partData, LOGSPROP_COLUMN_COLOR, "#FFFFFF");
                    if (logColor.length() == 9) {
                        // has also alpha, remove it
                        logColor = "#" + logColor.substring(3);
                    }

                    String logDataJson = (String) partData.get(TABLE_GPSLOG_DATA);
                    JSONArray root = new JSONArray(logDataJson);
                    int length = root.length();
                    Coordinate[] coords = new Coordinate[length];
                    List<GpsLogsData> logsData = new ArrayList<>();
                    for( int i = 0; i < coords.length; i++ ) {
                        JSONObject pointObj = root.getJSONObject(i);
                        double pLat = 0;
                        double pLon = 0;
                        double pAltim = 0;

                        if (pointObj.has(LOGSDATA_COLUMN_LAT_FILTERED) && pointObj.has(LOGSDATA_COLUMN_LON_FILTERED)
                                && pointObj.has(LOGSDATA_COLUMN_ALTIM)) {
                            pLat = pointObj.getDouble(LOGSDATA_COLUMN_LAT_FILTERED);
                            pLon = pointObj.getDouble(LOGSDATA_COLUMN_LON_FILTERED);
                            pAltim = pointObj.getDouble(LOGSDATA_COLUMN_ALTIM);
                        } else {
                            // then use the standard
                            pLat = pointObj.getDouble(LOGSDATA_COLUMN_LAT);
                            pLon = pointObj.getDouble(LOGSDATA_COLUMN_LON);
                            pAltim = pointObj.getDouble(LOGSDATA_COLUMN_ALTIM);
                        }
                        long pTs = pointObj.getLong(LOGSDATA_COLUMN_TS);

                        Coordinate coord = new Coordinate(pLon, pLat);
                        coords[i] = coord;
                        Point logPoint = gf.createPoint(coord);
                        GpsLogsData gpsLogsData = new GpsLogsData(logPoint, pAltim, pTs, null);
                        logsData.add(gpsLogsData);
                    }
                    LineString logLine = gf.createLineString(coords);
                    GpsLogs newLog = new GpsLogs(logText, startts, endts, logLine, logColor, width, gpapUser, project,
                            System.currentTimeMillis());
                    logsData.forEach(ld -> ld.gpsLog = newLog);

                    logsDao.create(newLog);
                    logsDataDao.create(logsData);
                    ServletUtils.debug("Uploaded log: " + logText);
                    break;

                default:
                    break;
                }

                String message = Messages.getString("UploadServlet.data_uploaded");
                ServletUtils.debug("SENDING RESPONSE MESSAGE: " + message); //$NON-NLS-1$
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, message);
                ctx.status(ks.getCode());
                ctx.result(ks.toJson());
            } else {
                sendNoPermission(ctx);
            }
        });

    }

    private static int getIndexIgnoreCase( List<String> strings, String string ) {
        for( int i = 0; i < strings.size(); i++ ) {
            if (strings.get(i).equalsIgnoreCase(string)) {
                return i;
            }
        }
        return -1;
    }

    public static void addClientProjectDataUploadRoute( Javalin app ) {
        app.post(ROUTES_PROJECTDATA_UPLOAD, ctx -> {
            User validUser = hasPermission(ctx);
            KukuratusLogger.logAccess(ROUTES_PROJECTDATA_UPLOAD, getRequestLogString(ctx, validUser));
            if (validUser != null) {
                DatabaseHandler dbHandler = DatabaseHandler.instance();
                try {

                    String errorMsg = null;
                    UploadedFile uploadedFile = ctx.uploadedFile("file");
                    if (uploadedFile != null) {
                        String fileName = uploadedFile.getFilename();
                        if (fileName != null) {
                            if (ServletUtils.isBaseMap(fileName)) {
                                byte[] byteArray = getByteArray(uploadedFile.getContent());
                                File folder = ServletUtils.getBasemapsFolder().get();
                                File file = new File(folder, fileName);
                                file = checkIfExists(file);
                                try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
                                    fos.write(byteArray);
                                }
                            } else if (ServletUtils.isProject(fileName)) {
                                byte[] byteArray = getByteArray(uploadedFile.getContent());
                                File folder = ServletUtils.getProjectsFolder().get();
                                File file = new File(folder, fileName);
                                file = checkIfExists(file);
                                try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
                                    fos.write(byteArray);
                                }
                            } else if (fileName.endsWith("tags.json")) {
                                String form = getString(uploadedFile.getContent());
                                Dao<Forms, ? > formsDao = dbHandler.getDao(Forms.class);

                                long countOf = formsDao.queryBuilder().where().eq(Forms.NAME_FIELD_NAME, fileName).countOf();
                                if (countOf == 0) {
                                } else {
                                    String name = fileName.replaceFirst("tags.json", "");
                                    if (!name.endsWith("_")) {
                                        name = name + "_";
                                    }
                                    fileName = name
                                            + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact_string)
                                            + "_tags.json";
                                }
                                Forms f = new Forms(fileName, form, validUser.uniqueName, FormStatus.VISIBLE.getStatusCode());
                                int create = formsDao.create(f);
                                if (create != 1) {
                                    errorMsg = "ERROR: An error occurred while inserting in the server.";
                                }
                            }

                        } else {
                            errorMsg = "ERROR: No file name supplied in data sent to server.";
                        }
                    } else {
                        errorMsg = "ERROR: No file object found in data sent to server.";
                    }

                    // Collection<Part> parts = ctx.req.getParts();
                    // if (parts.size() > 0) {
                    // Part part = parts.iterator().next();
                    // String partName = part.getName();
                    // if (partName.equals("file")) {
                    // String fileName = part.getSubmittedFileName();

                    // } else {
                    // errorMsg = "ERROR: No file object found in data sent to server.";
                    // }
                    // }

                    if (errorMsg != null) {
                        KukuratusLogger.logError(ROUTES_PROJECTDATA_UPLOAD, errorMsg, null);
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, errorMsg);
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                        return;
                    }

                } catch (Exception e) {
                    sendServerError(ctx, ROUTES_PROJECTDATA_UPLOAD, e);
                }

                String message = Messages.getString("UploadServlet.data_uploaded");
                ServletUtils.debug("SENDING RESPONSE MESSAGE: " + message); //$NON-NLS-1$
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, message);
                ctx.status(ks.getCode());
                ctx.result(ks.toJson());
            } else {
                sendNoPermission(ctx);
            }
        });

    }

    private static File checkIfExists( File file ) {
        if (file.exists()) {
            // add timestamp to it
            String name = FileUtilities.getNameWithoutExtention(file);
            int lastDot = file.getName().lastIndexOf(".");
            String ext = file.getName().substring(lastDot);
            file = new File(file.getParentFile(),
                    name + "_" + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSScompact_string) + ext);
        }
        return file;
    }

    public static void addGetDataRoute( Javalin app ) {

        app.post(ROUTES_GETDATA, ctx -> {
            try {

                User validUser = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_GETDATA, getRequestLogString(ctx, validUser));
                if (validUser != null) {
                    String surveyors = ctx.formParam(SURVEYORS);
                    String projects = ctx.formParam(PROJECTS);

                    JSONObject root = new JSONObject();

                    Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);

                    List<GpapUsers> users = null;

                    if (surveyors != null) {
                        String[] surveyorsArray = surveyors.split(";");
                        users = userDao.queryBuilder().where().eq(GpapUsers.ACTIVE_FIELD_NAME, 1).and()
                                .in(GpapUsers.NAME_FIELD_NAME, Arrays.asList(surveyorsArray)).query();
                    } else {
                        users = userDao.queryBuilder().where().eq(GpapUsers.ACTIVE_FIELD_NAME, 1).query();
                    }

                    if (!users.isEmpty()) {
                        Dao<GpapProject, ? > projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                        List<GpapProject> projectsList = null;
                        if (projects != null) {
                            String[] projectsArray = projects.split(";");
                            projectsList = projectDao.queryBuilder().where()
                                    .in(GpapProject.NAME_FIELD_NAME, Arrays.asList(projectsArray)).query();
                        }

                        // TODO parameterize users, from and to
                        Long from = null;
                        Long to = null;

                        Dao<GpsLogs, ? > logsDao = DatabaseHandler.instance().getDao(GpsLogs.class);
                        GssDatabaseUtilities.getLogs(root, logsDao, users, projectsList, null, null);

                        Dao<Notes, ? > notesDao = DatabaseHandler.instance().getDao(Notes.class);

                        GssDatabaseUtilities.getNotesMin(root, notesDao, projectDao, userDao, users, projectsList, null, projects);

                        Dao<Images, ? > imagesDao = DatabaseHandler.instance().getDao(Images.class);
                        GssDatabaseUtilities.getImages(root, imagesDao, projectDao, userDao, users, projectsList, null, null);

                    }
                    ctx.result(root.toString());
                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_GETDATA, e);
            }
        });
    }

    public static void addLogRoute( Javalin app ) {

        app.get(ROUTES_LOG, ctx -> {
            try {

                User validUser = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_LOG, getRequestLogString(ctx, validUser));
                if (validUser != null) {
                    String type = ctx.pathParam(":type");
                    EMessageType messageType = EMessageType.ALL;
                    if (type != null && type.length() > 0)
                        messageType = EMessageType.valueOf(type);

                    String limit = ctx.pathParam(":limit");
                    long lim = 1000;
                    if (limit != null) {
                        try {
                            lim = Long.parseLong(limit);
                        } catch (Exception e) {
                            // ignore
                        }
                    }

                    JSONObject root = new JSONObject();

                    Logger logDb = KukuratusWorkspace.getInstance().getLogDb();
                    List<Message> messagesList = logDb.getFilteredList(messageType, null, null, lim);

                    JSONArray logsArray = new JSONArray();
                    root.put(ServletUtils.LOG, logsArray); // $NON-NLS-1$
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    for( Message msg : messagesList ) {
                        JSONObject msgObj = new JSONObject();
                        msgObj.put(ServletUtils.LOGTS, f.format(new Date(msg.ts)));
                        msgObj.put(ServletUtils.LOGTYPE, EMessageType.fromCode(msg.type).name());
                        msgObj.put(ServletUtils.LOGMSG, msg.msg);
                        logsArray.put(msgObj);
                    }

                    ctx.result(root.toString());
                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_LOG, e);
            }
        });
    }

    public static void addGetDataByTypeRoute( Javalin app ) {

        // get data from the server by type and the primary key id
        app.get(ROUTES_GETDATA_BY_TYPE, ctx -> {
            User validUser = hasPermission(ctx);
            KukuratusLogger.logAccess(ROUTES_GETDATA_BY_TYPE, getRequestLogString(ctx, validUser));
            if (validUser != null) {
                String type = ctx.pathParam(":type");
                String id = ctx.pathParam(":id");

                try {
                    // images or notes are requested
                    if (type.equals(GssDatabaseUtilities.IMAGEDATA)) {
                        if (id != null) {
                            long idLong = Long.parseLong(id);
                            ImageData idObj = new ImageData(idLong);
                            Dao<ImageData, ? > dao = DatabaseHandler.instance().getDao(ImageData.class);
                            ImageData result = dao.queryForSameId(idObj);
                            ctx.result(result.data);
                        }
                    } else if (type.equals(GssDatabaseUtilities.IMAGES)) {
                        if (id != null) {
                            Dao<Images, ? > imagesDao = DatabaseHandler.instance().getDao(Images.class);
                            Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                            Dao<GpapProject, ? > projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                            long idLong = Long.parseLong(id);
                            JSONObject imageObj = GssDatabaseUtilities.getImageById(imagesDao, projectDao, userDao, idLong);
                            ctx.result(imageObj.toString());
                        }
                    } else if (type.equals(GssDatabaseUtilities.NOTES)) {
                        if (id != null) {
                            Dao<Notes, ? > notesDao = DatabaseHandler.instance().getDao(Notes.class);
                            Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                            Dao<GpapProject, ? > projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                            long idLong = Long.parseLong(id);
                            JSONObject noteObj = GssDatabaseUtilities.getNoteById(notesDao, projectDao, userDao, idLong);
                            ctx.result(noteObj.toString());
                        }
                    }
                } catch (Exception e) {
                    sendServerError(ctx, ROUTES_GETDATA_BY_TYPE, e);
                }
            } else {
                sendNoPermission(ctx);
            }
        });
    }

    public static void addClientGetBaseDataRoute( Javalin app ) {
        app.get(ROUTES_GET_BASEDATA, ctx -> {
            if (hasPermissionDoubleCheck(ctx, ROUTES_GET_BASEDATA)) {
                KukuratusLogger.logAccess(ROUTES_GET_BASEDATA, getRequestLogString(ctx, null));
                String fileName = ctx.queryParam("name");
                try {
                    if (fileName == null) {
                        // send list
                        String mapsListJson = ServletUtils.getMapsListJson();
                        ctx.result(mapsListJson);
                    } else {

                        ctx.res.setContentType("application/octet-stream"); //$NON-NLS-1$
                        ctx.res.setHeader("Content-disposition", "attachment; filename=" + fileName);

                        Optional<File> mapFileOpt = ServletUtils.getMapFile(fileName);
                        if (mapFileOpt.isPresent()) {

                            File file = mapFileOpt.get();
                            try (InputStream in = new BufferedInputStream(new FileInputStream(file));
                                    ServletOutputStream out = ctx.res.getOutputStream()) {
                                byte[] buffer = new byte[8192];
                                int numBytesRead;
                                while( (numBytesRead = in.read(buffer)) > 0 ) {
                                    out.write(buffer, 0, numBytesRead);
                                }
                            }
                        }

                    }
                } catch (Exception e) {
                    sendServerError(ctx, ROUTES_GET_BASEDATA + "/" + fileName, e);
                }
            } else {
                sendNoPermission(ctx);
            }
        });
    }

    public static void addClientGetFormsRoute( Javalin app ) {
        app.get(ROUTES_GET_FORMS, ctx -> {
            if (hasPermissionDoubleCheck(ctx, ROUTES_GET_FORMS)) {
                KukuratusLogger.logAccess(ROUTES_GET_FORMS, getRequestLogString(ctx, null));

                Dao<Forms, ? > formsDao = DatabaseHandler.instance().getDao(Forms.class);
                String tagName = ctx.queryParam("name");
                try {
                    if (tagName != null) {
                        Forms form = formsDao.queryBuilder().where().eq(Forms.NAME_FIELD_NAME, tagName).queryForFirst();
                        ctx.result(form.form);
                    } else {
                        List<Forms> visibleForms = formsDao.queryBuilder().orderBy(Forms.NAME_FIELD_NAME, true).query();
                        JSONObject root = new JSONObject();
                        JSONArray formsArray = new JSONArray();
                        root.put(ServletUtils.TAGS, formsArray); // $NON-NLS-1$
                        for( Forms form : visibleForms ) {
                            JSONObject formObj = new JSONObject();
                            formObj.put(ServletUtils.TAG, form.name); // $NON-NLS-1$
                            formObj.put(ServletUtils.TAGID, form.id); // $NON-NLS-1$
                            formsArray.put(formObj);
                        }

                        ctx.result(root.toString());
                    }
                } catch (Exception e) {
                    sendServerError(ctx, ROUTES_GET_FORMS + "/" + tagName, e);
                }
            } else {
                sendNoPermission(ctx);
            }
        });
    }

    public static void addGetImagedataRoute( Javalin app ) {
        app.get(ROUTES_GET_IMAGEDATA, ctx -> {
            User validUser = hasPermission(ctx);
            if (validUser != null) {
                String imageDataId = ctx.pathParam(":dataid");
                String sizeStr = ctx.pathParam(":size");
                KukuratusLogger.logAccess(ROUTES_GET_IMAGEDATA + "/" + imageDataId, getRequestLogString(ctx, null));

                try {
                    long imageDataIdLong = Long.parseLong(imageDataId);
                    Dao<ImageData, ? > dao = DatabaseHandler.instance().getDao(ImageData.class);
                    ImageData imageData = dao.queryBuilder().where().eq(ImageData.ID_FIELD_NAME, imageDataIdLong).queryForFirst();
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData.data));
                    if (sizeStr != null) {
                        int size = 800;
                        try {
                            size = Integer.parseInt(sizeStr);
                        } catch (Exception e) {
                        }
                        image = ImageUtilities.scaleImage(image, size);
                    }
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    ImageIO.write(image, "jpg", bo);
                    ctx.result(bo.toByteArray());
                } catch (Exception e) {
                    sendServerError(ctx, ROUTES_GET_IMAGEDATA + "/" + imageDataId, e);
                }
            } else {
                sendNoPermission(ctx);
            }
        });
    }

    // // public static void addGetImagedataRoute() {
    // // get(ROUTES_GET_IMAGEDATA, (req, res) -> {
    // // // User validUser = hasPermission(req);
    // // // if (validUser != null) {
    // // String userId = req.params(":userid");
    // // String imageDataId = req.params(":dataid");
    // // KukuratusLogger.logDebug(ROUTES_GET_IMAGEDATA + "/" + userId + "/" +
    // // imageDataId,
    // // getRequestLogString(req, null));

    // // try {
    // // long userIdLong = Long.parseLong(userId);
    // // long imageDataIdLong = Long.parseLong(imageDataId);
    // // Dao<ImageData, ?> dao =
    // DatabaseHandler.instance().getDao(ImageData.class);
    // // ImageData imageData =
    // dao.queryBuilder().where().eq(ImageData.ID_FIELD_NAME,
    // // imageDataIdLong).and()
    // // .eq(ImageData.GPAPUSER_FIELD_NAME, userIdLong).queryForFirst();
    // // return imageData.data;
    // // } catch (Exception e) {
    // // return sendServerError(res, ROUTES_GET_IMAGEDATA + "/" + userId + "/" +
    // // imageDataId, e);
    // // }
    // // // } else {
    // // // return sendNoPermission(res);
    // // // }
    // // });
    // // }

    public static void addLoginRoute( Javalin app ) {

        app.get(ROUTES_LOGIN, ctx -> {
            try {
                KukuratusLogger.logAccess(ROUTES_LOGIN, getRequestLogString(ctx, null));
                User user = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_LOGIN, getRequestLogString(ctx, user));
                if (user != null) {
                    boolean admin = RegistryHandler.INSTANCE.isAdmin(user);
                    JSONObject response = new JSONObject();
                    response.put(KEY_HASPERMISSION, true);
                    response.put(KEY_ISADMIN, admin);

                    // also get last used map background and map position
                    String baseMap = RegistryHandler.INSTANCE.getSettingByKey(KEY_BASEMAP, "Openstreetmap", user.getUniqueName());
                    response.put(KEY_BASEMAP, baseMap);
                    String xyz = RegistryHandler.INSTANCE.getSettingByKey(KEY_MAPCENTER, "0.0;0.0;6", user.getUniqueName());
                    response.put(KEY_MAPCENTER, xyz);
                    ctx.status(KukuratusStatus.CODE_200_OK);
                    ctx.result(response.toString());
                } else {
                    JSONObject response = new JSONObject();
                    response.put(KEY_HASPERMISSION, false);
                    ctx.result(response.toString());
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_LOGIN, e);
            }
        });
    }

    public static void addUserSettingsRoute( Javalin app ) {

        app.post(ROUTES_USERSETTINGS, ctx -> {
            try {
                User user = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_USERSETTINGS, getRequestLogString(ctx, user));
                if (user != null) {
                    String baseMap = ctx.formParam(KEY_BASEMAP);
                    if (baseMap != null) {
                        Settings s = new Settings(KEY_BASEMAP, baseMap, user.getUniqueName());
                        RegistryHandler.INSTANCE.insertOrUpdateSetting(s);
                    }
                    String mapCenter = ctx.formParam(KEY_MAPCENTER);
                    if (mapCenter != null) {
                        Settings s = new Settings(KEY_MAPCENTER, mapCenter, user.getUniqueName());
                        RegistryHandler.INSTANCE.insertOrUpdateSetting(s);
                    }
                    String bookmarks = ctx.formParam(KEY_BOOKMARKS);
                    if (bookmarks != null) {
                        Settings s = new Settings(KEY_BOOKMARKS, bookmarks, user.getUniqueName());
                        RegistryHandler.INSTANCE.insertOrUpdateSetting(s);
                    }
                    String automaticRegistrationMillis = ctx.formParam(KukuratusSession.KEY_AUTOMATIC_REGISTRATION);
                    if (automaticRegistrationMillis != null) {
                        Settings s = new Settings(KukuratusSession.KEY_AUTOMATIC_REGISTRATION, automaticRegistrationMillis,
                                user.getUniqueName());
                        RegistryHandler.INSTANCE.insertOrUpdateGlobalSetting(s);
                    }
                    ctx.result("OK");
                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_USERSETTINGS, e);
            }
        });

        app.get(ROUTES_USERSETTINGS_BY_TYPE, ctx -> {
            try {
                User user = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_USERSETTINGS_BY_TYPE, getRequestLogString(ctx, user));
                if (user != null) {
                    String type = ctx.pathParam(":type");
                    if (type.equals(KEY_BASEMAP)) {
                        String setting = RegistryHandler.INSTANCE.getSettingByKey(KEY_BASEMAP, "Openstreetmap",
                                user.getUniqueName());
                        ctx.result(setting);
                    } else if (type.equals(KEY_MAPCENTER)) {
                        String setting = RegistryHandler.INSTANCE.getSettingByKey(KEY_MAPCENTER, "0;0;6", user.getUniqueName());
                        ctx.result(setting);
                    } else if (type.equals(KEY_BOOKMARKS)) {
                        String setting = RegistryHandler.INSTANCE.getSettingByKey(KEY_BOOKMARKS, "earth:-160.0,160.0,-85.0,85.0",
                                user.getUniqueName());
                        ctx.result(setting);
                    }
                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_USERSETTINGS, e);
            }
        });
    }

    public static void addListByTypeRoute( Javalin app ) {

        app.get(ROUTES_LIST_TYPE, ctx -> {
            try {
                User validUser = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_LIST_TYPE, getRequestLogString(ctx, validUser));
                if (validUser != null) {
                    String type = ctx.pathParam(":type");
                    if (type.equals(SURVEYORS)) {
                        Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                        List<GpapUsers> users = userDao.queryForAll();
                        JSONObject root = new JSONObject();
                        JSONArray surveyorsArray = new JSONArray();
                        root.put(SURVEYORS, surveyorsArray);
                        for( GpapUsers gpapUsers : users ) {
                            surveyorsArray.put(gpapUsers.toJson());
                        }
                        ctx.result(root.toString());
                    } else if (type.equals(PROJECTS)) {
                        Dao<GpapProject, ? > projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                        List<GpapProject> projects = projectDao.queryForAll();
                        JSONObject root = new JSONObject();
                        JSONArray projectsArray = new JSONArray();
                        root.put(PROJECTS, projectsArray);
                        for( GpapProject gpapProject : projects ) {
                            projectsArray.put(gpapProject.name);
                        }
                        ctx.result(root.toString());
                    } else if (type.equals(WEBUSERS)) {
                        JSONObject root = new JSONObject();
                        JSONArray usersArray = new JSONArray();
                        root.put(WEBUSERS, usersArray);
                        List<Group> groups = RegistryHandler.INSTANCE.getGroupsWithAuthorizations();
                        for( Group group : groups ) {
                            List<User> usersList = RegistryHandler.INSTANCE.getUsersOfGroup(group);
                            for( User user : usersList ) {
                                user.setGroup(group);
                                usersArray.put(user.toJson());
                            }
                        }
                        ctx.result(root.toString());
                    } else {
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND, "Type not recognised: type");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    }
                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_LIST_TYPE, e);
            }
        });
    }

    public static void addUpdateByTypeRoute( Javalin app ) {

        app.post(ROUTES_UPDATE_TYPE, ctx -> {
            try {
                User validUser = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_UPDATE_TYPE, getRequestLogString(ctx, validUser));
                if (validUser != null) {
                    String type = ctx.pathParam(":type");
                    if (type.equals(SURVEYORS)) {
                        String id = ctx.formParam(GpapUsers.ID_FIELD_NAME);
                        String deviceId = ctx.formParam(GpapUsers.DEVICE_FIELD_NAME);
                        String name = ctx.formParam(GpapUsers.NAME_FIELD_NAME);
                        String contact = ctx.formParam(GpapUsers.CONTACT_FIELD_NAME);
                        String active = ctx.formParam(GpapUsers.ACTIVE_FIELD_NAME);

                        Dao<GpapUsers, ? > userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                        if (id != null) {
                            GpapUsers existingUser = userDao.queryBuilder().where()
                                    .eq(GpapUsers.ID_FIELD_NAME, Long.parseLong(id)).queryForFirst();
                            if (existingUser != null) {
                                // update
                                existingUser.deviceId = deviceId;
                                existingUser.name = name;
                                existingUser.contact = contact;
                                existingUser.active = Integer.parseInt(active);
                                userDao.update(existingUser);
                            } else {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                        "No user existing by the id: " + id);
                                ctx.status(ks.getCode());
                                ctx.result(ks.toJson());
                            }
                        } else {
                            // create new one
                            GpapUsers newUser = new GpapUsers(deviceId, name, contact, 1);
                            userDao.create(newUser);
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    } else if (type.equals(WEBUSERS)) {
                        String id = ctx.formParam(User.ID_FIELD_NAME);
                        String name = ctx.formParam(User.NAME_FIELD_NAME);
                        String uniqueName = ctx.formParam(User.UNIQUENAME_FIELD_NAME);
                        String email = ctx.formParam(User.EMAIL_FIELD_NAME);
                        String pwd = ctx.formParam(User.PASSWORD_FIELD_NAME);
                        String group = ctx.formParam(User.GROUP_FIELD_NAME);

                        if (id != null) {
                            User existingUser = RegistryHandler.INSTANCE.getUserById(Long.parseLong(id));
                            if (existingUser != null) {
                                // update
                                existingUser.uniqueName = uniqueName;
                                existingUser.name = name;
                                existingUser.email = email;
                                if (pwd != null) {
                                    existingUser.pwd = RegistryHandler.hashPwd(pwd);
                                }
                                if (group != null) {
                                    existingUser.group = RegistryHandler.INSTANCE.getGroupByName(group);
                                }
                                RegistryHandler.INSTANCE.updateUser(existingUser);
                            } else {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                        "No webuser existing by the id: " + id);
                                ctx.status(ks.getCode());
                                ctx.result(ks.toJson());
                            }
                        } else {
                            // create new one
                            Group groupObj = RegistryHandler.INSTANCE.getGroupByName(group);
                            User newUser = new User(name, uniqueName, email, pwd, groupObj);
                            RegistryHandler.INSTANCE.addUser(newUser);
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    } else {
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                "Type not recognised: " + type);
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    }

                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_UPDATE_TYPE, e);
            }
        });
    }

    public static void addDeleteByTypeRoute( Javalin app ) {

        app.post(ROUTES_DELETE_TYPE, ctx -> {
            try {
                User validUser = hasPermission(ctx);
                KukuratusLogger.logAccess(ROUTES_DELETE_TYPE, getRequestLogString(ctx, validUser));
                if (validUser != null) {
                    String type = ctx.pathParam(":type");
                    if (type.equals(WEBUSERS)) {
                        String id = ctx.formParam(User.ID_FIELD_NAME);
                        if (id != null) {
                            User existingUser = RegistryHandler.INSTANCE.getUserById(Long.parseLong(id));
                            if (existingUser != null) {
                                RegistryHandler.INSTANCE.deleteUser(existingUser);
                            } else {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                        "No webuser existing by the id: " + id);
                                ctx.status(ks.getCode());
                                ctx.result(ks.toJson());
                            }
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    } else if (type.equals(FORMS)) {
                        String id = ctx.formParam(Forms.ID_FIELD_NAME);
                        if (id != null) {
                            Dao<Forms, ? > formsDao = DatabaseHandler.instance().getDao(Forms.class);
                            Forms f = new Forms(Long.parseLong(id));
                            formsDao.delete(f);
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    } else if (type.equals(GPSLOGS)) {
                        String idStr = ctx.formParam(ID);
                        if (idStr != null) {
                            long id = Long.parseLong(idStr);
                            Dao<GpsLogs, ? > logDao = DatabaseHandler.instance().getDao(GpsLogs.class);
                            Dao<GpsLogsData, ? > logDataDao = DatabaseHandler.instance().getDao(GpsLogsData.class);

                            GpsLogsData dataToRemove = logDataDao.queryBuilder().where().eq(GpsLogsData.GPSLOGS_FIELD_NAME, id)
                                    .queryForFirst();
                            int deleted = logDataDao.delete(dataToRemove);
                            boolean error = false;
                            if (deleted == 1) {
                                GpsLogs logToRemove = new GpsLogs(id);
                                deleted = logDao.delete(logToRemove);
                                if (deleted != 1) {
                                    error = true;
                                }
                            } else {
                                error = true;
                            }
                            if (error) {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR,
                                        "An error occurred while deleting the gps log.");
                                ctx.status(ks.getCode());
                                ctx.result(ks.toJson());
                                return;
                            }
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    } else if (type.equals(NOTES)) {
                        String idStr = ctx.formParam(ID);
                        if (idStr != null) {
                            long id = Long.parseLong(idStr);

                            Dao<Notes, ? > notesDao = DatabaseHandler.instance().getDao(Notes.class);

                            Notes noteToRemove = notesDao.queryBuilder().where().eq(Notes.ID_FIELD_NAME, id).queryForFirst();
                            int deleted = notesDao.delete(noteToRemove);
                            boolean error = false;
                            if (deleted != 1) {
                                error = true;
                            }
                            if (error) {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR,
                                        "An error occurred while deleting the note.");
                                ctx.status(ks.getCode());
                                ctx.result(ks.toJson());
                                return;
                            }
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    } else {
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                "Type not recognised: " + type);
                        ctx.status(ks.getCode());
                        ctx.result(ks.toJson());
                    }

                } else {
                    sendNoPermission(ctx);
                }
            } catch (Exception e) {
                sendServerError(ctx, ROUTES_DELETE_TYPE, e);
            }
        });
    }

    //////////////////////////////////////
    // HELPER METHODS
    //////////////////////////////////////

    private static void sendNoPermission( Context ctx ) {
        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
        ctx.status(ks.getCode());
        ctx.result(ks.toJson());
    }

    private static void sendServerError( Context ctx, String route, Throwable e ) {
        KukuratusLogger.logError(route, e);
        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, "ERROR", e);
        ctx.status(ks.getCode());
        ctx.result(ks.toJson());
    }

    private static long getLong( HashMap<String, Object> partData, String key, long defaultValue ) {
        if (partData.containsKey(key)) {
            Object object = partData.get(key);
            if (object instanceof String) {
                String value = (String) partData.get(key);
                if (value.equals("null")) {
                    return defaultValue;
                } else {
                    return Long.parseLong(value);
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private static String getString( HashMap<String, Object> partData, String key, String defaultValue ) {
        if (partData.containsKey(key)) {
            Object object = partData.get(key);
            if (object instanceof String) {
                String value = (String) partData.get(key);
                if (value.equals("null")) {
                    return defaultValue;
                } else {
                    return value;
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private static double getDouble( HashMap<String, Object> partData, String key, double defaultValue ) {
        if (partData.containsKey(key)) {
            Object object = partData.get(key);
            if (object instanceof String) {
                String value = (String) partData.get(key);
                if (value.equals("null")) {
                    return defaultValue;
                } else {
                    return Double.parseDouble(value);
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private static float getFloat( HashMap<String, Object> partData, String key, float defaultValue ) {
        if (partData.containsKey(key)) {
            Object object = partData.get(key);
            if (object instanceof String) {
                String value = (String) partData.get(key);
                if (value.equals("null")) {
                    return defaultValue;
                } else {
                    return Float.parseFloat(value);
                }
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private static String getFilename( Part part ) {
        for( String cd : part.getHeader("content-disposition").split(";") ) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private static String getValue( Part part ) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[1024];
        for( int length = 0; (length = reader.read(buffer)) > 0; ) {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }

    private static byte[] getByteArray( Part part ) throws IOException {
        try (InputStream is = part.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while( (nRead = is.read(data, 0, data.length)) != -1 ) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            return byteArray;
        }
    }

    private static byte[] getByteArray( InputStream is ) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while( (nRead = is.read(data, 0, data.length)) != -1 ) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();
        return byteArray;
    }

    private static String getString( Part part ) throws IOException {
        try (InputStream is = part.getInputStream()) {
            String text = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
            return text;
        }
    }

    private static String getString( InputStream is ) throws IOException {
        String text = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
        return text;
    }

    private static String getRequestLogString( Context ctx, User user ) {
        String userStr = "";
        if (user != null) {
            userStr = " by user " + user.uniqueName;
        }
        return "Received request" + userStr + " from " + ctx.ip();
    }
}