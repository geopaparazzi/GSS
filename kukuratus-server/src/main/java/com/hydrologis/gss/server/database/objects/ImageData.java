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
 * The image data table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "imagedata")
public class ImageData {
    public static final String ID_FIELD_NAME = "id";
    public static final String DATA_FIELD_NAME = "data";
    public static final String THUMB_FIELD_NAME = "thumbnail";
    
    public static final String imagedataFKColumnDefinition = "long references imagedata(id) on delete cascade";


    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;
    
    @DatabaseField(columnName = THUMB_FIELD_NAME, canBeNull = true, dataType = DataType.BYTE_ARRAY)
    public byte[] thumbnail;

    @DatabaseField(columnName = DATA_FIELD_NAME, canBeNull = false, dataType = DataType.BYTE_ARRAY)
    public byte[] data;

    ImageData(){
    }

    public ImageData( long id ) {
        this.id = id;
    }

    public ImageData( byte[] thumbnail, byte[] data ) {
        this.thumbnail = thumbnail;
        this.data = data;
    }
}
