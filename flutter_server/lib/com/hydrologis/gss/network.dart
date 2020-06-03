import 'dart:convert';
import 'dart:html';
import 'dart:typed_data';

import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter_server/com/hydrologis/gss/variables.dart';

const DATA_NV_INTERVAL_SECONDS = 600;
const TIMESTAMP_KEY = "ts";
const VALUE_KEY = "v";
const WEBAPP_URL = "http://localhost:8080"; // TODO make empty for release

const API_DATA = "$WEBAPP_URL/data";
const API_LIST = "$WEBAPP_URL/list";
const API_LIST_SURVEYORS = "$API_LIST/surveyors";
const API_LIST_PROJECTS = "$API_LIST/projects";
const API_LOGIN = "$WEBAPP_URL/login";
const API_USERSETTINGS = "$WEBAPP_URL/usersettings";
const API_IMAGES = "$API_DATA/images";
const API_IMAGEDATA = "$API_DATA/imagedata";
// const API_IMAGEDATA = "$WEBAPP_URL/imagedata";
const API_NOTE = "$API_DATA/notes";

//const SERVER_IP = "172.26.181.138"; // office hydrologis

class ServerApi {
  static Map<String, String> getAuthRequestHeader(String user, String pwd) {
    String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pwd'));
    var requestHeaders = {"authorization": basicAuth};
    return requestHeaders;
  }

  static Future<String> getData(String user, String pwd,
      {List<String> surveyors,
      List<String> projects,
      fromTo,
      matchString}) async {
    String apiCall = "$API_DATA";

    Map<String, String> formData = {};
    if (surveyors != null) {
      formData[KEY_SURVEYORS] = surveyors.join(";");
    }
    if (projects != null) {
      formData[KEY_PROJECTS] = projects.join(";");
    }

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(apiCall, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> getNoteById(String user, String pwd, int id) async {
    String apiCall = "$API_NOTE/$id";

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<Uint8List> getImageThumbnailById(
      String user, String pwd, int id) async {
    String apiCall = "$API_IMAGES/$id";

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      Map<String, dynamic> imageMap = jsonDecode(request.response);
      var dataString = imageMap[DATA];
      var imgData = Base64Decoder().convert(dataString);
      return imgData;
    } else {
      return null;
    }
  }

  static Future<String> login(String user, String pwd) async {
    String apiCall = "$API_LOGIN";

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> logout(String user, String pwd,
      {basemap = "Mapsforge", mapCenter = "0;0;6"}) async {
    String apiCall = "$API_USERSETTINGS";

    Map<String, String> formData = {};
    if (basemap != null) {
      formData[KEY_BASEMAP] = basemap;
    }
    if (mapCenter != null) {
      formData[KEY_MAPCENTER] = mapCenter;
    }
    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(apiCall, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

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

  static Future<String> getSurveyors(String user, String pwd) async {
    String apiCall = "$API_LIST_SURVEYORS";

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> getProjects(String user, String pwd) async {
    String apiCall = "$API_LIST_PROJECTS";

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }
}
