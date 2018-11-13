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
package com.hydrologis.gss.server.views;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.ASqlTemplates;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.images.ImageUtilities;
import org.joda.time.DateTime;
import org.vaadin.addon.leaflet.LLayerGroup;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.LTileLayer;
import org.vaadin.addon.leaflet.LeafletMoveEndEvent;
import org.vaadin.addon.leaflet.LeafletMoveEndListener;
import org.vaadin.addon.leaflet.shared.Bounds;
import org.vaadin.addon.leaflet.shared.Point;

import com.hydrologis.gss.server.GssSession;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.GpsLogsProperties;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.kukuratus.libs.KukuratusLibs;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.database.DatabaseHandler;
import com.hydrologis.kukuratus.libs.maps.EOnlineTileSources;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.spi.SpiHandler;
import com.hydrologis.kukuratus.libs.utils.ImageSource;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class MapPage extends VerticalLayout implements View, com.hydrologis.kukuratus.libs.spi.MapPage {
    private static final long serialVersionUID = 1L;

    private LMap leafletMap;

    private HorizontalSplitPanel mainSplitPanel;
    private VerticalLayout surveyorsLayout;
    private List<GpapUsers> allDevices = new ArrayList<>();
    private Map<String, GpapUsers> deviceNamesMap = new HashMap<>();

    private Button reloadBtn;

    private Button zoomToBtn;

    private HashMap<String, LLayerGroup> userId4LoadedLayersMap = new HashMap<>();

    private Grid<GpapUsers> surveyorsGrid;

    private FileResource notesResource;

    private FileResource imagesResource;

    private DatabaseHandler dbh;

    private Long from;

    private Long to;

    private ComboBox<String> fromDateBox;

    private ComboBox<String> toDateBox;

    private Dao<Notes, ? > notesDao;

    private Dao<Images, ? > imagesDao;

    private Dao<GpsLogs, ? > logsDao;

    private Dao<GpsLogsProperties, ? > logsPropertiesDao;

    private List<String> allDates;

    @Override
    public void enter( ViewChangeEvent event ) {
        dbh = SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get();
        try {
            notesDao = dbh.getDao(Notes.class);
            imagesDao = dbh.getDao(Images.class);
            logsDao = dbh.getDao(GpsLogs.class);
            logsPropertiesDao = dbh.getDao(GpsLogsProperties.class);

            File dataFolder = KukuratusWorkspace.getInstance().getDataFolder();
            File notesFile = new File(dataFolder, "notes.png");
            File imagesFile = new File(dataFolder, "images.png");
            notesResource = new FileResource(notesFile);
            imagesResource = new FileResource(imagesFile);

            reloadDevices();

            mainSplitPanel = new HorizontalSplitPanel();
            mainSplitPanel.setSizeFull();
            mainSplitPanel.setSplitPosition(20, Unit.PERCENTAGE);

            createSurveyorsGrid();
            surveyorsLayout.setStyleName("layout-border", true);
            mainSplitPanel.setFirstComponent(surveyorsLayout);
            createMap();
            leafletMap.setStyleName("layout-border", true);
            mainSplitPanel.setSecondComponent(leafletMap);

            leafletMap.setSizeFull();
            surveyorsLayout.setSizeFull();

            addComponent(mainSplitPanel);
            setSizeFull();

            reloadSavedSurveyors();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void reloadSavedSurveyors() throws Exception {
        // loaded surveyors should be back
        String[] loadedGpapUsers = GssSession.getLoadedGpapUsers();
        if (loadedGpapUsers != null) {
            GpapUsers[] loaded = new GpapUsers[loadedGpapUsers.length];
            for( int i = 0; i < loaded.length; i++ ) {
                GpapUsers users = deviceNamesMap.get(loadedGpapUsers[i]);
                loaded[i] = users;
            }
            if (loaded.length > 0)
                surveyorsGrid.asMultiSelect().select(loaded);
        }

        allDates = getCurrentAllDates();

        fromDateBox.setItems(allDates);
        toDateBox.setItems(allDates);
    }

    private void createSurveyorsGrid() {
        surveyorsLayout = new VerticalLayout();

        reloadBtn = new Button(VaadinIcons.REFRESH);
        reloadBtn.setDescription("Refresh surveyors data.");
        reloadBtn.addClickListener(e -> {
            Set<GpapUsers> selectedUsers = surveyorsGrid.getSelectedItems();
            if (selectedUsers.size() == 0) {
                return;
            }
            HashSet<GpapUsers> selected = new HashSet<>();
            selected.addAll(selectedUsers);
            surveyorsGrid.asMultiSelect().updateSelection(new HashSet<>(), selected);
            surveyorsGrid.asMultiSelect().updateSelection(selected, new HashSet<>());
        });

        zoomToBtn = new Button(VaadinIcons.SEARCH);
        zoomToBtn.setDescription("Zoom to the whole extend of the surveyor.");
        zoomToBtn.addClickListener(e -> {
            Set<GpapUsers> selectedUsers = surveyorsGrid.getSelectedItems();
            if (selectedUsers.size() == 0) {
                return;
            }

            com.vividsolutions.jts.geom.Envelope envelope = new com.vividsolutions.jts.geom.Envelope();
            for( GpapUsers user : selectedUsers ) {
                LLayerGroup layersGroup = userId4LoadedLayersMap.get(user.deviceId);
                com.vividsolutions.jts.geom.Envelope env = layersGroup.getGeometry().getEnvelopeInternal();
                envelope.expandToInclude(env);
            }

            Bounds bounds = new Bounds();
            bounds.setNorthEastLat(envelope.getMaxY());
            bounds.setNorthEastLon(envelope.getMaxX());
            bounds.setSouthWestLat(envelope.getMinY());
            bounds.setSouthWestLon(envelope.getMinX());
            leafletMap.zoomToExtent(bounds);

        });

        boolean enable = false;
        enableSelectButtons(enable);

        Button reloadSurveyorsListBtn = new Button(VaadinIcons.FILE_REFRESH);
        reloadSurveyorsListBtn.setDescription("Reload the surveyors list.");
        reloadSurveyorsListBtn.addClickListener(e -> {
            reloadDevices();

            String[] loadedGpapUsers = GssSession.getLoadedGpapUsers();
            surveyorsGrid.setItems(allDevices);
            if (loadedGpapUsers != null) {
                GpapUsers[] loaded = new GpapUsers[loadedGpapUsers.length];
                for( int i = 0; i < loaded.length; i++ ) {
                    GpapUsers users = deviceNamesMap.get(loadedGpapUsers[i]);
                    loaded[i] = users;
                }
                surveyorsGrid.asMultiSelect().select(loaded);
            }
        });
        CssLayout btnLayout = new CssLayout(reloadBtn, zoomToBtn, reloadSurveyorsListBtn);
        btnLayout.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

//        btnLayout.setSizeFull();

        surveyorsGrid = new Grid<>(GpapUsers.class);
        surveyorsGrid.setItems(allDevices);
        surveyorsGrid.setSelectionMode(SelectionMode.MULTI);
        surveyorsGrid.setColumns();
        surveyorsGrid.addColumn(GpapUsers::getName).setCaption("Surveyors");
        surveyorsGrid.asMultiSelect().addValueChangeListener(e -> {
            Set<GpapUsers> selectedUsers = e.getValue();
            enableSelectButtons(!selectedUsers.isEmpty());

            try {
                allDates = getCurrentAllDates();
                fromDateBox.setItems(allDates);
                toDateBox.setItems(allDates);
                fromDateBox.setSelectedItem(null);
                toDateBox.setSelectedItem(null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            renderUserData();

        });
        surveyorsGrid.setSizeFull();

        surveyorsLayout.addComponent(btnLayout);
        surveyorsLayout.addComponent(surveyorsGrid);
        surveyorsLayout.setExpandRatio(surveyorsGrid, 1);

        try {

            fromDateBox = new ComboBox<>();
            fromDateBox.setPlaceholder("Start date filter");

            toDateBox = new ComboBox<>();
            toDateBox.setPlaceholder("End date filter");

            fromDateBox.setSelectedItem(null);
            toDateBox.setSelectedItem(null);

            fromDateBox.addValueChangeListener(e -> {
                String selectedDate = e.getValue();
                if (selectedDate != null) {
                    int selected = allDates.indexOf(selectedDate);
                    List<String> subList = allDates.subList(selected, allDates.size());
                    toDateBox.setItems(subList);

                    toDateBox.getSelectedItem().ifPresent(toItem -> {
                        int compareTo = toItem.compareTo(selectedDate);
                        if (compareTo < 0) {
                            toDateBox.setSelectedItem(selectedDate);
                        }
                    });

                }
                renderUserData();
            });
            toDateBox.addValueChangeListener(e -> {
                renderUserData();
            });

            fromDateBox.setWidth("100%");
            toDateBox.setWidth("100%");
            surveyorsLayout.addComponent(fromDateBox);
            surveyorsLayout.addComponent(toDateBox);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private List<String> getCurrentAllDates() throws Exception {
        Set<GpapUsers> selectedItems = surveyorsGrid.asMultiSelect().getSelectedItems();
        String whereStr = "";
        if (selectedItems.size() > 0) {
            String collect = selectedItems.stream().map(u -> String.valueOf(u.id)).collect(Collectors.joining(","));
            whereStr = "where gpapusersid in (" + collect + ")";
        }
        ASpatialDb db = dbh.getDb();

        String _whereStr = whereStr;
        return db.execOnConnection(connection -> {
            ASqlTemplates sqlTemplates = db.getType().getSqlTemplates();
            String notesTime = sqlTemplates.getFormatTimeSyntax(Notes.TIMESTAMP_FIELD_NAME, "yyyy-MM-dd");
            String imagesTime = sqlTemplates.getFormatTimeSyntax(Images.TIMESTAMP_FIELD_NAME, "yyyy-MM-dd");
            String logStartTime = sqlTemplates.getFormatTimeSyntax(GpsLogs.STARTTS_FIELD_NAME, "yyyy-MM-dd");
//            String logEndTime = sqlTemplates.getFormatTimeSyntax(GpsLogs.ENDTS_FIELD_NAME, "YYYY-mm-dd");

            String selectAllDates = "select distinct " + notesTime + " as ts from " + DatabaseHandler.getTableName(Notes.class)
                    + " " + _whereStr //
                    + " union " //
                    + "select distinct " + imagesTime + " as ts from " + DatabaseHandler.getTableName(Images.class) + " "
                    + _whereStr //
                    + " union " //
                    + "select distinct " + logStartTime + " as ts from " + DatabaseHandler.getTableName(GpsLogs.class) + " "
                    + _whereStr + " order by ts;";
            try (IHMStatement stmt = connection.createStatement(); IHMResultSet rs = stmt.executeQuery(selectAllDates)) {
                List<String> datesList = new ArrayList<>();
                while( rs.next() ) {
                    String date = rs.getString(1);
                    datesList.add(date);
                }
                return datesList;
            }
        });
    }

    private void renderUserData() {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fromValue = fromDateBox.getValue();
            if (fromValue != null) {
                String fromDate = fromValue + " 00:00:00";
                from = f.parse(fromDate).getTime();
            } else {
                from = null;
            }
            String toValue = toDateBox.getValue();
            if (toValue != null) {
                String toDate = toValue + " 23:59:00";
                to = f.parse(toDate).getTime();
            } else {
                to = null;
            }
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Set<GpapUsers> selectedUsers = surveyorsGrid.asMultiSelect().getValue();

        String[] userNames = selectedUsers.stream().map(u -> u.getName()).collect(Collectors.toList()).toArray(new String[0]);
        GssSession.setLoadedGpapUsers(userNames);

        // first remove layers
        List<Component> toRemove = new ArrayList<>();
        Iterator<Component> iterator = leafletMap.iterator();
        while( iterator.hasNext() ) {
            Component component = iterator.next();
            toRemove.add(component);
        }
        toRemove.forEach(comp -> {
            if (comp instanceof LLayerGroup) {
                LLayerGroup layer = (LLayerGroup) comp;
                leafletMap.removeComponent(layer);
            }
        });
        try {
            for( GpapUsers user : selectedUsers ) {
                LLayerGroup layersGroup = new LLayerGroup();

                addLogs(logsDao, logsPropertiesDao, user, layersGroup, from, to);
                addNotes(notesDao, user, layersGroup, from, to);
                addImages(dbh, imagesDao, user, layersGroup, from, to);

                leafletMap.addOverlay(layersGroup, user.name);

                userId4LoadedLayersMap.put(user.deviceId, layersGroup);
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    private void addLogs( Dao<GpsLogs, ? > logsDao, Dao<GpsLogsProperties, ? > logsPropertiesDao, GpapUsers user,
            LLayerGroup layer, Long from, Long to ) throws SQLException {
        Where<GpsLogs, ? > eq = logsDao.queryBuilder().where().eq(GpsLogs.GPAPUSER_FIELD_NAME, user);
        if (from != null) {
            eq = eq.and().ge(GpsLogs.STARTTS_FIELD_NAME, from);
        }
        if (to != null) {
            eq = eq.and().le(GpsLogs.ENDTS_FIELD_NAME, to);
        }

        List<GpsLogs> gpsLogs = eq.query();
        if (gpsLogs.size() > 0) {
            for( GpsLogs log : gpsLogs ) {
                GpsLogsProperties props = logsPropertiesDao.queryBuilder().where().eq(GpsLogsProperties.GPSLOGS_FIELD_NAME, log)
                        .queryForFirst();
                LPolyline leafletPolyline = new LPolyline(new KukuratusLibs().toOldJtsLinestring(log.the_geom));
                leafletPolyline.setColor(props.color);
                leafletPolyline.setClickable(true);
                leafletPolyline.setWeight((int) props.width);
                leafletPolyline.setPopup(getLogsHtml(log));

                layer.addComponent(leafletPolyline);
            }
        }
    }

    private void addImages( DatabaseHandler dbh, Dao<Images, ? > imagesDao, GpapUsers user, LLayerGroup layer, Long from,
            Long to ) throws SQLException {
        Where<Images, ? > eq = imagesDao.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user);
        if (from != null) {
            eq = eq.and().ge(Images.TIMESTAMP_FIELD_NAME, from);
        }
        if (to != null) {
            eq = eq.and().le(Images.TIMESTAMP_FIELD_NAME, to);
        }

        List<Images> imagesList = eq.query();
        if (imagesList.size() > 0) {
            for( Images image : imagesList ) {
                LMarker leafletMarker = new LMarker(new KukuratusLibs().toOldJtsPoint(image.the_geom));
                leafletMarker.setIcon(imagesResource);
                int iconSize = 36;
                leafletMarker.setIconSize(new Point(iconSize, iconSize));
                leafletMarker.setIconAnchor(new Point(iconSize, iconSize / 2));
//                            leafletMarker.addClickListener(listener);
                leafletMarker.setTitle(image.text);
                leafletMarker.setData(image.imageData);
                leafletMarker.addClickListener(ev -> {
                    try {
                        Object source = ev.getSource();
                        if (source instanceof LMarker) {
                            LMarker marker = (LMarker) source;
                            ImageData imageDataTmp = (ImageData) marker.getData();
                            Dao<ImageData, ? > imageDao = dbh.getDao(ImageData.class);
                            ImageData imageData = imageDao.queryForSameId(imageDataTmp);

                            String title = image.text + " ( " + formatDate(image.timestamp) + " )";
                            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData.data));
                            BufferedImage scaleImage = ImageUtilities.scaleImage(bufferedImage, 1000);

                            ImageSource imageSource = new ImageSource(scaleImage, title);
                            imageSource.openAsWindow();

                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                layer.addComponent(leafletMarker);
            }
        }
    }

    private void addNotes( Dao<Notes, ? > notesDao, GpapUsers user, LLayerGroup layer, Long from, Long to ) throws SQLException {
        Where<Notes, ? > eq = notesDao.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user);
        if (from != null) {
            eq = eq.and().ge(Notes.TIMESTAMP_FIELD_NAME, from);
        }
        if (to != null) {
            eq = eq.and().le(Notes.TIMESTAMP_FIELD_NAME, to);
        }

        List<Notes> notesList = eq.query();
        if (notesList.size() > 0) {
            for( Notes note : notesList ) {
                LMarker leafletMarker = new LMarker(new KukuratusLibs().toOldJtsPoint(note.the_geom));
                leafletMarker.setIcon(notesResource);
                int iconSize = 36;
                leafletMarker.setIconSize(new Point(iconSize, iconSize));
                leafletMarker.setIconAnchor(new Point(iconSize, iconSize / 2));
//                            leafletMarker.addClickListener(listener);
                leafletMarker.setTitle(note.text);
                leafletMarker.setPopup(getNotesHtml(note));
                leafletMarker.setPopupAnchor(new Point(-iconSize / 2, -iconSize / 2));

                layer.addComponent(leafletMarker);
            }
        }
    }

    private String getNotesHtml( Notes note ) {
        StringBuilder sb = new StringBuilder("<h2>" + note.text + "</h2>");
        sb.append("<table style='width:100%' border='0' cellpadding='5'>");
        sb.append("<tr><td><b>Timestamp</b></td><td>").append(formatDate(note.timestamp)).append("</td></tr>");
        sb.append("<tr><td><b>Elevation</b></td><td>").append(note.altimetry).append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private String getLogsHtml( GpsLogs log ) {
        StringBuilder sb = new StringBuilder("<h2>" + log.name + "</h2>");
        sb.append("<table style='width:100%' border='0' cellpadding='5'>");
        sb.append("<tr><td><b>Start timestamp</b></td><td>").append(formatDate(log.startTs)).append("</td></tr>");
        sb.append("<tr><td><b>End timestamp</b></td><td>").append(formatDate(log.endTs)).append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private String formatDate( Object dateLong ) {
        return new DateTime(Long.parseLong(dateLong.toString())).toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS);
    }

    private void enableSelectButtons( boolean enable ) {
        reloadBtn.setEnabled(enable);
        zoomToBtn.setEnabled(enable);
    }

    private void reloadDevices() {
        try {
            allDevices.clear();
            allDevices.addAll(SpiHandler.INSTANCE.getDbProviderSingleton().getDatabaseHandler().get().getDao(GpapUsers.class)
                    .queryForAll());
            deviceNamesMap.clear();
            deviceNamesMap.putAll(allDevices.stream().collect(Collectors.toMap(gu -> getUserName(gu), Function.identity())));
        } catch (SQLException e1) {
            KukuratusLogger.logError(this, e1);
        }
    }

    private void createMap() {
        leafletMap = new LMap();

        double[] lastMapPosition = GssSession.getLastMapPosition();
        if (lastMapPosition != null) {
            leafletMap.setView(lastMapPosition[1], lastMapPosition[0], lastMapPosition[2]);
        } else {
            leafletMap.setCenter(41.8919, 12.5113);
            leafletMap.setZoomLevel(15);
        }
        List<String> selectedTileSourcesNames = RegistryHandler.INSTANCE
                .getSelectedTileSourcesNames(AuthService.INSTANCE.getAuthenticatedUsername());
        for( String name : selectedTileSourcesNames ) {
            LTileLayer tmpLayer;
            if (name.equals(RegistryHandler.MAPSFORGE)) {
                tmpLayer = new LTileLayer();
                tmpLayer.setUrl("./mapsforge?z={z}&x={x}&y={y}");
                tmpLayer.setAttributionString("Mapsforge");
                tmpLayer.setMaxZoom(22);
            } else {
                EOnlineTileSources source = EOnlineTileSources.getByName(name);
                tmpLayer = new LTileLayer();
                tmpLayer.setUrl(source.getUrl());
                tmpLayer.setAttributionString(source.getAttribution());
                tmpLayer.setMaxZoom(Integer.parseInt(source.getMaxZoom()));
                tmpLayer.setDetectRetina(true);
            }
            leafletMap.addBaseLayer(tmpLayer, name);
        }

        leafletMap.addMoveEndListener(new LeafletMoveEndListener(){
            @Override
            public void onMoveEnd( LeafletMoveEndEvent event ) {
                Point center = event.getCenter();
                Double lat = center.getLat();
                Double lon = center.getLon();
                double zoomLevel = event.getZoomLevel();
                GssSession.setLastMapPosition(new double[]{lon, lat, zoomLevel});
            }
        });

    }

    private String getUserName( GpapUsers user ) {
        return user.name;
    }

    @Override
    public VaadinIcons getIcon() {
        return VaadinIcons.MAP_MARKER;
    }

    @Override
    public String getLabel() {
        return "Map View";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends View> Class<T> getNavigationViewClass() {
        return (Class<T>) this.getClass();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public boolean onlyAdmin() {
        return false;
    }
}
