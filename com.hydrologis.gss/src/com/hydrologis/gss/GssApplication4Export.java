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