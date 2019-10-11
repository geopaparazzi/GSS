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
import org.locationtech.jts.geom.Point;

import com.hydrologis.kukuratus.database.ISpatialTable;
import com.hydrologis.kukuratus.database.ormlite.KukuratusPointType;
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
    public static final String AZIMUTH_FIELD_NAME = "azimuth"; //$NON-NLS-1$
    public static final String TEXT_FIELD_NAME = "text"; //$NON-NLS-1$
    public static final String NOTE_FIELD_NAME = "notesid"; //$NON-NLS-1$
    public static final String IMAGEDATA_FIELD_NAME = "imagedataid"; //$NON-NLS-1$
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid"; //$NON-NLS-1$

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = KukuratusPointType.class)
    public Point the_geom;

    @DatabaseField(columnName = ALTIM_FIELD_NAME, canBeNull = false)
    public double altimetry;

    @DatabaseField(columnName = TIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long timestamp;

    @DatabaseField(columnName = AZIMUTH_FIELD_NAME, canBeNull = false)
    public double azimuth;

    @DatabaseField(columnName = TEXT_FIELD_NAME, canBeNull = false)
    public String text;

    @DatabaseField(columnName = NOTE_FIELD_NAME, index = true, foreign = true, canBeNull = true, columnDefinition = Notes.notesFKColumnDefinition)
    public Notes notes;

    @DatabaseField(columnName = IMAGEDATA_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = ImageData.imagedataFKColumnDefinition)
    public ImageData imageData;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    Images() {
    }

    public Images( long id ) {
        this.id = id;
    }

    public Images( Point the_geom, double altimetry, long timestamp, double azimuth, String text, Notes notes,
            ImageData imageData, GpapUsers gpapUser ) {
        this.the_geom = the_geom;
        this.altimetry = altimetry;
        this.timestamp = timestamp;
        this.azimuth = azimuth;
        this.text = text;
        this.notes = notes;
        this.imageData = imageData;
        this.gpapUser = gpapUser;
        the_geom.setSRID(ISpatialTable.SRID);
    }

}
