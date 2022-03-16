import 'dart:convert';

import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart' hide SIZE;
import 'package:flutter/material.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:latlong2/latlong.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:flutter_map_tappable_polyline/flutter_map_tappable_polyline.dart';

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
  int selectedNoteId;

  void refresh() {
    notifyListeners();
  }
}

class MapstateModel extends ChangeNotifier {
  TappablePolylineLayerOptions logs;
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

  void fitbounds({LatLngBounds newBounds}) {
    if (mapController != null) {
      mapController.fitBounds(newBounds ?? dataBounds);
      currentMapBounds = mapController.bounds;
    }
  }

  Future<void> getData(BuildContext context) async {
    // print("Data reload called");

    var filterStateModel =
        Provider.of<FilterStateModel>(context, listen: false);

    var userPwd = SmashSession.getSessionUser();

    // GET DATA FROM SERVER
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

    // LOAD LOG DATA
    // TODO find a fix for logs
    List<dynamic> logsList = json[LOGS];
    if (logsList != null) {
      List<TaggedPolyline> lines = [];
      for (int i = 0; i < logsList.length; i++) {
        dynamic logItem = logsList[i];
        var id = logItem[ID];
        var name = logItem[NAME];
        var colorHex = logItem[COLOR];

        // TODO for now colortables are not supported
        const ECOLORSEP = "@";
        if (colorHex.contains(ECOLORSEP)) {
          var split = colorHex.split(ECOLORSEP);
          colorHex = split[0];
        }
        var width = logItem[WIDTH];
        var coords = logItem[COORDS];
        var startts = logItem[STARTTS];
        var endts = logItem[ENDTS];

        List<LatLng> points = [];
        for (int j = 0; j < coords.length; j++) {
          var coord = coords[j];
          var lat = coord[Y];
          var lon = coord[X];

          var latLng = LatLongHelper.fromLatLon(lat, lon);
          dataBounds.extend(latLng);
          points.add(latLng);
        }

        lines.add(
          TaggedPolyline(
            tag: "$id@$name@$startts@$endts",
            points: points,
            strokeWidth: width,
            color: ColorExt(colorHex),
          ),
        );
      }
      logs = TappablePolylineLayerOptions(
        polylines: lines,
        polylineCulling: true,
        onTap: (List<TaggedPolyline> polylines, TapUpDetails details) {
          if (polylines.isEmpty) {
            return null;
          }
          return openLogDialog(context, polylines[0].tag);
        },
        // onMiss: () => print("No polyline tapped"),
      );
    }

    List<Marker> markers = <Marker>[];
    List<Attributes> attributesList = [];

    // LOAD SIMPLE IMAGES
    List<dynamic> imagesList = json[IMAGES];
    if (imagesList != null) {
      for (int i = 0; i < imagesList.length; i++) {
        dynamic imageItem = imagesList[i];
        var id = imageItem[ID];
        var dataId = imageItem[DATAID];
        var data = imageItem[DATA];
        var name = imageItem[NAME];
        var ts = imageItem[TS];
        var x = imageItem[X];
        var y = imageItem[Y];
        var latLng = LatLongHelper.fromLatLon(y, x);
        dataBounds.extend(latLng);
        var imgData = Base64Decoder().convert(data);
        var imageWidget = Image.memory(
          imgData,
          scale: 6.0,
        );
        markers.add(
            buildImage(this, screenHeight, x, y, name, dataId, imageWidget));

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
    }

    // LOAD ALL NOTES WITH SIMPLE INFOS
    // make sure that forms are loadd properly
    List<dynamic> simpleNotesList = json[NOTES];
    if (simpleNotesList != null) {
      for (int i = 0; i < simpleNotesList.length; i++) {
        dynamic noteItem = simpleNotesList[i];
        var id = noteItem[ID];
        var name = noteItem[NAME];
        var hasForm = noteItem[FORM];
        var ts = noteItem[TS];
        var x = noteItem[X];
        var y = noteItem[Y];
        var latLng = LatLongHelper.fromLatLon(y, x);
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
        if (hasForm) {
          markers.add(
              buildFormNote(this, x, y, name, id, iconData, size, colorExt));
        } else {
          markers
              .add(buildSimpleNote(this, x, y, name, id, icon, size, colorExt));
        }

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
    }

    mapMarkers = markers;
    attributes = attributesList;

    var delta = 0.01;
    if (mapMarkers.length > 0) {
      dataBounds = LatLngBounds(
        LatLongHelper.fromLatLon(
            dataBounds.south - delta, dataBounds.west - delta),
        LatLongHelper.fromLatLon(
            dataBounds.north + delta, dataBounds.east + delta),
      );
    } else {
      dataBounds = LatLngBounds(LatLng(-45, -90), LatLng(45, 90));
    }
  }
}
