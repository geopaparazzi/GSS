package com.hydrologis.kukuratus.gss;

public interface Vars {

    final static int SECTOR_COUNT = 16;

    final static String TEMPERATURE_ID = "0";
    final static String HUMIDITY_ID = "1";

    final static int WEBSOCKET_PORT = 8081;
    final static String WEBSOCKET_IP = "localhost";// "172.26.181.138";
    final static int WEBAPP_PORT = 8080;
    final static int MEASURESWRITINGINTERVALSECONDS = 60;

    final static String KEY_HASPERMISSION = "hasPermission";
    final static String KEY_ISADMIN = "isAdmin";
    final static String KEY_USER = "user";
    final static String KEY_PWD = "pwd";
    final static String KEY_BASEMAP = "basemap";
    final static String KEY_MAPCENTER = "mapcenter_xyz";
    final static String KEY_BOOKMARKS = "bookmarks";

    static final String SURVEYORS = "surveyors";
    static final String PROJECTS = "projects";
    static final String WEBUSERS = "webusers";
    static final String FORMS = "forms";
    static final String GPSLOGS = "gpslogs";
    static final String ID = "id";

    static final String AUTHORIZATION = "Authorization";

}
