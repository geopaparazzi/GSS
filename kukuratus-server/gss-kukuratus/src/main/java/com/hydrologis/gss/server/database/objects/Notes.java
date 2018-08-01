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
import java.util.Date;

import com.hydrologis.kukuratus.libs.database.ISpatialTable;
import com.hydrologis.kukuratus.libs.database.ormlite.PointTypeH2GIS;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Point;

/**
 * The notes class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "notes")
public class Notes implements ISpatialTable {
    public static final String ID_FIELD_NAME = "id";
    public static final String ALTIM_FIELD_NAME = "altim";
    public static final String TIMESTAMP_FIELD_NAME = "ts";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String TEXT_FIELD_NAME = "text";
    public static final String FORM_FIELD_NAME = "form";
    public static final String STYLE_FIELD_NAME = "style";
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid";

    public static final String notesFKColumnDefinition = "long references notes(id) on delete cascade";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    public long id;

    @DatabaseField(columnName = GEOM_FIELD_NAME, canBeNull = false, persisterClass = PointTypeH2GIS.class)
    public Point the_geom;

    @DatabaseField(columnName = ALTIM_FIELD_NAME, canBeNull = false)
    public double altimetry;

    @DatabaseField(columnName = TIMESTAMP_FIELD_NAME, canBeNull = false, index = true)
    public long timestamp;

    @DatabaseField(columnName = DESCRIPTION_FIELD_NAME, canBeNull = false)
    public String description;

    @DatabaseField(columnName = TEXT_FIELD_NAME, canBeNull = false)
    public String text;

    @DatabaseField(columnName = FORM_FIELD_NAME, canBeNull = true, dataType = DataType.LONG_STRING)
    public String form;

    @DatabaseField(columnName = STYLE_FIELD_NAME, canBeNull = true)
    public String style;

    @DatabaseField(columnName = GPAPUSER_FIELD_NAME, foreign = true, canBeNull = false, index = true, columnDefinition = GpapUsers.usersFKColumnDefinition)
    public GpapUsers gpapUser;

    Notes() {
    }

    public Notes( long id ) {
        this.id = id;
    }

    public Notes( Point the_geom, double altimetry, long timestamp, String description, String text, String form, String style,
            GpapUsers gpapUser ) {
        super();
        this.the_geom = the_geom;
        this.altimetry = altimetry;
        this.timestamp = timestamp;
        this.description = description;
        this.text = text;
        this.form = form;
        this.style = style;
        this.gpapUser = gpapUser;
        the_geom.setSRID(ISpatialTable.SRID);
    }

    public Notes( Point the_geom, double altimetry, Date timestamp, String description, String text, String form, String style,
            GpapUsers gpapUser ) {
        super();
        this.the_geom = the_geom;
        this.altimetry = altimetry;
        this.timestamp = timestamp.getTime();
        this.description = description;
        this.text = text;
        this.form = form;
        this.style = style;
        this.gpapUser = gpapUser;
    }

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public Point getThe_geom() {
        return the_geom;
    }

    public void setThe_geom( Point the_geom ) {
        this.the_geom = the_geom;
    }

    public double getAltimetry() {
        return altimetry;
    }

    public void setAltimetry( double altimetry ) {
        this.altimetry = altimetry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp( long timestamp ) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }

    public String getForm() {
        return form;
    }

    public void setForm( String form ) {
        this.form = form;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle( String style ) {
        this.style = style;
    }

    public GpapUsers getGpapUser() {
        return gpapUser;
    }

    public void setGpapUser( GpapUsers gpapUser ) {
        this.gpapUser = gpapUser;
    }

}
