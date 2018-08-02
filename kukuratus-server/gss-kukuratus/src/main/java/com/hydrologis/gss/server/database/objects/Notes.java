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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hortonmachine.gears.io.geopaparazzi.forms.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hydrologis.gss.server.GssDbProvider;
import com.hydrologis.gss.server.database.GssDatabaseHandler;
import com.hydrologis.kukuratus.libs.database.ISpatialTable;
import com.hydrologis.kukuratus.libs.database.ormlite.PointTypeH2GIS;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.utils.export.KmlRepresenter;
import com.j256.ormlite.dao.Dao;
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
public class Notes implements ISpatialTable, KmlRepresenter {
    public static final String ID_FIELD_NAME = "id";
    public static final String ALTIM_FIELD_NAME = "altim";
    public static final String TIMESTAMP_FIELD_NAME = "ts";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String TEXT_FIELD_NAME = "text";
    public static final String FORM_FIELD_NAME = "form";
    public static final String STYLE_FIELD_NAME = "style";
    public static final String GPAPUSER_FIELD_NAME = "gpapusersid";

    public static final String IMAGES_SEPARATOR = ";";

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

    private List<String> images;

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

    public String toKmlString() throws Exception {
        GssDatabaseHandler dbHandler = GssDbProvider.INSTANCE.getDatabaseHandler().get();
        Dao<Images, ? > imagesDAO = dbHandler.getDao(Images.class);

        images = new ArrayList<>();
        String name = makeXmlSafe(text);
        StringBuilder sB = new StringBuilder();
        sB.append("<Placemark>\n");
        // sB.append("<styleUrl>#red-pushpin</styleUrl>\n");
        sB.append("<styleUrl>#info-icon</styleUrl>\n");
        sB.append("<name>").append(name).append("</name>\n");
        sB.append("<description>\n");

        if (form != null && form.length() > 0) {

            sB.append("<![CDATA[\n");
            JSONObject sectionObject = new JSONObject(form);
            if (sectionObject.has(Utilities.ATTR_SECTIONNAME)) {
                String sectionName = sectionObject.getString(Utilities.ATTR_SECTIONNAME);
                sB.append("<h1>").append(sectionName).append("</h1>\n");
            }

            List<String> formsNames = Utilities.getFormNames4Section(sectionObject);
            for( String formName : formsNames ) {
                sB.append("<h2>").append(formName).append("</h2>\n");

                sB.append("<table style=\"text-align: left; width: 100%;\" border=\"1\" cellpadding=\"5\" cellspacing=\"2\">");
                sB.append("<tbody>");

                JSONObject form4Name = Utilities.getForm4Name(formName, sectionObject);
                JSONArray formItems = Utilities.getFormItems(form4Name);
                for( int i = 0; i < formItems.length(); i++ ) {
                    JSONObject formItem = formItems.getJSONObject(i);
                    if (!formItem.has(Utilities.TAG_KEY)) {
                        continue;
                    }

                    String type = formItem.getString(Utilities.TAG_TYPE);
                    String key = formItem.getString(Utilities.TAG_KEY);
                    String value = formItem.getString(Utilities.TAG_VALUE);

                    String label = key;
                    if (formItem.has(Utilities.TAG_LABEL)) {
                        label = formItem.getString(Utilities.TAG_LABEL);
                    }

                    if (type.equals(Utilities.TYPE_PICTURES)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(IMAGES_SEPARATOR);
                        for( String imageId : imageIdsSplit ) {

                            Images image = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                            String imgName = image.text;
                            sB.append("<tr>");
                            sB.append("<td colspan=\"2\" style=\"text-align: left; vertical-align: top; width: 100%;\">");
                            sB.append("<img src=\"").append(imgName).append("\" width=\"300\">");
                            sB.append("</td>");
                            sB.append("</tr>");

                            images.add(imageId);
                        }
                    } else if (type.equals(Utilities.TYPE_MAP)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        sB.append("<tr>");
                        // FIXME
                        String imageId = value.trim();
                        Images image = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                        String imgName = image.text;
                        sB.append("<td colspan=\"2\" style=\"text-align: left; vertical-align: top; width: 100%;\">");
                        sB.append("<img src=\"").append(imgName).append("\" width=\"300\">");
                        sB.append("</td>");
                        sB.append("</tr>");
                        images.add(imageId);
                    } else if (type.equals(Utilities.TYPE_SKETCH)) {
                        if (value.trim().length() == 0) {
                            continue;
                        }
                        String[] imageIdsSplit = value.split(IMAGES_SEPARATOR);
                        for( String imageId : imageIdsSplit ) {
                            Images image = imagesDAO.queryForSameId(new Images(Long.parseLong(imageId)));
                            String imgName = image.text;
                            sB.append("<tr>");
                            sB.append("<td colspan=\"2\" style=\"text-align: left; vertical-align: top; width: 100%;\">");
                            sB.append("<img src=\"").append(imgName).append("\" width=\"300\">");
                            sB.append("</td>");
                            sB.append("</tr>");

                            images.add(imageId);
                        }
                    } else {
                        sB.append("<tr>");
                        sB.append("<td style=\"text-align: left; vertical-align: top; width: 50%;\">");
                        sB.append(label);
                        sB.append("</td>");
                        sB.append("<td style=\"text-align: left; vertical-align: top; width: 50%;\">");
                        sB.append(value);
                        sB.append("</td>");
                        sB.append("</tr>");
                    }
                }
                sB.append("</tbody>");
                sB.append("</table>");
            }
            sB.append("]]>\n");
        } else {
            String description = makeXmlSafe(this.description);
            sB.append(description);
            sB.append("\n");
            sB.append(new Date(timestamp));
        }

        sB.append("</description>\n");
        sB.append("<gx:balloonVisibility>1</gx:balloonVisibility>\n");
        sB.append("<Point>\n");
        sB.append("<coordinates>").append(the_geom.getX()).append(",").append(the_geom.getY()).append(",0</coordinates>\n");
        sB.append("</Point>\n");
        sB.append("</Placemark>\n");

        return sB.toString();
    }

    public boolean hasImages() {
        return images != null && images.size() > 0;
    }

    public List<String> getImageIds() {
        if (images == null) {
            try {
                images = Utilities.getImageIds(form);
            } catch (Exception e) {
                KukuratusLogger.logError(this, e);
            }
        }
        if (images == null) {
            return Collections.emptyList();
        }
        return images;
    }

}
