import 'dart:convert' as JSON;
import 'dart:html' as html;

import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';

class SmashSession {
  static Future<bool> login(String user, String password) async {
    var responsJson = await ServerApi.login(user, password);
    if (responsJson != null) {
      var jsonMap = JSON.jsonDecode(responsJson);
      var hasPermission = jsonMap[KEY_HASPERMISSION];
      if (hasPermission) {
        setSessionUser(user, password);
        var isAdmin = jsonMap[KEY_ISADMIN];
        if (isAdmin != null) {
          html.window.sessionStorage[KEY_ISADMIN] = "$isAdmin";
        } else {
          html.window.sessionStorage[KEY_ISADMIN] = "false";
        }

        var lastBasemap = jsonMap[KEY_BASEMAP];
        if (lastBasemap != null) {
          html.window.sessionStorage[KEY_BASEMAP] = "$lastBasemap";
        }
        var lastMapcenter = jsonMap[KEY_MAPCENTER];
        if (lastMapcenter != null) {
          html.window.sessionStorage[KEY_MAPCENTER] = "$lastMapcenter";
        }
      }
      return hasPermission;
    } else {
      return false;
    }
  }

  static bool isLogged() {
    return html.window.sessionStorage[KEY_USER] != null;
  }

  static bool isAdmin() {
    String isAdminStr = html.window.sessionStorage[KEY_ISADMIN];
    return isAdminStr != null && isAdminStr.toLowerCase() == 'true';
  }

  static void setSessionUser(String user, String pwd) {
    html.window.sessionStorage[KEY_USER] = user;
    html.window.sessionStorage[KEY_PWD] = pwd;
  }

  static void setBasemap(String basemap) {
    html.window.sessionStorage[KEY_BASEMAP] = basemap;
  }

  static String getBasemap() {
    return html.window.sessionStorage[KEY_BASEMAP] ??= "Openstreetmap";
  }

  static void setMapcenter(double lon, double lat, double zoom) {
    html.window.sessionStorage[KEY_MAPCENTER] = "$lon;$lat;$zoom";
  }

  static List<double> getMapcenter() {
    String xyz = html.window.sessionStorage[KEY_MAPCENTER];
    if (xyz == null) {
      return [0, 0, 1];
    }
    var split = xyz.split(";");
    return [
      double.parse(split[0]),
      double.parse(split[1]),
      double.parse(split[2]),
    ];
  }

  static List<String> getSessionUser() {
    return [
      html.window.sessionStorage[KEY_USER],
      html.window.sessionStorage[KEY_PWD]
    ];
  }

  static void logout({mapCenter}) async {
    String user = html.window.sessionStorage.remove(KEY_USER);
    String pwd = html.window.sessionStorage.remove(KEY_PWD);
    html.window.sessionStorage.remove(KEY_ISADMIN);
    String baseMap = html.window.sessionStorage.remove(KEY_BASEMAP);
    html.window.sessionStorage.remove(KEY_MAPCENTER);

    await ServerApi.logout(user, pwd, basemap: baseMap, mapCenter: mapCenter);
  }
}
