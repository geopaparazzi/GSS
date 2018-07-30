package com.hydrologis.gss.server.views;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.SQLException;
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

import com.hydrologis.gss.server.GssDbProvider;
import com.hydrologis.gss.server.GssSession;
import com.hydrologis.gss.server.database.GssDatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.GpsLogsProperties;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.kukuratus.libs.auth.AuthService;
import com.hydrologis.kukuratus.libs.maps.EOnlineTileSources;
import com.hydrologis.kukuratus.libs.registry.RegistryHandler;
import com.hydrologis.kukuratus.libs.utils.ImageSource;
import com.hydrologis.kukuratus.libs.utils.KukuratusLogger;
import com.hydrologis.kukuratus.libs.workspace.KukuratusWorkspace;
import com.j256.ormlite.dao.Dao;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vividsolutions.jts.geom.Envelope;

public class MapPage extends VerticalLayout implements View {
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

    @Override
    public void enter( ViewChangeEvent event ) {

        File dataFolder = KukuratusWorkspace.getInstance().getDataFolder(null).get();
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
    }

    private void reloadSavedSurveyors() {
        // loaded surveyors should be back
        String[] loadedGpapUsers = GssSession.getLoadedGpapUsers();
        if (loadedGpapUsers != null) {
            GpapUsers[] loaded = new GpapUsers[loadedGpapUsers.length];
            for( int i = 0; i < loaded.length; i++ ) {
                GpapUsers users = deviceNamesMap.get(loadedGpapUsers[i]);
                loaded[i] = users;
            }
            surveyorsGrid.asMultiSelect().select(loaded);
        }
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
            surveyorsGrid.asMultiSelect().updateSelection(new HashSet<>(), selectedUsers);
            surveyorsGrid.asMultiSelect().updateSelection(selectedUsers, new HashSet<>());
        });

        zoomToBtn = new Button(VaadinIcons.SEARCH);
        zoomToBtn.setDescription("Zoom to the whole extend of the surveyor.");
        zoomToBtn.addClickListener(e -> {
            Set<GpapUsers> selectedUsers = surveyorsGrid.getSelectedItems();
            if (selectedUsers.size() == 0) {
                return;
            }

            Envelope envelope = new Envelope();
            for( GpapUsers user : selectedUsers ) {
                LLayerGroup layersGroup = userId4LoadedLayersMap.get(user.deviceId);
                Envelope env = layersGroup.getGeometry().getEnvelopeInternal();
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

            String[] userNames = selectedUsers.stream().map(u -> u.getName()).collect(Collectors.toList()).toArray(new String[0]);
            GssSession.setLoadedGpapUsers(userNames);

            GssDatabaseHandler dbh = GssDbProvider.INSTANCE.getDatabaseHandler().get();

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
                Dao<Notes, ? > notesDao = dbh.getDao(Notes.class);
                Dao<Images, ? > imagesDao = dbh.getDao(Images.class);
                Dao<GpsLogs, ? > logsDao = dbh.getDao(GpsLogs.class);
                Dao<GpsLogsProperties, ? > logsPropertiesDao = dbh.getDao(GpsLogsProperties.class);

                for( GpapUsers user : selectedUsers ) {
                    LLayerGroup layersGroup = new LLayerGroup();

                    addLogs(logsDao, logsPropertiesDao, user, layersGroup);
                    addNotes(notesDao, user, layersGroup);
                    addImages(dbh, imagesDao, user, layersGroup);

                    leafletMap.addOverlay(layersGroup, user.name);

                    userId4LoadedLayersMap.put(user.deviceId, layersGroup);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

        });
        surveyorsGrid.setSizeFull();

        surveyorsLayout.addComponent(btnLayout);
        surveyorsLayout.addComponent(surveyorsGrid);
        surveyorsLayout.setExpandRatio(surveyorsGrid, 1);

    }

    private void addLogs( Dao<GpsLogs, ? > logsDao, Dao<GpsLogsProperties, ? > logsPropertiesDao, GpapUsers user,
            LLayerGroup layer ) throws SQLException {
        List<GpsLogs> gpsLogs = logsDao.queryBuilder().where().eq(GpsLogs.GPAPUSER_FIELD_NAME, user).query();
        if (gpsLogs.size() > 0) {
            for( GpsLogs log : gpsLogs ) {
                GpsLogsProperties props = logsPropertiesDao.queryBuilder().where().eq(GpsLogsProperties.GPSLOGS_FIELD_NAME, log)
                        .queryForFirst();
                LPolyline leafletPolyline = new LPolyline(log.the_geom);
                leafletPolyline.setColor(props.color);
                leafletPolyline.setClickable(true);
                leafletPolyline.setWeight((int) props.width);
                leafletPolyline.setPopup(getLogsHtml(log));

                layer.addComponent(leafletPolyline);
            }
        }
    }

    private void addImages( GssDatabaseHandler dbh, Dao<Images, ? > imagesDao, GpapUsers user, LLayerGroup layer )
            throws SQLException {
        List<Images> imagesList = imagesDao.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user).query();
        if (imagesList.size() > 0) {
            for( Images image : imagesList ) {
                LMarker leafletMarker = new LMarker(image.the_geom);
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

    private void addNotes( Dao<Notes, ? > notesDao, GpapUsers user, LLayerGroup layer ) throws SQLException {
        List<Notes> notesList = notesDao.queryBuilder().where().eq(Notes.GPAPUSER_FIELD_NAME, user).query();
        if (notesList.size() > 0) {
            for( Notes note : notesList ) {
                LMarker leafletMarker = new LMarker(note.the_geom);
                leafletMarker.setIcon(notesResource);
                int iconSize = 36;
                leafletMarker.setIconSize(new Point(iconSize, iconSize));
                leafletMarker.setIconAnchor(new Point(iconSize, iconSize / 2));
//                            leafletMarker.addClickListener(listener);
                leafletMarker.setTitle(note.text);
                leafletMarker.setPopup(getNotesHtml(note));
                leafletMarker.setPopupAnchor(new Point(iconSize / 2, iconSize / 2));

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
            allDevices.addAll(GssDbProvider.INSTANCE.getDatabaseHandler().get().getDao(GpapUsers.class).queryForAll());
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
}
