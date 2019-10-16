import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong/latlong.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';

const TITLE = 'Geopaparazzi Survey Server';
const Color MAIN_COLOR = Colors.green;
final Color MAIN_COLOR_BACKGROUND = Colors.green[200];
const DEFAULT_PADDING = 15.0;
const DEFAULT_FONTSIZE = 36.0;
const CHART_FONTSIZE = 18;
const NOVALUE = " - ";
const ABOUTPAGE_INDEX = 1000;

const WEBAPP = 'http://localhost:8080';

const DEFAULT_GREY_COLOR = Colors.grey;
final TextStyle DEFAULT_GREYSTYLE = TextStyle(
    fontSize: DEFAULT_FONTSIZE, color: DEFAULT_GREY_COLOR, fontFamily: "Arial");
final TextStyle DEFAULT_BLACKSTYLE = TextStyle(
    fontSize: DEFAULT_FONTSIZE, color: Colors.black, fontFamily: "Arial");
final TextStyle DEFAULT_GREYSTYLE_SMALL = TextStyle(
    fontSize: DEFAULT_FONTSIZE / 2,
    color: DEFAULT_GREY_COLOR,
    fontFamily: "Arial");

/// An ISO8601 date formatter (yyyy-MM-dd HH:mm:ss).
final DateFormat ISO8601_TS_FORMATTER = DateFormat("yyyy-MM-dd HH:mm:ss");

/// An ISO8601 time formatter (HH:mm:ss).
final DateFormat ISO8601_TS_TIME_FORMATTER = DateFormat("HH:mm:ss");

/// An ISO8601 day formatter (yyyy-MM-dd).
final DateFormat ISO8601_TS_DAY_FORMATTER = DateFormat("yyyy-MM-dd");

// API VARS START
final String Y = "y";
final String X = "x";
final String COORDS = "coords";
final String ENDTS = "endts";
final String STARTTS = "startts";
final String NAME = "name";
final String WIDTH = "width";
final String COLOR = "color";
final String ID = "id";
final String DATAID = "dataid";
final String DATA = "data";
final String LOGS = "logs";
final String NOTES = "notes";
final String FORMS = "forms";
final String FORM = "form";
final String IMAGES = "images";
final String TS = "ts";

// API VARS END

final DEFAULT_TILELAYER = AVAILABLE_LAYERS_MAP[MAPSFORGE]; //'Openstreetmap'];

class MapstateModel extends ChangeNotifier {
  TileLayerOptions _backgroundLayer = DEFAULT_TILELAYER;

  double _centerLon = 11.0;
  double _centerLat = 46.0;
  double _currentZoom = 8;

  TileLayerOptions get backgroundLayer => _backgroundLayer;

  set backgroundLayer(TileLayerOptions backgroundLayer) {
    _backgroundLayer = backgroundLayer;
    print("event backgroundLayer");
    notifyListeners();
  }

  get centerLat => _centerLat;

  get centerLon => _centerLon;

  get currentZoom => _currentZoom;

  void setMapPosition(double lon, double lat, double zoom) {
    _centerLat = lat;
    _centerLon = lon;
    _currentZoom = zoom;
    print("event setMapPosition");
    notifyListeners();
  }

  void reset() {
    _backgroundLayer = DEFAULT_TILELAYER;
    print("event reset");
    notifyListeners();
  }
}
