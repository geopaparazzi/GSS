/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package com.hydrologis.gss.entrypoints;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.widgets.Composite;

import eu.hydrologis.stage.libs.utils.StageUtils;

/**
 * The main entry point.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class MainEntryPoint extends AbstractEntryPoint {
    @Override
    protected void createContents( final Composite parent ) {
        String url = StageUtils.getPathforEntryPointID(DashboardEntryPoint.ID);
        StageUtils.openUrl(url, false);
    }
}
