/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.database;

import com.codename1.properties.DoubleProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.LongProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * The notes class.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Notes implements PropertyBusinessObject {
//        sB.append(NotesTableFields.COLUMN_LON.getFieldName()).append(" REAL NOT NULL, ");
//        sB.append(NotesTableFields.COLUMN_LAT.getFieldName()).append(" REAL NOT NULL,");
//        sB.append(NotesTableFields.COLUMN_ALTIM.getFieldName()).append(" REAL NOT NULL,");
//        sB.append(NotesTableFields.COLUMN_TS.getFieldName()).append(" DATE NOT NULL,");
//        sB.append(NotesTableFields.COLUMN_DESCRIPTION.getFieldName()).append(" TEXT, ");
//        sB.append(NotesTableFields.COLUMN_TEXT.getFieldName()).append(" TEXT NOT NULL, ");
//        sB.append(NotesTableFields.COLUMN_FORM.getFieldName()).append(" CLOB, ");
//        sB.append(NotesTableFields.COLUMN_STYLE.getFieldName()).append(" TEXT,");
//        sB.append(NotesTableFields.COLUMN_ISDIRTY.getFieldName()).append(" INTEGER");    

    public final LongProperty<Notes> id = new LongProperty<>("_id");
    public final DoubleProperty< Notes> longitude = new DoubleProperty<>("lon");
    public final DoubleProperty< Notes> latitude = new DoubleProperty<>("lat");
    public final DoubleProperty< Notes> altitude = new DoubleProperty<>("altim");
    public final LongProperty<Notes> timeStamp = new LongProperty<>("ts");
    public final Property<String, Notes> description = new Property<>("description");
    public final Property<String, Notes> text = new Property<>("text");
    public final Property<String, Notes> form = new Property<>("form");
    public final IntProperty<Notes> isDirty = new IntProperty<>("isdirty");
    public final Property<String, Notes> style = new Property<>("style");

    private final PropertyIndex idx = new PropertyIndex(this, "Notes", id, longitude, latitude,
            altitude, timeStamp, description, text, form, isDirty, style
    );

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    @Override
    public int hashCode() {
        return idx.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        return idx.equals(((Notes) obj).getPropertyIndex());
    }

}
