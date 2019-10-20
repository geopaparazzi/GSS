import 'dart:convert' as JSON;
import 'dart:html';
import 'dart:html' as HTML;

import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter_server/com/hydrologis/gss/variables.dart';

const DATA_NV_INTERVAL_SECONDS = 600;
const TIMESTAMP_KEY = "ts";
const VALUE_KEY = "v";
const WEBAPP_URL = "http://localhost:8080"; // TODO make empty for release

const API_DATA = "$WEBAPP_URL/data";
const API_LOGIN = "$WEBAPP_URL/login";
const API_IMAGE = "$API_DATA/images";
const API_IMAGEDATA = "$WEBAPP_URL/imagedata";

//const SERVER_IP = "172.26.181.138"; // office hydrologis

class ServerApi {
  static Future<String> getData({users, from, to}) async {
    String apiCall = "$API_DATA";
    String values = await HttpRequest.getString(apiCall);
    return values;
  }

  static Future<String> login(String user, String pwd) async {
    String apiCall = "$API_LOGIN";

    Map<String, String> formData = {
      "user": user,
      "pwd": pwd,
    };

    HttpRequest request = await HttpRequest.postFormData(apiCall, formData);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> getImageBytesById(int id) async {
    String apiCall = "$API_IMAGE/$id";

    HttpRequest request = await HttpRequest.request(apiCall, method: 'GET');
    if (request.status == 200) {
      print(request.response.runtimeType);
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
