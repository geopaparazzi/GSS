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

import org.json.JSONObject;

@DatabaseTable(tableName = "users")
public class User {

    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String NAME_FIELD_NAME = "name"; //$NON-NLS-1$
    public static final String UNIQUENAME_FIELD_NAME = "uniquename"; //$NON-NLS-1$
    public static final String EMAIL_FIELD_NAME = "email"; //$NON-NLS-1$
    public static final String PASSWORD_FIELD_NAME = "password"; //$NON-NLS-1$
    public static final String GROUP_FIELD_NAME = "group_id"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public int id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = UNIQUENAME_FIELD_NAME, canBeNull = false, unique = true)
    public String uniqueName;

    @DatabaseField(columnName = EMAIL_FIELD_NAME, canBeNull = false)
    public String email;

    @DatabaseField(columnName = PASSWORD_FIELD_NAME, canBeNull = false)
    public String pwd;

    @DatabaseField(foreign = true, columnName = GROUP_FIELD_NAME)
    public Group group;

    public User() {
    }

    public User( String name, String uniqueName, String email, String pwd, Group group ) {
        this.name = name;
        this.uniqueName = uniqueName;
        this.email = email;
        if (pwd != null)
            this.pwd = RegistryHandler.hashPwd(pwd);
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName( String uniqueName ) {
        this.uniqueName = uniqueName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail( String email ) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd( String pwd ) {
        this.pwd = pwd;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup( Group group ) {
        this.group = group;
    }

    public JSONObject toJson() {
        JSONObject surveyor = new JSONObject();
        surveyor.put(ID_FIELD_NAME, id);
        surveyor.put(NAME_FIELD_NAME, name);
        surveyor.put(UNIQUENAME_FIELD_NAME, uniqueName);
        surveyor.put(EMAIL_FIELD_NAME, email);
        surveyor.put(GROUP_FIELD_NAME, group.getDescription());
        // password is never sent on purpose
        return surveyor;
    }
}
