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
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import java.util.Base64;

import com.hydrologis.kukuratus.database.ISpatialTable;
import com.hydrologis.kukuratus.database.ormlite.KukuratusPointType;
import com.hydrologis.kukuratus.gss.GssDatabaseUtilities;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The notes class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "images")
public class Images implements ISpatialTable {
    public static final String ID_FIELD_NAME = "id"; //$NON-NLS-1$
    public static final String ALTIM_FIELD_NAME = "altim"; //$NON-NLS-1$
    public static final String TIMESTAMP_FIELD_NAME = "ts"; //$NON-NLS-1$
    public static final String UPLOADTIMESTAMP_FIELD_NAME = "uploadts"; //$NON-NLS-1$
    public static final String AZIMUTH_FIELD_NAME = "azimuth"; //$NON-NLS-1$
    public static final String TEXT_FIELD_NAME = "text"; //$NON-NLS-1$
    public static final String NOTE_FIELD_NAME = "notesid"; //$NON-NLS-1$
    public static final String IMAGEDATA_FIELD_NAME = "imagedataid"; //$NON-NLS-1$
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid"; //$NON-NLS-1$
    public static final String GPAPPROJECT_FIELD_NAME = "gpapprojectid"; //$NON-NLS-1$
    public static final String THUMB_FIELD_NAME = "thumbnail"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = KukuratusPointType.class)
    public Point the_geom;

    @DatabaseField(columnName = ALTIM_FIELD_NAME, canBeNull = false)
    public double altimetry;

    @DatabaseField(columnName = TIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long timestamp;

    @DatabaseField(columnName = UPLOADTIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long uploadTimestamp;

    @DatabaseField(columnName = AZIMUTH_FIELD_NAME, canBeNull = false)
    public double azimuth;

    @DatabaseField(columnName = TEXT_FIELD_NAME, canBeNull = false)
    public String text;

    @DatabaseField(columnName = NOTE_FIELD_NAME, index = true, foreign = true, canBeNull = true, columnDefinition = Notes.notesFKColumnDefinition)
    public Notes notes;

    @DatabaseField(columnName = IMAGEDATA_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = ImageData.imagedataFKColumnDefinition)
    public ImageData imageData;

    @DatabaseField(columnName = THUMB_FIELD_NAME, canBeNull = true, dataType = DataType.BYTE_ARRAY)
    public byte[] thumbnail;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    @DatabaseField(columnName = GPAPPROJECT_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapProject.projectsFKColumnDefinition)
    public GpapProject gpapProject;

    Images() {
    }

    public Images( long id ) {
        this.id = id;
    }

    public Images( Point the_geom, double altimetry, long timestamp, double azimuth, String text, Notes notes,
            ImageData imageData, GpapUsers gpapUser, byte[] thumbnail, GpapProject gpapProject, long uploadTimestamp ) {
        this.the_geom = the_geom;
        this.altimetry = altimetry;
        this.timestamp = timestamp;
        this.azimuth = azimuth;
        this.text = text;
        this.notes = notes;
        this.imageData = imageData;
        this.gpapUser = gpapUser;
        this.gpapProject = gpapProject;
        this.thumbnail = thumbnail;
        this.uploadTimestamp = uploadTimestamp;
        the_geom.setSRID(ISpatialTable.SRID);
    }


    public JSONObject toJson() {
        JSONObject imageObject = new JSONObject();
        imageObject.put(ID_FIELD_NAME, id);
        Coordinate c = the_geom.getCoordinate();
        imageObject.put(GssDatabaseUtilities.X, c.x);
        imageObject.put(GssDatabaseUtilities.Y, c.y);
        imageObject.put(TIMESTAMP_FIELD_NAME, timestamp);
        imageObject.put(GssDatabaseUtilities.NAME, text);
        imageObject.put(GssDatabaseUtilities.DATAID, imageData.id);
        byte[] thumbBytes = thumbnail;
        String encodedThumb = Base64.getEncoder().encodeToString(thumbBytes);
        imageObject.put(GssDatabaseUtilities.DATA, encodedThumb);
        imageObject.put(GssDatabaseUtilities.PROJECT, gpapProject.getName());
        imageObject.put(GssDatabaseUtilities.SURVEYOR, gpapUser.getName());
        return imageObject;
    }
}
