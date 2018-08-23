/*******************************************************************************
 * Copyright (c) 2018 HydroloGIS S.r.l.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.hydrologis.gss;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class GssApplication4Export implements IApplication {

    public static final String ID = "com.hydrologis.gss.GssApplication4Export";

    @Override
    public Object start( IApplicationContext context ) throws Exception {
        while( true ) {
            Thread.sleep(1000);
        }
    }

    @Override
    public void stop() {
    }
}
