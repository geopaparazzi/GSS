package com.hydrologis.kukuratus.gss;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

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

import org.hortonmachine.dbs.compat.objects.QueryResult;
import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.StringUtilities;
import org.hortonmachine.gears.utils.files.FileUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import io.javalin.Javalin;
import spark.Request;
import spark.Response;

public class GssServerApiJavalin implements Vars {

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
    static final String ROUTES_GET_IMAGEDATA = "/imagedata/:dataid";
    // static final String ROUTES_GET_IMAGEDATA = "/imagedata/:userid/:dataid";
    static final String ROUTES_UPLOAD = "/upload";
    static final String ROUTES_PROJECTDATA_UPLOAD = "/dataupload";
    static final String ROUTES_LOGIN = "/login";
    static final String ROUTES_USERSETTINGS = "/usersettings";
    static final String ROUTES_TILES_SOURCE_Z_X_Y = "/tiles/:source/:z/:x/:y";

    static final String NOTE_OBJID = "note";
    static final String IMAGE_OBJID = "image";
    static final String LOG_OBJID = "gpslog";
    static final String TYPE_KEY = "type";

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

    private static User hasPermission(Request req) throws Exception {
        try {
            String authHeader = req.headers(AUTHORIZATION); // $NON-NLS-1$
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
            return user;
        } catch (Exception e) {
            KukuratusLogger.logError("GssServerApi#hasPermission", e);
            return null;
        }
    }

    private static boolean hasPermissionDoubleCheck(Request req, String tag) throws Exception {
        try {
            String authHeader = req.headers(AUTHORIZATION);
            String[] userPwd = NetworkUtilities.getUserPwdWithBasicAuthentication(authHeader);
            User user = RegistryHandler.INSTANCE.isLoginOk(userPwd[0], userPwd[1]);
            if (user != null) {
                return true;
            }
            Object procObj = ServletUtils.canProceed(req, tag);
            if (!(procObj instanceof KukuratusStatus)) {
                return true;
            }
        } catch (Exception e) {
            KukuratusLogger.logError("GssServerApi#hasPermissionDoubleCheck", e);
        }
        return false;
    }

    public static void addCheckRoute(Javalin app) {
        app.get("/check", ctx -> {
            ctx.result("It works. " + DateTime.now().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS));
        });
    }

    public static void addTilesRoute(ITilesGenerator mapsforgeTilesGenerator) {

        get(ROUTES_TILES_SOURCE_Z_X_Y, (req, res) -> {
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
    }

    public static void addClientUploadRoute() {
        post(ROUTES_UPLOAD, "multipart/form-data", (req, res) -> {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(ServletUtils.tmpDir);
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

            KukuratusLogger.logDebug("GssServer#post(/upload", getRequestLogString(req, null));

            Object procObj = ServletUtils.canProceed(req, ROUTES_UPLOAD);
            if (procObj instanceof KukuratusStatus) {
                KukuratusStatus ks = (KukuratusStatus) procObj;
                res.status(ks.getCode());
                return ks.toJson();
            } else if (procObj instanceof String) {
                String deviceId = (String) procObj;

                DatabaseHandler dbHandler = DatabaseHandler.instance();
                GeometryFactory gf = GeometryUtilities.gf();
                Dao<GpapUsers, ?> usersDao = dbHandler.getDao(GpapUsers.class);
                GpapUsers gpapUser = usersDao.queryBuilder().where().eq(GpapUsers.DEVICE_FIELD_NAME, deviceId)
                        .queryForFirst();

                Dao<GpapProject, ?> projectDao = dbHandler.getDao(GpapProject.class);

                Dao<Notes, ?> notesDao = dbHandler.getDao(Notes.class);

                Dao<GpsLogs, ?> logsDao = dbHandler.getDao(GpsLogs.class);
                Dao<GpsLogsData, ?> logsDataDao = dbHandler.getDao(GpsLogsData.class);

                Dao<ImageData, ?> imageDataDao = dbHandler.getDao(ImageData.class);
                Dao<Images, ?> imagesDao = dbHandler.getDao(Images.class);

                Collection<Part> parts = req.raw().getParts();

                HashMap<String, Object> partData = new HashMap<String, Object>();
                for (Part part : parts) {
                    String partName = part.getName();
                    if (partName.startsWith(TABLE_IMAGE_DATA)) {
                        byte[] byteArray = getByteArray(part);
                        partData.put(partName, byteArray);
                    } else {
                        String value = getValue(part);
                        partData.put(partName, value);
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

                switch (type) {
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
                        QueryResult tableRecords = dbHandler.getDb().getTableRecordsMapIn(Notes.TABLE_NAME, env, 1, -1,
                                null);
                        long previousId = -1;
                        if (!tableRecords.geometries.isEmpty()) {
                            int indexOf = tableRecords.names.indexOf(Notes.ID_FIELD_NAME);
                            if (indexOf != -1) {
                                Object[] objects = tableRecords.data.get(0);
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

                        Notes serverNote = new Notes(point, altim, ts, descr, text, form, marker, size, rotation, color,
                                accuracy, heading, speed, speedaccuracy, gpapUser, project, previousId,
                                System.currentTimeMillis());
                        notesDao.create(serverNote);
                        if (form != null) {
                            List<String> imageIds = Utilities.getImageIds(form);
                            if (!imageIds.isEmpty()) {
                                HashMap<String, String> oldId2NewMap = new HashMap<>();
                                for (String imageIdString : imageIds) {
                                    byte[] imageData = (byte[]) partData.get(
                                            TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_IMAGE + "_" + imageIdString);
                                    byte[] thumbData = (byte[]) partData.get(
                                            TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_THUMBNAIL + "_" + imageIdString);
                                    ImageData imgData = new ImageData(imageData, gpapUser);
                                    imageDataDao.create(imgData);
                                    Point imgPoint = gf.createPoint(new Coordinate(lon, lat));
                                    Images img = new Images(imgPoint, altim, ts, -1, imageIdString, serverNote, imgData,
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

                        byte[] imageData = (byte[]) partData.get(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_IMAGE);
                        byte[] thumbnailData = (byte[]) partData
                                .get(TABLE_IMAGE_DATA + "_" + IMAGESDATA_COLUMN_THUMBNAIL);

                        ImageData imgData = new ImageData(imageData, gpapUser);
                        imageDataDao.create(imgData);
                        Point imgPoint = gf.createPoint(new Coordinate(imgLon, imgLat));
                        Images img = new Images(imgPoint, imgAltim, imgTs, -1, imgText, null, imgData, gpapUser,
                                thumbnailData, project, System.currentTimeMillis());
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
                        for (int i = 0; i < coords.length; i++) {
                            JSONObject pointObj = root.getJSONObject(i);
                            double pLat = pointObj.getDouble(LOGSDATA_COLUMN_LAT);
                            double pLon = pointObj.getDouble(LOGSDATA_COLUMN_LON);
                            double pAltim = pointObj.getDouble(LOGSDATA_COLUMN_ALTIM);
                            long pTs = pointObj.getLong(LOGSDATA_COLUMN_TS);

                            Coordinate coord = new Coordinate(pLon, pLat);
                            coords[i] = coord;
                            Point logPoint = gf.createPoint(coord);
                            GpsLogsData gpsLogsData = new GpsLogsData(logPoint, pAltim, pTs, null);
                            logsData.add(gpsLogsData);
                        }
                        LineString logLine = gf.createLineString(coords);
                        GpsLogs newLog = new GpsLogs(logText, startts, endts, logLine, logColor, width, gpapUser,
                                project, System.currentTimeMillis());
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
                res.status(ks.getCode());
                return ks.toJson();
            } else {
                return sendNoPermission(res);
            }
        });

    }

    public static void addClientProjectDataUploadRoute() {
        post(ROUTES_PROJECTDATA_UPLOAD, "multipart/form-data", (req, res) -> {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(ServletUtils.tmpDir);
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

            User validUser = hasPermission(req);
            KukuratusLogger.logDebug(ROUTES_PROJECTDATA_UPLOAD, getRequestLogString(req, validUser));
            if (validUser != null) {
                DatabaseHandler dbHandler = DatabaseHandler.instance();
                try {

                    Collection<Part> parts = req.raw().getParts();

                    String errorMsg = null;
                    if (parts.size() > 0) {
                        Part part = parts.iterator().next();
                        String partName = part.getName();
                        if (partName.equals("file")) {
                            String fileName = part.getSubmittedFileName();
                            if (fileName != null) {
                                if (ServletUtils.isBaseMap(fileName)) {
                                    byte[] byteArray = getByteArray(part);
                                    File folder = ServletUtils.getBasemapsFolder().get();
                                    File file = new File(folder, fileName);
                                    try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
                                        fos.write(byteArray);
                                    }
                                } else if (ServletUtils.isProject(fileName)) {
                                    byte[] byteArray = getByteArray(part);
                                    File folder = ServletUtils.getProjectsFolder().get();
                                    File file = new File(folder, fileName);
                                    try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
                                        fos.write(byteArray);
                                    }
                                } else if (fileName.endsWith("tags.json")) {
                                    String form = getString(part);
                                    Dao<Forms, ?> formsDao = dbHandler.getDao(Forms.class);

                                    Forms f = new Forms(fileName, form, validUser.uniqueName,
                                            FormStatus.VISIBLE.getStatusCode());
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
                    }

                    if (errorMsg != null) {
                        KukuratusLogger.logError("GssServer#post(" + ROUTES_PROJECTDATA_UPLOAD, errorMsg, null);
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR,
                                errorMsg);
                        res.status(ks.getCode());
                        return ks.toJson();
                    }

                } catch (Exception e) {
                    return sendServerError(res, ROUTES_PROJECTDATA_UPLOAD, e);
                }

                String message = Messages.getString("UploadServlet.data_uploaded");
                ServletUtils.debug("SENDING RESPONSE MESSAGE: " + message); //$NON-NLS-1$
                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, message);
                res.status(ks.getCode());
                return ks.toJson();
            } else {
                return sendNoPermission(res);
            }
        });

    }

    public static void addGetDataRoute() {

        post(ROUTES_GETDATA, (req, res) -> {
            try {

                User validUser = hasPermission(req);
                KukuratusLogger.logDebug(ROUTES_GETDATA, getRequestLogString(req, validUser));
                if (validUser != null) {
                    String surveyors = req.queryParams(SURVEYORS);
                    String projects = req.queryParams(PROJECTS);

                    JSONObject root = new JSONObject();

                    Dao<GpapUsers, ?> userDao = DatabaseHandler.instance().getDao(GpapUsers.class);

                    List<GpapUsers> users = null;

                    if (surveyors != null) {
                        String[] surveyorsArray = surveyors.split(";");
                        users = userDao.queryBuilder().where().eq(GpapUsers.ACTIVE_FIELD_NAME, 1).and()
                                .in(GpapUsers.NAME_FIELD_NAME, Arrays.asList(surveyorsArray)).query();
                    } else {
                        users = userDao.queryBuilder().where().eq(GpapUsers.ACTIVE_FIELD_NAME, 1).query();
                    }

                    Dao<GpapProject, ?> projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                    List<GpapProject> projectsList = null;
                    if (projects != null) {
                        String[] projectsArray = projects.split(";");
                        projectsList = projectDao.queryBuilder().where()
                                .in(GpapProject.NAME_FIELD_NAME, Arrays.asList(projectsArray)).query();
                    }

                    // TODO parameterize users, from and to
                    Long from = null;
                    Long to = null;

                    // Dao<GpsLogs, ?> logsDao = DatabaseHandler.instance().getDao(GpsLogs.class);
                    // GssDatabaseUtilities.getLogs(root, logsDao, users, projectsList, null, null);

                    Dao<Notes, ?> notesDao = DatabaseHandler.instance().getDao(Notes.class);

                    // simple notes
                    GssDatabaseUtilities.getNotes(root, notesDao, projectDao, userDao, users, projectsList, null, null,
                            false);
                    // form notes
                    GssDatabaseUtilities.getNotes(root, notesDao, projectDao, userDao, users, projectsList, null, null,
                            true);

                    Dao<Images, ?> imagesDao = DatabaseHandler.instance().getDao(Images.class);
                    GssDatabaseUtilities.getImages(root, imagesDao, projectDao, userDao, users, projectsList, null,
                            null);

                    return root.toString();
                } else {
                    return sendNoPermission(res);
                }
            } catch (Exception e) {
                return sendServerError(res, ROUTES_GETDATA, e);
            }
        });
    }

    public static void addGetDataByTypeRoute() {

        // get data from the server by type and the primary key id
        get(ROUTES_GETDATA_BY_TYPE, (req, res) -> {
            User validUser = hasPermission(req);
            KukuratusLogger.logDebug(ROUTES_GETDATA_BY_TYPE, getRequestLogString(req, validUser));
            if (validUser != null) {
                String type = req.params(":type");
                String id = req.params(":id");

                try {
                    // images or notes are requested
                    if (type.equals(GssDatabaseUtilities.IMAGEDATA)) {
                        if (id != null) {
                            long idLong = Long.parseLong(id);
                            ImageData idObj = new ImageData(idLong);
                            Dao<ImageData, ?> dao = DatabaseHandler.instance().getDao(ImageData.class);
                            ImageData result = dao.queryForSameId(idObj);
                            return result.data;
                        }
                    } else if (type.equals(GssDatabaseUtilities.IMAGES)) {
                        if (id != null) {
                            Dao<Images, ?> imagesDao = DatabaseHandler.instance().getDao(Images.class);
                            Dao<GpapUsers, ?> userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                            Dao<GpapProject, ?> projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                            long idLong = Long.parseLong(id);
                            JSONObject imageObj = GssDatabaseUtilities.getImageById(imagesDao, projectDao, userDao,
                                    idLong);
                            return imageObj.toString();
                        }
                    } else if (type.equals(GssDatabaseUtilities.NOTES)) {
                        if (id != null) {
                            Dao<Notes, ?> notesDao = DatabaseHandler.instance().getDao(Notes.class);
                            Dao<GpapUsers, ?> userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                            Dao<GpapProject, ?> projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                            long idLong = Long.parseLong(id);
                            JSONObject noteObj = GssDatabaseUtilities.getNoteById(notesDao, projectDao, userDao,
                                    idLong);
                            return noteObj.toString();
                        }
                    }
                    return "";
                } catch (Exception e) {
                    return sendServerError(res, ROUTES_GETDATA_BY_TYPE, e);
                }
            } else {
                return sendNoPermission(res);
            }
        });
    }

    public static void addClientGetBaseDataRoute() {
        get(ROUTES_GET_BASEDATA, (req, res) -> {
            if (hasPermissionDoubleCheck(req, ROUTES_GET_BASEDATA)) {
                KukuratusLogger.logDebug("GssServer#get(" + ROUTES_GET_BASEDATA, getRequestLogString(req, null));
                String fileName = req.queryParams("name");
                try {
                    if (fileName == null) {
                        // send list
                        String mapsListJson = ServletUtils.getMapsListJson();
                        return mapsListJson;
                    } else {

                        res.type("application/octet-stream"); //$NON-NLS-1$
                        res.header("Content-disposition", "attachment; filename=" + fileName); //$NON-NLS-1$ //$NON-NLS-2$

                        Optional<File> mapFileOpt = ServletUtils.getMapFile(fileName);
                        if (mapFileOpt.isPresent()) {

                            File file = mapFileOpt.get();
                            try (InputStream in = new BufferedInputStream(new FileInputStream(file));
                                    ServletOutputStream out = res.raw().getOutputStream()) {
                                byte[] buffer = new byte[8192];
                                int numBytesRead;
                                while ((numBytesRead = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, numBytesRead);
                                }
                            }
                        }

                    }
                    return "";
                } catch (Exception e) {
                    return sendServerError(res, ROUTES_GET_BASEDATA + "/" + fileName, e);
                }
            } else {
                return sendNoPermission(res);
            }
        });
    }

    public static void addClientGetFormsRoute() {
        get(ROUTES_GET_FORMS, (req, res) -> {
            if (hasPermissionDoubleCheck(req, ROUTES_GET_BASEDATA)) {
                KukuratusLogger.logDebug(ROUTES_GET_FORMS, getRequestLogString(req, null));

                Dao<Forms, ?> formsDao = DatabaseHandler.instance().getDao(Forms.class);
                String tagName = req.queryParams("name");
                try {
                    if (tagName != null) {
                        Forms form = formsDao.queryBuilder().where().eq(Forms.NAME_FIELD_NAME, tagName).queryForFirst();
                        return form.form;
                    } else {
                        List<Forms> visibleForms = formsDao.queryBuilder().orderBy(Forms.NAME_FIELD_NAME, true).query();
                        JSONObject root = new JSONObject();
                        JSONArray formsArray = new JSONArray();
                        root.put(ServletUtils.TAGS, formsArray); // $NON-NLS-1$
                        for (Forms form : visibleForms) {
                            JSONObject formObj = new JSONObject();
                            formObj.put(ServletUtils.TAG, form.name); // $NON-NLS-1$
                            formObj.put(ServletUtils.TAGID, form.id); // $NON-NLS-1$
                            formsArray.put(formObj);
                        }

                        return root.toString();
                    }
                } catch (Exception e) {
                    return sendServerError(res, ROUTES_GET_FORMS + "/" + tagName, e);
                }
            } else {
                return sendNoPermission(res);
            }
        });
    }

    public static void addGetImagedataRoute() {
        get(ROUTES_GET_IMAGEDATA, (req, res) -> {
            // User validUser = hasPermission(req);
            // if (validUser != null) {
            String imageDataId = req.params(":dataid");
            KukuratusLogger.logDebug(ROUTES_GET_IMAGEDATA + "/" + imageDataId, getRequestLogString(req, null));

            try {
                long imageDataIdLong = Long.parseLong(imageDataId);
                Dao<ImageData, ?> dao = DatabaseHandler.instance().getDao(ImageData.class);
                ImageData imageData = dao.queryBuilder().where().eq(ImageData.ID_FIELD_NAME, imageDataIdLong)
                        .queryForFirst();
                return imageData.data;
            } catch (Exception e) {
                return sendServerError(res, ROUTES_GET_IMAGEDATA + "/" + imageDataId, e);
            }
            // } else {
            // return sendNoPermission(res);
            // }
        });
    }
    // public static void addGetImagedataRoute() {
    // get(ROUTES_GET_IMAGEDATA, (req, res) -> {
    // // User validUser = hasPermission(req);
    // // if (validUser != null) {
    // String userId = req.params(":userid");
    // String imageDataId = req.params(":dataid");
    // KukuratusLogger.logDebug(ROUTES_GET_IMAGEDATA + "/" + userId + "/" +
    // imageDataId,
    // getRequestLogString(req, null));

    // try {
    // long userIdLong = Long.parseLong(userId);
    // long imageDataIdLong = Long.parseLong(imageDataId);
    // Dao<ImageData, ?> dao = DatabaseHandler.instance().getDao(ImageData.class);
    // ImageData imageData = dao.queryBuilder().where().eq(ImageData.ID_FIELD_NAME,
    // imageDataIdLong).and()
    // .eq(ImageData.GPAPUSER_FIELD_NAME, userIdLong).queryForFirst();
    // return imageData.data;
    // } catch (Exception e) {
    // return sendServerError(res, ROUTES_GET_IMAGEDATA + "/" + userId + "/" +
    // imageDataId, e);
    // }
    // // } else {
    // // return sendNoPermission(res);
    // // }
    // });
    // }

    public static void addLoginRoute() {

        get(ROUTES_LOGIN, (req, res) -> {
            try {
                User user = hasPermission(req);
                KukuratusLogger.logDebug(ROUTES_LOGIN, getRequestLogString(req, user));
                if (user != null) {
                    boolean admin = RegistryHandler.INSTANCE.isAdmin(user);
                    JSONObject response = new JSONObject();
                    response.put(KEY_HASPERMISSION, true);
                    response.put(KEY_ISADMIN, admin);

                    // also get last used map background and map position
                    String baseMap = RegistryHandler.INSTANCE.getSettingByKey(KEY_BASEMAP, "Mapsforge",
                            user.getUniqueName());
                    response.put(KEY_BASEMAP, baseMap);
                    String xyz = RegistryHandler.INSTANCE.getSettingByKey(KEY_MAPCENTER, "0.0;0.0;6",
                            user.getUniqueName());
                    response.put(KEY_MAPCENTER, xyz);
                    res.status(KukuratusStatus.CODE_200_OK);
                    return response.toString();
                }
                JSONObject response = new JSONObject();
                response.put(KEY_HASPERMISSION, false);
                return response.toString();
            } catch (Exception e) {
                return sendServerError(res, ROUTES_LOGIN, e);
            }
        });
    }

    public static void addUserSettingsRoute() {

        post(ROUTES_USERSETTINGS, (req, res) -> {
            try {
                User user = hasPermission(req);
                KukuratusLogger.logDebug(ROUTES_USERSETTINGS, getRequestLogString(req, user));
                if (user != null) {
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
                    String automaticRegistrationMillis = req.queryParams(KukuratusSession.KEY_AUTOMATIC_REGISTRATION);
                    if (automaticRegistrationMillis != null) {
                        Settings s = new Settings(KukuratusSession.KEY_AUTOMATIC_REGISTRATION,
                                automaticRegistrationMillis, user.getUniqueName());
                        RegistryHandler.INSTANCE.insertOrUpdateGlobalSetting(s);
                    }
                    return "OK";
                } else {
                    return sendNoPermission(res);
                }
            } catch (Exception e) {
                return sendServerError(res, ROUTES_USERSETTINGS, e);
            }
        });
    }

    public static void addListByTypeRoute() {

        get(ROUTES_LIST_TYPE, (req, res) -> {
            try {
                User validUser = hasPermission(req);
                KukuratusLogger.logDebug(ROUTES_LIST_TYPE, getRequestLogString(req, validUser));
                if (validUser != null) {
                    String type = req.params(":type");
                    if (type.equals(SURVEYORS)) {
                        Dao<GpapUsers, ?> userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
                        List<GpapUsers> users = userDao.queryForAll();
                        JSONObject root = new JSONObject();
                        JSONArray surveyorsArray = new JSONArray();
                        root.put(SURVEYORS, surveyorsArray);
                        for (GpapUsers gpapUsers : users) {
                            surveyorsArray.put(gpapUsers.toJson());
                        }
                        return root.toString();
                    } else if (type.equals(PROJECTS)) {
                        Dao<GpapProject, ?> projectDao = DatabaseHandler.instance().getDao(GpapProject.class);
                        List<GpapProject> projects = projectDao.queryForAll();
                        JSONObject root = new JSONObject();
                        JSONArray projectsArray = new JSONArray();
                        root.put(PROJECTS, projectsArray);
                        for (GpapProject gpapProject : projects) {
                            projectsArray.put(gpapProject.name);
                        }
                        return root.toString();
                    } else if (type.equals(WEBUSERS)) {
                        JSONObject root = new JSONObject();
                        JSONArray usersArray = new JSONArray();
                        root.put(WEBUSERS, usersArray);
                        List<Group> groups = RegistryHandler.INSTANCE.getGroupsWithAuthorizations();
                        for (Group group : groups) {
                            List<User> usersList = RegistryHandler.INSTANCE.getUsersOfGroup(group);
                            for (User user : usersList) {
                                user.setGroup(group);
                                usersArray.put(user.toJson());
                            }
                        }
                        return root.toString();
                    }

                    KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                            "Type not recognised: type");
                    res.status(ks.getCode());
                    return ks.toJson();
                } else {
                    return sendNoPermission(res);
                }
            } catch (Exception e) {
                return sendServerError(res, ROUTES_LIST_TYPE, e);
            }
        });
    }

    public static void addUpdateByTypeRoute() {

        post(ROUTES_UPDATE_TYPE, (req, res) -> {
            try {
                User validUser = hasPermission(req);
                KukuratusLogger.logDebug(ROUTES_UPDATE_TYPE, getRequestLogString(req, validUser));
                if (validUser != null) {
                    String type = req.params(":type");
                    if (type.equals(SURVEYORS)) {
                        String id = req.queryParams(GpapUsers.ID_FIELD_NAME);
                        String deviceId = req.queryParams(GpapUsers.DEVICE_FIELD_NAME);
                        String name = req.queryParams(GpapUsers.NAME_FIELD_NAME);
                        String contact = req.queryParams(GpapUsers.CONTACT_FIELD_NAME);
                        String active = req.queryParams(GpapUsers.ACTIVE_FIELD_NAME);

                        Dao<GpapUsers, ?> userDao = DatabaseHandler.instance().getDao(GpapUsers.class);
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
                                res.status(ks.getCode());
                                return ks.toJson();
                            }
                        } else {
                            // create new one
                            GpapUsers newUser = new GpapUsers(deviceId, name, contact, 1);
                            userDao.create(newUser);
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        res.status(ks.getCode());
                        return ks.toJson();
                    } else if (type.equals(WEBUSERS)) {
                        String id = req.queryParams(User.ID_FIELD_NAME);
                        String name = req.queryParams(User.NAME_FIELD_NAME);
                        String uniqueName = req.queryParams(User.UNIQUENAME_FIELD_NAME);
                        String email = req.queryParams(User.EMAIL_FIELD_NAME);
                        String pwd = req.queryParams(User.PASSWORD_FIELD_NAME);
                        String group = req.queryParams(User.GROUP_FIELD_NAME);

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
                                RegistryHandler.INSTANCE.updateUser(existingUser);
                            } else {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                        "No webuser existing by the id: " + id);
                                res.status(ks.getCode());
                                return ks.toJson();
                            }
                        } else {
                            // create new one
                            Group groupObj = RegistryHandler.INSTANCE.getGroupByName(group);
                            User newUser = new User(name, uniqueName, email, pwd, groupObj);
                            RegistryHandler.INSTANCE.addUser(newUser);
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        res.status(ks.getCode());
                        return ks.toJson();
                    } else {
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                "Type not recognised: " + type);
                        res.status(ks.getCode());
                        return ks.toJson();
                    }

                } else {
                    return sendNoPermission(res);
                }
            } catch (Exception e) {
                return sendServerError(res, ROUTES_UPDATE_TYPE, e);
            }
        });
    }

    public static void addDeleteByTypeRoute() {

        post(ROUTES_DELETE_TYPE, (req, res) -> {
            try {
                User validUser = hasPermission(req);
                KukuratusLogger.logDebug(ROUTES_DELETE_TYPE, getRequestLogString(req, validUser));
                if (validUser != null) {
                    String type = req.params(":type");
                    if (type.equals(WEBUSERS)) {
                        String id = req.queryParams(User.ID_FIELD_NAME);
                        if (id != null) {
                            User existingUser = RegistryHandler.INSTANCE.getUserById(Long.parseLong(id));
                            if (existingUser != null) {
                                RegistryHandler.INSTANCE.deleteUser(existingUser);
                            } else {
                                KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                        "No webuser existing by the id: " + id);
                                res.status(ks.getCode());
                                return ks.toJson();
                            }
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        res.status(ks.getCode());
                        return ks.toJson();
                    } else if (type.equals(FORMS)) {
                        String id = req.queryParams(Forms.ID_FIELD_NAME);
                        if (id != null) {
                            Dao<Forms, ?> formsDao = DatabaseHandler.instance().getDao(Forms.class);
                            Forms f = new Forms(Long.parseLong(id));
                            formsDao.delete(f);
                        }
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_200_OK, "Ok");
                        res.status(ks.getCode());
                        return ks.toJson();
                    } else {
                        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_404_NOTFOUND,
                                "Type not recognised: " + type);
                        res.status(ks.getCode());
                        return ks.toJson();
                    }

                } else {
                    return sendNoPermission(res);
                }
            } catch (Exception e) {
                return sendServerError(res, ROUTES_DELETE_TYPE, e);
            }
        });
    }

    //////////////////////////////////////
    // HELPER METHODS
    //////////////////////////////////////

    private static String sendNoPermission(Response res) {
        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_403_FORBIDDEN, "No permission for request.");
        res.status(ks.getCode());
        return ks.toJson();
    }

    private static String sendServerError(Response res, String route, Throwable e) {
        KukuratusLogger.logError(route, e);
        KukuratusStatus ks = new KukuratusStatus(KukuratusStatus.CODE_500_INTERNAL_SERVER_ERROR, "ERROR", e);
        res.status(ks.getCode());
        return ks.toJson();
    }

    private static long getLong(HashMap<String, Object> partData, String key, long defaultValue) {
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

    private static String getString(HashMap<String, Object> partData, String key, String defaultValue) {
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

    private static double getDouble(HashMap<String, Object> partData, String key, double defaultValue) {
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

    private static float getFloat(HashMap<String, Object> partData, String key, float defaultValue) {
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

    private static String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private static String getValue(Part part) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[1024];
        for (int length = 0; (length = reader.read(buffer)) > 0;) {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }

    private static byte[] getByteArray(Part part) throws IOException {
        try (InputStream is = part.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            return byteArray;
        }
    }

    private static String getString(Part part) throws IOException {
        try (InputStream is = part.getInputStream()) {
            String text = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
            return text;
        }
    }

    private static String getRequestLogString(Request req, User user) {
        String userStr = "";
        if (user != null) {
            userStr = " by user " + user.uniqueName;
        }
        return "Received request" + userStr + " from " + req.raw().getRemoteAddr();
    }
}