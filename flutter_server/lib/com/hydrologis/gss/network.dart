import 'dart:convert';
import 'dart:html';
import 'dart:typed_data';

import 'package:charts_flutter/flutter.dart' as charts;
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:http/http.dart';

const DATA_NV_INTERVAL_SECONDS = 600;
const TIMESTAMP_KEY = "ts";
const VALUE_KEY = "v";
const WEBAPP_URL = "http://localhost:8080"; // TODO make empty for release

const API_DATA = "$WEBAPP_URL/data";
const API_LIST = "$WEBAPP_URL/list";
const API_UPDATE = "$WEBAPP_URL/update";
const API_DELETE = "$WEBAPP_URL/delete";
const API_UPDATE_SURVEYOR = "$API_UPDATE/surveyors";
const API_UPDATE_WEBUSER = "$API_UPDATE/webusers";
const API_DELETE_WEBUSER = "$API_DELETE/webusers";
const API_DELETE_FORMS = "$API_DELETE/forms";
const API_LIST_SURVEYORS = "$API_LIST/surveyors";
const API_LIST_WEBUSERS = "$API_LIST/webusers";
const API_LIST_PROJECTS = "$API_LIST/projects";
const API_LOGIN = "$WEBAPP_URL/login";
const API_USERSETTINGS = "$WEBAPP_URL/usersettings";
const API_IMAGES = "$API_DATA/images";
const API_IMAGEDATA = "$API_DATA/imagedata";
// const API_IMAGEDATA = "$WEBAPP_URL/imagedata";
const API_NOTE = "$API_DATA/notes";
const API_DATA_DOWNLOAD_PATH = "$WEBAPP_URL/datadownload";
const API_DATA_UPLOAD_PATH = "$WEBAPP_URL/dataupload";
const API_TAGS_DOWNLOAD_PATH = "$WEBAPP_URL/tagsdownload";

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

  static Future<String> getSurveyorsJson(String user, String pwd) async {
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

  static Future<String> getWebusersJson(String user, String pwd) async {
    String apiCall = "$API_LIST_WEBUSERS";

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

  static Future<String> updateOrAddSurveyor(
      String user, String pwd, dynamic surveyor) async {
    Map<String, String> formData = {}
      ..[SURVEYOR_DEVICE_FIELD_NAME] =
          surveyor[SURVEYOR_DEVICE_FIELD_NAME].toString()
      ..[SURVEYOR_NAME_FIELD_NAME] =
          surveyor[SURVEYOR_NAME_FIELD_NAME].toString()
      ..[SURVEYOR_ACTIVE_FIELD_NAME] =
          surveyor[SURVEYOR_ACTIVE_FIELD_NAME].toString()
      ..[SURVEYOR_CONTACT_FIELD_NAME] =
          surveyor[SURVEYOR_CONTACT_FIELD_NAME].toString();
    if (surveyor[SURVEYOR_ID_FIELD_NAME] != null) {
      formData[SURVEYOR_ID_FIELD_NAME] =
          surveyor[SURVEYOR_ID_FIELD_NAME].toString();
    }

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(
        API_UPDATE_SURVEYOR, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return null;
    } else {
      return request.response;
    }
  }

  static Future<String> updateOrAddWebuser(
      String user, String pwd, dynamic webuser) async {
    Map<String, String> formData = {}
      ..[WEBUSER_UNIQUENAME_FIELD_NAME] =
          webuser[WEBUSER_UNIQUENAME_FIELD_NAME].toString()
      ..[WEBUSER_NAME_FIELD_NAME] = webuser[WEBUSER_NAME_FIELD_NAME].toString()
      ..[WEBUSER_GROUP_FIELD_NAME] =
          webuser[WEBUSER_GROUP_FIELD_NAME].toString()
      ..[WEBUSER_EMAIL_FIELD_NAME] =
          webuser[WEBUSER_EMAIL_FIELD_NAME].toString();
    if (webuser[WEBUSER_PASSWORD_FIELD_NAME] != null) {
      formData[WEBUSER_PASSWORD_FIELD_NAME] =
          webuser[WEBUSER_PASSWORD_FIELD_NAME].toString();
    }
    if (webuser[WEBUSER_ID_FIELD_NAME] != null) {
      formData[WEBUSER_ID_FIELD_NAME] =
          webuser[WEBUSER_ID_FIELD_NAME].toString();
    }

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(
        API_UPDATE_WEBUSER, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return null;
    } else {
      return request.response;
    }
  }

  static Future<String> enableAutomaticRegistration(
      String user, String pwd) async {
    Map<String, String> formData = {};
    formData[KEY_AUTOMATIC_REGISTRATION] =
        DateTime.now().millisecondsSinceEpoch.toString();
    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(
        API_USERSETTINGS, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return request.response;
    } else {
      return null;
    }
  }

  static Future<String> deleteWebuser(
      String user, String pwd, dynamic webuser) async {
    Map<String, String> formData = {
      WEBUSER_ID_FIELD_NAME: webuser[WEBUSER_ID_FIELD_NAME].toString()
    };

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(
        API_DELETE_WEBUSER, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return null;
    } else {
      return request.response;
    }
  }

  /// Get the list of data available for the devices to download.
  static Future<Map<String, List<String>>> getProjectData(
      String user, String pwd) async {
    Map<String, List<String>> resultData = {
      PROJECTDATA_MAPS: <String>[],
      PROJECTDATA_PROJECTS: <String>[],
      PROJECTDATA_TAGS: <String>[],
      PROJECTDATA_TAGSID: <String>[],
    };
    String apiCall = "$API_DATA_DOWNLOAD_PATH";
    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.request(apiCall,
        method: 'GET', requestHeaders: requestHeaders);
    if (request.status == 200) {
      var data = jsonDecode(request.response);
      List<dynamic> maps = data[PROJECTDATA_MAPS];
      if (maps != null && maps.length > 0) {
        maps.forEach((element) {
          resultData[PROJECTDATA_MAPS]
              .add(element[PROJECTDATA_NAME].toString());
        });
      }
      List<dynamic> projects = data[PROJECTDATA_PROJECTS];
      if (projects != null && projects.length > 0) {
        projects.forEach((element) {
          resultData[PROJECTDATA_PROJECTS]
              .add(element[PROJECTDATA_NAME].toString());
        });
      }

      // download forms now
      apiCall = "$API_TAGS_DOWNLOAD_PATH";
      request = await HttpRequest.request(apiCall,
          method: 'GET', requestHeaders: requestHeaders);
      if (request.status == 200) {
        var data = jsonDecode(request.response);
        List<dynamic> tags = data[PROJECTDATA_TAGS];
        if (tags != null && tags.length > 0) {
          tags.forEach((element) {
            resultData[PROJECTDATA_TAGS]
                .add(element[PROJECTDATA_TAG].toString());
            resultData[PROJECTDATA_TAGSID]
                .add(element[PROJECTDATA_TAGID].toString());
          });
        }
      }
    }
    return resultData;
  }

  static Future<String> deleteProjectForm(
      String user, String pwd, String id) async {
    Map<String, String> formData = {ID: id};

    Map<String, String> requestHeaders = getAuthRequestHeader(user, pwd);
    HttpRequest request = await HttpRequest.postFormData(
        API_DELETE_FORMS, formData,
        requestHeaders: requestHeaders);
    if (request.status == 200) {
      return null;
    } else {
      return request.response;
    }
  }
}
