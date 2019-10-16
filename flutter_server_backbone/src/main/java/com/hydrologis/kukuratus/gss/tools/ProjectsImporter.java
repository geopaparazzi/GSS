/*******************************************************************************
 * Copyright (C) 2018 HydroloGIS S.r.l. (www.hydrologis.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Antonello Andrea (http://www.hydrologis.com)
 ******************************************************************************/
package com.hydrologis.kukuratus.gss.tools;

import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOG_PROPERTIES;
import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_IMAGES;
import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_IMAGE_DATA;
import static org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_NOTES;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.EDb;
import org.hortonmachine.dbs.compat.IHMConnection;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.h2gis.H2GisDb;
import org.hortonmachine.dbs.spatialite.hm.SpatialiteDb;
import org.hortonmachine.dbs.spatialite.hm.SqliteDb;
import org.hortonmachine.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsLog;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsPoint;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.GpsLogsPropertiesTableFields;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.ImageDataTableFields;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.ImageTableFields;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.TableDescriptions.NotesTableFields;
import org.hortonmachine.gears.utils.files.FileUtilities;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import com.hydrologis.kukuratus.database.DatabaseHandler;
import com.hydrologis.kukuratus.database.ISpatialTable;
import com.hydrologis.kukuratus.gss.database.GpapProject;
import com.hydrologis.kukuratus.gss.database.GpapUsers;
import com.hydrologis.kukuratus.gss.database.GpsLogs;
import com.hydrologis.kukuratus.gss.database.GpsLogsData;
import com.hydrologis.kukuratus.gss.database.ImageData;
import com.hydrologis.kukuratus.gss.database.Images;
import com.hydrologis.kukuratus.gss.database.Notes;
import com.j256.ormlite.dao.Dao;

public class ProjectsImporter {
    private static final GeometryFactory gf = GeometryUtilities.gf();
    private static final String idFN = NotesTableFields.COLUMN_ID.getFieldName();
    private static final String tsFN = NotesTableFields.COLUMN_TS.getFieldName();
    private static final String altimFN = NotesTableFields.COLUMN_ALTIM.getFieldName();
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

    private static final String logPropIdFN = GpsLogsPropertiesTableFields.COLUMN_LOGID.getFieldName();
    private static final String logPropColorFN = GpsLogsPropertiesTableFields.COLUMN_PROPERTIES_COLOR.getFieldName();
    private static final String logPropWidthFN = GpsLogsPropertiesTableFields.COLUMN_PROPERTIES_WIDTH.getFieldName();

    private List<Class< ? >> tableClasses = Arrays.asList(//
            GpapUsers.class, //
            GpapProject.class, //
            Notes.class, //
            ImageData.class, //
            Images.class, //
            GpsLogs.class, //
            GpsLogsData.class);

    public ProjectsImporter() throws Exception {

        String[] dbsToImport = {
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170216_075147_compleanno_hydrologis.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170205_130453_valencia_geopap.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20180317_091712_bonn_codesprint.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20160319_064638_vacanze_rest.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170120_123422_skiri.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20161221_092537_torino_arcobaleno.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20160816_172638_bonn_foss4g.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20160210_092652_hydrologis_11.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20121127_075920_sopraluogo_helsinki.gpap", //$NON-NLS-1$
                "/home/hydrologis/Dropbox/geopaparazzi/projects/geopaparazzi_20170310_riogambis_with_forms.gpap"}; //$NON-NLS-1$

        String dbPath = "/home/hydrologis/TMP/TESTGSS/DATA/gss_database.mv.db"; //$NON-NLS-1$
        File dbFile = new File(dbPath);
        dbFile.delete();

        try (ASpatialDb db = EDb.fromFileDesktop(dbFile).getSpatialDb()) {
            if (!db.open(dbPath)) {
                db.initSpatialMetadata(null);
            }

            DatabaseHandler dbHandler = DatabaseHandler.init(db);

            createTables(dbHandler);
            dbHandler.populateWithDefaults();
            dbHandler.populateForDemo();

            Dao<Notes, ? > notesDao = dbHandler.getDao(Notes.class);
            Dao<Images, ? > imagesDao = dbHandler.getDao(Images.class);
            Dao<ImageData, ? > imageDataDao = dbHandler.getDao(ImageData.class);
            Dao<GpapUsers, ? > gpapUsersDao = dbHandler.getDao(GpapUsers.class);
            Dao<GpapProject, ? > gpapProjectDao = dbHandler.getDao(GpapProject.class);
            Dao<GpsLogs, ? > logsDao = dbHandler.getDao(GpsLogs.class);
            Dao<GpsLogsData, ? > logsDataDao = dbHandler.getDao(GpsLogsData.class);

            for( String tmpDbPath : dbsToImport ) {
                String dummyDeviceId = FileUtilities.getNameWithoutExtention(new File(tmpDbPath));
                String userName = "user-" + dummyDeviceId;
                String projectName = "project-" + dummyDeviceId;

                System.out.println("Importing db: " + dummyDeviceId); //$NON-NLS-1$

                GpapUsers user = new GpapUsers(dummyDeviceId, userName, null, null);
                gpapUsersDao.create(user);
                GpapProject project = new GpapProject(projectName);
                gpapProjectDao.create(project);

                try (SqliteDb gpapDb = new SqliteDb()) {
                    gpapDb.open(tmpDbPath);

                    gpapDb.execOnConnection(gpapConnection -> {

                        // projectInfo(connection, outputFolderFile);

                        importNotes(gpapConnection, notesDao, imagesDao, imageDataDao, user, project);

                        insertImage(user, gpapConnection, null, null, imagesDao, imageDataDao, project);

                        importGpsLog(gpapConnection, logsDao, logsDataDao, user, project);

                        return null;
                    });

                }

            }

        }
    }

    private void createTables( DatabaseHandler dbHandler ) throws Exception {
        ASpatialDb db = dbHandler.getDb();
        for( Class< ? > tClass : tableClasses ) {
            System.out.println("Create if not exists: " + DatabaseHandler.getTableName(tClass)); //$NON-NLS-1$
            dbHandler.createTableIfNotExists(tClass);
            if (ISpatialTable.class.isAssignableFrom(tClass)) {
                if (db instanceof H2GisDb) {
                    H2GisDb h2gisDb = (H2GisDb) db;
                    String tableName = DatabaseHandler.getTableName(tClass);
                    h2gisDb.addSrid(tableName, DatabaseHandler.TABLES_EPSG, ISpatialTable.GEOM_FIELD_NAME);
                    h2gisDb.createSpatialIndex(tableName, ISpatialTable.GEOM_FIELD_NAME);
                } else if (db instanceof SpatialiteDb) {
                    // SpatialiteDb spatialiteDb = (SpatialiteDb) db;
                    String tableName = DatabaseHandler.getTableName(tClass);
                    String geometryType = DatabaseHandler.getGeometryType(tClass);
                    db.execOnConnection(conn -> {
                        // SELECT RecoverGeometryColumn('pipespieces', 'the_geom', 4326,
                        // 'LINESTRING', 'XY')
                        String sql = "SELECT RecoverGeometryColumn('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "', " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + DatabaseHandler.TABLES_EPSG + ", '" + geometryType + "', 'XY')"; //$NON-NLS-1$ //$NON-NLS-2$
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }
                        // SELECT CreateSpatialIndex('pipespieces','the_geom');
                        sql = "SELECT CreateSpatialIndex('" + tableName + "','" + ISpatialTable.GEOM_FIELD_NAME + "')"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        try (IHMStatement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                        }

                        return null;
                    });
                }
            }
        }
    }

    private void importGpsLog( IHMConnection gpapConnection, Dao<GpsLogs, ? > logsDao, Dao<GpsLogsData, ? > logsDataDao,
            GpapUsers user, GpapProject project ) throws Exception {
        List<GpsLog> logsList = OmsGeopaparazzi4Converter.getGpsLogsList(gpapConnection);
        for( GpsLog log : logsList ) {

            String sql = "select " + // //$NON-NLS-1$
                    logPropColorFN + "," + // //$NON-NLS-1$
                    logPropWidthFN + " from " + // //$NON-NLS-1$
                    TABLE_GPSLOG_PROPERTIES + " where " + logPropIdFN + "=" + log.id; //$NON-NLS-1$ //$NON-NLS-2$
            String color = "#FF0000";
            float width = 3;
            try (IHMStatement statement = gpapConnection.createStatement(); IHMResultSet rs = statement.executeQuery(sql);) {
                if (rs.next()) {
                    color = rs.getString(1);
                    width = rs.getFloat(2);
                }
            }

            List<GpsPoint> gpsPointList = log.points;
            List<Coordinate> logCoordinates = gpsPointList.stream().map(gp -> new Coordinate(gp.lon, gp.lat))
                    .collect(Collectors.toList());
            LineString logLine = gf.createLineString(logCoordinates.toArray(new Coordinate[logCoordinates.size()]));
            GpsLogs newLog = new GpsLogs(log.id, log.text, log.startTime, log.endTime, logLine, color, width, user, project,
                    System.currentTimeMillis());
            logsDao.create(newLog);

            List<GpsLogsData> dataList = new ArrayList<>();
            for( GpsPoint gpsPoint : gpsPointList ) {
                Coordinate c = new Coordinate(gpsPoint.lon, gpsPoint.lat);
                Point point = gf.createPoint(c);

                GpsLogsData gpsLogsData = new GpsLogsData(point, gpsPoint.altim, gpsPoint.utctime, newLog);
                dataList.add(gpsLogsData);
            }
            logsDataDao.create(dataList);

        }

    }

    private void importNotes( IHMConnection gpapConnection, Dao<Notes, ? > notesDao, Dao<Images, ? > imagesDao,
            Dao<ImageData, ? > imageDataDao, GpapUsers user, GpapProject project ) throws Exception {
        String sql = "select " + // //$NON-NLS-1$
                idFN + "," + // //$NON-NLS-1$
                latFN + "," + // //$NON-NLS-1$
                lonFN + "," + // //$NON-NLS-1$
                altimFN + "," + // //$NON-NLS-1$
                tsFN + "," + // //$NON-NLS-1$
                textFN + "," + // //$NON-NLS-1$
                descFN + "," + // //$NON-NLS-1$
                styleFN + "," + // //$NON-NLS-1$
                formFN + " from " + // //$NON-NLS-1$
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
                    descr = ""; //$NON-NLS-1$
                String style = rs.getString(styleFN);
                if (lat == 0 || lon == 0) {
                    continue;
                }
                // and then create the features
                Coordinate c = new Coordinate(lon, lat);
                Point point = gf.createPoint(c);

                Notes note = new Notes(point, gpapNoteId, altim, ts, descr, text, form, style, user, project, null,
                        System.currentTimeMillis());
                notesDao.create(note);

                if (form != null && form.trim().length() > 0) {
                    // check for images and import those also
                    insertImage(user, gpapConnection, gpapNoteId, note.id, imagesDao, imageDataDao, project);
                }

            }

        }

    }

    private void insertImage( GpapUsers user, IHMConnection gpapConnection, Long gpapNoteId, Long newNoteId,
            Dao<Images, ? > imagesDao, Dao<ImageData, ? > imageDataDao, GpapProject project ) throws Exception {

        String sql = "select i." + // //$NON-NLS-1$
                imgIdFN + "," + // //$NON-NLS-1$
                imgLonFN + "," + // //$NON-NLS-1$
                imgLatFN + "," + // //$NON-NLS-1$
                imgAltimFN + "," + // //$NON-NLS-1$
                imgTsFN + "," + // //$NON-NLS-1$
                imgAzimFN + "," + // //$NON-NLS-1$
                imgTextFN + "," + // //$NON-NLS-1$
                imgImagedataidFN + "," + // //$NON-NLS-1$
                imgdImadedataDataFN + "," + // //$NON-NLS-1$
                imgdImadedataThumbFN + //
                " from " + TABLE_IMAGES + " i, " + TABLE_IMAGE_DATA + " id  where " + // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "i." + imgImagedataidFN + "=" + "id." + imgdImadedataidFN + " and "; // //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        if (gpapNoteId != null) {
            sql += imgNoteidFN + "=" + gpapNoteId; //$NON-NLS-1$
        } else {
            sql += " (" + imgNoteidFN + " is null or " + imgNoteidFN + "=-1)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        try (IHMStatement statement = gpapConnection.createStatement(); IHMResultSet rs = statement.executeQuery(sql);) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            while( rs.next() ) {
                int i = 1;
                long imgId = rs.getLong(i++);
                double lon = rs.getDouble(i++);
                double lat = rs.getDouble(i++);
                Point point = gf.createPoint(new Coordinate(lon, lat));
                double altim = rs.getDouble(i++);
                long ts = rs.getLong(i++);
                double azim = rs.getDouble(i++);
                String text = rs.getString(i++);
                long imgDataId = rs.getLong(i++);
                byte[] imgBytes = rs.getBytes(i++);
                byte[] thumbBytes = rs.getBytes(i++);

                ImageData imgData = new ImageData(imgDataId, imgBytes, user);
                imageDataDao.create(imgData);

                Notes note = null;
                if (newNoteId != null) {
                    note = new Notes(newNoteId);
                }

                Images img = new Images(point, imgId, altim, ts, azim, text, note, imgData, user, thumbBytes, project,
                        System.currentTimeMillis());
                imagesDao.create(img);
            }
        }
    }

    public static void main( String[] args ) throws Exception {
        new ProjectsImporter();
    }

}
