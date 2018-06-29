/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package com.hydrologis.gss.entrypoints;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.images.ImageUtilities;
import org.joda.time.DateTime;

import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.GssDbProvider;
import com.hydrologis.gss.GssSession;
import com.hydrologis.gss.map.GssMapBrowser;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.server.database.objects.GpsLogs;
import com.hydrologis.gss.server.database.objects.ImageData;
import com.hydrologis.gss.server.database.objects.Images;
import com.hydrologis.gss.server.database.objects.Notes;
import com.hydrologis.gss.utils.DevicesTableContentProvider;
import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.hydrologis.gss.utils.ImageCache;
import com.j256.ormlite.dao.Dao;

import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.html.HtmlFeatureChooser;
import eu.hydrologis.stage.libs.images.ImageDialog;
import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.map.IMapObserver;
import eu.hydrologis.stage.libs.map.MapsHandler;
import eu.hydrologis.stage.libs.providers.data.SpatialDbDataProvider;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.StageUtils;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class MapviewerEntryPoint extends StageEntryPoint implements IMapObserver, ProgressListener {
    public static final String ID = "com.hydrologis.gss.entrypoints.MapviewerEntryPoint";

    private static final String[] FIELDS_FOR_LOGS = new String[]{GpsLogs.NAME_FIELD_NAME, GpsLogs.STARTTS_FIELD_NAME,
            GpsLogs.ENDTS_FIELD_NAME, GpsLogs.ID_FIELD_NAME};
    private static final String[] FIELDS_FOR_NOTES = new String[]{Notes.TEXT_FIELD_NAME, Notes.ALTIM_FIELD_NAME,
            Notes.TIMESTAMP_FIELD_NAME};
    private static final String[] FIELDS_FOR_IMAGES = new String[]{Images.IMAGEDATA_FIELD_NAME, Images.TIMESTAMP_FIELD_NAME,
            Images.TEXT_FIELD_NAME};
    public static final String NOTES = "Notes";
    public static final String LOGS = "Gps Logs";
    public static final String IMAGES = "Media";

    private static final long serialVersionUID = 1L;

    private Display display;

    private Shell parentShell;
    private GssMapBrowser mapBrowser;
    private Composite mapComposite;

    private SashForm mainSashComposite;

    private Combo devicesCombo;

    private DatabaseHandler databaseHandler;

    private TableViewer devicesTableViewer;

    private List<GpapUsers> visibleDevices = new ArrayList<>();

    private GssDbProvider dbp;
    private List<GpapUsers> allDevices = new ArrayList<>();
    private Map<String, GpapUsers> deviceNamesMap = new HashMap<>();

    @Override
    protected void createPage( final Composite parent ) {

        eu.hydrologis.stage.libs.registry.User loggedUser = GssLoginDialog.checkUserLogin(getShell());
        boolean isAdmin = RegistryHandler.INSTANCE.isAdmin(loggedUser);
        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(isAdmin);
        if (loggedUser == null || !permissionsHandler.isAllowed(redirectUrl)) {
            StageUtils.permissionDeniedPage(parent, redirectUrl);
            return;
        }

        String name = loggedUser.getName();
        String uniquename = loggedUser.getUniqueName();
        StageUtils.logAccessIp(redirectUrl, "user:" + uniquename);

        parentShell = parent.getShell();
        display = parent.getDisplay();

        dbp = GssContext.instance().getDbProvider();
        databaseHandler = dbp.getDatabaseHandler();
        reloadDevices();

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));

        final Composite horizToolbarComposite = new Composite(composite, SWT.NONE);
        GridData horizToolbarGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        horizToolbarGD.horizontalSpan = 2;
        horizToolbarComposite.setLayoutData(horizToolbarGD);
        GridLayout toolbarLayout = new GridLayout(7, false);
        horizToolbarComposite.setLayout(toolbarLayout);

        final Composite vertToolbarComposite = new Composite(composite, SWT.NONE);
        vertToolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout vertToolbarLayout = new GridLayout(1, false);
        vertToolbarComposite.setLayout(vertToolbarLayout);

        GssGuiUtilities.addLogo(horizToolbarComposite);
        GssGuiUtilities.addPagesLinks(vertToolbarComposite, this, isAdmin);

        ToolBar toolsToolBar = new ToolBar(horizToolbarComposite, SWT.FLAT);

        final ToolItem devicesItem = new ToolItem(toolsToolBar, SWT.CHECK);
        devicesItem.setImage(ImageCache.getInstance().getImage(display, ImageCache.GROUP));
        devicesItem.setWidth(300);
        devicesItem.setText("      Surveyors      ");
        devicesItem.setToolTipText("Toggle the surveyors view to add and remove data to visualize.");
        devicesItem.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if (devicesItem.getSelection()) {
                    mainSashComposite.setMaximizedControl(null);
                } else {
                    mainSashComposite.setMaximizedControl(mapComposite);
                }
                GssSession.setDevicesVisible(devicesItem.getSelection());
            }
        });
        GssGuiUtilities.addToolBarSeparator(toolsToolBar);
        final ToolItem zoomToAllItem = new ToolItem(toolsToolBar, SWT.PUSH);
        zoomToAllItem.setImage(eu.hydrologis.stage.libs.utils.ImageCache.getInstance().getImage(display,
                eu.hydrologis.stage.libs.utils.ImageCache.ZOOM_TO_ALL));
        zoomToAllItem.setWidth(300);
        zoomToAllItem.setText("Zoom to all");
        zoomToAllItem.setToolTipText("Zoom to all teh current data.");
        zoomToAllItem.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                mapBrowser.zoomToAll();
            }
        });

        GssGuiUtilities.addVerticalFiller(vertToolbarComposite);
        GssGuiUtilities.addHorizontalFiller(horizToolbarComposite);
        GssGuiUtilities.addAdminTools(vertToolbarComposite, this, isAdmin);
        GssGuiUtilities.addLogoutButton(horizToolbarComposite);

        // SASH FOR MAP + FEATURE PROPERTIES
        mainSashComposite = new SashForm(composite, SWT.HORIZONTAL);
        GridData mainCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainSashComposite.setLayoutData(mainCompositeGD);

        Composite devicesComposite = new Composite(mainSashComposite, SWT.None);
        GridLayout attributesLayout = new GridLayout(1, true);
        attributesLayout.marginWidth = 0;
        attributesLayout.marginHeight = 0;
        devicesComposite.setLayout(attributesLayout);
        GridData attributesGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        devicesComposite.setLayoutData(attributesGD);

        // Button closeViewButton = new Button(featurePropertiesComposite, SWT.PUSH | SWT.FLAT);
        // GridData closeViewButtonGD = new GridData(SWT.END, SWT.FILL, false, false);
        // closeViewButton.setLayoutData(closeViewButtonGD);
        // Image closeViewImage = ImageCache.getInstance().getImage(display, ImageCache.CLOSE_VIEW);
        // closeViewButton.setImage(closeViewImage);
        // closeViewButton.setToolTipText("Close properties view");
        // closeViewButton.addSelectionListener(new SelectionAdapter(){
        // @Override
        // public void widgetSelected( SelectionEvent e ) {
        // mainSashComposite.setMaximizedControl(mapAndAttributesSashComposite);
        // }
        // });

        try {
            createDevicesCombo(devicesComposite);

            mapComposite = new Composite(mainSashComposite, SWT.None);
            GridLayout mapLayout = new GridLayout(1, true);
            mapLayout.marginWidth = 0;
            mapLayout.marginHeight = 0;
            mapComposite.setLayout(mapLayout);
            GridData mapGD = new GridData(GridData.FILL, GridData.FILL, true, true);
            mapComposite.setLayoutData(mapGD);

            mapBrowser = new GssMapBrowser(mapComposite, SWT.BORDER);
            GridData mapBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            mapBrowser.setLayoutData(mapBrowserGD);
            mapBrowser.addMapObserver(this);
            mapBrowser.addProgressListener(this);
            loadMapBrowser();

            mainSashComposite.setWeights(new int[]{2, 8});
            if (!GssSession.areDevicesVisible()) {
                mainSashComposite.setMaximizedControl(mapComposite);
            } else {
                zoomToAllItem.setSelection(true);
            }

            GssGuiUtilities.addFooter(name, composite);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void reloadDevices() {
        try {
            allDevices.clear();
            allDevices.addAll(databaseHandler.getDao(GpapUsers.class).queryForAll());
            deviceNamesMap.clear();
            deviceNamesMap.putAll(allDevices.stream().collect(Collectors.toMap(gu -> getUserName(gu), Function.identity())));
        } catch (SQLException e1) {
            StageLogger.logError(this, e1);
        }
    }

    @Override
    public void completed( ProgressEvent event ) {
        String[] loadedGpapUsers = GssSession.getLoadedGpapUsers();
        if (loadedGpapUsers != null) {
            for( String loadedGpapUser : loadedGpapUsers ) {
                GpapUsers gpapUser = deviceNamesMap.get(loadedGpapUser);
                visibleDevices.add(gpapUser);
                addDeviceToMap(gpapUser);
            }
        }
        devicesTableViewer.setInput(visibleDevices);
    }

    private void loadMapBrowser() {
        if (mapBrowser != null) {
            String mapHtml = mapBrowser.getMapHtml();
            mapHtml = HtmlFeatureChooser.INSTANCE.addDebugging(mapHtml);
            mapHtml = HtmlFeatureChooser.INSTANCE.addUndefinedProgressBar(mapHtml);
            mapHtml = HtmlFeatureChooser.INSTANCE.addCursors(mapHtml);
            String replacement = "";
            try {
                replacement += MapsHandler.INSTANCE.addSelectedMaps(mapBrowser);
            } catch (SQLException e) {
                StageLogger.logError(this, e);
            }

            mapHtml = mapBrowser.addLayersInHtml(mapHtml, replacement);

            mapBrowser.setText(mapHtml);
        }
    }

    public GssMapBrowser getMapBrowser() {
        return mapBrowser;
    }

    private void createDevicesCombo( Composite parent ) throws Exception {

        Composite devicesViewerGroup = new Composite(parent, SWT.BORDER);
        GridData devicesViewerGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        devicesViewerGroup.setLayoutData(devicesViewerGroupGD);
        GridLayout devicesViewerLayout = new GridLayout(4, false);
        devicesViewerLayout.marginWidth = 15;
        devicesViewerLayout.marginHeight = 15;
        devicesViewerLayout.verticalSpacing = 15;
        devicesViewerGroup.setLayout(devicesViewerLayout);
        // devicesViewerGroup.setText("Surveyors");

        devicesCombo = new Combo(devicesViewerGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        GridData devicesGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        devicesCombo.setLayoutData(devicesGD);
        devicesCombo.setItems(deviceNamesMap.keySet().toArray(new String[0]));
        devicesCombo.setToolTipText("Select surveyor to add");

        Button addButton = new Button(devicesViewerGroup, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        addButton.setImage(eu.hydrologis.stage.libs.utils.ImageCache.getInstance().getImage(display,
                eu.hydrologis.stage.libs.utils.ImageCache.ADD));
        addButton.setToolTipText("Add selected surveyor to the map.");
        addButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String selected = devicesCombo.getText();
                GpapUsers user = deviceNamesMap.get(selected);
                visibleDevices.removeIf(d -> d == null);
                if (!visibleDevices.contains(user)) {
                    visibleDevices.add(user);
                }
                devicesTableViewer.setInput(visibleDevices);
                addDeviceToMap(user);
                addDevicesToSession();
            }
        });
        Button addAllButton = new Button(devicesViewerGroup, SWT.PUSH);
        addAllButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        addAllButton.setImage(eu.hydrologis.stage.libs.utils.ImageCache.getInstance().getImage(display,
                eu.hydrologis.stage.libs.utils.ImageCache.ADDALL));
        addAllButton.setToolTipText("Add all surveyors to the map.");
        addAllButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String[] all = devicesCombo.getItems();
                for( String selected : all ) {
                    GpapUsers user = deviceNamesMap.get(selected);
                    if (!visibleDevices.contains(user)) {
                        visibleDevices.add(user);
                    }
                    visibleDevices.removeIf(d -> d == null);
                    devicesTableViewer.setInput(visibleDevices);
                    addDeviceToMap(user);
                    addDevicesToSession();
                }
            }
        });
        Button refreshButton = new Button(devicesViewerGroup, SWT.PUSH);
        refreshButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        refreshButton.setImage(eu.hydrologis.stage.libs.utils.ImageCache.getInstance().getImage(display,
                eu.hydrologis.stage.libs.utils.ImageCache.REFRESH));
        refreshButton.setToolTipText("Reload the surveyors list.");
        refreshButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                reloadDevices();
                devicesCombo.setItems(deviceNamesMap.keySet().toArray(new String[0]));
            }
        });

        // TABLE
        Composite tableComposite = new Composite(devicesViewerGroup, SWT.NONE);
        GridData tableCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableCompositeGD.horizontalSpan = 4;
        tableComposite.setLayoutData(tableCompositeGD);

        devicesTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        devicesTableViewer.setContentProvider(DevicesTableContentProvider.getInstance());

        TableColumn deviceColumn = createColumn(devicesTableViewer, "Surveyor", 0);

        TableColumnLayout layout = new TableColumnLayout();
        tableComposite.setLayout(layout);
        layout.setColumnData(deviceColumn, new ColumnWeightData(100, true));

        devicesTableViewer.getTable().setHeaderVisible(false);
        devicesTableViewer.getTable().setLinesVisible(true);
        GridData devicesTableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        devicesTableGD.horizontalSpan = 3;
        devicesTableViewer.getTable().setLayoutData(devicesTableGD);
        devicesTableViewer.addDoubleClickListener(new IDoubleClickListener(){
            @Override
            public void doubleClick( DoubleClickEvent event ) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof GpapUsers) {
                    GpapUsers user = (GpapUsers) firstElement;
                    String notesLayer = getLayerName(user, NOTES);
                    String logsLayer = getLayerName(user, LOGS);
                    String script = mapBrowser.getZoomToLayers(new String[]{notesLayer, logsLayer});
                    mapBrowser.runScript(script);
                }
            }
        });

        // add right click menu
        MenuManager manager = new MenuManager();
        devicesTableViewer.getControl().setMenu(manager.createContextMenu(devicesTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @SuppressWarnings({"unchecked", "unused"})
            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                IStructuredSelection selection = (IStructuredSelection) devicesTableViewer.getSelection();
                if (selection.isEmpty()) {
                    return;
                }
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof GpapUsers) {
                    List<GpapUsers> usersList = selection.toList();

                    GpapUsers selectedUser = (GpapUsers) firstElement;
                    ImageDescriptor zoomID = null; // ImageDescriptor.createFromImageData(eu.hydrologis.stage.libs.utils.ImageCache
                    // .getInstance().getImage(display,
                    // eu.hydrologis.stage.libs.utils.ImageCache.ZOOM_TO_ALL).getImageData());
                    manager.add(new Action("Zoom to selected", zoomID){
                        @Override
                        public void run() {
                            List<String> layerNames = new ArrayList<>();
                            for( GpapUsers gpapUsers : usersList ) {
                                String notesLayer = getLayerName(gpapUsers, NOTES);
                                String logsLayer = getLayerName(gpapUsers, LOGS);
                                layerNames.add(notesLayer);
                                layerNames.add(logsLayer);
                            }

                            String script = mapBrowser.getZoomToLayers(layerNames.toArray(new String[0]));
                            mapBrowser.runScript(script);
                        }
                    });

                    ImageDescriptor reloadID = null; // ImageDescriptor.createFromImageData(eu.hydrologis.stage.libs.utils.ImageCache
                    // .getInstance().getImage(display,
                    // eu.hydrologis.stage.libs.utils.ImageCache.ZOOM_TO_ALL).getImageData());
                    manager.add(new Action("Reload data", reloadID){
                        @Override
                        public void run() {
                            List<String> layerNames = new ArrayList<>();
                            removeDeviceFromMap(layerNames);
                            for( GpapUsers gpapUsers : usersList ) {
                                addDeviceToMap(gpapUsers);
                            }
                        }
                    });
                    manager.add(new Separator());

                    ImageDescriptor removeID = null;// ImageDescriptor.createFromImage(eu.hydrologis.stage.libs.utils.ImageCache
                    // .getInstance().getImage(display,
                    // eu.hydrologis.stage.libs.utils.ImageCache.DELETE));
                    manager.add(new Action("Remove selected", removeID){
                        @Override
                        public void run() {
                            List<String> namesToRemove = new ArrayList<>();
                            for( GpapUsers user : usersList ) {
                                if (visibleDevices.contains(user)) {
                                    visibleDevices.remove(user);
                                    namesToRemove.add(getUserName(user));
                                }
                            }
                            devicesTableViewer.setInput(visibleDevices);
                            removeDeviceFromMap(namesToRemove);
                            addDevicesToSession();
                        }
                    });
                }

            }

        });
        manager.setRemoveAllWhenShown(true);

        parent.layout();
    }

    private void addDevicesToSession() {
        String[] newLoadedGpapUsers = new String[visibleDevices.size()];
        for( int i = 0; i < newLoadedGpapUsers.length; i++ ) {
            newLoadedGpapUsers[i] = getUserName(visibleDevices.get(i));
        }
        GssSession.setLoadedGpapUsers(newLoadedGpapUsers);
    }

    private void addDeviceToMap( GpapUsers user ) {
        mapBrowser.runScript(mapBrowser.getStartUndefindedProgress("Loading device data..."));
        try {
            SpatialDbDataProvider notesProv = new SpatialDbDataProvider(dbp.getDb(), getLayerName(user, NOTES),
                    DatabaseHandler.getTableName(Notes.class), FIELDS_FOR_NOTES);
            String notesName = notesProv.getName();
            String notesScript = mapBrowser.getRemoveDataLayer(notesName);
            String notesGeoJson = notesProv.asGeoJson(Notes.GPAPUSER_FIELD_NAME + "=" + user.id);
            if (notesGeoJson != null) {
                notesGeoJson = notesGeoJson.replaceAll("'", "`");
                notesScript += "addJsonMapCheck('" + notesName + "','" + notesGeoJson + "',null, false);";
            }
            mapBrowser.runScript(notesScript);

            SpatialDbDataProvider logsProv = new SpatialDbDataProvider(dbp.getDb(), getLayerName(user, LOGS),
                    DatabaseHandler.getTableName(GpsLogs.class), FIELDS_FOR_LOGS);
            String logsName = logsProv.getName();
            String logsScript = mapBrowser.getRemoveDataLayer(logsName);
            String logsGeoJson = logsProv.asGeoJson(Notes.GPAPUSER_FIELD_NAME + "=" + user.id);
            if (logsGeoJson != null) {
                // logsGeoJson = logsGeoJson.replaceAll("'", "`");
                logsScript += "addJsonMapCheck('" + logsName + "','" + logsGeoJson + "','" + GpsLogs.ID_FIELD_NAME + "', false);";
            }
            mapBrowser.runScript(logsScript);

            SpatialDbDataProvider imagesProv = new SpatialDbDataProvider(dbp.getDb(), getLayerName(user, IMAGES),
                    DatabaseHandler.getTableName(Images.class), FIELDS_FOR_IMAGES);
            String imagesName = imagesProv.getName();
            String imagesScript = mapBrowser.getRemoveDataLayer(imagesName);
            String imagesGeoJson = imagesProv.asGeoJson(Notes.GPAPUSER_FIELD_NAME + "=" + user.id);
            if (imagesGeoJson != null) {
                // logsGeoJson = logsGeoJson.replaceAll("'", "`");
                imagesScript += "addJsonMapCheck('" + imagesName + "','" + imagesGeoJson + "',null, false);";
            }
            mapBrowser.runScript(imagesScript);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mapBrowser.runScript(mapBrowser.getStopUndefindedProgress());
        }

    }

    private void removeDeviceFromMap( List<String> namesToRemove ) {
        try {
            String script = "";
            for( String name : namesToRemove ) {
                String layerName = getLayerName(name, NOTES);
                script += mapBrowser.getRemoveDataLayer(layerName);
                layerName = getLayerName(name, LOGS);
                script += mapBrowser.getRemoveDataLayer(layerName);
                layerName = getLayerName(name, IMAGES);
                script += mapBrowser.getRemoveDataLayer(layerName);
            }
            mapBrowser.runScript(script);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getLayerName( GpapUsers user, String postFix ) {
        return postFix + ": " + user.name;
    }
    private String getLayerName( String comboString, String postFix ) {
        return postFix + ": " + comboString;
    }

    private TableColumn createColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new DeviceLabelProvider());
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        // column.addSelectionListener( new SelectionAdapter() {
        // @Override
        // public void widgetSelected( SelectionEvent event ) {
        // int sortDirection = updateSortDirection( ( TableColumn )event.widget );
        // sort( viewer, COL_FIRST_NAME, sortDirection == SWT.DOWN );
        // }
        // } );
        return column;
    }

    private class DeviceLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText( Object element ) {
            if (element instanceof GpapUsers) {
                GpapUsers user = (GpapUsers) element;
                return getUserName(user);
            }
            return "???";
        }
    }

    private String getUserName( GpapUsers user ) {
        return user.name;
    }

    @Override
    public void onClick( double lon, double lat, int zoomLevel ) {
    }

    @Override
    public void layerFeatureClicked( double lon, double lat, String layerName, Object[] properties ) {
        if (layerName.startsWith(NOTES) || layerName.startsWith(LOGS)) {
            StringBuilder sb = new StringBuilder("<h2>" + layerName + "</h2>");
            String LF = "<br />";
            sb.append(LF);
            sb.append("<table style='width:100%' border='1' cellpadding='5'>");
            for( int i = 0; i < properties.length; i++ ) {
                Object object = properties[i];
                if (object != null) {
                    if (object instanceof Object[]) {
                        Object[] infoPair = (Object[]) object;
                        if (infoPair.length == 2 && infoPair[0] != null && infoPair[1] != null) {
                            String[] nameAndValue = getNameAndValue(infoPair);
                            if (nameAndValue == null) {
                                continue;
                            }
                            sb.append("<tr><td><b>");
                            sb.append(nameAndValue[0]);
                            sb.append("</b></td><td>");
                            sb.append(nameAndValue[1]).append("</td></tr>");// .append(LF);
                        }
                    }

                }
            }
            sb.append("</table>");
            try {
                String msg = sb.toString();
                String openPopup = mapBrowser.getOpenPopup(lon, lat, msg);
                mapBrowser.runScript(openPopup);
            } catch (Exception e1) {
                StageLogger.logError(this, e1);
            }
        } else if (layerName.startsWith(IMAGES)) {
            long imageDataId = -1;
            String timestamp = "";
            String name = "";

            for( int i = 0; i < properties.length; i++ ) {
                Object object = properties[i];
                if (object != null) {
                    if (object instanceof Object[]) {
                        Object[] infoPair = (Object[]) object;
                        if (infoPair[0].toString().equalsIgnoreCase(Images.IMAGEDATA_FIELD_NAME)) {
                            imageDataId = Long.parseLong(infoPair[1].toString());
                        } else if (infoPair[0].toString().equalsIgnoreCase(Images.TIMESTAMP_FIELD_NAME)) {
                            timestamp = formatDate(infoPair[1]);
                        } else if (infoPair[0].toString().equalsIgnoreCase(Images.TEXT_FIELD_NAME)) {
                            name = infoPair[1].toString();
                        }
                    }

                }
            }
            if (imageDataId != -1) {
                String title = name + " ( " + timestamp + " )";
                try {
                    Dao<ImageData, ? > imageDao = dbp.getDatabaseHandler().getDao(ImageData.class);
                    ImageData imageDataForQuery = new ImageData(imageDataId);
                    ImageData imageData = imageDao.queryForSameId(imageDataForQuery);
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData.data));
                    BufferedImage scaleImage = ImageUtilities.scaleImage(bufferedImage, 1000);
                    ImageDialog imageDialog = new ImageDialog(parentShell, title, scaleImage);
                    imageDialog.open();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String[] getNameAndValue( Object[] infoPair ) {
        try {
            if (infoPair[0].equals(GpsLogs.STARTTS_FIELD_NAME)) {
                return new String[]{"Start Timestamp", formatDate(infoPair[1])};
            } else if (infoPair[0].equals(GpsLogs.ENDTS_FIELD_NAME)) {
                return new String[]{"End Timestamp", formatDate(infoPair[1])};
            } else if (infoPair[0].equals(GpsLogs.NAME_FIELD_NAME)) {
                return new String[]{"Log Name", infoPair[1].toString()};
            } else if (infoPair[0].equals(Notes.ALTIM_FIELD_NAME)) {
                return new String[]{"Elevation", infoPair[1].toString()};
            } else if (infoPair[0].equals(Notes.TEXT_FIELD_NAME)) {
                return new String[]{"Notes", infoPair[1].toString()};
            } else if (infoPair[0].equals(Notes.TIMESTAMP_FIELD_NAME)) {
                return new String[]{"Timestamp", formatDate(infoPair[1])};
            } else if (infoPair[0].equals(GpsLogs.ID_FIELD_NAME)) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[]{infoPair[0].toString(), infoPair[1].toString()};
    }

    private String formatDate( Object dateLong ) {
        return new DateTime(Long.parseLong(dateLong.toString())).toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS);
    }

    @Override
    public void changed( ProgressEvent event ) {
    }

}
