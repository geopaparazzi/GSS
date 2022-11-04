import 'dart:convert' as JSON;
import 'dart:html' as html;
import 'dart:convert';

import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';

class SmashSession {
  /// Checks credentials and returns an error message or null if login is ok.
  static Future<String> login(
      String user, String password, Project project) async {
    var responseText = await ServerApi.login(user, password, project.id);
    if (!responseText.startsWith(NETWORKERROR_PREFIX)) {
      var token = responseText;
      setSessionToken(token);
      setSessionUser(user, password, project);

      var configMap = await ServerApi.getUserConfigurations();
      var lastBasemap = configMap[KEY_BASEMAP];
      if (lastBasemap != null) {
        html.window.sessionStorage[KEY_BASEMAP] = lastBasemap;
      }
      var lastMapcenter = configMap[KEY_MAPCENTER];
      if (lastMapcenter != null) {
        html.window.sessionStorage[KEY_MAPCENTER] = lastMapcenter;
      }
      var bookmarks = configMap[KEY_BOOKMARKS];
      if (bookmarks != null) {
        html.window.sessionStorage[KEY_BOOKMARKS] = bookmarks;
      }
      return null;
    } else {
      var errorMap =
          jsonDecode(responseText.replaceFirst(NETWORKERROR_PREFIX, ""));
      var errorText = errorMap['error'] ?? responseText;
      return errorText;
    }
  }

  static bool isLogged() {
    return html.window.sessionStorage[KEY_TOKEN] != null;
  }

  static List<String> getSessionUser() {
    return [
      html.window.sessionStorage[KEY_USER],
      html.window.sessionStorage[KEY_PWD]
    ];
  }

  static void setSessionUser(String user, String pwd, Project project) {
    html.window.sessionStorage[KEY_USER] = user;
    html.window.sessionStorage[KEY_PWD] = pwd;
    html.window.sessionStorage[KEY_PROJECT] = project.toJsonString();
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

  static Project getSessionProject() {
    var projectJson = html.window.sessionStorage[KEY_PROJECT];
    if (projectJson == null) {
      html.window.sessionStorage.remove(KEY_TOKEN);
      html.window.location.reload();
    }
    return Project.fromJson(projectJson);
  }

  static void setBasemap(String basemap) {
    html.window.sessionStorage[KEY_BASEMAP] = basemap;
  }

  static String getBasemap() {
    return html.window.sessionStorage[KEY_BASEMAP] ??= "Openstreetmap";
  }

  static void setBookmarks(String bookmarksString) {
    html.window.sessionStorage[KEY_BOOKMARKS] = bookmarksString;
  }

  static String getBookmarks() {
    return html.window.sessionStorage[KEY_BOOKMARKS] ??= "";
  }

  static void setMapcenter(double lon, double lat, double zoom) {
    html.window.sessionStorage[KEY_MAPCENTER] = "$lon;$lat;$zoom";
  }

  static List<double> getMapcenter() {
    String xyz = html.window.sessionStorage[KEY_MAPCENTER];
    if (xyz == null) {
      return null;
    }
    var split = xyz.split(";");
    return [
      double.parse(split[0]),
      double.parse(split[1]),
      double.parse(split[2]),
    ];
  }

  static void logout({mapCenter}) async {
    var tokenheader = ServerApi.getTokenHeader();
    var projectName = getSessionProject();

    html.window.sessionStorage.remove(KEY_USER);
    html.window.sessionStorage.remove(KEY_PWD);
    html.window.sessionStorage.remove(KEY_TOKEN);
    String baseMap = html.window.sessionStorage.remove(KEY_BASEMAP);
    html.window.sessionStorage.remove(KEY_MAPCENTER);
    String bookmarks = html.window.sessionStorage.remove(KEY_BOOKMARKS);

    await ServerApi.saveUserConfigurations(tokenheader, projectName,
        basemap: baseMap, mapCenter: mapCenter, bookmarks: bookmarks);
  }
}
