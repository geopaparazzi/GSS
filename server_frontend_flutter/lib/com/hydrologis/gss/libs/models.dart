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
import 'package:dart_jts/dart_jts.dart' as JTS;
// import 'package:flutter_map_tappable_polyline/flutter_map_tappable_polyline.dart';

class FilterStateModel extends ChangeNotifier {
  List<String>? _surveyors;

  List<int>? _fromToTimestamp;

  List<String>? _projects;

  String? _matchingText;

  List<String>? get surveyors => _surveyors;

  void setSurveyors(List<String> surveyors) {
    _surveyors = surveyors;
    notifyListeners();
  }

  void setSurveyorsQuiet(List<String> surveyors) {
    _surveyors = surveyors;
  }

  List<int>? get fromToTimestamp => _fromToTimestamp;

  void setFromToTimestamp(List<int> fromToTimestamp) {
    _fromToTimestamp = fromToTimestamp;
    notifyListeners();
  }

  List<String>? get projects => _projects;

  void setProjects(List<String> projects) {
    _projects = projects;
    notifyListeners();
  }

  void setProjectsQuiet(List<String> projects) {
    _projects = projects;
  }

  String? get matchingText => _matchingText;

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
          "${TimeUtilities.ISO8601_TS_FORMATTER.format(DateTime.fromMillisecondsSinceEpoch(_fromToTimestamp![0]))}";
      timespan +=
          " to ${TimeUtilities.ISO8601_TS_FORMATTER.format(DateTime.fromMillisecondsSinceEpoch(_fromToTimestamp![1]))}";
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
  int? selectedNoteId;

  void refresh() {
    notifyListeners();
  }
}

class MapstateModel extends ChangeNotifier {
  PolylineLayer? logs;
  // TappablePolylineLayerOptions logs;
  List<Marker> mapMarkers = [];
  List<Attributes> attributes = [];
  LatLngBounds? dataBounds = null;
  BuildContext? currentMapContext;

  double screenHeight = 600;

  bool showAttributes = false;

  MapController? mapController;

  LatLngBounds? currentMapBounds;

  Map<String, Widget> layersMap = {};

  void reloadMap() {
    notifyListeners();
  }

  void fitbounds({LatLngBounds? newBounds}) {
    if (mapController != null && (newBounds != null || dataBounds != null)) {
      mapController!.fitBounds(newBounds ?? dataBounds!);
      currentMapBounds = mapController!.bounds;
    }
  }

  void setBackgroundLayers(Map<String, Widget> layers) {
    layersMap = layers;
  }

  Map<String, Widget> getBackgroundLayers() {
    return layersMap;
  }

  Future<void> getData(BuildContext context) async {
    // print("Data reload called");

    // var filterStateModel =
    //     Provider.of<FilterStateModel>(context, listen: false);

    // GET DATA FROM SERVER
    var notesList = await WebServerApi.getRenderNotes(
        // surveyors: filterStateModel.surveyors,
        // projects: filterStateModel.projects,
        // matchString: filterStateModel.matchingText,
        // fromTo: filterStateModel.fromToTimestamp,
        );

    dataBounds = null;

    var logsList = await WebServerApi.getGpslogs();

    // LOAD LOG DATA
    if (logsList != null) {
      // List<TaggedPolyline> lines = [];
      List<Polyline> lines = [];
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
        var startts = logItem[STARTTS];
        var endts = logItem[ENDTS];

        var geom = logItem[THE_GEOM];
        JTS.LineString line =
            JTS.WKTReader().read(geom.split(";")[1]) as JTS.LineString;
        var coordinates = line.getCoordinates();
        List<LatLng> points =
            coordinates.map((c) => LatLongHelper.fromLatLon(c.y, c.x)).toList();

        var env = line.getEnvelopeInternal();
        if (dataBounds == null) {
          dataBounds = LatLngBounds(
            LatLongHelper.fromLatLon(env.getMinY(), env.getMinX()),
            LatLongHelper.fromLatLon(env.getMaxY(), env.getMaxX()),
          );
        } else {
          dataBounds!
              .extend(LatLongHelper.fromLatLon(env.getMinY(), env.getMinX()));
          dataBounds!
              .extend(LatLongHelper.fromLatLon(env.getMaxY(), env.getMaxX()));
        }

        lines.add(
          Polyline(
            points: points,
            strokeWidth: width,
            color: ColorExt(colorHex),
          ),
        );
        // lines.add(
        //   TaggedPolyline(
        //     tag: "$id@$name@$startts@$endts",
        //     points: points,
        //     strokeWidth: width,
        //     color: ColorExt(colorHex),
        //   ),
        // );
      }
      logs = PolylineLayer(
        polylines: lines,
        polylineCulling: true,
      );
      // logs = TappablePolylineLayerOptions(
      //   polylines: lines,
      //   polylineCulling: true,
      //   onTap: (List<TaggedPolyline> polylines, TapUpDetails details) {
      //     if (polylines.isEmpty) {
      //       return null;
      //     }
      //     return openLogDialog(context, polylines[0].tag);
      //   },
      //   // onMiss: () => print("No polyline tapped"),
      // );
    }

    List<Marker> markers = <Marker>[];
    List<Attributes> attributesList = [];

    var renderImages = await WebServerApi.getRenderImages();
    // LOAD SIMPLE IMAGES
    if (renderImages != null) {
      for (int i = 0; i < renderImages.length; i++) {
        dynamic imageItem = renderImages[i];
        var id = imageItem[ID];
        // var dataId = imageItem[DATAID];
        // var data = imageItem[DATA];
        var name = imageItem[TEXT];
        var geom = imageItem[THE_GEOM];
        JTS.Point point = JTS.WKTReader().read(geom.split(";")[1]) as JTS.Point;
        // var x = imageItem[X];
        // var y = imageItem[Y];
        var latLng = LatLongHelper.fromLatLon(point.getY(), point.getX());
        if (dataBounds == null) {
          dataBounds = LatLngBounds(latLng, latLng);
        } else {
          dataBounds!.extend(latLng);
        }
        var thumb = imageItem[THUMBNAIL];
        var imgData = Base64Decoder().convert(thumb);
        var imageWidget = Image.memory(
          imgData,
          scale: 3.0,
        );
        markers.add(buildImage(this, screenHeight, point.getX(), point.getY(),
            name, id, imageWidget));

        attributesList.add(Attributes()
          ..id = id
          ..marker = imageWidget
          ..point = latLng
          ..text = name);
      }
    }

    // LOAD ALL NOTES WITH SIMPLE INFOS
    // make sure that forms are loaded properly
    if (notesList != null && notesList.isNotEmpty) {
      for (int i = 0; i < notesList.length; i++) {
        Map<String, dynamic> noteItem = notesList[i];
        var id = noteItem[ID];
        var name = noteItem['label'];
        var geom = noteItem[THE_GEOM];
        JTS.Point point = JTS.WKTReader().read(geom.split(";")[1]) as JTS.Point;
        var latLng = LatLongHelper.fromLatLon(point.getY(), point.getX());
        if (dataBounds == null) {
          dataBounds = LatLngBounds(latLng, latLng);
        } else {
          dataBounds!.extend(latLng);
        }

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
        markers
            .add(buildSimpleNote(this, latLng, name, id, icon, size, colorExt));

        attributesList.add(Attributes()
          ..id = id
          ..marker = icon
          ..point = latLng
          ..text = name);
      }
    }

    mapMarkers = markers;
    attributes = attributesList;

    var delta = 0.01;
    if (mapMarkers.length > 0 && dataBounds != null) {
      dataBounds = LatLngBounds(
        LatLongHelper.fromLatLon(
            dataBounds!.south - delta, dataBounds!.west - delta),
        LatLongHelper.fromLatLon(
            dataBounds!.north + delta, dataBounds!.east + delta),
      );
    } else {
      dataBounds = LatLngBounds(LatLng(-45, -90), LatLng(45, 90));
    }
  }
}
