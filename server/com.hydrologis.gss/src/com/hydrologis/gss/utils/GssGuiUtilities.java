/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss.utils;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.hydrologis.gss.GssSession;
import com.hydrologis.gss.entrypoints.DashboardEntryPoint;
import com.hydrologis.gss.entrypoints.MapviewerEntryPoint;
import com.hydrologis.gss.entrypoints.SettingsEntryPoint;

import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utils.StageUtils;

public class GssGuiUtilities {
    public static final String KEY_USER_VIEWS_PERMISSIONS = "KEY_USER_VIEWS_PERMISSIONS";
    public static final String DELIMITER = ";";

    public static void addFooter( String userName, final Composite parent ) {
        // FOOTER
        final Composite footerComposite = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        layoutData.horizontalSpan = 2;
        footerComposite.setLayoutData(layoutData);
        footerComposite.setLayout(new GridLayout(1, false));

        Label footerLabel = new Label(footerComposite, SWT.NONE);
        footerLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        footerLabel.setText("<small>Welcome <b>" + userName
                + "</b> - Geopaparazzi Survey Server is powered by <a href='http://www.hydrologis.com'>HydroloGIS</a></small>");
        footerLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
    }

    public static void addLogo( Composite toolbarComposite ) {
        Label iconLabel = new Label(toolbarComposite, SWT.NONE);
        iconLabel.setText("");
        iconLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
        Image logoImage = com.hydrologis.gss.utils.ImageCache.getInstance().getImage(toolbarComposite.getDisplay(),
                com.hydrologis.gss.utils.ImageCache.LOGO);
        iconLabel.setImage(logoImage);
    }

    public static void addHorizontalFiller( Composite toolbarComposite ) {
        Label filler = new Label(toolbarComposite, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    public static void addVerticalFiller( Composite toolbarComposite ) {
        Label filler = new Label(toolbarComposite, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
    }

    @SuppressWarnings("serial")
    public static ToolBar addPagesLinks( Composite toolbarComposite, Object caller, boolean userIsAdmin ) {
        Composite composite = new Composite(toolbarComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.VERTICAL);
        // addToolBarSeparator(toolBar);

        GridData toolBarGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        toolBar.setLayoutData(toolBarGD);

        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(userIsAdmin);

        if (!(caller instanceof DashboardEntryPoint)) {
            String path = StageUtils.getPathforEntryPointID(DashboardEntryPoint.ID);
            if (permissionsHandler.isAllowed(path)) {
                final ToolItem mapViewerItem = new ToolItem(toolBar, SWT.PUSH);
                mapViewerItem.setImage(com.hydrologis.gss.utils.ImageCache.getInstance().getImage(toolBar.getDisplay(),
                        com.hydrologis.gss.utils.ImageCache.DASHBOARD));
                mapViewerItem.setWidth(150);
                mapViewerItem.setText("Dashboard");
                mapViewerItem.setToolTipText("View the Dashboard");
                mapViewerItem.addSelectionListener(new SelectionAdapter(){
                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        StageUtils.openUrl(path, false);
                    }
                });
            }
        }
        if (!(caller instanceof MapviewerEntryPoint)) {
            String path = StageUtils.getPathforEntryPointID(MapviewerEntryPoint.ID);
            if (permissionsHandler.isAllowed(path)) {
                final ToolItem mapViewerItem = new ToolItem(toolBar, SWT.PUSH);
                mapViewerItem.setImage(ImageCache.getInstance().getImage(toolBar.getDisplay(), ImageCache.TABLE_SPATIAL));
                mapViewerItem.setWidth(150);
                mapViewerItem.setText("    Map    ");
                mapViewerItem.setToolTipText("Map Viewer");
                mapViewerItem.addSelectionListener(new SelectionAdapter(){
                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        StageUtils.openUrl(path, false);
                    }
                });
            }
        }
        return toolBar;
    }

    public static void addToolBarSeparator( ToolBar toolBar ) {
        final ToolItem sepItem = new ToolItem(toolBar, SWT.SEPARATOR);
        sepItem.setWidth(30);
    }

    @SuppressWarnings("serial")
    public static ToolBar addAdminTools( Composite toolbarComposite, Object caller, boolean userIsAdmin ) {
        Composite composite = new Composite(toolbarComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.VERTICAL);
        GridData toolBarGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        toolBar.setLayoutData(toolBarGD);

        // addToolBarSeparator(toolBar);
        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(userIsAdmin);

        if (!(caller instanceof SettingsEntryPoint)) {
            String path = StageUtils.getPathforEntryPointID(SettingsEntryPoint.ID);
            if (permissionsHandler.isAllowed(path)) {
                final ToolItem settingsViewerItem = new ToolItem(toolBar, SWT.PUSH);
                settingsViewerItem.setImage(com.hydrologis.gss.utils.ImageCache.getInstance()
                        .getImage(toolbarComposite.getDisplay(), com.hydrologis.gss.utils.ImageCache.WEBUSERS));
                settingsViewerItem.setWidth(150);
                settingsViewerItem.setText("Settings");
                settingsViewerItem.setToolTipText("Workspace settings and configurations");
                settingsViewerItem.addSelectionListener(new SelectionAdapter(){
                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        StageUtils.openUrl(path, false);
                    }
                });
            }
        }

        return toolBar;
    }

    @SuppressWarnings("serial")
    public static ToolBar addLogoutButton( Composite toolbarComposite ) {
        Composite composite = new Composite(toolbarComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        ToolBar toolBar = new ToolBar(composite, SWT.FLAT);
        GridData toolBarGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        toolBar.setLayoutData(toolBarGD);
        // addToolBarSeparator(toolBar);

        final ToolItem aboutItem = new ToolItem(toolBar, SWT.PUSH);
        aboutItem.setImage(ImageCache.getInstance().getImage(toolbarComposite.getDisplay(), ImageCache.HELP));
        aboutItem.setWidth(150);
        aboutItem.setText("About");
        aboutItem.setToolTipText("About this application.");
        aboutItem.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                AboutDialog aboutDialog = new AboutDialog(toolbarComposite.getShell(), "ABOUT");
                aboutDialog.open();
            }
        });
        final ToolItem logoutItem = new ToolItem(toolBar, SWT.PUSH);
        logoutItem.setImage(ImageCache.getInstance().getImage(toolbarComposite.getDisplay(), ImageCache.DISCONNECT));
        logoutItem.setWidth(150);
        logoutItem.setText("Logout");
        logoutItem.setToolTipText("Exit the application.");
        logoutItem.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                GssSession.clearSession();
                String path = StageUtils.getPathforEntryPointID(DashboardEntryPoint.ID);
                StageUtils.openUrl(path, false);
            }
        });

        return toolBar;
    }

}
