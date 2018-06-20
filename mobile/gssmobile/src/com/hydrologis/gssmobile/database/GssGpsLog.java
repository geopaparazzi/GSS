/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydrologis.gssmobile.database;

import com.codename1.properties.IntProperty;
import com.codename1.properties.LongProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * The gps log class.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GssGpsLog implements PropertyBusinessObject {

    public final LongProperty<GssGpsLog> id = new LongProperty<>("_id");
    public final Property<String, GssGpsLog> name = new Property<>("name");
    public final LongProperty<GssGpsLog> startts = new LongProperty<>("startts");
    public final LongProperty<GssGpsLog> endts = new LongProperty<>("endts");
    public final IntProperty<GssGpsLog> isDirty = new IntProperty<>("isdirty");

    private final PropertyIndex idx = new PropertyIndex(this, "GpsLogs", id, name, startts,
            endts, isDirty
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
        return idx.equals(((GssGpsLog) obj).getPropertyIndex());
    }

}
