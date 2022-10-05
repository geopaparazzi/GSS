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
package com.hydrologis.kukuratus.registry;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "settings")
public class Settings {

    public static final String KEY_FIELD_NAME = "key"; //$NON-NLS-1$
    public static final String VALUE_FIELD_NAME = "value"; //$NON-NLS-1$
    public static final String USER_FIELD_NAME = "username"; //$NON-NLS-1$

    public static final String GLOBALUSER = "globalsetting"; //$NON-NLS-1$

    @DatabaseField(id = true, useGetSet = true)
    private String id;

    @DatabaseField(uniqueCombo = true, columnName = KEY_FIELD_NAME, canBeNull = false)
    public String key;

    @DatabaseField(columnName = VALUE_FIELD_NAME, canBeNull = false)
    public String value;

    @DatabaseField(uniqueCombo = true, columnName = USER_FIELD_NAME, canBeNull = false)
    public String userName;

    Settings() {
    }

    public Settings( String key, String value, String userName ) {
        this.key = key;
        this.value = value;
        this.userName = userName;
        if (userName == null) {
            this.userName = GLOBALUSER;
        }
    }

    public String getId() {
        return key + "-" + userName; //$NON-NLS-1$
    }

    public void setId( String id ) {
        this.id = id;
    }

}
