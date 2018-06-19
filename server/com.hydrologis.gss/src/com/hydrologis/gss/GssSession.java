package com.hydrologis.gss;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.RWT;

public class GssSession {

    public static final String KEY_USERNAME = "GSS_KEY_USERNAME";
    public static final String KEY_LOADED_GPAPUSERS = "GSS_KEY_LOADED_GPAPUSERS";
    public static final String KEY_DEVICES_VISIBLE = "KEY_DEVICES_VISIBLE";

    public static Optional<String> getUniqueUserName() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        return Optional.ofNullable((String) httpSession.getAttribute(KEY_USERNAME));
    }

    public static void setUniqueUserName( String name ) {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        httpSession.setAttribute(KEY_USERNAME, name);
    }

    public static String[] getLoadedGpapUsers() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        return (String[]) httpSession.getAttribute(KEY_LOADED_GPAPUSERS);
    }

    public static void setLoadedGpapUsers( String[] loadedGpspUsers ) {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        httpSession.setAttribute(KEY_LOADED_GPAPUSERS, loadedGpspUsers);
    }

    public static boolean areDevicesVisible() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        String attribute = (String) httpSession.getAttribute(KEY_DEVICES_VISIBLE);
        return Boolean.parseBoolean(attribute);
    }

    public static void setDevicesVisible( boolean areVisible ) {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        httpSession.setAttribute(KEY_DEVICES_VISIBLE, areVisible ? "true" : "false");
    }

    public static void clearSession() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        httpSession.removeAttribute(KEY_USERNAME);
        httpSession.removeAttribute(KEY_LOADED_GPAPUSERS);
    }

}
