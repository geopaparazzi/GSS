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

@DatabaseTable(tableName = "groups")
public class Group {

    public static final String DESCR_FIELD_NAME = "description"; //$NON-NLS-1$
    public static final String AUTHORIZATION_FIELD_NAME = "authorization_id"; //$NON-NLS-1$

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = DESCR_FIELD_NAME, canBeNull = false)
    private String description;

    @DatabaseField(foreign = true, columnName = AUTHORIZATION_FIELD_NAME)
    private Authorization authorization;

    public Group() {
    }

    public Group( String descr, Authorization authorization ) {
        this.description = descr;
        this.authorization = authorization;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String name ) {
        this.description = name;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization( Authorization authorization ) {
        this.authorization = authorization;
    }

    @Override
    public String toString() {
        return description;
    }

}
