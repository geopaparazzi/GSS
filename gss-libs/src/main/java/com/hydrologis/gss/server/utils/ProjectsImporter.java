package com.hydrologis.gss.server.utils;

import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_IMAGE_DATA;
import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_IMAGES;
import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_NOTES;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMConnection;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.spatialite.hm.SqliteDb;
import org.hortonmachine.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.hortonmachine.gears.io.geopaparazzi.OmsGeopaparazziProject3To4Converter;
import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.DaoImages;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.ImageDataTableFields;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.ImageTableFields;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.NotesTableFields;
import org.hortonmachine.gears.utils.files.FileUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.json.JSONObject;

import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.j256.ormlite.dao.Dao;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class ProjectsImporter {
    private static final GeometryFactory gf = GeometryUtilities.gf();
    private static final String idFN = NotesTableFields.COLUMN_ID.getFieldName();
    private static final String tsFN = NotesTableFields.COLUMN_TS.getFieldName();
    private static final String altimFN = NotesTableFields.COLUMN_ALTIM.getFieldName();
    private static final String dirtyFN = NotesTableFields.COLUMN_ISDIRTY.getFieldName();
    private static final String formFN = NotesTableFields.COLUMN_FORM.getFieldName();
    private static final String latFN = NotesTableFields.COLUMN_LAT.getFieldName();
    private static final String lonFN = NotesTableFields.COLUMN_LON.getFieldName();
    private static final String textFN = NotesTableFields.COLUMN_TEXT.getFieldName();
    private static final String descFN = NotesTableFields.COLUMN_DESCRIPTION.getFieldName();
    private static final String styleFN = NotesTableFields.COLUMN_STYLE.getFieldName();

    private static final String imgIdFN = ImageTableFields.COLUMN_ID.getFieldName();
    private static final String imgLonFN = ImageTableFields.COLUMN_LON.getFieldName();
    private static final String imgLatFN = ImageTableFields.COLUMN_LAT.getFieldName();
    private static final String imgAltimFN = ImageTableFields.COLUMN_ALTIM.getFieldName();
    private static final String imgTsFN = ImageTableFields.COLUMN_TS.getFieldName();
    private static final String imgAzimFN = ImageTableFields.COLUMN_AZIM.getFieldName();
    private static final String imgTextFN = ImageTableFields.COLUMN_TEXT.getFieldName();
    private static final String imgNoteidFN = ImageTableFields.COLUMN_NOTE_ID.getFieldName();
    private static final String imgImagedataidFN = ImageTableFields.COLUMN_IMAGEDATA_ID.getFieldName();

    private static final String imgdImadedataidFN = ImageDataTableFields.COLUMN_ID.getFieldName();
    private static final String imgdImadedataDataFN = ImageDataTableFields.COLUMN_IMAGE.getFieldName();
    private static final String imgdImadedataThumbFN = ImageDataTableFields.COLUMN_THUMBNAIL.getFieldName();

    public ProjectsImporter() throws Exception {

        String[] dbsToImport = {
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170216_075147_compleanno_hydrologis.gpap",
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170205_130453_valencia_geopap.gpap",
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20180317_091712_bonn_codesprint.gpap",
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20160319_064638_vacanze_rest.gpap",
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170310_riogambis_with_forms.gpap"};

        String dbPath = "/home/hydrologis/TMP/GEOPAPSERVER/database.mv.db";
        File dbFile = new File(dbPath);
        dbFile.delete();

        try (ASpatialDb db = EDb.fromFileDesktop(dbFile).getSpatialDb()) {
            if (!db.open(dbPath)) {
                db.initSpatialMetadata(null);
            }

            DatabaseHandler dbHandler = DatabaseHandler.instance(db);

            dbHandler.createTables();
            dbHandler.populateWithDefaults();
            dbHandler.populateForDemo();

            Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);
            Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);
            Dao<ImageData, ? > imageDataDao = dbHandler.getDao(ImageData.class);
            Dao<GpapUsers, ? > gpapUsersDao = dbHandler.getDao(GpapUsers.class);

            for( String tmpDbPath : dbsToImport ) {
                String dummyDeviceId = FileUtilities.getNameWithoutExtention(new File(tmpDbPath));

                System.out.println("Importing db: " + dummyDeviceId);

                GpapUsers user = new GpapUsers(dummyDeviceId, dummyDeviceId, null, null, null);
                gpapUsersDao.create(user);

                try (SqliteDb gpapDb = new SqliteDb()) {
                    gpapDb.open(tmpDbPath);

                    gpapDb.execOnConnection(gpapConnection -> {

                        // projectInfo(connection, outputFolderFile);

                        importNotes(gpapConnection, notesDao, imagesDao, imageDataDao, user);

                        insertImage(user, gpapConnection, null, null, imagesDao, imageDataDao);

                        // gpsLogToShapefiles(connection, user);
                        // /*
                        // * import media as point shapefile, containing the path
                        // */
                        // mediaToShapeFile(connection, mediaFolderFile, pm);

                        return null;
                    });

                }

            }

        }
    }

    private void importNotes( IHMConnection gpapConnection, Dao<Notes, ? > notesDao, Dao<Images, ? > imagesDao,
            Dao<ImageData, ? > imageDataDao, GpapUsers user ) throws Exception {
        String sql = "select " + //
                idFN + "," + //
                latFN + "," + //
                lonFN + "," + //
                altimFN + "," + //
                tsFN + "," + //
                textFN + "," + //
                descFN + "," + //
                styleFN + "," + //
                formFN + " from " + //
                TABLE_NOTES;

        try (IHMStatement statement = gpapConnection.createStatement(); IHMResultSet rs = statement.executeQuery(sql);) {
            while( rs.next() ) {
                long gpapNoteId = rs.getLong(idFN);
                String form = rs.getString(formFN);
                double lat = rs.getDouble(latFN);
                double lon = rs.getDouble(lonFN);
                double altim = rs.getDouble(altimFN);
                long ts = rs.getLong(tsFN);
                String text = rs.getString(textFN);
                String descr = rs.getString(descFN);
                if (descr == null)
                    descr = "";
                String style = rs.getString(styleFN);
                if (lat == 0 || lon == 0) {
                    continue;
                }
                // and then create the features
                Coordinate c = new Coordinate(lon, lat);
                Point point = gf.createPoint(c);

                Notes note = new Notes(point, altim, ts, descr, text, form, style, user);
                notesDao.create(note);

                if (form != null && form.trim().length() > 0) {
                    // check for images and import those also
                    insertImage(user, gpapConnection, gpapNoteId, note.id, imagesDao, imageDataDao);
                }

            }

        }

    }

    // private void checkImages( GpapUsers user, IHMConnection gpapConnection, long gpapNoteId, long
    // newNoteId,
    // Dao<Images, ? > imagesDao, Dao<ImageData, ? > imageDataDao, String formString ) throws
    // Exception {
    // JSONObject sectionObject = new JSONObject(formString);
    // String sectionName = sectionObject.getString("sectionname");
    // sectionName = sectionName.replaceAll("\\s+", "_");
    // List<String> formNames4Section = Utilities.getFormNames4Section(sectionObject);
    //
    // LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
    // LinkedHashMap<String, String> typesMap = new LinkedHashMap<>();
    // GeopaparazziUtilities.extractValues(sectionObject, formNames4Section, valuesMap, typesMap);
    //
    // Set<Entry<String, String>> entrySet = valuesMap.entrySet();
    //
    // for( Entry<String, String> entry : entrySet ) {
    // String key = entry.getKey();
    // String type = typesMap.get(key);
    // if (isMedia(type)) {
    // String value = entry.getValue();
    // // extract images to media folder
    // String[] imageSplit = value.split(OmsGeopaparazziProject3To4Converter.IMAGE_ID_SEPARATOR);
    // for( String image : imageSplit ) {
    // image = image.trim();
    // if (image.length() == 0)
    // continue;
    // long imageId = Long.parseLong(image);
    //
    // insertImageById(user, gpapConnection, gpapNoteId, newNoteId, imagesDao, imageDataDao,
    // imageId);
    //
    // }
    // }
    // }
    // }

    private void insertImage( GpapUsers user, IHMConnection gpapConnection, Long gpapNoteId, Long newNoteId,
            Dao<Images, ? > imagesDao, Dao<ImageData, ? > imageDataDao ) throws Exception {

        String sql = "select " + //
                imgLonFN + "," + //
                imgLatFN + "," + //
                imgAltimFN + "," + //
                imgTsFN + "," + //
                imgAzimFN + "," + //
                imgTextFN + "," + //
                imgdImadedataDataFN + "," + //
                imgdImadedataThumbFN + //
                " from " + TABLE_IMAGES + " i, " + TABLE_IMAGE_DATA + " id  where " + //
                "i." + imgImagedataidFN + "=" + "id." + imgdImadedataidFN + " and "; //

        if (gpapNoteId != null) {
            sql += imgNoteidFN + "=" + gpapNoteId;
        } else {
            sql += " (" + imgNoteidFN + " is null or " + imgNoteidFN + "=-1)";
        }

        try (IHMStatement statement = gpapConnection.createStatement(); IHMResultSet rs = statement.executeQuery(sql);) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            while( rs.next() ) {
                int i = 1;
                double lon = rs.getDouble(i++);
                double lat = rs.getDouble(i++);
                Point point = gf.createPoint(new Coordinate(lon, lat));
                double altim = rs.getDouble(i++);
                long ts = rs.getLong(i++);
                double azim = rs.getDouble(i++);
                String text = rs.getString(i++);
                byte[] imgBytes = rs.getBytes(i++);
                byte[] thumbBytes = rs.getBytes(i++);

                ImageData imgData = new ImageData(imgBytes, thumbBytes);
                imageDataDao.create(imgData);

                Notes note = null;
                if (newNoteId != null) {
                    note = new Notes(newNoteId);
                }

                Images img = new Images(point, altim, ts, azim, text, note, imgData, user);
                imagesDao.create(img);
            }
        }

    }

    public static boolean isMedia( String type ) {
        return type.equals("pictures") || type.equals("map") || type.equals("sketch");
    }

    public static void main( String[] args ) throws Exception {
        new ProjectsImporter();
    }

}
