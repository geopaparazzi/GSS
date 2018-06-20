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
public class GssNote implements PropertyBusinessObject {
    public final LongProperty<GssNote> id = new LongProperty<>("_id");
    public final DoubleProperty< GssNote> longitude = new DoubleProperty<>("lon");
    public final DoubleProperty< GssNote> latitude = new DoubleProperty<>("lat");
    public final DoubleProperty< GssNote> altitude = new DoubleProperty<>("altim");
    public final LongProperty<GssNote> timeStamp = new LongProperty<>("ts");
    public final Property<String, GssNote> description = new Property<>("description");
    public final Property<String, GssNote> text = new Property<>("text");
    public final Property<String, GssNote> form = new Property<>("form");
    public final IntProperty<GssNote> isDirty = new IntProperty<>("isdirty");
    public final Property<String, GssNote> style = new Property<>("style");

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
        return idx.equals(((GssNote) obj).getPropertyIndex());
    }

}
