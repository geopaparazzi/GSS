import 'dart:convert';
import 'dart:html';

import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter_server/com/hydrologis/gss/variables.dart';

const DATA_NV_INTERVAL_SECONDS = 600;
const TIMESTAMP_KEY = "ts";
const VALUE_KEY = "v";
const WEBAPP_URL = "http://localhost:8080"; // TODO make empty for release

const API_DATA = "$WEBAPP_URL/data";
const API_LIST = "$WEBAPP_URL/list";
const API_LIST_SURVEYORS = "$API_LIST/surveyors";
const API_LOGIN = "$WEBAPP_URL/login";
const API_USERSETTINGS = "$WEBAPP_URL/usersettings";
const API_IMAGE = "$API_DATA/images";
const API_IMAGEDATA = "$WEBAPP_URL/imagedata";

//const SERVER_IP = "172.26.181.138"; // office hydrologis

class ServerApi {
  static Future<String> getData(String user, String pwd,
      {List<String> surveyors, projects, fromTo, matchString}) async {
    String apiCall = "$API_DATA";

    Map<String, String> formData = {};
    if (surveyors != null) {
      formData[KEY_SURVEYORS] = surveyors.join(";");
    }

    String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pwd'));
    HttpRequest request = await HttpRequest.postFormData(apiCall, formData,
        requestHeaders: {"authorization": basicAuth});

//    HttpRequest request = await HttpRequest.request(apiCall,
//        method: 'GET', requestHeaders: {"authorization": basicAuth});

    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> login(String user, String pwd) async {
    String apiCall = "$API_LOGIN";

    String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pwd'));
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: {"authorization": basicAuth});
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
    String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pwd'));
    HttpRequest request = await HttpRequest.postFormData(apiCall, formData,
        requestHeaders: {"authorization": basicAuth});
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> getImageBytesById(
      String user, String pwd, int id) async {
    String apiCall = "$API_IMAGE/$id";
    String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pwd'));
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: {"authorization": basicAuth});
    if (request.status == 200) {
      print(request.response.runtimeType);
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> getSurveyors(String user, String pwd) async {
    String apiCall = "$API_LIST_SURVEYORS";

    String basicAuth = 'Basic ' + base64Encode(utf8.encode('$user:$pwd'));
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: {"authorization": basicAuth});
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

//  /// Send recipe to the server.
//  static Future<HttpRequest> uploadRecipe(
//      String fileName, String recipeContent) async {
//    Map<String, String> data = {
//      "name": fileName,
//      "recipe": recipeContent,
//    };
//    HttpRequest response =
//    await HttpRequest.postFormData(API_RECIPE_UPLOAD, data);
//    return response;
//  }
}
