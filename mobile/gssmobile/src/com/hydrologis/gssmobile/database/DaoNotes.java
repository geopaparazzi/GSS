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

    public static List<Notes> getNotesList(Database db) throws Exception {

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
                NotesTableFields.COLUMN_ISDIRTY.getFieldName() + ", "
                + //
                NotesTableFields.COLUMN_STYLE.getFieldName()
                + " FROM " + TABLE_NOTES + " where " + NotesTableFields.COLUMN_ISDIRTY.getFieldName() + "=" + 1;

        List<Notes> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.executeQuery(query);

            while (cursor.next()) {
                Row row = cursor.getRow();
//                 private final PropertyIndex idx = new PropertyIndex(this, "Notes", id, longitude, latitude,
//            altitude, timeStamp, description, text, form, isDirty, style
//    );
                int i = 0;

                Notes note = new Notes();
                note.id.set(row.getLong(i++));
                note.longitude.set(row.getDouble(i++));
                note.latitude.set(row.getDouble(i++));
                note.altitude.set(row.getDouble(i++));
                note.timeStamp.set(row.getLong(i++));
                note.description.set(row.getString(i++));
                note.text.set(row.getString(i++));
                note.form.set(row.getString(i++));
                note.isDirty.set(row.getInteger(i++));
                note.style.set(row.getString(i++));
                notes.add(note);
            }
        } finally {
            Util.cleanup(cursor);
        }
        return notes;
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

        private String fieldName;
        private Class fieldClass;

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
