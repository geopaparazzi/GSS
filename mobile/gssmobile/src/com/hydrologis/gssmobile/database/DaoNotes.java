/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.database;

import com.codename1.db.Cursor;
import com.codename1.db.Database;
import com.codename1.db.Row;
import com.codename1.io.Util;
import com.hydrologis.cn1.libs.HyLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hydrologis
 */
public class DaoNotes {

    /**
     * The notes table name.
     */
    public static final String TABLE_NOTES = "notes";

    public static List<GssNote> getAllNotesList(Database db) throws Exception {

        String query = "SELECT "
                + //
                NotesTableFields.COLUMN_ID.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_LON.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_LAT.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_ALTIM.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_TS.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_DESCRIPTION.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_TEXT.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_FORM.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_STYLE.getFieldName()
                + " FROM " + TABLE_NOTES + " where " + NotesTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1 + " order by " + NotesTableFields.COLUMN_TS.getFieldName();

        List<GssNote> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            //HyLog.p(query);
            cursor = db.executeQuery(query);

            while (cursor.next()) {
                Row row = cursor.getRow();
                int i = 0;
                GssNote note = new GssNote();
                note.id = row.getLong(i++);
                note.longitude = row.getDouble(i++);
                note.latitude = row.getDouble(i++);
                note.altitude = row.getDouble(i++);
                note.timeStamp = row.getLong(i++);
                note.description = row.getString(i++);
                note.text = row.getString(i++);
                note.form = row.getString(i++);
                note.style = row.getString(i++);
                notes.add(note);
            }
        } finally {
            Util.cleanup(cursor);
        }
        return notes;
    }

    public static List<GssNote> getFormNotesList(Database db) throws Exception {

        String query = "SELECT "
                + //
                NotesTableFields.COLUMN_ID.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_LON.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_LAT.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_ALTIM.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_TS.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_DESCRIPTION.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_TEXT.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_FORM.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_STYLE.getFieldName()
                + " FROM " + TABLE_NOTES //
                + " where "
                + NotesTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1 //
                + " and " + NotesTableFields.COLUMN_FORM.getFieldName() + "<>''" //
                + " order by " + NotesTableFields.COLUMN_TS.getFieldName();

        List<GssNote> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            //HyLog.p(query);
            cursor = db.executeQuery(query);

            while (cursor.next()) {
                Row row = cursor.getRow();
                int i = 0;
                GssNote note = new GssNote();
                note.id = row.getLong(i++);
                note.longitude = row.getDouble(i++);
                note.latitude = row.getDouble(i++);
                note.altitude = row.getDouble(i++);
                note.timeStamp = row.getLong(i++);
                note.description = row.getString(i++);
                note.text = row.getString(i++);
                note.form = row.getString(i++);
                note.style = row.getString(i++);
                notes.add(note);
            }
        } finally {
            Util.cleanup(cursor);
        }
        return notes;
    }

    public static List<GssNote> getSimpleNotesList(Database db) throws Exception {

        String query = "SELECT "
                + //
                NotesTableFields.COLUMN_ID.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_LON.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_LAT.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_ALTIM.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_TS.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_DESCRIPTION.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_TEXT.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_STYLE.getFieldName()
                + " FROM " + TABLE_NOTES //
                + " where "
                + NotesTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1 //
                + " and " + NotesTableFields.COLUMN_FORM.getFieldName() + "=''" //
                + " order by " + NotesTableFields.COLUMN_TS.getFieldName();

        List<GssNote> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            //HyLog.p(query);
            cursor = db.executeQuery(query);

            while (cursor.next()) {
                Row row = cursor.getRow();
                int i = 0;
                GssNote note = new GssNote();
                note.id = row.getLong(i++);
                note.longitude = row.getDouble(i++);
                note.latitude = row.getDouble(i++);
                note.altitude = row.getDouble(i++);
                note.timeStamp = row.getLong(i++);
                note.description = row.getString(i++);
                note.text = row.getString(i++);
                note.style = row.getString(i++);
                notes.add(note);
            }
        } finally {
            Util.cleanup(cursor);
        }
        return notes;
    }

    public static void clearDirty(Database db) throws IOException {
        String update = "update " + TABLE_NOTES + " set " + NotesTableFields.COLUMN_ISDIRTY.getFieldName() + "=0";
        if (db != null) {
            db.execute(update);
        }
    }

    public static void makeDirty(Database db) throws IOException {
        String update = "update " + TABLE_NOTES + " set " + NotesTableFields.COLUMN_ISDIRTY.getFieldName() + "=1";
        if (db != null) {
            db.execute(update);
        }
    }

    public static enum NotesTableFields {
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
         * Description of the note.
         */
        COLUMN_DESCRIPTION("description", String.class),
        /**
         * Simple text of the note.
         */
        COLUMN_TEXT("text", String.class),
        /**
         * Form data of the note.
         */
        COLUMN_FORM("form", String.class),
        /**
         * Is dirty field (0 = false, 1 = true)
         */
        COLUMN_ISDIRTY("isdirty", Integer.class),
        /**
         * Style of the note.
         */
        COLUMN_STYLE("style", String.class);

        private final String fieldName;
        private final Class fieldClass;

        NotesTableFields(String fieldName, Class fieldClass) {
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
