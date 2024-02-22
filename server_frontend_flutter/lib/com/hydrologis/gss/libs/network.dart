import 'dart:convert';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:http/http.dart';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:smashlibs/com/hydrologis/flutterlibs/utils/logging.dart';
import 'package:smashlibs/smashlibs.dart';
import 'dart:html' as html;

const NETWORKERROR_PREFIX = "ERROR:";

const DATA_NV_INTERVAL_SECONDS = 600;
const TIMESTAMP_KEY = "ts";
const VALUE_KEY = "v";
const doLocal = String.fromEnvironment('DOLOCAL', defaultValue: 'false');
const WEBAPP_URL = doLocal == 'true' ? "http://localhost:8000/" : "../";

// const API_CONFIGRATION_URL = "${WEBAPP_URL}admin/";
const API_LOGIN = "${WEBAPP_URL}api/login/";
const API_CSRF = "${WEBAPP_URL}api/csrf/";
const API_USERS = "${WEBAPP_URL}api/users/";

const API_PROJECTNAMES = "${WEBAPP_URL}api/projectnames/";
const API_RENDERNOTES = "${WEBAPP_URL}api/rendernotes/";
const API_LASTUSERPOSITIONS = "${WEBAPP_URL}api/lastuserpositions/";
const API_NOTES = "${WEBAPP_URL}api/notes/";
const API_GPSLOGS = "${WEBAPP_URL}api/gpslogs/";
const API_RENDERIMAGES = "${WEBAPP_URL}api/renderimages/";
const API_RENDERSIMPLEIMAGES = "${WEBAPP_URL}api/rendersimpleimages/";
// const API_IMAGES = "${WEBAPP_URL}api/images/";
const API_WMSSOURCES = "${WEBAPP_URL}api/wmssources/";
const API_TMSSOURCES = "${WEBAPP_URL}api/tmssources/";
const API_USERCONFIGS = "${WEBAPP_URL}api/userconfigurations/";
const API_FORMNAMES = "${WEBAPP_URL}api/formnames/";
const API_FORMS = "${WEBAPP_URL}api/forms/";

// const API_PROJECT_PARAM = "project=";

const LOG = "log";
const LOGTS = "ts";
const LOGTYPE = "type";
const LOGMSG = "msg";

class WebServerApi {
  /// Wrapper of post to add csrf token.
  static Future<Response> csrfPost(Uri uri,
      {Map<String, String>? headers, Object? body, Encoding? encoding}) async {
    // add csrf token for posts
    var csrfToken = SmashSession.getCsrfToken();
    if (csrfToken != null) {
      if (headers == null) {
        headers = {};
      }
      headers["X-CSRFToken"] = csrfToken;
    }

    return await post(uri, headers: headers, body: body);
  }

  /// Login to get a token using credentials.
  ///
  /// Returns a string starting with ERROR if problems arised.
  static Future<String> login(String user, String pwd, int projectId) async {
    Map<String, dynamic> formData = {
      "username": user,
      "password": pwd,
      "project": projectId
    };

    final uri = Uri.parse("$API_LOGIN");
    Response response;
    try {
      response = await csrfPost(
        uri,
        headers: {'Content-Type': 'application/json; charset=UTF-8'},
        body: json.encode(formData),
      );
    } catch (e) {
      return NETWORKERROR_PREFIX + "{\"error\": \"${e.toString()}\"}";
    }
    if (response.statusCode == 200) {
      return json.decode(response.body)['token'];
    } else {
      return NETWORKERROR_PREFIX + response.body;
    }
  }

  static Map<String, String> getTokenHeader() {
    var sessionToken = SmashSession.getSessionToken();
    var requestHeaders = {"Authorization": "Token " + sessionToken!};
    return requestHeaders;
  }

  static Future<Uint8List?> getImageThumbnail(int id) async {
    var project = SmashSession.getSessionProject();
    var uri =
        Uri.parse("$API_RENDERIMAGES$id/" + "?$API_PROJECT_PARAM${project.id}");
    var requestHeaders = getTokenHeader();

    var response = await get(uri, headers: requestHeaders);
    if (response.statusCode == 200) {
      Map<String, dynamic> imageMap = jsonDecode(response.body);
      var dataString = imageMap[THUMBNAIL];
      var imgData = Base64Decoder().convert(dataString);
      return imgData;
    } else {
      return null;
    }
  }

  static Future<List<dynamic>?> getRenderImages() async {
    var project = SmashSession.getSessionProject();
    var uri =
        Uri.parse("$API_RENDERSIMPLEIMAGES?$API_PROJECT_PARAM${project.id}");
    var requestHeaders = getTokenHeader();

    var response = await get(uri, headers: requestHeaders);
    if (response.statusCode == 200) {
      List<dynamic> imagesList = jsonDecode(response.body);
      return imagesList;
    } else {
      return null;
    }
  }

  static Future<dynamic> getRenderNotes() async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse(API_RENDERNOTES + "?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      var notesList = jsonDecode(response.body);
      return notesList;
    } else {
      return null;
    }
  }

  static Future<List<dynamic>?> getLastUserPositions() async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri =
        Uri.parse(API_LASTUSERPOSITIONS + "?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      var positionsList = jsonDecode(response.body);
      return positionsList;
    } else {
      return null;
    }
  }

  static Future<dynamic> getGpslogs() async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse(API_GPSLOGS + "?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      var notesList = jsonDecode(response.body);
      return notesList;
    } else {
      return null;
    }
  }

  static Future<String?> getNote(int noteId) async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse(API_NOTES + "$noteId/?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      return response.body;
    } else {
      return null;
    }
  }

  static Future<String?> getUserName(int userId) async {
    var tokenHeader = getTokenHeader();
    var uri = Uri.parse(API_USERS + "$userId/");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      var userMap = jsonDecode(response.body);
      return userMap['username'];
    } else {
      return null;
    }
  }

  static Future<List<WebProject>> getProjects() async {
    try {
      var response = await get(Uri.parse(API_PROJECTNAMES));
      if (response.statusCode == 200) {
        var list = jsonDecode(response.body);
        List<WebProject> projectsList = List<WebProject>.from(
            list.map((projectMap) => WebProject.fromMap(projectMap)));
        return projectsList;
      } else {
        throw new StateError(response.body);
      }
    } on Exception catch (e) {
      print(e);
      return [];
    }
  }

  static Future<Map<int, String>> getFormNames() async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();

    var uri = Uri.parse("$API_FORMNAMES?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      var formsList = jsonDecode(response.body);
      // get names from list of maps
      var id2namesMap = <int, String>{};
      for (var formMap in formsList) {
        id2namesMap[formMap['id']] = formMap['name'];
      }
      return id2namesMap;
    } else {
      return {};
    }
  }

  static Future<ServerForm?> getForm(int id) async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse("$API_FORMS$id?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      dynamic formMap = jsonDecode(response.body);
      if (formMap is List && formMap.isNotEmpty) {
        formMap = formMap[0];
      }
      ServerForm form = ServerForm.fromMap(formMap);
      return form;
    } else {
      return null;
    }
  }

  static Future<String?> putForm(ServerForm form,
      {bool onlyRename = false}) async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri =
        Uri.parse("$API_FORMS${form.id}/?$API_PROJECT_PARAM${project.id}");
    var formMap = form.toMap();
    if (onlyRename) {
      formMap = form.toMapForRename();
    }
    var response =
        await put(uri, headers: tokenHeader, body: jsonEncode(formMap));
    if (response.statusCode == 200) {
      return null;
    } else {
      return response.body;
    }
  }

  static Future<String?> postForm(ServerForm form,
      {bool onlyRename = false}) async {
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse("$API_FORMS?$API_PROJECT_PARAM${project.id}");
    var formMap = form.toMap();
    if (onlyRename) {
      formMap = form.toMapForRename();
    }
    var response =
        await put(uri, headers: tokenHeader, body: jsonEncode(formMap));
    if (response.statusCode == 200) {
      var responseMap = jsonDecode(response.body);
      var newFormId = responseMap['id'];
      form.id = newFormId;
      return null;
    } else {
      return response.body;
    }
  }

  static Future<Map<String, String>?> getUserConfigurations() async {
    Map<String, String> config = {};
    var tokenHeader = getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse(API_USERCONFIGS + "?$API_PROJECT_PARAM${project.id}");
    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      var dataList = jsonDecode(response.body);
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

  static Future<String?> saveUserConfigurations(tokenHeader, WebProject project,
      {basemap, mapCenter, bookmarks}) async {
    var uri = Uri.parse(API_USERCONFIGS + "?$API_PROJECT_PARAM${project.id}");

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
    var formDataMap = {'configurations': formData};
    var dataJson = jsonEncode(formDataMap);
    Response response;
    try {
      tokenHeader["Content-type"] = "application/json";
      response = await csrfPost(uri, body: dataJson, headers: tokenHeader);
      if (response.statusCode == 200) {
        return response.body;
      } else {
        return null;
      }
    } catch (e) {
      print(e);
      return null;
    }
  }

  static Future<Map<String, Widget>> getBackGroundLayers() async {
    Map<String, Widget> layers = {};

    bool error = false;
    try {
      var tokenHeader = getTokenHeader();
      var project = SmashSession.getSessionProject();
      var uri = Uri.parse(API_WMSSOURCES + "?$API_PROJECT_PARAM${project.id}");
      var response = await get(uri, headers: tokenHeader);
      if (response.statusCode == 200) {
        var list = jsonDecode(response.body);
        for (var item in list) {
          var epsg = item['epsg'];
          var crs = epsg == 4326 ? Epsg4326() : Epsg3857();
          layers[item['label']] = Opacity(
            opacity: item['opacity'],
            child: TileLayer(
              additionalOptions: {
                "name": item['label'],
                "Attribution": item['attribution'],
              },
              maxZoom: 21,
              maxNativeZoom: 21,
              backgroundColor: Colors.transparent,
              wmsOptions: WMSTileLayerOptions(
                crs: crs,
                version: item['version'],
                transparent: item['transparent'],
                format: item['imageformat'],
                baseUrl: item['getcapabilities'] + "?",
                layers: [item['layername']],
              ),
              // overrideTilesWhenUrlChanges: true,
              errorTileCallback: (tile, exception, stacktrace) {
                // ignore tiles that can't load to avoid
                SMLogger().e("Unable to load WMS tile: ${tile.coordinatesKey}",
                    null, stacktrace);
              },
            ),
          );
        }
      }
      project = SmashSession.getSessionProject();
      uri = Uri.parse(API_TMSSOURCES + "?$API_PROJECT_PARAM${project.id}");
      response = await get(uri, headers: tokenHeader);
      if (response.statusCode == 200) {
        var list = jsonDecode(response.body);
        for (var item in list) {
          layers[item['label']] = Opacity(
            opacity: item['opacity'],
            child: TileLayer(
              tms: false,
              additionalOptions: {
                "name": item['label'],
                "Attribution": item['attribution'],
              },
              subdomains: item['subdomains'] != null
                  ? item['subdomains'].split(',')
                  : [],
              maxZoom: item['maxzoom'],
              urlTemplate: item['urltemplate'],
              backgroundColor: Colors.transparent,
              // overrideTilesWhenUrlChanges: true,
              errorTileCallback: (tile, exception, stacktrace) {
                // ignore tiles that can't load to avoid
                SMLogger().e("Unable to load TMS tile: ${tile.coordinatesKey}",
                    null, stacktrace);
              },
            ),
          );
        }
      }
    } catch (e) {
      error = true;
      if (e is Exception) {
        SMLogger().e("ERROR", e, null);
      } else {
        SMLogger().e(e.toString(), null, null);
      }
    }
    if (error || layers.isEmpty) {
      // fallback on OSM
      layers[DEFAULTLAYERNAME] = getDefaultLayer();
    }
    return layers;
  }

  static TileLayer getDefaultLayer() {
    return TileLayer(
      // tms: false,
      maxZoom: 21,
      maxNativeZoom: 16,
      urlTemplate: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
      tileProvider: NetworkNoRetryTileProvider(),
    );
  }
}

class WebProject {
  int? id;
  String? name;

  String toJsonString() {
    return jsonEncode(toMap());
  }

  Map toMap() {
    return {
      'name': name,
      'id': id,
    };
  }

  static WebProject fromJson(String projectJson) {
    var projectMap = jsonDecode(projectJson);
    return fromMap(projectMap);
  }

  static WebProject fromMap(Map<String, dynamic> projectMap) {
    return WebProject()
      ..id = projectMap['id']
      ..name = projectMap['name'];
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) || other is WebProject && id == other.id;

  @override
  int get hashCode => id.hashCode;
}
