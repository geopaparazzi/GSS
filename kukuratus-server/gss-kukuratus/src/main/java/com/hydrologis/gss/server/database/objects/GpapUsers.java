/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server.database.objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The geopaparazzi users class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpapusers")
public class GpapUsers {
    public static final String ID_FIELD_NAME = "id";
    public static final String DEVICE_FIELD_NAME = "deviceid";
    public static final String NAME_FIELD_NAME = "name";
    public static final String USERNAME_FIELD_NAME = "username";
    public static final String PASSWORD_FIELD_NAME = "password";
    public static final String CONTACT_FIELD_NAME = "contact";

    public static final String usersFKColumnDefinition = "long references gpapusers(id) on delete cascade";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = DEVICE_FIELD_NAME, canBeNull = false)
    public String deviceId;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = USERNAME_FIELD_NAME, canBeNull = true)
    public String username;

    @DatabaseField(columnName = PASSWORD_FIELD_NAME, canBeNull = true)
    public String password;

    @DatabaseField(columnName = CONTACT_FIELD_NAME, canBeNull = true)
    public String contact;

    GpapUsers() {
    }

    public GpapUsers( long id ) {
        this.id = id;
    }

    public GpapUsers( String deviceId, String name, String username, String password, String contact ) {
        this.deviceId = deviceId;
        this.name = name;
        this.username = username;
        this.password = password;
        this.contact = contact;
    }

}
