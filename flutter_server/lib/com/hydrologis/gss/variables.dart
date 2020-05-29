import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:intl/intl.dart';

const TITLE = 'Geopaparazzi Survey Server';
const NOVALUE = " - ";
const ABOUTPAGE_INDEX = 1000;

const WEBAPP = 'http://localhost:8080';

const KEY_HASPERMISSION = "hasPermission";
const KEY_ISADMIN = "isAdmin";
const KEY_USER = "user";
const KEY_PWD = "pwd";
const KEY_BASEMAP = "basemap";
const KEY_MAPCENTER = "mapcenter_xyz";

const KEY_SURVEYORS = "surveyors";
const KEY_PROJECTS = "projects";

/// An ISO8601 date formatter (yyyy-MM-dd HH:mm:ss).
final DateFormat ISO8601_TS_FORMATTER = DateFormat("yyyy-MM-dd HH:mm:ss");

/// An ISO8601 time formatter (HH:mm:ss).
final DateFormat ISO8601_TS_TIME_FORMATTER = DateFormat("HH:mm:ss");

/// An ISO8601 day formatter (yyyy-MM-dd).
final DateFormat ISO8601_TS_DAY_FORMATTER = DateFormat("yyyy-MM-dd");

// API VARS START
const Y = "y";
const X = "x";
const COORDS = "coords";
const ENDTS = "endts";
const STARTTS = "startts";
const NAME = "name";
const WIDTH = "width";
const COLOR = "color";
const SIZE = "size";
const MARKER = "marker";
const ID = "id";
const DATAID = "dataid";
const DATA = "data";
const LOGS = "logs";
const NOTES = "notes";
const FORMS = "forms";
const FORM = "form";
const USER = "user";
const IMAGES = "images";
const TS = "ts";
const SURVEYOR = "surveyor";
const PROJECT = "project";

// API VARS END

final DEFAULT_TILELAYER = AVAILABLE_MAPS[MAPSFORGE];
