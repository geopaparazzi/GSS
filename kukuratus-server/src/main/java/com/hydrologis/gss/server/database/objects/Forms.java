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
package com.hydrologis.gss.server.database.objects;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The forms database table class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "forms")
public class Forms {
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String TIMESTAMP_FIELD_NAME = "ts"; //$NON-NLS-1$
    public static final String NAME_FIELD_NAME = "name"; //$NON-NLS-1$
    public static final String FORM_FIELD_NAME = "form"; //$NON-NLS-1$
    public static final String WEBUSER_FIELD_NAME = "webuseruniquename"; //$NON-NLS-1$
    public static final String STATUS_FIELD_NAME = "status"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = NAME_FIELD_NAME, canBeNull = false)
    public String name;

    @DatabaseField(columnName = FORM_FIELD_NAME, canBeNull = false, dataType = DataType.LONG_STRING)
    public String form;

    @DatabaseField(columnName = WEBUSER_FIELD_NAME, canBeNull = false, index = true)
    public String webUser;

    @DatabaseField(columnName = STATUS_FIELD_NAME, canBeNull = false)
    public int status;

    Forms() {
    }

    public Forms( long id ) {
        this.id = id;
    }

    public Forms( String name, String form, String webUser, int status ) {
        super();
        this.name = name;
        this.form = form;
        this.webUser = webUser;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getForm() {
        return form;
    }

    public void setForm( String form ) {
        this.form = form;
    }

    public String getWebUser() {
        return webUser;
    }

    public void setWebUser( String webUser ) {
        this.webUser = webUser;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus( int status ) {
        this.status = status;
    }

}
