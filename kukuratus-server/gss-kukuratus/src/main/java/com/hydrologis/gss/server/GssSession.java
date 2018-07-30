package com.hydrologis.gss.server;

import com.vaadin.server.VaadinSession;

public class GssSession {

    public static final String KEY_LOADED_GPAPUSERS = "GSS_KEY_LOADED_GPAPUSERS";
    public static final String KEY_LAST_MAP_POSITION = "GSS_KEY_LAST_MAP_POSITION";

    public static String[] getLoadedGpapUsers() {
        return (String[]) VaadinSession.getCurrent().getAttribute(KEY_LOADED_GPAPUSERS);
    }

    public static void setLoadedGpapUsers( String[] loadedGpspUsers ) {
        VaadinSession.getCurrent().setAttribute(KEY_LOADED_GPAPUSERS, loadedGpspUsers);
    }

    public static void setLastMapPosition( double[] lonLatZoom ) {
        VaadinSession.getCurrent().setAttribute(KEY_LAST_MAP_POSITION, lonLatZoom);
    }

    public static double[] getLastMapPosition() {
        return (double[]) VaadinSession.getCurrent().getAttribute(KEY_LAST_MAP_POSITION);
    }
}
