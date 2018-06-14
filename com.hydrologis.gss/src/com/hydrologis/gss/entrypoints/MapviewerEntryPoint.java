/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package com.hydrologis.gss.entrypoints;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.hortonmachine.dbs.compat.ASpatialDb;
import org.hortonmachine.dbs.compat.IHMResultSet;
import org.hortonmachine.dbs.compat.IHMStatement;
import org.hortonmachine.dbs.compat.objects.QueryResult;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.utils.geometry.GeometryUtilities;
import org.joda.time.DateTime;

import com.gasleaksensors.databases.v2.core.handlers.DatabaseHandler;
import com.gasleaksensors.databases.v2.core.objects.PipeTypes;
import com.gasleaksensors.databases.v2.core.objects.Pipes;
import com.gasleaksensors.libsV2.IDbProvider;
import com.gasleaksensors.libsV2.SensitContextV2;
import com.hydrologis.gss.map.GssMapBrowser;
import com.hydrologis.gss.utils.QueryResultContentProvider;
import com.hydrologis.gss.utils.SensitDatabaseUtilities;
import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssMapsHandler;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.html.HtmlFeatureChooser;
import eu.hydrologis.stage.libs.images.ImageDialog;
import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.map.IMapObserver;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.MessageDialogUtil;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class MapviewerEntryPoint extends StageEntryPoint implements IMapObserver {

    public static final String ID = "com.gasleaksensors.server.app.entrypoints.MapviewerEntryPoint";

    private static final String DATA_VIEWER = "Data viewer";

    private static final long serialVersionUID = 1L;

    private Display display;

    private Shell parentShell;
    private Group attributesViewerGroup;

    private GssMapBrowser mapBrowser;
    private SashForm mapAndAttributesSashComposite;
    private Composite mapComposite;

    private SashForm mainSashComposite;

    private Composite featurePropertiesViewerGroup;

    private TableViewer featurePropertiesTableViewer;

    private Label selectedLayerLabel;

    private Combo plantsCmbo;

    private int selectedSessionYear;

    private boolean pipesInfoIsOn = false;

    private Map<Long, String> typesIdToLabel = new HashMap<>();

    @SuppressWarnings("serial")
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

        IDbProvider dbp = GssContext.instance().getDbProviderForSession();
        try {
            DatabaseHandler databaseHandler = dbp.getDatabaseHandler();
            typesIdToLabel = databaseHandler.getDao(PipeTypes.class).queryForAll().stream()
                    .collect(Collectors.toMap(p -> p.id, p -> p.label));
        } catch (Exception e2) {
            StageLogger.logError(this, e2);
        }

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
        try {
            ToolBar toolsToolBar = new ToolBar(horizToolbarComposite, SWT.FLAT);
            GssGuiUtilities.addToolBarSeparator(toolsToolBar);
            createPlantsCombo(toolsToolBar);
            final ToolItem percentageItem = new ToolItem(toolsToolBar, SWT.PUSH);
            percentageItem.setImage(com.hydrologis.gss.utils.ImageCache.getInstance().getImage(display,
                    com.hydrologis.gss.utils.ImageCache.WORKSTATS));
            percentageItem.setWidth(150);
            percentageItem.setText("Work Stats");
            percentageItem.setToolTipText("Show Work Statistics on selected Plant");
            percentageItem.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {

                    String selectedPlantCode = getSelectedPlantCode();
                    if (selectedPlantCode == null) {
                        return;
                    }
                    try {
                        mapBrowser.runScript(
                                mapBrowser.getStartUndefindedProgress("Calculating statistics for " + selectedPlantCode));
                        int[] workStats = SensitDatabaseUtilities.getWorkStats(selectedPlantCode);
                        String msg = "Worked percentage: " + workStats[0] + "% which is " + workStats[1] + " of a total of "
                                + workStats[2] + "Km.";
                        MessageDialogUtil.openInformation(parentShell, "INFO", msg, null);
                    } catch (Exception e1) {
                        StageLogger.logError(this, e1);
                    } finally {
                        mapBrowser.runScript(mapBrowser.getStopUndefindedProgress());
                    }
                }
            });
            final ToolItem pipesInfoItem = new ToolItem(toolsToolBar, SWT.PUSH);
            pipesInfoItem.setImage(ImageCache.getInstance().getImage(display, ImageCache.INFOTOOL_ON));
            pipesInfoItem.setWidth(150);
            pipesInfoItem.setText("Pipe Info");
            pipesInfoItem.setToolTipText("Click on a pipe to visualize its information.");
            pipesInfoItem.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    if (pipesInfoIsOn) {
                        pipesInfoItem.setImage(ImageCache.getInstance().getImage(display, ImageCache.INFOTOOL_ON));
                    } else {
                        pipesInfoItem.setImage(ImageCache.getInstance().getImage(display, ImageCache.INFOTOOL_OFF));
                    }
                    pipesInfoIsOn = !pipesInfoIsOn;
                    mapBrowser.runScript(mapBrowser.getTogglePointerCursor());
                }
            });

        } catch (Exception e) {
            StageLogger.logError(this, e);
        }
        GssGuiUtilities.addVerticalFiller(vertToolbarComposite);
        GssGuiUtilities.addHorizontalFiller(horizToolbarComposite);
        GssGuiUtilities.addAdminTools(vertToolbarComposite, this, isAdmin);
        GssGuiUtilities.addLogoutButton(horizToolbarComposite);

        // SASH FOR MAP + FEATURE PROPERTIES
        mainSashComposite = new SashForm(composite, SWT.HORIZONTAL);
        GridData mainCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainSashComposite.setLayoutData(mainCompositeGD);

        // SASH FOR MAP + ATTRIBUTES TABLE
        mapAndAttributesSashComposite = new SashForm(mainSashComposite, SWT.VERTICAL);
        GridData mapCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mapAndAttributesSashComposite.setLayoutData(mapCompositeGD);

        mapComposite = new Composite(mapAndAttributesSashComposite, SWT.None);
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
        loadMapBrowser();

        Composite attributesComposite = new Composite(mapAndAttributesSashComposite, SWT.None);
        GridLayout attributesLayout = new GridLayout(1, true);
        attributesLayout.marginWidth = 0;
        attributesLayout.marginHeight = 0;
        attributesComposite.setLayout(attributesLayout);
        GridData attributesGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        attributesComposite.setLayoutData(attributesGD);

        attributesViewerGroup = new Group(attributesComposite, SWT.NONE);
        GridData attributesViewerGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        attributesViewerGroup.setLayoutData(attributesViewerGroupGD);
        attributesViewerGroup.setLayout(new GridLayout(1, false));
        attributesViewerGroup.setText(DATA_VIEWER);

        Composite featurePropertiesComposite = new Composite(mainSashComposite, SWT.None);
        GridLayout featurePropertiesLayout = new GridLayout(2, false);
        featurePropertiesLayout.marginWidth = 10;
        featurePropertiesLayout.marginHeight = 10;
        featurePropertiesComposite.setLayout(featurePropertiesLayout);
        GridData featurePropertiesGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        featurePropertiesComposite.setLayoutData(featurePropertiesGD);

        selectedLayerLabel = new Label(featurePropertiesComposite, SWT.NONE);
        GridData selectedLayerLabelGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        selectedLayerLabel.setLayoutData(selectedLayerLabelGD);

        Button closeViewButton = new Button(featurePropertiesComposite, SWT.PUSH | SWT.FLAT);
        GridData closeViewButtonGD = new GridData(SWT.END, SWT.FILL, false, false);
        closeViewButton.setLayoutData(closeViewButtonGD);
        Image closeViewImage = ImageCache.getInstance().getImage(display, ImageCache.CLOSE_VIEW);
        closeViewButton.setImage(closeViewImage);
        closeViewButton.setToolTipText("Close properties view");
        closeViewButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                mainSashComposite.setMaximizedControl(mapAndAttributesSashComposite);
            }
        });

        featurePropertiesViewerGroup = new Composite(featurePropertiesComposite, SWT.NONE);
        GridData propertiesViewerGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        propertiesViewerGroupGD.horizontalSpan = 2;
        featurePropertiesViewerGroup.setLayoutData(propertiesViewerGroupGD);
        // featurePropertiesViewerGroup.setLayout(new GridLayout(1, false));
        // featurePropertiesViewerGroup.setText("Feature Properties");

        // SET SASH LAYOUT PROPERTIES
        mapAndAttributesSashComposite.setWeights(new int[]{6, 3});
        mapAndAttributesSashComposite.setMaximizedControl(mapComposite);

        mainSashComposite.setWeights(new int[]{7, 3});
        mainSashComposite.setMaximizedControl(mapAndAttributesSashComposite);

        GssGuiUtilities.addFooter(name, composite);
    }

    private void loadMapBrowser() {
        if (mapBrowser != null) {
            String mapHtml = mapBrowser.getMapHtml();
            mapHtml = HtmlFeatureChooser.INSTANCE.addUndefinedProgressBar(mapHtml);
            mapHtml = HtmlFeatureChooser.INSTANCE.addCursors(mapHtml);
            selectedSessionYear = GssContext.instance().getSelectedSessionYear();

            String replacement = "";
            try {
                replacement += GssMapsHandler.INSTANCE.addSelectedMaps(mapBrowser);
            } catch (SQLException e) {
                StageLogger.logError(this, e);
            }

            replacement += mapBrowser.getAddTileLayer("pipes", "pipes",
                    "./getpipetile?z={z}&x={x}&y={y}&year=" + selectedSessionYear, 22, 22, "© Comune Roma", true, true);
            replacement += mapBrowser.getAddTileLayer("work", "work",
                    "./getworktile?z={z}&x={x}&y={y}&year=" + selectedSessionYear, 22, 22, "© Comune Roma", true, true);
            replacement += mapBrowser.getAddTileLayer("events", "events",
                    "./geteventstile?z={z}&x={x}&y={y}&year=" + selectedSessionYear, 22, 22, "© Comune Roma", true, true);

            mapHtml = mapBrowser.addLayersInHtml(mapHtml, replacement);

            mapBrowser.setText(mapHtml);
        }
    }

    public GssMapBrowser getMapBrowser() {
        return mapBrowser;
    }

    private void createPlantsCombo( final ToolBar toolBar ) throws Exception {

        ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);
        plantsCmbo = new Combo(toolBar, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        plantsCmbo.setLayoutData(layoutData);
        final HashMap<String, Geometry> plantNames = SensitDatabaseUtilities.getImpianti2ConvexHullMap();

        plantsCmbo.setItems(plantNames.keySet().toArray(new String[0]));
        plantsCmbo.setToolTipText("Select Plant to visualize");
        toolItem.setControl(plantsCmbo);
        toolItem.setWidth(200);

        plantsCmbo.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                final String plantCode = getSelectedPlantCode();
                if (plantCode == null) {
                    return;
                }

                try {
                    Geometry plantBounds = plantNames.get(plantCode);
                    mapBrowser.runScript(mapBrowser.getZoomToGeometryAndHighlight(plantBounds));
                } catch (Exception e1) {
                    StageLogger.logError(this, e1);
                }
            }

        });
    }

    private String getSelectedPlantCode() {
        String impCode = plantsCmbo.getText();
        if (impCode.trim().length() == 0) {
            return null;
        }
        return impCode;
    }

    private void createFeaturePropertiesTableViewer( Composite parent, Object[] properties ) throws Exception {
        if (featurePropertiesTableViewer != null)
            featurePropertiesTableViewer.getControl().dispose();

        featurePropertiesTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        featurePropertiesTableViewer.setContentProvider(QueryResultContentProvider.getInstance());

        TableColumn fieldColumn = createColumn(featurePropertiesTableViewer, "FIELD", 0);
        TableColumn valueColumn = createColumn(featurePropertiesTableViewer, "VALUE", 1);

        TableColumnLayout layout = new TableColumnLayout();
        parent.setLayout(layout);
        layout.setColumnData(fieldColumn, new ColumnWeightData(30, true));
        layout.setColumnData(valueColumn, new ColumnWeightData(70, true));

        featurePropertiesTableViewer.getTable().setHeaderVisible(true);
        featurePropertiesTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        featurePropertiesTableViewer.getTable().setLayoutData(tableData);

        featurePropertiesTableViewer.setInput(properties);

        // add right click menu
        MenuManager manager = new MenuManager();
        featurePropertiesTableViewer.getControl().setMenu(manager.createContextMenu(featurePropertiesTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("serial")
            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                if (featurePropertiesTableViewer.getSelection() instanceof IStructuredSelection) {
                    manager.add(new Action("Close table", null){
                        @Override
                        public void run() {
                            mainSashComposite.setMaximizedControl(mapAndAttributesSashComposite);
                            // if (featurePropertiesTableViewer != null)
                            // featurePropertiesTableViewer.getControl().dispose();
                        }
                    });

                    final IStructuredSelection selection = (IStructuredSelection) featurePropertiesTableViewer.getSelection();
                    if (selection.isEmpty()) {
                        return;
                    }

                    String imgString = null;
                    Object object = selection.getFirstElement();
                    if (object instanceof Object[]) {
                        Object[] objects = (Object[]) object;
                        String string = objects[1].toString();
                        if (string.toLowerCase().contains(".jpg")) {
                            imgString = string;
                        }
                    }

                    if (imgString != null) {
                        File dataFolder = StageWorkspace.getInstance().getGlobalDataFolder().get();
                        String[] imgsSplit = imgString.split(";");
                        List<File> imageFiles = new ArrayList<>();
                        for( String imgRelPath : imgsSplit ) {
                            File imgFile = new File(dataFolder, imgRelPath);
                            if (imgFile.exists()) {
                                imageFiles.add(imgFile);

                                // TODO change for multi
                                break;
                            }
                        }
                        if (imageFiles.size() > 0) {
                            manager.add(new Action("Open images", null){
                                @Override
                                public void run() {
                                    ImageDialog imageDialog = new ImageDialog(parentShell, "Images", imageFiles);
                                    imageDialog.open();
                                }
                            });
                        }
                    }
                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        parent.layout();
    }

    private TableColumn createColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new RecordLabelProvider(dataIndex));
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

    private class RecordLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        private int dataIndex;
        public RecordLabelProvider( int dataIndex ) {
            this.dataIndex = dataIndex;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof Object[]) {
                Object[] data = (Object[]) element;
                Object obj = data[dataIndex];
                if (obj != null) {
                    return obj.toString();
                } else {
                    return "NULL";
                }
            }
            return "";
        }
    }

    @Override
    public void layerFeatureClicked( String layerName, Object[] properties ) {
        selectedLayerLabel.setText("Layer: " + layerName);
        mainSashComposite.setMaximizedControl(null);
        try {
            createFeaturePropertiesTableViewer(featurePropertiesViewerGroup, properties);
        } catch (Exception e) {
            StageLogger.logError(this, e);
        }
    }

    @Override
    public void onClick( double lon, double lat, int zoomLevel ) {
        if (pipesInfoIsOn) {
            ASpatialDb db = GssContext.instance().getDbProviderForSession().getDb();
            Point point = GeometryUtilities.gf().createPoint(new Coordinate(lon, lat));
            Envelope env = point.getEnvelopeInternal();
            env.expandBy(0.0001);
            try {
                QueryResult result = db.getTableRecordsMapIn(DatabaseHandler.getTableName(Pipes.class), env, false, -1, -1, null);

                int size = result.data.size();
                int geometryIndex = result.geometryIndex;
                Object[] nearestAttr = null;
                Geometry nearestGeom = null;
                double minDistance = Double.POSITIVE_INFINITY;
                for( int i = 0; i < size; i++ ) {
                    Geometry geom = (Geometry) result.data.get(i)[geometryIndex];
                    double distance = geom.distance(point);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestAttr = result.data.get(i);
                        nearestGeom = geom;
                    }
                }
                if (nearestAttr != null) {
                    StringBuilder workSb = new StringBuilder();
                    StringBuilder sb = new StringBuilder("<h2>Pipe Information</h2>");
                    // String LF = "\n\n";
                    String LF = "<br />";
                    sb.append(LF);

                    String nonWorked = "";
                    sb.append("<table style='width:100%' border='1' cellpadding='5'>");
                    for( int i = 0; i < nearestAttr.length; i++ ) {
                        if (i == geometryIndex) {
                            continue;
                        }
                        Object object = nearestAttr[i];
                        String name = result.names.get(i);
                        if (object != null) {
                            String objStr = object.toString();
                            if (objStr.trim().length() == 0) {
                                continue;
                            }

                            if (name.equals("PARENTID")) {
                                continue;
                            } else if (name.equals("MATERIAL")) {
                                continue;
                            } else if (name.equals("ID")) {
                                name = "Id";

                                long id = Long.parseLong(objStr);
                                String sql = "select min(e.SYSTEMTIMESTAMP), max(e.SYSTEMTIMESTAMP) , ST_LENGTH(ST_COLLECT(pp.the_geom)) "
                                        + "from events e , pipepieces pp where pp.PARENTID=" + id
                                        + " and pp.EVENTSID is not null and pp.EVENTSID=e.id group by pp.PARENTID";

                                Geometry _nearestGeom = nearestGeom;
                                nonWorked = db.execOnConnection(connection -> {
                                    try (IHMStatement stmt = connection.createStatement();
                                            IHMResultSet rs = stmt.executeQuery(sql);) {
                                        if (rs.next()) {
                                            long startTS = rs.getLong(1);
                                            long endTS = rs.getLong(2);
                                            double workedLength = rs.getDouble(3);
                                            double pipeLength = _nearestGeom.getLength();

                                            double perc = 100.0 * workedLength / pipeLength;
                                            int percInt = (int) Math.round(perc);

                                            workSb.append("<tr><td><b>Worked between</b></td><td>").append(
                                                    new DateTime(startTS).toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS));
                                            workSb.append("<br/>")
                                                    .append(new DateTime(endTS)
                                                            .toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS))
                                                    .append("</td></tr>");
                                            workSb.append("<tr><td><b>Completed percentage </b></td><td>").append(percInt)
                                                    .append("%").append("</td></tr>");
                                        } else {
                                            return "<br /><b><font color='red'>NB: This pipe was not worked.</font></b>";
                                        }
                                    }

                                    return "";
                                });

                            } else if (name.equals("DIAMETER")) {
                                continue;
                            } else if (name.equals("PIPESTATUSID")) {
                                continue;
                            } else if (name.equals("PIECESCOUNT")) {
                                continue;
                            } else if (name.equals("WORKEDCOUNT")) {
                                continue;
                            } else if (name.equals("ADMINISTRATION")) {
                                name = "Administration";
                            } else if (name.equals("LENGTHM")) {
                                name = "Length [m]";
                                double length = Double.parseDouble(objStr);
                                objStr = "" + (int) length;
                            } else if (name.equals("STREET")) {
                                name = "Street";
                            } else if (name.equals("CUSTOMERID")) {
                                name = "Customer Id";
                            } else if (name.equals("PLANT")) {
                                name = "Plant";
                            } else if (name.equals("PIPETYPESID")) {
                                name = "Type";
                                long type = Integer.parseInt(objStr);
                                String label = typesIdToLabel.get(type);
                                if (label == null) {
                                    label = "";
                                }
                                objStr = label;
                            }

                            sb.append("<tr><td><b>");
                            sb.append(name);
                            sb.append("</b></td><td>");
                            sb.append(objStr).append("</td></tr>");// .append(LF);
                        }
                    }
                    sb.append(workSb.toString());
                    sb.append("</table>");
                    try {

                        String msg = sb.toString() + nonWorked + "<br /><br /><br />";
                        String openPopup = mapBrowser.getOpenPopup(lon, lat, msg);
                        String highlight = mapBrowser.getHighlightGeometry(nearestGeom, null);
                        mapBrowser.runScript(highlight + openPopup);
                    } catch (Exception e1) {
                        StageLogger.logError(this, e1);
                    }
                }

            } catch (Exception e) {
                StageLogger.logError(this, e);
            }

        }
    }

}
