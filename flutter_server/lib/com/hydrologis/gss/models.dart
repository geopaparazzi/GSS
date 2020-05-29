import 'dart:convert';

import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart' hide SIZE;
import 'package:flutter/material.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:latlong/latlong.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

class FilterStateModel extends ChangeNotifier {
  List<String> _surveyors;

  List<int> _fromToTimestamp;

  List<String> _projects;

  String _matchingText;

  List<String> get surveyors => _surveyors;

  void setSurveyors(List<String> surveyors) {
    _surveyors = surveyors;
    notifyListeners();
  }

  void setSurveyorsQuiet(List<String> surveyors) {
    _surveyors = surveyors;
  }

  List<int> get fromToTimestamp => _fromToTimestamp;

  void setFromToTimestamp(List<int> fromToTimestamp) {
    _fromToTimestamp = fromToTimestamp;
    notifyListeners();
  }

  List<String> get projects => _projects;

  void setProjects(List<String> projects) {
    _projects = projects;
    notifyListeners();
  }

  void setProjectsQuiet(List<String> projects) {
    _projects = projects;
  }

  String get matchingText => _matchingText;

  void setMatchingText(String matchingText) {
    _matchingText = matchingText;
    notifyListeners();
  }

  void reset() {
    _surveyors = null;
    _fromToTimestamp = null;
    _projects = null;
    _matchingText = null;
  }

  @override
  String toString() {
    String timespan = "No timepsan";
    if (_fromToTimestamp != null) {
      timespan = "Timespan: ";
      timespan +=
          "${TimeUtilities.ISO8601_TS_FORMATTER.format(DateTime.fromMillisecondsSinceEpoch(_fromToTimestamp[0]))}";
      timespan +=
          " to ${TimeUtilities.ISO8601_TS_FORMATTER.format(DateTime.fromMillisecondsSinceEpoch(_fromToTimestamp[1]))}";
    }

    String str = """
    Filter state:
      Projects: ${_projects?.join(";")}
      Surveyors: ${_surveyors?.join(";")}
      $timespan
    """;
    return str;
  }
}

class AttributesTableStateModel extends ChangeNotifier {
  void refresh() {
    notifyListeners();
  }
}

class MapstateModel extends ChangeNotifier {
  PolylineLayerOptions logs;
  List<Marker> mapMarkers = [];
  List<Attributes> attributes = [];
  LatLngBounds dataBounds = LatLngBounds();
  BuildContext currentMapContext;

  double screenHeight = 600;

  bool showAttributes = true;

  MapController mapController;

  LatLngBounds currentMapBounds;

  void reloadMap() {
    notifyListeners();
  }

  void fitbounds() {
    if (mapController != null) {
      currentMapBounds = dataBounds;
      mapController.fitBounds(dataBounds);
    }
  }

  Future<void> getData(BuildContext context) async {
    print("Data reload called");

    var filterStateModel =
        Provider.of<FilterStateModel>(context, listen: false);

    var userPwd = SmashSession.getSessionUser();

    var data = await ServerApi.getData(
      userPwd[0],
      userPwd[1],
      surveyors: filterStateModel.surveyors,
      projects: filterStateModel.projects,
      matchString: filterStateModel.matchingText,
      fromTo: filterStateModel.fromToTimestamp,
    );
    Map<String, dynamic> json = jsonDecode(data);

    dataBounds = LatLngBounds();

    // TODO find a fix for logs
//   List<dynamic> logsList = json[LOGS];
//   if (logsList != null) {
//     List<Polyline> lines = [];
//     for (int i = 0; i < logsList.length; i++) {
//       dynamic logItem = logsList[i];
// //        var id = logItem[ID];
//       // var name = logItem[NAME];
//       var colorHex = logItem[COLOR];
//       var width = logItem[WIDTH];
//       var coords = logItem[COORDS];

//       List<LatLng> points = [];
//       for (int j = 0; j < coords.length; j++) {
//         var coord = coords[j];
//         var latLng = LatLng(coord[Y], coord[X]);
//         _dataBounds.extend(latLng);
//         points.add(latLng);
//       }

//       lines.add(Polyline(
//           points: points, strokeWidth: width, color: ColorExt(colorHex)));
//     }
//     _logs = PolylineLayerOptions(
//       polylines: lines,
//       polylineCulling: true,
//     );
//   }

    List<Marker> markers = <Marker>[];
    List<Attributes> attributesList = [];

    List<dynamic> imagesList = json[IMAGES];
    for (int i = 0; i < imagesList.length; i++) {
      dynamic imageItem = imagesList[i];
      var id = imageItem[ID];
      var dataId = imageItem[DATAID];
      var data = imageItem[DATA];
      var name = imageItem[NAME];
      var ts = imageItem[TS];
      var x = imageItem[X];
      var y = imageItem[Y];
      var latLng = LatLng(y, x);
//      print("$latLng : $name");
      dataBounds.extend(latLng);
      var imgData = Base64Decoder().convert(data);
      var imageWidget = Image.memory(
        imgData,
        scale: 6.0,
      );
      markers.add(buildImage(this, screenHeight, x, y, name, dataId, imgData));

      var surveyor = imageItem[SURVEYOR];
      var project = imageItem[PROJECT];
      attributesList.add(Attributes()
        ..id = id
        ..marker = imageWidget
        ..point = latLng
        ..project = project
        ..text = name
        ..timeStamp = ts
        ..user = surveyor);
    }

    List<dynamic> simpleNotesList = json[NOTES];
    for (int i = 0; i < simpleNotesList.length; i++) {
      dynamic noteItem = simpleNotesList[i];
//      print(noteItem);
      var id = noteItem[ID];
      var name = noteItem[NAME];
      var ts = noteItem[TS];
      var x = noteItem[X];
      var y = noteItem[Y];
      var latLng = LatLng(y, x);
//      print("$latLng : $name");
      dataBounds.extend(latLng);

      var marker = noteItem[MARKER];
      var size = noteItem[SIZE];
      var color = noteItem[COLOR];
      var iconData = getSmashIcon(marker);
      var colorExt = ColorExt(color);
      var icon = Icon(
        iconData,
        size: size,
        color: colorExt,
      );
      markers.add(buildSimpleNote(x, y, name, icon, size, colorExt));

      var surveyor = noteItem[SURVEYOR];
      var project = noteItem[PROJECT];
      attributesList.add(Attributes()
        ..id = id
        ..marker = icon
        ..point = latLng
        ..project = project
        ..text = name
        ..timeStamp = ts
        ..user = surveyor);
    }

    List<dynamic> formNotesList = json[FORMS];
    for (int i = 0; i < formNotesList.length; i++) {
      dynamic formItem = formNotesList[i];
//      print(formItem);
      var noteId = formItem[ID];
      var name = formItem[NAME];
      var form = formItem[FORM];
      var ts = formItem[TS];
      var x = formItem[X];
      var y = formItem[Y];
      var latLng = LatLng(y, x);
//      print("$latLng : $name");
//      print(latLng);
      dataBounds.extend(latLng);

      var marker = formItem[MARKER];
      var size = formItem[SIZE];
      var color = formItem[COLOR];
      var iconData = getSmashIcon(marker);
      var colorExt = ColorExt(color);
      var icon = Icon(
        iconData,
        size: size,
        color: colorExt,
      );
      markers.add(
          buildFormNote(this, x, y, name, form, noteId, icon, size, colorExt));

      var surveyor = formItem[SURVEYOR];
      var project = formItem[PROJECT];
      attributesList.add(Attributes()
        ..id = noteId
        ..marker = icon
        ..point = latLng
        ..project = project
        ..text = name
        ..timeStamp = ts
        ..user = surveyor);
    }

    mapMarkers = markers;
    attributes = attributesList;

    var delta = 0.01;
    dataBounds = LatLngBounds(
        LatLng(dataBounds.south - delta, dataBounds.west - delta),
        LatLng(dataBounds.north + delta, dataBounds.east + delta));
  }
}
