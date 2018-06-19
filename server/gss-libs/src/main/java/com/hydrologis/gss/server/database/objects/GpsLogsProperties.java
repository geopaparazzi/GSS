package com.hydrologis.gss.server.database.objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The gps log properties table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogsproperties")
public class GpsLogsProperties {
    public static final String ID_FIELD_NAME = "id";
    public static final String COLOR_FIELD_NAME = "color";
    public static final String WIDTH_FIELD_NAME = "width";
    public static final String GPSLOGS_FIELD_NAME = "gpslogsid";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = COLOR_FIELD_NAME, canBeNull = false)
    public String color;

    @DatabaseField(columnName = WIDTH_FIELD_NAME, canBeNull = false)
    public float width;

    @DatabaseField(columnName = GPSLOGS_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpsLogs.gpslogFKColumnDefinition)
    public GpsLogs gpsLog;

    GpsLogsProperties() {
    }

    public GpsLogsProperties( long id ) {
        this.id = id;
    }

    public GpsLogsProperties( String color, float width, GpsLogs gpsLog ) {
        this.color = color;
        this.width = width;
        this.gpsLog = gpsLog;
    }

}