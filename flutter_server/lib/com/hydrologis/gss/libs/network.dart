import 'dart:convert';
import 'package:http/http.dart';
import 'dart:typed_data';
import 'dart:html';

import 'package:flutter/material.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:smashlibs/com/hydrologis/flutterlibs/utils/logging.dart';

const NETWORKERROR_PREFIX = "ERROR:";

const DATA_NV_INTERVAL_SECONDS = 600;
const TIMESTAMP_KEY = "ts";
const VALUE_KEY = "v";
const doLocal = String.fromEnvironment('DOLOCAL', defaultValue: 'false');
const WEBAPP_URL = doLocal == 'true' ? "http://localhost:8000" : "";

const API_CONFIGRATION_URL = "$WEBAPP_URL/admin";
const API_LOGIN = "$WEBAPP_URL/api/login";
const API_USERS = "$WEBAPP_URL/api/users";

const API_PROJECTNAMES = "$WEBAPP_URL/api/projectnames";
const API_RENDERNOTES = "$WEBAPP_URL/api/rendernotes";
const API_NOTES = "$WEBAPP_URL/api/notes";
const API_GPSLOGS = "$WEBAPP_URL/api/gpslogs";
const API_RENDERIMAGES = "$WEBAPP_URL/api/renderimages";
const API_IMAGES = "$WEBAPP_URL/api/images";
const API_WMSSOURCES = "$WEBAPP_URL/api/wmssources";
const API_TMSSOURCES = "$WEBAPP_URL/api/tmssources";
const API_USERCONFIGS = "$WEBAPP_URL/api/userconfigurations";

const API_PROJECT_PARAM = "project=";

const LOG = "log";
const LOGTS = "ts";
const LOGTYPE = "type";
const LOGMSG = "msg";

class ServerApi {
  /// Login to get a token using credentials.
  ///
  /// Returns a string starting with ERROR if problems arised.
  static Future<String> login(String user, String pwd, String project) async {
    Map<String, String> formData = {
      "username": user,
      "password": pwd,
      "project": project
    };

    final uri = Uri.parse("$API_LOGIN");
    Response response;
    try {
      response = await post(
        uri,
        headers: {'Content-Type': 'application/json; charset=UTF-8'},
        body: json.encode(formData),
      );
    } catch (e) {
      return NETWORKERROR_PREFIX + "Permission denied.";
    }
    if (response != null && response.statusCode == 200) {
      return json.decode(response.body)['token'];
    } else {
      return NETWORKERROR_PREFIX + response.body;
    }
  }

  static Map<String, String> getTokenHeader() {
    var sessionToken = SmashSession.getSessionToken();
    var requestHeaders = {"Authorization": "Token " + sessionToken};
    return requestHeaders;
  }

  // static Future<String> getDbinfo(String user, String pwd) async {
  //   String apiCall = "$API_DBINFO";

  //   Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
  //   HttpRequest request = await HttpRequest.request(apiCall,
  //       method: 'GET', requestHeaders: requestHeaders);
  //   if (request.status == 200) {
  //     return request.response;
  //   } else {
  //     return null;
  //   }
  // }

  // static Future<String> getData(String user, String pwd,
  //     {List<String> surveyors,
  //     List<String> projects,
  //     fromTo,
  //     matchString}) async {
  //   String apiCall = "$API_DATA";

  //   Map<String, String> formData = {};
  //   if (surveyors != null) {
  //     formData[KEY_SURVEYORS] = surveyors.join(";");
  //   }
  //   if (projects != null) {
  //     formData[KEY_PROJECTS] = projects.join(";");
  //   }

  //   Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
  //   HttpRequest request = await HttpRequest.postFormData(apiCall, formData,
  //       requestHeaders: requestHeaders);
  //   if (request.status == 200) {
  //     return request.response;
  //   } else {
  //     return null;
  //   }
  // }

  // static Future<String> getNoteById(String user, String pwd, int id) async {
  //   String apiCall = "$API_NOTE/$id";

  //   Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
  //   HttpRequest request = await HttpRequest.request(apiCall,
  //       method: 'GET', requestHeaders: requestHeaders);
  //   if (request.status == 200) {
  //     return request.response;
  //   } else {
  //     return null;
  //   }
  // }

  static Future<Uint8List> getImageThumbnail(int id) async {
    var projectName = SmashSession.getSessionProject();
    String apiCall =
        "$API_RENDERIMAGES/$id" + "?" + API_PROJECT_PARAM + projectName;
    ;

    var requestHeaders = getTokenHeader();
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      Map<String, dynamic> imageMap = jsonDecode(request.response);
      var dataString = imageMap[THUMBNAIL];
      var imgData = Base64Decoder().convert(dataString);
      return imgData;
    } else {
      return null;
    }
  }

  // /// Send a user setting to the server.
  // ///
  // /// The setting is identified by the [key].
  // static Future<String> setUserSetting(
  //     String user, String pwd, String key, String value) async {
  //   String apiCall = "$API_USERSETTINGS";

  //   Map<String, String> formData = {};
  //   formData[key] = value;
  //   Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
  //   HttpRequest request = await HttpRequest.postFormData(apiCall, formData,
  //       requestHeaders: requestHeaders);
  //   if (request.status == 200) {
  //     return request.response;
  //   } else {
  //     return null;
  //   }
  // }

  // // Get a single string user setting from the server by its [key].
  // static Future<String> getUserSetting(
  //     String user, String pwd, String key) async {
  //   String apiCall = "$API_USERSETTINGS/$key";

  //   Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
  //   HttpRequest request = await HttpRequest.request(apiCall,
  //       method: 'GET', requestHeaders: requestHeaders);
  //   if (request.status == 200) {
  //     return request.response;
  //   } else {
  //     return null;
  //   }
  // }

  // static Future<String> getImageBytesById(
  //     String user, String pwd, int id) async {
  //   String apiCall = "$API_IMAGE/$id";
  //   Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
  //   HttpRequest request = await HttpRequest.request(apiCall,
  //       method: 'GET', requestHeaders: requestHeaders);
  //   if (request.status == 200) {
  //     print(request.response.runtimeType);
  //     return request.response;
  //   } else {
  //     return null;
  //   }
  // }

  static Future<String> getSurveyorsJson(String user, String pwd) async {
    // String apiCall = "$API_LIST_SURVEYORS";

    // Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    // HttpRequest request = await HttpRequest.request(apiCall,
    //     method: 'GET', requestHeaders: requestHeaders);
    // if (request.status == 200) {
    //   return request.response;
    // } else {
    //   return null;
    // }
  }

  static Future<String> deleteGpsLog(
      String user, String pwd, int gpsLogId) async {
    // Map<String, String> formData = {ID: gpsLogId.toString()};

    // Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    // HttpRequest request = await HttpRequest.postFormData(
    //     API_DELETE_GPSLOG, formData,
    //     requestHeaders: requestHeaders);
    // if (request.status == 200) {
    //   return null;
    // } else {
    //   return request.response;
    // }
  }

  static Future<String> deleteNote(String user, String pwd, int noteId) async {
    // Map<String, String> formData = {ID: noteId.toString()};

    // Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    // HttpRequest request = await HttpRequest.postFormData(
    //     API_DELETE_NOTES, formData,
    //     requestHeaders: requestHeaders);
    // if (request.status == 200) {
    //   return null;
    // } else {
    //   return request.response;
    // }
  }

  static Future<dynamic> getRenderNotes() async {
    var tokenHeader = getTokenHeader();
    var projectName = SmashSession.getSessionProject();
    var url = API_RENDERNOTES + "?" + API_PROJECT_PARAM + projectName;
    HttpRequest request = await HttpRequest.request(url,
        method: 'GET', requestHeaders: tokenHeader);
    if (request.status == 200) {
      var notesList = jsonDecode(request.responseText);
      return notesList;
    } else {
      return null;
    }
  }

  static Future<dynamic> getGpslogs() async {
    var tokenHeader = getTokenHeader();
    var projectName = SmashSession.getSessionProject();
    var url = API_GPSLOGS + "?" + API_PROJECT_PARAM + projectName;
    HttpRequest request = await HttpRequest.request(url,
        method: 'GET', requestHeaders: tokenHeader);
    if (request.status == 200) {
      var notesList = jsonDecode(request.responseText);
      return notesList;
    } else {
      return null;
    }
  }

  static Future<String> getNote(int noteId) async {
    var tokenHeader = getTokenHeader();
    var projectName = SmashSession.getSessionProject();
    var url = API_NOTES + "/$noteId" + "?" + API_PROJECT_PARAM + projectName;
    HttpRequest request = await HttpRequest.request(url,
        method: 'GET', requestHeaders: tokenHeader);
    if (request.status == 200) {
      return request.responseText;
    } else {
      return null;
    }
  }

  static Future<String> getUserName(int userId) async {
    var tokenHeader = getTokenHeader();
    var url = API_USERS + "/$userId";
    HttpRequest request = await HttpRequest.request(url,
        method: 'GET', requestHeaders: tokenHeader);
    if (request.status == 200) {
      var userMap = jsonDecode(request.responseText);
      return userMap['username'];
    } else {
      return null;
    }
  }

  static Future<List<String>> getProjectNames() async {
    HttpRequest request =
        await HttpRequest.request(API_PROJECTNAMES, method: 'GET');
    if (request.status == 200) {
      var list = jsonDecode(request.responseText);
      List<String> namesList =
          List<String>.from(list.map((projectMap) => projectMap['name']));
      return namesList;
    } else {
      return null;
    }
  }

  static Future<Map<String, String>> getUserConfigurations() async {
    Map<String, String> config = {};
    var tokenHeader = getTokenHeader();
    var projectName = SmashSession.getSessionProject();
    var url = API_USERCONFIGS + "?" + API_PROJECT_PARAM + projectName;
    HttpRequest request = await HttpRequest.request(url,
        method: 'GET', requestHeaders: tokenHeader);
    if (request.status == 200) {
      var dataList = jsonDecode(request.responseText);
      for (var item in dataList) {
        var key = item['key'];
        var value = item['value'];
        config[key] = value;
      }
      return config;
    } else {
      return null;
    }
  }

  static Future<String> saveUserConfigurations(tokenHeader, projectName,
      {basemap, mapCenter, bookmarks}) async {
    var url = API_USERCONFIGS + "?" + API_PROJECT_PARAM + projectName;

    List<Map<String, String>> formData = [];
    if (basemap != null) {
      formData.add({'key': KEY_BASEMAP, 'value': basemap});
    }
    if (mapCenter != null) {
      formData.add({'key': KEY_MAPCENTER, 'value': mapCenter});
    }
    if (bookmarks != null) {
      formData.add({'key': KEY_BOOKMARKS, 'value': bookmarks});
    }
    var dataJson = jsonEncode(formData);
    HttpRequest request = await HttpRequest.request(url,
        method: "PUT", sendData: dataJson, requestHeaders: tokenHeader);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<List<TileLayerOptions>> getBackGroundLayers() async {
    List<TileLayerOptions> layers = [];

    try {
      var tokenHeader = getTokenHeader();
      var projectName = SmashSession.getSessionProject();
      var url = API_WMSSOURCES + "?" + API_PROJECT_PARAM + projectName;
      HttpRequest request = await HttpRequest.request(url,
          method: 'GET', requestHeaders: tokenHeader);
      if (request.status == 200) {
        var list = jsonDecode(request.responseText);
        for (var item in list) {
          layers.add(TileLayerOptions(
            additionalOptions: {
              "name": item['name'],
              "Attribution": item['attribution'],
            },
            opacity: item['opacity'],
            backgroundColor: Colors.transparent,
            wmsOptions: WMSTileLayerOptions(
              crs: Epsg3857(),
              version: item['version'],
              transparent: item['transparent'],
              format: item['imageformat'],
              baseUrl: item['getcapabilities'] + "?",
              layers: [item['layername']],
            ),
            overrideTilesWhenUrlChanges: true,
            errorTileCallback: (tile, exception) {
              // ignore tiles that can't load to avoid
              SMLogger().e("Unable to load WMS tile: ${tile.coordsKey}",
                  exception, null);
            },
          ));
        }
      }
      projectName = SmashSession.getSessionProject();
      url = API_TMSSOURCES + "?" + API_PROJECT_PARAM + projectName;
      request = await HttpRequest.request(url,
          method: 'GET', requestHeaders: tokenHeader);
      if (request.status == 200) {
        var list = jsonDecode(request.responseText);
        for (var item in list) {
          layers.add(TileLayerOptions(
            tms: false,
            additionalOptions: {
              "name": item['label'],
              "Attribution": item['attribution'],
            },
            subdomains: item['subdomains'] != null
                ? item['subdomains'].split(',')
                : null,
            maxZoom: item['maxzoom'],
            opacity: item['opacity'],
            urlTemplate: item['urltemplate'],
            backgroundColor: Colors.transparent,
            overrideTilesWhenUrlChanges: true,
            errorTileCallback: (tile, exception) {
              // ignore tiles that can't load to avoid
              SMLogger().e("Unable to load TMS tile: ${tile.coordsKey}",
                  exception, null);
            },
          ));
        }
      }
    } catch (e) {
      SMLogger().e("ERROR", e, null);
      // fallback on OSM and Wikimedia
      layers.add(TileLayerOptions(
        tms: false,
        subdomains: const ['a', 'b', 'c'],
        maxZoom: 19,
        urlTemplate: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
        tileProvider: NetworkNoRetryTileProvider(),
      ));
      layers.add(TileLayerOptions(
        tms: false,
        maxZoom: 19,
        urlTemplate: "https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png",
        tileProvider: NetworkNoRetryTileProvider(),
      ));
    }
    return layers;
  }
}
