import 'package:flutter_server/com/hydrologis/gss/libs/layers.dart';
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
const KEY_BOOKMARKS = "bookmarks";
const KEY_AUTOMATIC_REGISTRATION = "GSS_KEY_AUTOMATIC_REGISTRATION";

const KEY_SURVEYORS = "surveyors";
const KEY_WEBUSERS = "webusers";
const KEY_PROJECTS = "projects";

const PROJECTDATA_MAPS = "maps"; //$NON-NLS-1$
const PROJECTDATA_PROJECTS = "projects"; //$NON-NLS-1$
const PROJECTDATA_NAME = "name"; //$NON-NLS-1$
const PROJECTDATA_TAGS = "tags"; //$NON-NLS-1$
const PROJECTDATA_TAG = "tag"; //$NON-NLS-1$
const PROJECTDATA_TAGSID = "tagsid"; //$NON-NLS-1$
const PROJECTDATA_TAGID = "tagid"; //$NON-NLS-1$

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
const PREVIOUSID = "previd";
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

const SURVEYOR_ID_FIELD_NAME = "id"; //$NON-NLS-1$
const SURVEYOR_DEVICE_FIELD_NAME = "deviceid"; //$NON-NLS-1$
const SURVEYOR_NAME_FIELD_NAME = "name"; //$NON-NLS-1$
const SURVEYOR_ACTIVE_FIELD_NAME = "active"; //$NON-NLS-1$
const SURVEYOR_CONTACT_FIELD_NAME = "contact"; //$NON-NLS-1$

const WEBUSER_ID_FIELD_NAME = "id"; //$NON-NLS-1$
const WEBUSER_NAME_FIELD_NAME = "name"; //$NON-NLS-1$
const WEBUSER_UNIQUENAME_FIELD_NAME = "uniquename"; //$NON-NLS-1$
const WEBUSER_EMAIL_FIELD_NAME = "email"; //$NON-NLS-1$
const WEBUSER_PASSWORD_FIELD_NAME = "password"; //$NON-NLS-1$
const WEBUSER_GROUP_FIELD_NAME = "group_id"; //$NON-NLS-1$

const GPSLOGS = "gpslogs"; //$NON-NLS-1$

const ADMINGROUP = "administrators"; //$NON-NLS-1$
const USERGROUP = "users"; //$NON-NLS-1$

// API VARS END

final DEFAULT_TILELAYER = AVAILABLE_MAPS[DEFAULTMAP];

const NOTE_ZOOM_BUFFER = 0.01;
