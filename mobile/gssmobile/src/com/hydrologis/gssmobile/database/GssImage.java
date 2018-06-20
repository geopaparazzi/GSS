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
public class GssImage implements PropertyBusinessObject {

    public final LongProperty<GssImage> id = new LongProperty<>("_id");
    public final DoubleProperty< GssImage> longitude = new DoubleProperty<>("lon");
    public final DoubleProperty< GssImage> latitude = new DoubleProperty<>("lat");
    public final DoubleProperty< GssImage> altitude = new DoubleProperty<>("altim");
    public final LongProperty<GssImage> timeStamp = new LongProperty<>("ts");
    public final DoubleProperty<String> azimuth = new DoubleProperty<>("azim");
    public final Property<String, GssImage> text = new Property<>("text");
    public final LongProperty<GssImage> noteId = new LongProperty<>("note_id");
    public final LongProperty<GssImage> imageDataId = new LongProperty<>("imagedata_id");
    public final IntProperty<GssImage> isDirty = new IntProperty<>("isdirty");

    private final PropertyIndex idx = new PropertyIndex(this, "Images", id, longitude, latitude,
            altitude, timeStamp, azimuth, text, noteId, imageDataId, isDirty
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
        return idx.equals(((GssImage) obj).getPropertyIndex());
    }

}
