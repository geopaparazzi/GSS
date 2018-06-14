/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package com.hydrologis.gss.entrypoints;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.hortonmachine.gears.libs.modules.HMConstants;

import com.gasleaksensors.databases.v2.core.objects.Sessions;
import com.hydrologis.gss.GssContext;
import com.hydrologis.gss.GssDbProvider;
import com.hydrologis.gss.map.GssMapBrowser;
import com.hydrologis.gss.server.database.DatabaseHandler;
import com.hydrologis.gss.server.database.objects.GpapUsers;
import com.hydrologis.gss.utils.DevicesTableContentProvider;
import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssMapsHandler;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.hydrologis.gss.utils.ImageCache;

import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.html.HtmlFeatureChooser;
import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.map.IMapObserver;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.StageUtils;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class MapviewerEntryPoint extends StageEntryPoint implements IMapObserver {

    public static final String ID = "com.hydrologis.gss.entrypoints.MapviewerEntryPoint";

    private static final long serialVersionUID = 1L;

    private Display display;

    private Shell parentShell;
    private GssMapBrowser mapBrowser;
    private Composite mapComposite;

    private SashForm mainSashComposite;

    private Combo devicesCombo;

    private DatabaseHandler databaseHandler;

    private TableViewer devicesTableViewer;

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

        GssDbProvider dbp = GssContext.instance().getDbProvider();
        databaseHandler = dbp.getDatabaseHandler();

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
        devicesItem.setWidth(200);
        devicesItem.setText("Toggle Devices View");
        devicesItem.setToolTipText("Toggle the devices view to add and remove data to visualize.");
        devicesItem.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if (devicesItem.getSelection()) {
                    mainSashComposite.setMaximizedControl(null);
                } else {
                    mainSashComposite.setMaximizedControl(mapComposite);
                }
            }
        });

        GssGuiUtilities.addToolBarSeparator(toolsToolBar);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        loadMapBrowser();

        mainSashComposite.setWeights(new int[]{2, 8});
        mainSashComposite.setMaximizedControl(mapComposite);

        GssGuiUtilities.addFooter(name, composite);
    }

    private void loadMapBrowser() {
        if (mapBrowser != null) {
            String mapHtml = mapBrowser.getMapHtml();
            mapHtml = HtmlFeatureChooser.INSTANCE.addUndefinedProgressBar(mapHtml);
            mapHtml = HtmlFeatureChooser.INSTANCE.addCursors(mapHtml);
            String replacement = "";
            try {
                replacement += GssMapsHandler.INSTANCE.addSelectedMaps(mapBrowser);
            } catch (SQLException e) {
                StageLogger.logError(this, e);
            }

            // replacement += mapBrowser.getAddTileLayer("pipes", "pipes",
            // "./getpipetile?z={z}&x={x}&y={y}", 22, 22, "© Comune Roma", true, true);
            // replacement += mapBrowser.getAddTileLayer("work", "work",
            // "./getworktile?z={z}&x={x}&y={y}", 22, 22, "© Comune Roma", true, true);
            // replacement += mapBrowser.getAddTileLayer("events", "events",
            // "./geteventstile?z={z}&x={x}&y={y}", 22, 22, "© Comune Roma", true, true);

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
        devicesViewerGroup.setLayout(new GridLayout(3, true));
        // devicesViewerGroup.setText("Surveyors");

        devicesCombo = new Combo(devicesViewerGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        GridData devicesGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        devicesGD.horizontalSpan = 3;
        devicesCombo.setLayoutData(devicesGD);

        List<GpapUsers> devices = databaseHandler.getDao(GpapUsers.class).queryForAll();
        Map<String, GpapUsers> deviceNames = devices.stream().collect(Collectors.toMap(gu -> {
            if (gu.name != null) {
                return gu.name;
            } else {
                return gu.deviceId;
            }
        }, Function.identity()));
        devicesCombo.setItems(deviceNames.keySet().toArray(new String[0]));
        devicesCombo.setToolTipText("Select device to add");

        Composite tableComposite = new Composite(devicesViewerGroup, SWT.NONE);
        GridData tableCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableCompositeGD.horizontalSpan = 3;
        tableComposite.setLayoutData(tableCompositeGD);

        devicesTableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        devicesTableViewer.setContentProvider(DevicesTableContentProvider.getInstance());

        TableColumn deviceColumn = createColumn(devicesTableViewer, "Device", 0);

        TableColumnLayout layout = new TableColumnLayout();
        tableComposite.setLayout(layout);
        layout.setColumnData(deviceColumn, new ColumnWeightData(100, true));

        devicesTableViewer.getTable().setHeaderVisible(false);
        devicesTableViewer.getTable().setLinesVisible(true);
        GridData devicesTableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        devicesTableGD.horizontalSpan = 3;
        devicesTableViewer.getTable().setLayoutData(devicesTableGD);

        devicesTableViewer.setInput(devices);

        Button addButton = new Button(devicesViewerGroup, SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        addButton.setText("   +   ");
        addButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {

            }
        });
        new Label(devicesViewerGroup, SWT.NONE);

        Button removeButton = new Button(devicesViewerGroup, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        removeButton.setText("   -   ");
        removeButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {

            }
        });

        // add right click menu
        // MenuManager manager = new MenuManager();
        // devicesTableViewer.getControl().setMenu(manager.createContextMenu(devicesTableViewer.getControl()));
        // manager.addMenuListener(new IMenuListener(){
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // public void menuAboutToShow( IMenuManager manager ) {
        // if (devicesTableViewer.getSelection() instanceof IStructuredSelection) {
        //
        // }
        // }
        //
        // });
        // manager.setRemoveAllWhenShown(true);

        parent.layout();
    }

    private String getSelectedPlantCode() {
        String impCode = devicesCombo.getText();
        if (impCode.trim().length() == 0) {
            return null;
        }
        return impCode;
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
                if (user.name != null && user.name.length() > 0) {
                    return user.name;
                } else {
                    return user.deviceId;
                }
            }
            return "???";
        }
    }

    @Override
    public void onClick( double lon, double lat, int zoomLevel ) {
    }

    @Override
    public void layerFeatureClicked( String layerName, Object[] properties ) {
        // TODO Auto-generated method stub

    }

}
