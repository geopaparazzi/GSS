package com.hydrologis.kukuratus.gss;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import com.hydrologis.kukuratus.gss.database.GpapProject;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

public class GssDatabaseUtilities {
    public static final String NOTES = "notes";
    public static final String FORMS = "forms";
    public static final String IMAGES = "images";
    public static final String FORM = "form";
    public static final String USER = "user";
    public static final String Y = "y";
    public static final String X = "x";
    public static final String COORDS = "coords";
    public static final String ENDTS = "endts";
    public static final String TS = "ts";
    public static final String STARTTS = "startts";
    public static final String NAME = "name";
    public static final String WIDTH = "width";
    public static final String COLOR = "color";
    public static final String MARKER = "marker";
    public static final String SIZE = "size";
    public static final String ID = "id";
    public static final String DATAID = "dataid";
    public static final String DATA = "data";
    public static final String LOGS = "logs";

    // public static String formatDate( long dateLong ) {
    // return new
    // DateTime(dateLong).toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS);
    // }

    /**
     * Get the logs a json.
     * 
     * @param root              the root to which to add the logs. If not available,
     *                          the root is created.
     * @param logsDao
     * @param logsPropertiesDao
     * @param users
     * @param from
     * @param to
     * @return the root object that was used.
     * @throws SQLException
     */
    public static JSONObject getLogs(JSONObject root, Dao<GpsLogs, ?> logsDao, List<GpapUsers> users,
            List<GpapProject> projects, long[] fromTo, String textMatching) throws SQLException {
        QueryBuilder<GpsLogs, ?> qb = logsDao.queryBuilder();
        List<GpsLogs> gpsLogs;
        if (users != null || projects != null || fromTo != null || textMatching != null) {
            Where<GpsLogs, ?> where = qb.where();
            boolean needAnd = false;
            if (users != null) {
                if (needAnd)
                    where = where.and();
                where = where.in(GpsLogs.GPAPUSER_FIELD_NAME, users);
                needAnd = true;
            }
            if (projects != null) {
                if (needAnd)
                    where = where.and();
                where = where.in(GpsLogs.GPAPPROJECT_FIELD_NAME, projects);
                needAnd = true;
            }
            if (fromTo != null) {
                if (needAnd)
                    where = where.and();
                where = where.and().ge(GpsLogs.STARTTS_FIELD_NAME, fromTo[0]).and().le(GpsLogs.ENDTS_FIELD_NAME,
                        fromTo[1]);
                needAnd = true;
            }
            gpsLogs = where.query();
        } else {
            gpsLogs = qb.query();
        }

        if (root == null) {
            root = new JSONObject();
        }

        JSONArray jsonLogs = new JSONArray();
        root.put(LOGS, jsonLogs);

        if (gpsLogs.size() > 0) {
            for (GpsLogs log : gpsLogs) {
                JSONObject logObject = new JSONObject();
                jsonLogs.put(logObject);
                logObject.put(ID, log.id);
                logObject.put(COLOR, log.color);
                logObject.put(WIDTH, log.width);
                logObject.put(NAME, log.name);
                logObject.put(STARTTS, log.startTs);
                logObject.put(ENDTS, log.endTs);
                JSONArray jsonCoords = new JSONArray();
                logObject.put(COORDS, jsonCoords);
                Coordinate[] coords = log.the_geom.getCoordinates();
                for (Coordinate c : coords) {
                    JSONObject coordObject = new JSONObject();
                    coordObject.put(X, c.x);
                    coordObject.put(Y, c.y);
                    jsonCoords.put(coordObject);
                }
            }
        }

        return root;
    }

    public static JSONObject getNotes(JSONObject root, Dao<Notes, ?> notesDao, List<GpapUsers> users,
            List<GpapProject> projects, long[] fromTo, String textMatching, boolean typeForm) throws SQLException {
        if (root == null) {
            root = new JSONObject();
        }

        QueryBuilder<Notes, ?> qb = notesDao.queryBuilder();
        List<Notes> notesList;
        Where<Notes, ?> where = qb.where();
        boolean needAnd = false;
        JSONArray jsonNotes = new JSONArray();
        if (typeForm) {
            if (needAnd)
                where.and();
            where.ne(Notes.FORM_FIELD_NAME, "");
            where.isNotNull(Notes.FORM_FIELD_NAME);
            where.and(2);
            root.put(FORMS, jsonNotes);
            needAnd = true;
        } else {
            if (needAnd)
                where.and();
            where.eq(Notes.FORM_FIELD_NAME, "");
            where.isNull(Notes.FORM_FIELD_NAME);
            where.or(2);
            root.put(NOTES, jsonNotes);
            needAnd = true;
        }
        if (users != null) {
            if (needAnd)
                where.and();
            where.in(Notes.GPAPUSER_FIELD_NAME, users);
            needAnd = true;
        }
        if (projects != null) {
            if (needAnd)
                where.and();

            where.in(Notes.GPAPPROJECT_FIELD_NAME, projects);
            needAnd = true;
        }
        if (fromTo != null) {
            if (needAnd)
                where.and();
            where.between(Notes.TIMESTAMP_FIELD_NAME, fromTo[0], fromTo[1]);
            needAnd = true;
        }
        notesList = where.query();

        if (notesList != null) {
            for (Notes note : notesList) {
                JSONObject noteObject = new JSONObject();
                jsonNotes.put(noteObject);
                noteObject.put(ID, note.id);
                noteObject.put(NAME, note.text);
                noteObject.put(TS, note.timestamp);
                noteObject.put(USER, note.gpapUser.id);
                noteObject.put(MARKER, note.marker);
                noteObject.put(SIZE, note.size);
                noteObject.put(COLOR, note.color);
                if (typeForm) {
                    noteObject.put(FORM, note.form);
                }
                Coordinate c = note.the_geom.getCoordinate();
                noteObject.put(X, c.x);
                noteObject.put(Y, c.y);

                // System.out.println((typeForm ? "form: " : "simple ") + c.x + "/" + c.y + ": "
                // + note.text);

            }
        }
        return root;
    }

    public static JSONObject getImages(JSONObject root, Dao<Images, ?> imagesDao, List<GpapUsers> users,
            List<GpapProject> projects, long[] fromTo, String textMatching) throws Exception {
        if (root == null) {
            root = new JSONObject();
        }

        QueryBuilder<Images, ?> qb = imagesDao.queryBuilder();
        List<Images> imagesList;
        Where<Images, ?> where = qb.where();
        boolean needAnd = false;
        if (users != null || projects != null || fromTo != null || textMatching != null) {
            if (users != null) {
                if (needAnd)
                    where = where.and();
                where = where.in(Images.GPAPUSER_FIELD_NAME, users);
                needAnd = true;
            }
            if (projects != null) {
                if (needAnd)
                    where = where.and();
                where = where.in(Images.GPAPPROJECT_FIELD_NAME, projects);
                needAnd = true;
            }
            if (fromTo != null) {
                if (needAnd)
                    where = where.and();
                where = where.ge(Images.TIMESTAMP_FIELD_NAME, fromTo[0]).and().le(Images.TIMESTAMP_FIELD_NAME,
                        fromTo[1]);
                needAnd = true;
            }
        }
        if (needAnd)
            where = where.and();
        where.isNull(Images.NOTE_FIELD_NAME);
        imagesList = where.query();

        JSONArray jsonImages = new JSONArray();
        root.put(IMAGES, jsonImages);
        if (imagesList != null) {
            for (Images image : imagesList) {
                JSONObject imageObject = new JSONObject();
                jsonImages.put(imageObject);
                imageObject.put(ID, image.id);
                Coordinate c = image.the_geom.getCoordinate();
                imageObject.put(X, c.x);
                imageObject.put(Y, c.y);
                imageObject.put(TS, image.timestamp);
                imageObject.put(NAME, image.text);
                imageObject.put(DATAID, image.imageData.id);
                byte[] thumbBytes = image.thumbnail;
                String encodedThumb = Base64.getEncoder().encodeToString(thumbBytes);
                imageObject.put(DATA, encodedThumb);

            }
        }
        return root;
    }

}
