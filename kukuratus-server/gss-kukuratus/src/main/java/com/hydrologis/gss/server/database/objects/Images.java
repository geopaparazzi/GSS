/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.server.database.objects;
import com.hydrologis.kukuratus.libs.database.ISpatialTable;
import com.hydrologis.kukuratus.libs.database.ormlite.PointTypeH2GIS;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Point;

/**
 * The notes class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "images")
public class Images implements ISpatialTable {
    public static final String ID_FIELD_NAME = "id";
    public static final String ALTIM_FIELD_NAME = "altim";
    public static final String TIMESTAMP_FIELD_NAME = "ts";
    public static final String AZIMUTH_FIELD_NAME = "azimuth";
    public static final String TEXT_FIELD_NAME = "text";
    public static final String NOTE_FIELD_NAME = "notesid";
    public static final String IMAGEDATA_FIELD_NAME = "imagedataid";
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = PointTypeH2GIS.class)
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
