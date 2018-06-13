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