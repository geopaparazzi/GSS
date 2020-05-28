import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:intl/intl.dart';

const TITLE = 'Geopaparazzi Survey Server';
const NOVALUE = " - ";
const ABOUTPAGE_INDEX = 1000;

const WEBAPP = 'http://localhost:8080';

final String KEY_HASPERMISSION = "hasPermission";
final String KEY_ISADMIN = "isAdmin";
final String KEY_USER = "user";
final String KEY_PWD = "pwd";
final String KEY_BASEMAP = "basemap";
final String KEY_MAPCENTER = "mapcenter_xyz";

final String KEY_SURVEYORS = "surveyors";
final String KEY_PROJECTS = "projects";

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
final String SIZE = "size";
final String MARKER = "marker";
final String ID = "id";
final String DATAID = "dataid";
final String DATA = "data";
final String LOGS = "logs";
final String NOTES = "notes";
final String FORMS = "forms";
final String FORM = "form";
final String USER = "user";
final String IMAGES = "images";
final String TS = "ts";

// API VARS END

final DEFAULT_TILELAYER = AVAILABLE_MAPS['Openstreetmap'];

