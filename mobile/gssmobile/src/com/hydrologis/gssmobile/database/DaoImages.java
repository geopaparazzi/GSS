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
package com.hydrologis.gssmobile.database;

import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.codename1.io.Util;
import com.hydrologis.cn1.libs.HyLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public class DaoImages {

    public static final int MAXBLOBSIZE = 1900000;

    /**
     * Image table name.
     */
    public static final String TABLE_IMAGES = "images";
    /**
     * Image data table name.
     */
    public static final String TABLE_IMAGE_DATA = "imagedata";

    public static List<GssImage> getImagesList(Database db, boolean withData) throws Exception {

        final String imgLonFN = ImageTableFields.COLUMN_LON.getFieldName();
        final String imgLatFN = ImageTableFields.COLUMN_LAT.getFieldName();
        final String imgAltimFN = ImageTableFields.COLUMN_ALTIM.getFieldName();
        final String imgTsFN = ImageTableFields.COLUMN_TS.getFieldName();
        final String imgAzimFN = ImageTableFields.COLUMN_AZIM.getFieldName();
        final String imgTextFN = ImageTableFields.COLUMN_TEXT.getFieldName();
        final String imgNoteidFN = ImageTableFields.COLUMN_NOTE_ID.getFieldName();
        final String imgImagedataidFN = ImageTableFields.COLUMN_IMAGEDATA_ID.getFieldName();

        String query = "select "
                + //
                imgLonFN + ","
                + //
                imgLatFN + ","
                + //
                imgAltimFN + ","
                + //
                imgTsFN + ","
                + //
                imgAzimFN + ","
                + //
                imgTextFN + ","
                + //
                imgNoteidFN + ","
                + //
                imgImagedataidFN
                + //
                " from " + TABLE_IMAGES + " where "
                + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1 + " order by " + imgTsFN;

        List<GssImage> images = new ArrayList<>();
        Cursor cursor = null;

        try {
            //HyLog.p(query);
            cursor = db.executeQuery(query);
            while (cursor.next()) {
                Row row = cursor.getRow();
                int i = 0;
                GssImage image = new GssImage();
                image.longitude = row.getDouble(i++);
                image.latitude = row.getDouble(i++);
                image.altitude = row.getDouble(i++);
                image.timeStamp = row.getLong(i++);
                image.azimuth = row.getDouble(i++);
                image.text = row.getString(i++);
                image.noteId = row.getLong(i++);
                images.add(image);

                if (withData) {
                    long imageDataId = row.getLong(i++);
                    getImageData(db, imageDataId, image);
                }

            }
        } finally {
            Util.cleanup(cursor);
        }
        return images;
    }

    public static List<GssImage> getLonelyImagesList(Database db, boolean withData) throws Exception {

        final String imgIdFN = ImageTableFields.COLUMN_ID.getFieldName();
        final String imgLonFN = ImageTableFields.COLUMN_LON.getFieldName();
        final String imgLatFN = ImageTableFields.COLUMN_LAT.getFieldName();
        final String imgAltimFN = ImageTableFields.COLUMN_ALTIM.getFieldName();
        final String imgTsFN = ImageTableFields.COLUMN_TS.getFieldName();
        final String imgAzimFN = ImageTableFields.COLUMN_AZIM.getFieldName();
        final String imgTextFN = ImageTableFields.COLUMN_TEXT.getFieldName();
        final String imgNoteidFN = ImageTableFields.COLUMN_NOTE_ID.getFieldName();
        final String imgImagedataidFN = ImageTableFields.COLUMN_IMAGEDATA_ID.getFieldName();

        String query = "select "
                + //
                imgIdFN + ","
                + //
                imgLonFN + ","
                + //
                imgLatFN + ","
                + //
                imgAltimFN + ","
                + //
                imgTsFN + ","
                + //
                imgAzimFN + ","
                + //
                imgTextFN + ","
                + //
                imgNoteidFN + ","
                + //
                imgImagedataidFN
                + //
                " from " + TABLE_IMAGES + " where "
                + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1 //
                + " and " + imgNoteidFN + "=-1" //
                + " order by " + imgTsFN;

        List<GssImage> images = new ArrayList<>();
        Cursor cursor = null;

        try {
            //HyLog.p(query);
            cursor = db.executeQuery(query);
            while (cursor.next()) {
                Row row = cursor.getRow();
                int i = 0;
                GssImage image = new GssImage();
                image.id = row.getLong(i++);
                image.longitude = row.getDouble(i++);
                image.latitude = row.getDouble(i++);
                image.altitude = row.getDouble(i++);
                image.timeStamp = row.getLong(i++);
                image.azimuth = row.getDouble(i++);
                image.text = row.getString(i++);
                image.noteId = row.getLong(i++);
                images.add(image);

                if (withData) {
                    long imageDataId = row.getLong(i++);
                    getImageData(db, imageDataId, image);
                }

            }
        } finally {
            Util.cleanup(cursor);
        }
        return images;
    }

    public static List<GssImage> getImagesListForNoteId(Database db, long noteId, boolean withData) throws Exception {

        final String imgLonFN = ImageTableFields.COLUMN_LON.getFieldName();
        final String imgLatFN = ImageTableFields.COLUMN_LAT.getFieldName();
        final String imgAltimFN = ImageTableFields.COLUMN_ALTIM.getFieldName();
        final String imgTsFN = ImageTableFields.COLUMN_TS.getFieldName();
        final String imgAzimFN = ImageTableFields.COLUMN_AZIM.getFieldName();
        final String imgTextFN = ImageTableFields.COLUMN_TEXT.getFieldName();
        final String imgNoteidFN = ImageTableFields.COLUMN_NOTE_ID.getFieldName();
        final String imgImagedataidFN = ImageTableFields.COLUMN_IMAGEDATA_ID.getFieldName();

        String query = "select "
                + //
                imgLonFN + ","
                + //
                imgLatFN + ","
                + //
                imgAltimFN + ","
                + //
                imgTsFN + ","
                + //
                imgAzimFN + ","
                + //
                imgTextFN + ","
                + //
                imgNoteidFN + ","
                + //
                imgImagedataidFN
                + //
                " from " + TABLE_IMAGES + " where "
                + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1 //
                + " and " + imgNoteidFN + "=" + noteId //
                + " order by " + imgTsFN;

        List<GssImage> images = new ArrayList<>();
        Cursor cursor = null;

        try {
            //HyLog.p(query);
            cursor = db.executeQuery(query);
            while (cursor.next()) {
                Row row = cursor.getRow();
                int i = 0;
                GssImage image = new GssImage();
                image.longitude = row.getDouble(i++);
                image.latitude = row.getDouble(i++);
                image.altitude = row.getDouble(i++);
                image.timeStamp = row.getLong(i++);
                image.azimuth = row.getDouble(i++);
                image.text = row.getString(i++);
                image.noteId = row.getLong(i++);
                images.add(image);

                if (withData) {
                    long imageDataId = row.getLong(i++);
                    getImageData(db, imageDataId, image);
                }

            }
        } finally {
            Util.cleanup(cursor);
        }
        return images;
    }

    private static void getImageData(Database db, long imageDataId, GssImage image) throws IOException {
        final String imgdImadedataidFN = ImageDataTableFields.COLUMN_ID.getFieldName();
        final String imgdImadedataDataFN = ImageDataTableFields.COLUMN_IMAGE.getFieldName();
        final String imgdImadedataThumbFN = ImageDataTableFields.COLUMN_THUMBNAIL.getFieldName();

        long blobSize = getBlobSize(imageDataId, db);
        if (blobSize < MAXBLOBSIZE || blobSize < 0 /* try if no size available */) {
            String query = "select " + imgdImadedataDataFN + ", " + imgdImadedataThumbFN + " from " + TABLE_IMAGE_DATA
                    + " where " + imgdImadedataidFN + "=" + imageDataId;
            try {
                Cursor cursor = null;
                try {
                    //HyLog.p(query);
                    cursor = db.executeQuery(query);
                    while (cursor.next()) {
                        Row row = cursor.getRow();
                        byte[] thumbBytes = row.getBlob(1);
                        image.dataThumb = thumbBytes;
                        byte[] imageBytes = row.getBlob(0);
                        image.data = imageBytes;
                    }
                } finally {
                    Util.cleanup(cursor);
                }
            } catch (Exception ex) {
                HyLog.e(ex);
            }
        } else {
            try {
                getImagePieces(imageDataId, db, image, blobSize);
            } catch (IOException ex1) {
                HyLog.e(ex1);
            }
        }

    }

    private static long getBlobSize(long imageDataId, Database db) throws IOException {
        String sizeQuery = "SELECT " + ImageDataTableFields.COLUMN_ID.getFieldName()
                +//
                ", length(" + ImageDataTableFields.COLUMN_IMAGE.getFieldName() + ") "
                +//
                "FROM " + TABLE_IMAGE_DATA
                +//
                " WHERE " + ImageDataTableFields.COLUMN_ID.getFieldName() + "=" + imageDataId;
        //"length(" + ImageDataTableFields.COLUMN_IMAGE.getFieldName() + ") > 1000000";
        Cursor sizeCursor = null;
        long blobSize = -1;
        try {
            sizeCursor = db.executeQuery(sizeQuery);
            if (sizeCursor.next()) {
                Row sizerow = sizeCursor.getRow();
                blobSize = sizerow.getLong(1);
            }
        } finally {
            Util.cleanup(sizeCursor);
        }
//        if (HyLog.DO_DEBUG) {
//            HyLog.d("Defined image blob size: " + blobSize + " vs max size: " + MAXBLOBSIZE);
//        }
        return blobSize;
    }

    private static void getImagePieces(long imageDataId, Database db, GssImage image, long blobSize) throws IOException {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            for (long i = 1; i <= blobSize; i = i + MAXBLOBSIZE) {
                long from = i;
                long size = MAXBLOBSIZE;
                if (from + size > blobSize) {
                    size = blobSize - from + 1;
                }
                String tmpQuery = "SELECT " + ImageDataTableFields.COLUMN_THUMBNAIL.getFieldName() + ", substr(" + ImageDataTableFields.COLUMN_IMAGE.getFieldName()
                        + "," + from + ", " + size + ") FROM " + TABLE_IMAGE_DATA + " WHERE " + ImageDataTableFields.COLUMN_ID.getFieldName() + "=" + imageDataId;
                Cursor imageCunchCursor = null;
                try {
                    imageCunchCursor = db.executeQuery(tmpQuery);
                    if (imageCunchCursor.next()) {
                        Row imgrow = imageCunchCursor.getRow();
                        if (image.dataThumb == null) {
                            byte[] thumbData = imgrow.getBlob(0);
                            image.dataThumb = thumbData;
                        }
                        byte[] blobData = imgrow.getBlob(1);
                        bout.write(blobData);
                    }
                } finally {
                    Util.cleanup(imageCunchCursor);
                }
            }
            byte[] imageData = bout.toByteArray();
            image.data = imageData;
        }
    }

    public static void clearDirty(Database db) throws IOException {
        if (db != null) {
            String update = "update " + TABLE_IMAGES + " set " + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=0";
            db.execute(update);
        }
    }

    public static void clearDirtyByNoteId(Database db, long noteId) throws IOException {
        if (db != null) {
            String update = "update " + TABLE_IMAGES + " set " + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=0 where "
                    + ImageTableFields.COLUMN_NOTE_ID.getFieldName() + "=" + noteId;
            db.execute(update);
        }
    }

    public static void clearDirtyById(Database db, long id) throws IOException {
        if (db != null) {
            String update = "update " + TABLE_IMAGES + " set " + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=0 where "
                    + ImageTableFields.COLUMN_ID.getFieldName() + "=" + id;
            db.execute(update);
        }
    }

    public static void makeDirty(Database db) throws IOException {
        if (db != null) {
            String update = "update " + TABLE_IMAGES + " set " + ImageTableFields.COLUMN_ISDIRTY.getFieldName() + "=1";
            db.execute(update);
        }
    }

    public static enum ImageTableFields {
        /**
         * id of the note, Generated by the db.
         */
        COLUMN_ID("_id", Long.class),
        /**
         * Longitude of the note in WGS84.
         */
        COLUMN_LON("lon", Double.class),
        /**
         * Latitude of the note in WGS84.
         */
        COLUMN_LAT("lat", Double.class),
        /**
         * Elevation of the note.
         */
        COLUMN_ALTIM("altim", Double.class),
        /**
         * Timestamp of the note.
         */
        COLUMN_TS("ts", Long.class),
        /**
         * The azimuth of the picture.
         */
        COLUMN_AZIM("azim", Double.class),
        /**
         * A name or text for the image.
         */
        COLUMN_TEXT("text", String.class),
        /**
         * Is dirty field (0=false, 1=true)
         */
        COLUMN_ISDIRTY("isdirty", Integer.class),
        /**
         * An optional note id, to which it is bound to.
         */
        COLUMN_NOTE_ID("note_id", Long.class),
        /**
         * The id of the connected image data.
         */
        COLUMN_IMAGEDATA_ID("imagedata_id", Long.class);

        private String fieldName;
        private Class fieldClass;

        ImageTableFields(String fieldName, Class fieldClass) {
            this.fieldName = fieldName;
            this.fieldClass = fieldClass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class getFieldClass() {
            return fieldClass;
        }
    }

    public static enum ImageDataTableFields {
        /**
         * id of the note, Generated by the db.
         */
        COLUMN_ID("_id", Long.class),
        /**
         * The image data.
         */
        COLUMN_IMAGE("data", byte[].class),
        /**
         * The image thumbnail data.
         */
        COLUMN_THUMBNAIL("thumbnail", byte[].class);

        private String fieldName;
        private Class fieldClass;

        ImageDataTableFields(String fieldName, Class fieldClass) {
            this.fieldName = fieldName;
            this.fieldClass = fieldClass;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class getFieldClass() {
            return fieldClass;
        }
    }
}
