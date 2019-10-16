package com.hydrologis.kukuratus.gss;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

import com.hydrologis.kukuratus.database.ISpatialTable;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

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
    public static final String ID = "id";
    public static final String DATAID = "dataid";
    public static final String DATA = "data";
    public static final String LOGS = "logs";

//    public static String formatDate( long dateLong ) {
//        return new DateTime(dateLong).toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS);
//    }

    /**
     * Get the logs a json. 
     * 
     * @param root the root to which to add the logs. If not available, the root is created.
     * @param logsDao
     * @param logsPropertiesDao
     * @param users
     * @param from
     * @param to
     * @return the root object that was used.
     * @throws SQLException
     */
    public static JSONObject getLogs( JSONObject root, Dao<GpsLogs, ? > logsDao, List<GpapUsers> users, Long from, Long to )
            throws SQLException {
        Where<GpsLogs, ? > eq = logsDao.queryBuilder().where().in(GpsLogs.GPAPUSER_FIELD_NAME, users);
        if (from != null) {
            eq = eq.and().ge(GpsLogs.STARTTS_FIELD_NAME, from);
        }
        if (to != null) {
            eq = eq.and().le(GpsLogs.ENDTS_FIELD_NAME, to);
        }

        if (root == null) {
            root = new JSONObject();
        }

        JSONArray jsonLogs = new JSONArray();
        root.put(LOGS, jsonLogs);

        List<GpsLogs> gpsLogs = eq.query();
        if (gpsLogs.size() > 0) {
            for( GpsLogs log : gpsLogs ) {
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
                for( Coordinate c : coords ) {
                    JSONObject coordObject = new JSONObject();
                    coordObject.put(X, c.x);
                    coordObject.put(Y, c.y);
                    jsonCoords.put(coordObject);
                }
            }
        }

        return root;
    }

    public static JSONObject getNotes( JSONObject root, Dao<Notes, ? > notesDao, List<GpapUsers> users, Long from, Long to,
            boolean typeForm ) throws SQLException {
        if (root == null) {
            root = new JSONObject();
        }

        Where<Notes, ? > eq = notesDao.queryBuilder().where().in(Notes.GPAPUSER_FIELD_NAME, users);
        if (from != null) {
            eq = eq.and().ge(Notes.TIMESTAMP_FIELD_NAME, from);
        }
        if (to != null) {
            eq = eq.and().le(Notes.TIMESTAMP_FIELD_NAME, to);
        }

        JSONArray jsonNotes = new JSONArray();
        if (typeForm) {
            eq = eq.and().ne(Notes.FORM_FIELD_NAME, "");
            root.put(FORMS, jsonNotes);
        } else {
            eq = eq.and().eq(Notes.FORM_FIELD_NAME, "");
            root.put(NOTES, jsonNotes);
        }

        List<Notes> notesList = eq.query();

        if (notesList.size() > 0) {
            for( Notes note : notesList ) {
                JSONObject noteObject = new JSONObject();
                jsonNotes.put(noteObject);
                noteObject.put(ID, note.id);
                noteObject.put(NAME, note.text);
                noteObject.put(TS, note.timestamp);
                noteObject.put(USER, note.gpapUser.id);
                if (typeForm) {
                    noteObject.put(FORM, note.form);
                }
                Coordinate c = note.the_geom.getCoordinate();
                noteObject.put(X, c.x);
                noteObject.put(Y, c.y);
            }
        }
        return root;
    }

    public static JSONObject getImages( JSONObject root, ASpatialDb db, List<GpapUsers> users, Long from, Long to )
            throws Exception {
        if (root == null) {
            root = new JSONObject();
        }

        String sql = "SELECT " + Images.ID_FIELD_NAME + ", st_x(" + ISpatialTable.GEOM_FIELD_NAME + "), st_y("
                + ISpatialTable.GEOM_FIELD_NAME + "), " + Images.TIMESTAMP_FIELD_NAME + ", " + Images.TEXT_FIELD_NAME + ", "
                + Images.IMAGEDATA_FIELD_NAME + ", " + Images.THUMB_FIELD_NAME + " FROM images WHERE " + Images.NOTE_FIELD_NAME
                + " is null";
        if (from != null) {
            sql += " and " + Images.TIMESTAMP_FIELD_NAME + ">" + from;
        }
        if (to != null) {
            sql += " and " + Images.TIMESTAMP_FIELD_NAME + "<" + to;
        }
        String _sql = sql;

        JSONArray jsonImages = new JSONArray();
        root.put(IMAGES, jsonImages);
        db.execOnConnection(connection -> {

            try (IHMStatement stmt = connection.createStatement(); IHMResultSet rs = stmt.executeQuery(_sql)) {
                while( rs.next() ) {
                    int i = 1;
                    JSONObject imageObject = new JSONObject();
                    jsonImages.put(imageObject);

                    imageObject.put(ID, rs.getLong(i++));
                    imageObject.put(X, rs.getDouble(i++));
                    imageObject.put(Y, rs.getDouble(i++));
                    imageObject.put(TS, rs.getLong(i++));
                    imageObject.put(NAME, rs.getString(i++));
                    imageObject.put(DATAID, rs.getLong(i++));
                    byte[] thumbBytes = rs.getBytes(i++);
                    String encodedThumb = Base64.getEncoder().encodeToString(thumbBytes);
                    imageObject.put(DATA, encodedThumb);
                }
            }
            return null;
        });
        return root;
    }

}
