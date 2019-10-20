import 'dart:html' as html;
import 'dart:convert' as JSON;
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';

class SmashSession {
  static Future<void> login(String user, String password) async {
    var responsJson = await ServerApi.login(user, password);
    var jsonMap = JSON.jsonDecode(responsJson);
    var hasPermission = jsonMap[KEY_HASPERMISSION];
    if (hasPermission) {
      setSessionUser(user);
    }
    var isAdmin = jsonMap[KEY_ISADMIN];
    if (isAdmin) {
      html.window.sessionStorage[KEY_ISADMIN] = "$isAdmin";
    }
  }

  static bool isLogged() {
    return html.window.sessionStorage[KEY_USERNAME] != null;
  }

  static bool isAdmini() {
    String isAdminStr = html.window.sessionStorage[KEY_ISADMIN];
    return isAdminStr != null && isAdminStr.toLowerCase() == 'true';
  }

  static void setSessionUser(String user) {
    html.window.sessionStorage[KEY_USERNAME] = user;
  }

  static String getSessionUser() {
    return html.window.sessionStorage[KEY_USERNAME];
  }

  static void logout() {
    html.window.sessionStorage.remove(KEY_USERNAME);
    html.window.sessionStorage.remove(KEY_ISADMIN);
  }
}
