package com.hydrologis.kukuratus.gss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

import com.hydrologis.kukuratus.gss.database.GpapProject;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class GssDatabaseUtilities {
    public static final String NOTES = "notes";
    public static final String FORMS = "forms";
    public static final String IMAGES = "images";
    public static final String IMAGEDATA = "imagedata";
    public static final String FORM = "form";
    public static final String USER = "user";
    public static final String Y = "y";
    public static final String X = "x";
    public static final String COORDS = "coords";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String DATAID = "dataid";
    public static final String DATA = "data";
    public static final String LOGS = "logs";
    public static final String SURVEYOR = "surveyor";
    public static final String PROJECT = "project";

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
    public static JSONObject getLogs( JSONObject root, Dao<GpsLogs, ? > logsDao, List<GpapUsers> users,
            List<GpapProject> projects, long[] fromTo, String textMatching ) throws SQLException {
        QueryBuilder<GpsLogs, ? > qb = logsDao.queryBuilder();
        List<GpsLogs> gpsLogs;
        boolean hasUsers = users != null && users.size() > 0;
        boolean hasProjects = projects != null;
        boolean hasTime = fromTo != null;
        if (hasUsers || hasProjects || hasTime || textMatching != null) {
            Where<GpsLogs, ? > where = qb.where();
            boolean needAnd = false;
            if (hasUsers) {
                if (needAnd)
                    where = where.and();
                where = where.in(GpsLogs.GPAPUSER_FIELD_NAME, users);
                needAnd = true;
            }
            if (hasProjects) {
                if (needAnd)
                    where = where.and();
                where = where.in(GpsLogs.GPAPPROJECT_FIELD_NAME, projects);
                needAnd = true;
            }
            if (hasTime) {
                if (needAnd)
                    where = where.and();
                where = where.and().ge(GpsLogs.STARTTS_FIELD_NAME, fromTo[0]).and().le(GpsLogs.ENDTS_FIELD_NAME, fromTo[1]);
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
            for( GpsLogs log : gpsLogs ) {
                JSONObject logObject = new JSONObject();
                jsonLogs.put(logObject);
                logObject.put(ID, log.id);
                logObject.put(GpsLogs.COLOR_FIELD_NAME, log.color);
                logObject.put(GpsLogs.WIDTH_FIELD_NAME, log.width);
                logObject.put(NAME, log.name);
                logObject.put(GpsLogs.STARTTS_FIELD_NAME, log.startTs);
                logObject.put(GpsLogs.ENDTS_FIELD_NAME, log.endTs);
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

    /**
     * Get all notes with minimum possible info to keep net load low.
     * 
     * @param root
     * @param notesDao
     * @param projectDao
     * @param userDao
     * @param users
     * @param projects
     * @param fromTo
     * @param textMatching
     * @return
     * @throws SQLException
     */
    public static JSONObject getNotesMin( JSONObject root, Dao<Notes, ? > notesDao, Dao<GpapProject, ? > projectDao,
            Dao<GpapUsers, ? > userDao, List<GpapUsers> users, List<GpapProject> projects, long[] fromTo, String textMatching )
            throws SQLException {
        if (root == null) {
            root = new JSONObject();
        }

        QueryBuilder<Notes, ? > qb = notesDao.queryBuilder();
        List<Notes> notesList;
        Where<Notes, ? > where = qb.where();
        boolean needAnd = false;
        boolean hasUsers = users != null && users.size() > 0;
        boolean hasProjects = projects != null;
        boolean hasTime = fromTo != null;
        JSONArray jsonNotes = new JSONArray();
        root.put(NOTES, jsonNotes);
        if (hasUsers) {
            if (needAnd)
                where.and();
            where.in(Notes.GPAPUSER_FIELD_NAME, users);
            needAnd = true;
        }
        if (hasProjects) {
            if (needAnd)
                where.and();

            where.in(Notes.GPAPPROJECT_FIELD_NAME, projects);
            needAnd = true;
        }
        if (hasTime) {
            if (needAnd)
                where.and();
            where.between(Notes.TIMESTAMP_FIELD_NAME, fromTo[0], fromTo[1]);
            needAnd = true;
        }

        // get only last of the versions
        String versionsSql = "id in (select  max(" + Notes.ID_FIELD_NAME + ")  from " + Notes.TABLE_NAME + " group by ST_asText("
                + Notes.GEOM_FIELD_NAME + ") )";
        where.and().raw(versionsSql);
        qb.orderBy(Notes.ID_FIELD_NAME, false);
        notesList = where.query();

        if (notesList != null) {
            for( Notes note : notesList ) {
                projectDao.refresh(note.gpapProject);
                userDao.refresh(note.gpapUser);

                jsonNotes.put(note.toJsonMin());
            }
        }
        return root;
    }
    public static JSONObject getNotes( JSONObject root, Dao<Notes, ? > notesDao, Dao<GpapProject, ? > projectDao,
            Dao<GpapUsers, ? > userDao, List<GpapUsers> users, List<GpapProject> projects, long[] fromTo, String textMatching,
            boolean typeForm ) throws SQLException {
        if (root == null) {
            root = new JSONObject();
        }

        QueryBuilder<Notes, ? > qb = notesDao.queryBuilder();
        List<Notes> notesList;
        Where<Notes, ? > where = qb.where();
        boolean needAnd = false;
        boolean hasUsers = users != null && users.size() > 0;
        boolean hasProjects = projects != null;
        boolean hasTime = fromTo != null;
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
        if (hasUsers) {
            if (needAnd)
                where.and();
            where.in(Notes.GPAPUSER_FIELD_NAME, users);
            needAnd = true;
        }
        if (hasProjects) {
            if (needAnd)
                where.and();

            where.in(Notes.GPAPPROJECT_FIELD_NAME, projects);
            needAnd = true;
        }
        if (hasTime) {
            if (needAnd)
                where.and();
            where.between(Notes.TIMESTAMP_FIELD_NAME, fromTo[0], fromTo[1]);
            needAnd = true;
        }

        // get only last of the versions
        String versionsSql = "id in (select  max(" + Notes.ID_FIELD_NAME + ")  from " + Notes.TABLE_NAME + " group by ST_asText("
                + Notes.GEOM_FIELD_NAME + ") )";
        where.and().raw(versionsSql);
        qb.orderBy(Notes.ID_FIELD_NAME, false);
        notesList = where.query();

        if (notesList != null) {
            for( Notes note : notesList ) {
                projectDao.refresh(note.gpapProject);
                userDao.refresh(note.gpapUser);

                jsonNotes.put(note.toJson());
            }
        }
        return root;
    }

    public static JSONObject getNoteById( Dao<Notes, ? > notesDao, Dao<GpapProject, ? > projectDao, Dao<GpapUsers, ? > userDao,
            long id ) throws SQLException {
        QueryBuilder<Notes, ? > qb = notesDao.queryBuilder();
        Notes note = qb.where().eq(Notes.ID_FIELD_NAME, id).queryForFirst();
        if (note != null) {
            projectDao.refresh(note.gpapProject);
            userDao.refresh(note.gpapUser);
            return note.toJson();
        }
        return new JSONObject();
    }

    public static JSONObject getImages( JSONObject root, Dao<Images, ? > imagesDao, Dao<GpapProject, ? > projectDao,
            Dao<GpapUsers, ? > userDao, List<GpapUsers> users, List<GpapProject> projects, long[] fromTo, String textMatching )
            throws Exception {
        if (root == null) {
            root = new JSONObject();
        }

        QueryBuilder<Images, ? > qb = imagesDao.queryBuilder();
        List<Images> imagesList;
        Where<Images, ? > where = qb.where();
        boolean needAnd = false;
        boolean hasUsers = users != null && users.size() > 0;
        boolean hasProjects = projects != null;
        boolean hasTime = fromTo != null;
        if (hasUsers || hasProjects || hasTime || textMatching != null) {
            if (hasUsers) {
                if (needAnd)
                    where = where.and();
                where = where.in(Images.GPAPUSER_FIELD_NAME, users);
                needAnd = true;
            }
            if (hasProjects) {
                if (needAnd)
                    where = where.and();
                where = where.in(Images.GPAPPROJECT_FIELD_NAME, projects);
                needAnd = true;
            }
            if (hasTime) {
                if (needAnd)
                    where = where.and();
                where = where.ge(Images.TIMESTAMP_FIELD_NAME, fromTo[0]).and().le(Images.TIMESTAMP_FIELD_NAME, fromTo[1]);
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
            for( Images image : imagesList ) {
                projectDao.refresh(image.gpapProject);
                userDao.refresh(image.gpapUser);
                jsonImages.put(image.toJson());
            }
        }
        return root;
    }

    public static JSONObject getImageById( Dao<Images, ? > imagesDao, Dao<GpapProject, ? > projectDao, Dao<GpapUsers, ? > userDao,
            long id ) throws Exception {

        QueryBuilder<Images, ? > qb = imagesDao.queryBuilder();
        Images image = qb.where().eq(Images.ID_FIELD_NAME, id).queryForFirst();

        if (image != null) {
            projectDao.refresh(image.gpapProject);
            userDao.refresh(image.gpapUser);
            return image.toJson();
        }
        return new JSONObject();
    }

    /**
     * Updates the image ids on the server side.
     *
     * @param formString    the form.
     * @param oldIds2NewMap the map of ids to substitute.
     * @return the updated form string.
     * @throws Exception if something goes wrong.
     */
    public static String updateImagesIds( String formString, HashMap<String, String> oldIds2NewMap ) throws Exception {
        JSONObject sectionObject = new JSONObject(formString);
        List<String> formsNames = Utilities.getFormNames4Section(sectionObject);
        for( String formName : formsNames ) {
            JSONObject form4Name = Utilities.getForm4Name(formName, sectionObject);
            JSONArray formItems = Utilities.getFormItems(form4Name);
            for( int i = 0; i < formItems.length(); i++ ) {
                JSONObject formItem = formItems.getJSONObject(i);
                if (!formItem.has(Utilities.TAG_KEY)) {
                    continue;
                }

                String type = formItem.getString(Utilities.TAG_TYPE);
                String value = "";
                if (formItem.has(Utilities.TAG_VALUE))
                    value = formItem.getString(Utilities.TAG_VALUE);

                if (type.equals(Utilities.TYPE_PICTURES) || type.equals(Utilities.TYPE_IMAGELIB)) {
                    if (value.trim().length() == 0) {
                        continue;
                    }
                    String[] imageSplit = value.split(";");
                    if (imageSplit.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for( int j = 0; j < imageSplit.length; j++ ) {
                            String oldId = imageSplit[j].trim();
                            String newId = oldIds2NewMap.get(oldId);
                            if (newId == null) {
                                throw new Exception("ERROR in images id map.");
                            }
                            if (j > 0) {
                                sb.append(";");
                            }
                            sb.append(newId);
                        }
                        formItem.put(Utilities.TAG_VALUE, sb.toString());
                    }
                } else if (type.equals(Utilities.TYPE_MAP)) {
                    if (value.trim().length() == 0) {
                        continue;
                    }
                    String image = value.trim();
                    String newId = oldIds2NewMap.get(image);
                    if (newId == null) {
                        throw new Exception("ERROR in images id map.");
                    }
                    formItem.put(Utilities.TAG_VALUE, newId);
                } else if (type.equals(Utilities.TYPE_SKETCH)) {
                    if (value.trim().length() == 0) {
                        continue;
                    }
                    String[] imageSplit = value.split(";");
                    if (imageSplit.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for( int j = 0; j < imageSplit.length; j++ ) {
                            String oldId = imageSplit[j].trim();
                            String newId = oldIds2NewMap.get(oldId);
                            if (newId == null) {
                                throw new Exception("ERROR in images id map.");
                            }
                            if (j > 0) {
                                sb.append(";");
                            }
                            sb.append(newId);
                        }
                        formItem.put(Utilities.TAG_VALUE, sb.toString());
                    }
                }
            }
        }
        return sectionObject.toString();

    }

    /**
     * Get the form item label out of a form string.
     *
     * @param formString the form.
     * @return The form label or null.
     * @throws Exception if something goes wrong.
     */
    public static String getFormLabel( String formString, String defaultValue ) throws Exception {
        if (formString != null && formString.length() > 0) {
            JSONObject sectionObject = new JSONObject(formString);
            List<String> formsNames = Utilities.getFormNames4Section(sectionObject);
            for( String formName : formsNames ) {
                JSONObject form4Name = Utilities.getForm4Name(formName, sectionObject);
                JSONArray formItems = Utilities.getFormItems(form4Name);
                for( int i = 0; i < formItems.length(); i++ ) {
                    JSONObject formItem = formItems.getJSONObject(i);
                    if (!formItem.has(Utilities.TAG_KEY)) {
                        continue;
                    }

                    String value = "";
                    if (formItem.has(Utilities.TAG_VALUE))
                        value = formItem.getString(Utilities.TAG_VALUE);
                    if (formItem.has(Utilities.TAG_ISLABEL)) {
                        String isLabelStr = formItem.getString(Utilities.TAG_ISLABEL);
                        if (isLabelStr.toLowerCase().equals("true") || isLabelStr.toLowerCase().equals("yes")) {
                            if(value.trim().length() > 0 )
                                return value;
                            else 
                                return defaultValue; 
                        }

                    }

                }
            }
        }
        return defaultValue;
    }

}