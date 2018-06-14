package com.hydrologis.gss;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.RWT;

public class GssSession {

    public static final String KEY_USERNAME = "GSS_KEY_USERNAME";

    public static Optional<String> getUniqueUserName() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        return Optional.ofNullable((String) httpSession.getAttribute(KEY_USERNAME));
    }

    public static void setUniqueUserName( String name ) {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        httpSession.setAttribute(KEY_USERNAME, name);
    }

    public static void clearSession() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        httpSession.removeAttribute(KEY_USERNAME);
    }

}
