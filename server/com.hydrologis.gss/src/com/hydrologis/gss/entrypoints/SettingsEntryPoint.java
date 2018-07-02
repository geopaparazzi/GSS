/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package com.hydrologis.gss.entrypoints;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.hydrologis.gss.entrypoints.settings.DevicesUIHandler;
import com.hydrologis.gss.entrypoints.settings.OtherConfigsUIHandler;
import com.hydrologis.gss.utils.GssGuiUtilities;
import com.hydrologis.gss.utils.GssLoginDialog;
import com.hydrologis.gss.utils.GssUserPermissionsHandler;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.stage.libs.entrypoints.StageEntryPoint;
import eu.hydrologis.stage.libs.entrypoints.fragments.MapsUIHandler;
import eu.hydrologis.stage.libs.entrypoints.fragments.WebUsersUIHandler;
import eu.hydrologis.stage.libs.registry.RegistryHandler;
import eu.hydrologis.stage.libs.utils.StageUtils;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SettingsEntryPoint extends StageEntryPoint {
    public static final String ID = "com.hydrologis.gss.entrypoints.SettingsEntryPoint";

    private static final long serialVersionUID = 1L;

    private Composite mainComposite;

    private eu.hydrologis.stage.libs.registry.User loggedUser;

    @Override
    protected void createPage( final Composite parent ) {
        loggedUser = GssLoginDialog.checkUserLogin(getShell());
        boolean isAdmin = RegistryHandler.INSTANCE.isAdmin(loggedUser);
        GssUserPermissionsHandler permissionsHandler = new GssUserPermissionsHandler(isAdmin);
        if (loggedUser == null || !permissionsHandler.isAllowed(redirectUrl)) {
            StageUtils.permissionDeniedPage(parent, redirectUrl);
            return;
        }

        String name = loggedUser.getName();
        String uniquename = loggedUser.getUniqueName();
        StageUtils.logAccessIp(redirectUrl, "user:" + uniquename);

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
        GssGuiUtilities.addHorizontalFiller(horizToolbarComposite);
        GssGuiUtilities.addVerticalFiller(vertToolbarComposite);
        GssGuiUtilities.addAdminTools(vertToolbarComposite, this, isAdmin);
        GssGuiUtilities.addLogoutButton(horizToolbarComposite);

        mainComposite = new Composite(composite, SWT.None);
        GridLayout mainLayout = new GridLayout(3, true);
        mainLayout.marginWidth = 5;
        mainLayout.horizontalSpacing = 15;
        mainLayout.marginHeight = 5;
        mainComposite.setLayout(mainLayout);
        GridData mainGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        mainComposite.setLayoutData(mainGD);

        Group buttonsGroup = new Group(mainComposite, SWT.NONE);
        buttonsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        buttonsGroup.setLayout(new GridLayout(1, true));
        buttonsGroup.setText("Settings");

        Group parametersComposite = new Group(mainComposite, SWT.NONE);
        GridData parametersCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        parametersCompositeGD.horizontalSpan = 2;
        parametersComposite.setText("Parameters");
        parametersComposite.setLayoutData(parametersCompositeGD);
        StackLayout parametersStackLayout = new StackLayout();
        parametersComposite.setLayout(parametersStackLayout);

        WebUsersUIHandler webUsersHandler = new WebUsersUIHandler();
        webUsersHandler.buildGui(buttonsGroup, parametersComposite, loggedUser);

        DevicesUIHandler devicesUIHandler = new DevicesUIHandler();
        devicesUIHandler.buildGui(buttonsGroup, parametersComposite);

        double centerX = 12.4853;
        double centerY = 41.8685;
        Envelope env = new Envelope(new Coordinate(centerX, centerY));
        env.expandBy(0.1);
        MapsUIHandler mapsUIHandler = new MapsUIHandler(env);
        mapsUIHandler.buildGui(buttonsGroup, parametersComposite);

        OtherConfigsUIHandler otherConfigsUIHandler = new OtherConfigsUIHandler();
        otherConfigsUIHandler.buildGui(buttonsGroup, parametersComposite, permissionsHandler);

        addFillLabel(buttonsGroup);

        GssGuiUtilities.addFooter(name, composite);
    }

    private void addFillLabel( Composite mainComposite ) {
        Label filler = new Label(mainComposite, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

}
