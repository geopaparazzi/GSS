package com.hydrologis.gss.server;

import com.vaadin.server.VaadinSession;

public class GssSession {

    public static final String KEY_LOADED_GPAPUSERS = "GSS_KEY_LOADED_GPAPUSERS";

    public static String[] getLoadedGpapUsers() {
        return (String[]) VaadinSession.getCurrent().getAttribute(KEY_LOADED_GPAPUSERS);
    }

    public static void setLoadedGpapUsers( String[] loadedGpspUsers ) {
        VaadinSession.getCurrent().setAttribute(KEY_LOADED_GPAPUSERS, loadedGpspUsers);
    }
}
