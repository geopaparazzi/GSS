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
package com.hydrologis.kukuratus.gss.database;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

/**
 * The geopaparazzi users class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpapusers")
public class GpapUsers {
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String DEVICE_FIELD_NAME = "deviceid"; //$NON-NLS-1$
    public static final String NAME_FIELD_NAME = "name"; //$NON-NLS-1$
    public static final String ACTIVE_FIELD_NAME = "active"; //$NON-NLS-1$
    public static final String CONTACT_FIELD_NAME = "contact"; //$NON-NLS-1$

    public static final String usersFKColumnDefinition = "bigint references gpapusers(id) on delete cascade"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = DEVICE_FIELD_NAME, canBeNull = false, unique = true)
    public String deviceId;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = CONTACT_FIELD_NAME, canBeNull = true)
    public String contact;

    @DatabaseField(columnName = ACTIVE_FIELD_NAME, canBeNull = false)
    public int active;

    public GpapUsers() {
    }

    public GpapUsers(long id) {
        this.id = id;
    }

    public GpapUsers(String deviceId, String name, String contact, int active) {
        this.deviceId = deviceId;
        this.name = name;
        this.active = active;
        this.contact = contact;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public JSONObject toJson() {
        JSONObject surveyor = new JSONObject();
        surveyor.put(ID_FIELD_NAME, id);
        surveyor.put(DEVICE_FIELD_NAME, deviceId);
        surveyor.put(NAME_FIELD_NAME, name);
        surveyor.put(CONTACT_FIELD_NAME, contact);
        surveyor.put(ACTIVE_FIELD_NAME, active);
        return surveyor;
    }

}
