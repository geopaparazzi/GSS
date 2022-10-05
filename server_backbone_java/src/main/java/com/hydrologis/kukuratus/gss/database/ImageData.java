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
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The image data table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "imagedata")
public class ImageData {
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String DATA_FIELD_NAME = "data"; //$NON-NLS-1$

    public static final String GPAPUSER_FIELD_NAME = "gpapusersid"; //$NON-NLS-1$

    public static final String imagedataFKColumnDefinition = "bigint references imagedata(id) on delete cascade"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = DATA_FIELD_NAME, canBeNull = false, dataType = DataType.BYTE_ARRAY)
    public byte[] data;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    ImageData() {
    }

    public ImageData( long id ) {
        this.id = id;
    }

    public ImageData( byte[] data, GpapUsers gpapUser ) {
        this.data = data;
        this.gpapUser = gpapUser;
    }
}
