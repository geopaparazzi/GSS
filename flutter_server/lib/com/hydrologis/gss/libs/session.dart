import 'dart:convert' as JSON;
import 'dart:html' as html;

import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';

class SmashSession {
  /// Checks credentials and returns an error message or null if login is ok.
  static Future<String> login(
      String user, String password, String project) async {
    var responseText = await ServerApi.login(user, password, project);
    if (!responseText.startsWith(NETWORKERROR_PREFIX)) {
      var token = responseText;
      setSessionToken(token);
      setSessionUser(user, password, project);

      // var jsonMap = JSON.jsonDecode(responsJson);
      // var hasPermission = jsonMap[KEY_HASPERMISSION];
      // if (hasPermission) {
      //   setSessionUser(user, password);
      //   var isAdmin = jsonMap[KEY_ISADMIN];
      //   if (isAdmin != null) {
      //     html.window.sessionStorage[KEY_ISADMIN] = "$isAdmin";
      //   } else {
      //     html.window.sessionStorage[KEY_ISADMIN] = "false";
      //   }

      //   var lastBasemap = jsonMap[KEY_BASEMAP];
      //   if (lastBasemap != null) {
      //     html.window.sessionStorage[KEY_BASEMAP] = "$lastBasemap";
      //   }
      //   var lastMapcenter = jsonMap[KEY_MAPCENTER];
      //   if (lastMapcenter != null) {
      //     html.window.sessionStorage[KEY_MAPCENTER] = "$lastMapcenter";
      //   }
      // }
      return null;
    } else {
      return responseText;
    }
  }

  static bool isLogged() {
    return html.window.sessionStorage[KEY_TOKEN] != null;
  }

  static bool isAdmin() {
    String isAdminStr = html.window.sessionStorage[KEY_ISADMIN];
    return isAdminStr != null && isAdminStr.toLowerCase() == 'true';
  }

  static void setSessionUser(String user, String pwd, String project) {
    html.window.sessionStorage[KEY_USER] = user;
    html.window.sessionStorage[KEY_PWD] = pwd;
    html.window.sessionStorage[KEY_PROJECT] = project;
  }

  static void setSessionToken(String token) {
    html.window.sessionStorage[KEY_TOKEN] = token;
  }

  static String getSessionToken() {
    var token = html.window.sessionStorage[KEY_TOKEN];
    if (token == null) {
      html.window.location.reload();
    }
    return token;
  }

  static String getSessionProject() {
    var project = html.window.sessionStorage[KEY_PROJECT];
    if (project == null) {
      html.window.sessionStorage.remove(KEY_TOKEN);
      html.window.location.reload();
    }
    return project;
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
    String token = html.window.sessionStorage.remove(KEY_TOKEN);
    html.window.sessionStorage.remove(KEY_ISADMIN);
    String baseMap = html.window.sessionStorage.remove(KEY_BASEMAP);
    html.window.sessionStorage.remove(KEY_MAPCENTER);

    await ServerApi.logout(user, pwd, basemap: baseMap, mapCenter: mapCenter);
  }
}
