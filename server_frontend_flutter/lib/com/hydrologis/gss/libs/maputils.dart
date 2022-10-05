import 'dart:convert';

import 'package:after_layout/after_layout.dart';
import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart'
    hide TextStyle;
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/formutils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:horizontal_data_table/horizontal_data_table.dart';
import 'package:latlong2/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:dart_jts/dart_jts.dart' as JTS;

Marker buildSimpleNote(MapstateModel mapState, LatLng latLng, String name,
    int noteId, Icon icon, double size, Color color) {
  List lengthHeight = guessTextDimensions(name, size);
  return Marker(
    width: lengthHeight[0],
    height: size + lengthHeight[1],
    point: latLng,
    builder: (ctx) => new Container(
      child: GestureDetector(
        child: Column(
          children: <Widget>[
            icon,
            FittedBox(
              child: Container(
                decoration: new BoxDecoration(
                    color: color,
                    borderRadius:
                        new BorderRadius.all(const Radius.circular(5.0))),
                child: new Center(
                  child: Padding(
                    padding: const EdgeInsets.all(5.0),
                    child: Text(
                      name,
                      style: TextStyle(
                          fontWeight: FontWeight.normal, color: Colors.black),
                    ),
                  ),
                ),
              ),
            )
          ],
        ),
        onTap: () async {
          // if (mapState.showAttributes) {
          //   var model =
          //       Provider.of<AttributesTableStateModel>(ctx, listen: false);
          //   model.selectedNoteId = noteId;
          //   model.refresh();
          // } else {
          openNoteDialog(ctx, noteId);
          // }
        },
      ),
    ),
  );
}

List guessTextDimensions(String name, double size) {
  var lengthHeight = [];
  final constraints = BoxConstraints(
    maxWidth: 800.0, // maxwidth calculated
    minHeight: 0.0,
    minWidth: 0.0,
  );

  RenderParagraph renderParagraph = RenderParagraph(
    TextSpan(
      text: name,
      style: TextStyle(
        fontSize: 36,
      ),
    ),
    textDirection: TextDirection.ltr,
    maxLines: 1,
  );
  renderParagraph.layout(constraints);
  double textlen = renderParagraph.getMinIntrinsicWidth(36).ceilToDouble();
  double textHeight = renderParagraph.getMinIntrinsicHeight(36).ceilToDouble();

  textlen = textlen > size ? textlen : size;
  lengthHeight.add(textlen);
  lengthHeight.add(textHeight);
  return lengthHeight;
}

Marker buildImage(MapstateModel mapState, double screenHeight, var x, var y,
    String name, var dataId, var imageWidget) {
  return Marker(
    width: 180,
    height: 180,
    point: new LatLng(y, x),
    builder: (ctx) => new Container(
      child: GestureDetector(
        onTap: () {
          // if (mapState.showAttributes) {
          //   var model =
          //       Provider.of<AttributesTableStateModel>(ctx, listen: false);
          //   model.selectedNoteId = dataId;
          //   model.refresh();
          // } else {
          openImageDialog(mapState.currentMapContext, name, dataId,
              hideRotate: false);
          // }
        },
        child: imageWidget,
      ),
    ),
  );
}

Marker buildFormNote(MapstateModel mapState, var x, var y, String name,
    var noteId, IconData iconData, double size, Color color) {
  // List lengthHeight = guessTextDimensions(name, size);

  double textExtraHeight = MARKER_ICON_TEXT_EXTRA_HEIGHT;
  if (name == null || name.length == 0) {
    textExtraHeight = 0;
  }

  return Marker(
    width: size * MARKER_ICON_TEXT_EXTRA_WIDTH_FACTOR,
    height: size + textExtraHeight,
    point: new LatLng(y, x),
    builder: (ctx) => new Container(
      child: GestureDetector(
        child: MarkerIcon(
          iconData,
          color,
          size,
          name,
          SmashColors.mainTextColorNeutral,
          color.withAlpha(80),
        ),
        onTap: () async {
          // if (mapState.showAttributes) {
          //   var model =
          //       Provider.of<AttributesTableStateModel>(ctx, listen: false);
          //   model.selectedNoteId = noteId;
          //   model.refresh();
          // } else {
          await openNoteDialog(ctx, noteId);
          // }
        },
      ),
    ),
  );
}

openNoteDialog(BuildContext context, int noteId) async {
  var data = await ServerApi.getNote(noteId);
  Map<String, dynamic> noteItem = jsonDecode(data);
  var user = noteItem[USER];
  var userName = await ServerApi.getUserName(user);
  noteItem[USER] = userName;
  var form = noteItem[FORM];
  var widget;
  var h = 300.0;
  var w = 400.0;
  if (form != null) {
    h = 900.0;
    w = 900.0;
    widget = VersionedNoteWidget(noteItem);
  } else {
    var id = noteItem[ID];
    var name = noteItem[TEXT];
    var ts = noteItem[TS];

    var geom = noteItem[THE_GEOM];
    JTS.Point point = JTS.WKTReader().read(geom.split(";")[1]);
    var map = {
      "ID": id,
      "Text": name,
      "Timestamp": ts,
      "Surveyor": userName,
      "Latitude": point.getY().toStringAsFixed(6),
      "Longitude": point.getX().toStringAsFixed(6),
    };

    widget = Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: SmashUI.titleText(
            name,
            textAlign: TextAlign.center,
            useColor: true,
          ),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Center(child: TableUtilities.fromMap(map)),
          ),
        ),
      ],
    );
  }

  Dialog openNoteDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: h,
      width: w,
      child: Center(child: widget),
    ),
  );
  await showDialog(
      context: context, builder: (BuildContext context) => openNoteDialog);
}

openLogDialog(BuildContext context, String logInfo) async {
  var split = logInfo.split("@");
  var idStr = split[0];
  var map = {
    "ID": idStr,
    "Name": split[1],
    "Start": TimeUtilities.ISO8601_TS_FORMATTER
        .format(DateTime.fromMillisecondsSinceEpoch(int.parse(split[2]))),
    "End": TimeUtilities.ISO8601_TS_FORMATTER
        .format(DateTime.fromMillisecondsSinceEpoch(int.parse(split[3]))),
    // "Project": project,
    // "Surveyor": surveyor,
  };

  var id = int.parse(idStr);
  var widget;
  var h = 300.0;
  var w = 400.0;

  widget = Column(
    mainAxisSize: MainAxisSize.min,
    children: [
      Padding(
        padding: const EdgeInsets.all(8.0),
        child: SmashUI.titleText(
          split[1],
          textAlign: TextAlign.center,
          useColor: true,
        ),
      ),
      Expanded(
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Center(child: TableUtilities.fromMap(map)),
        ),
      ),
      // if (SmashSession.isAdmin())
      //   Align(
      //     alignment: Alignment.bottomRight,
      //     child: IconButton(
      //         tooltip: "Delete this log from the database.",
      //         icon: Icon(SmashIcons.deleteIcon),
      //         color: SmashColors.mainDanger,
      //         onPressed: () async {
      //           Navigator.pop(context);
      //           var userPwd = SmashSession.getSessionUser();
      //           String response =
      //               await ServerApi.deleteGpsLog(userPwd[0], userPwd[1], id);
      //           if (response != null) {
      //             SmashDialogs.showErrorDialog(context, response);
      //           } else {
      //             MapstateModel mapstateModel =
      //                 Provider.of<MapstateModel>(context, listen: false);
      //             await mapstateModel.getData(context);
      //             mapstateModel.reloadMap();
      //           }
      //         }),
      //   ),
    ],
  );

  Dialog openLogDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: h,
      width: w,
      child: Center(child: widget),
    ),
  );
  await showDialog(
      context: context, builder: (BuildContext context) => openLogDialog);
}

openImageDialog(BuildContext context, String name, int imageId,
    {hideRotate = true}) {
  var h = MediaQuery.of(context).size.height;
  Dialog mapSelectionDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: NetworkImageWidget(
      "$API_IMAGES$imageId/",
      name,
      h * 0.7,
      hideRotate: hideRotate,
    ),
  );
  showDialog(
      context: context, builder: (BuildContext context) => mapSelectionDialog);
}

Future openBookmarksDialog(BuildContext context) async {
  var size = 500.0;
  Dialog bookmarksDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: BookmarksWidget(),
    ),
  );
  showDialog(
      context: context, builder: (BuildContext context) => bookmarksDialog);
}

class VersionedNoteWidget extends StatefulWidget {
  final Map<String, dynamic> noteItem;

  VersionedNoteWidget(this.noteItem);

  @override
  _VersionedNoteWidgetState createState() => _VersionedNoteWidgetState();
}

class _VersionedNoteWidgetState extends State<VersionedNoteWidget> {
  Map<String, dynamic> noteItem;
  int _initial;
  int _current;
  int _previous;
  bool _hasHistory = true;

  @override
  void initState() {
    noteItem = widget.noteItem;
    // initially the last in time is shown, with a previous if available
    _initial = noteItem[ID];
    _current = _initial;
    _previous = noteItem[PREVIOUSID];
    if (_previous == -1) {
      _previous = null;
      _hasHistory = false;
    }
    super.initState();
  }

  Future loadNote() async {
    var data = await ServerApi.getNote(_current);
    noteItem = jsonDecode(data);
    var userName = await ServerApi.getUserName(noteItem[USER]);
    noteItem[USER] = userName;
    _current = noteItem[ID];
    _previous = noteItem[PREVIOUSID];
    if (_previous == -1) {
      _previous = null;
    }
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    var id = noteItem[ID];
    var name = noteItem[TEXT];
    var ts = noteItem[TS];
    var geom = noteItem[THE_GEOM];
    JTS.Point point = JTS.WKTReader().read(geom.split(";")[1]);
    var p = LatLng(point.getY(), point.getX());
    var user = noteItem[USER];
    var sectionMap = noteItem[FORM];
    var sectionName = sectionMap[ATTR_SECTIONNAME];
    var titleWidget = SmashUI.titleText(
      sectionName,
      color: SmashColors.mainBackground,
      bold: true,
    );
    var formHelper =
        ServerFormHelper(_current, sectionName, sectionMap, titleWidget, p);

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: SmashUI.titleText(
            name,
            textAlign: TextAlign.center,
            useColor: true,
          ),
        ),
        Expanded(
          child: MasterDetailPage(
            formHelper,
            doScaffold: false,
            isReadOnly: true,
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: Row(
            mainAxisSize: MainAxisSize.max,
            children: [
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: SmashUI.smallText(
                      "Note: $id Surveyor: $user      Timestamp: $ts",
                      textAlign: TextAlign.center,
                      color: Colors.grey),
                ),
              ),
              _previous != null
                  ? IconButton(
                      tooltip: "View previous version.",
                      icon: Icon(MdiIcons.skipPrevious),
                      color: SmashColors.mainDecorations,
                      onPressed: () async {
                        _current = _previous;
                        await loadNote();
                      },
                    )
                  : Container(),
              _hasHistory && _current != _initial
                  ? IconButton(
                      icon: Icon(MdiIcons.skipForward),
                      color: SmashColors.mainDecorations,
                      tooltip: "View current version.",
                      onPressed: () async {
                        _current = _initial;
                        await loadNote();
                      },
                    )
                  : Container(),
            ],
          ),
        ),
      ],
    );
  }
}

openFilterDialog(BuildContext context) {
  var size = 600.0;
  Dialog filterDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: FilterWidget(),
    ),
  );
  showDialog(context: context, builder: (BuildContext context) => filterDialog);
}

class FilterWidget extends StatefulWidget {
  FilterWidget();
  _FilterWidgetState createState() => _FilterWidgetState();
}

class _FilterWidgetState extends State<FilterWidget>
    with AfterLayoutMixin<FilterWidget> {
  Map<String, bool> _projectsToActive;
  Map<String, bool> _surveyorsToActive;
  List<String> _projectNames;
  List<String> _surveyorNames;
  bool _doSurveyors = true;

  bool _dataLoaded = false;

  FilterStateModel _filterStateModel;

  @override
  Future<void> afterFirstLayout(BuildContext context) async {
    _filterStateModel = Provider.of<FilterStateModel>(context, listen: false);
    var sessionUser = SmashSession.getSessionUser();
    // String responsJson =
    //     await ServerApi.getProjects(sessionUser[0], sessionUser[1]);

    // var jsonMap = jsonDecode(responsJson);

    // List<dynamic> projects = jsonMap[KEY_PROJECTS];
    // List<String> filterProjects = _filterStateModel.projects;

    // Map<String, bool> tmp = {};
    // projects.forEach((name) {
    //   tmp[name] = filterProjects != null ? filterProjects.contains(name) : true;
    // });

    // _projectsToActive = tmp;
    // _projectNames = _projectsToActive.keys.toList();

    // responsJson =
    //     await ServerApi.getSurveyorsJson(sessionUser[0], sessionUser[1]);

    // jsonMap = jsonDecode(responsJson);

    // List<dynamic> surveyors = jsonMap[KEY_SURVEYORS];
    // List<String> filterSurveyors = _filterStateModel.surveyors;

    // tmp = {};
    // surveyors.forEach((map) {
    //   var active = map[SURVEYOR_ACTIVE_FIELD_NAME];
    //   if (active == 1) {
    //     var name = map[SURVEYOR_NAME_FIELD_NAME];

    //     tmp[name] =
    //         filterSurveyors != null ? filterSurveyors.contains(name) : true;
    //   }
    // });

    // _surveyorsToActive = tmp;
    // _surveyorNames = _surveyorsToActive.keys.toList();

    setState(() {
      _dataLoaded = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (!_dataLoaded) {
      return SmashCircularProgress();
    } else {
      List<dynamic> names = _doSurveyors ? _surveyorNames : _projectNames;
      Map<String, bool> name2active =
          _doSurveyors ? _surveyorsToActive : _projectsToActive;
      var title = _doSurveyors ? "SURVEYORS" : "PROJECTS";

      return Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: SmashUI.titleText(title),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              IconButton(
                icon: Icon(
                  MdiIcons.checkboxMultipleMarkedOutline,
                  color: SmashColors.mainDecorations,
                ),
                tooltip: "Select all",
                onPressed: () {
                  setSelectionTo(true);
                },
              ),
              IconButton(
                icon: Icon(
                  MdiIcons.checkboxMultipleBlankOutline,
                  color: SmashColors.mainDecorations,
                ),
                tooltip: "Unselect all",
                onPressed: () {
                  setSelectionTo(false);
                },
              )
            ],
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListView.builder(
                itemCount: names.length,
                itemBuilder: (BuildContext context, int index) {
                  var name = names[index];
                  var isActive = name2active[name];
                  return CheckboxListTile(
                      title: Text(name),
                      value: isActive,
                      onChanged: (selected) {
                        setState(() {
                          name2active[name] = selected;
                        });
                      });
                },
              ),
            ),
          ),
          ButtonBar(
            alignment: MainAxisAlignment.spaceEvenly,
            children: [
              TextButton(
                child: const Text('CANCEL'),
                onPressed: () {
                  Navigator.pop(context);
                },
              ),
              TextButton(
                child: const Text('RESET'),
                onPressed: () async {
                  _filterStateModel.reset();
                  var mapstateModel =
                      Provider.of<MapstateModel>(context, listen: false);
                  await mapstateModel.getData(context);
                  mapstateModel.fitbounds();
                  mapstateModel.reloadMap();
                  Navigator.pop(context);
                },
              ),
              TextButton(
                child: Text(_doSurveyors ? 'PROJECTS' : 'SURVEYORS'),
                onPressed: () {
                  setState(() {
                    _doSurveyors = !_doSurveyors;
                  });
                },
              ),
              TextButton(
                child: const Text('OK'),
                onPressed: () async {
                  _projectsToActive.removeWhere((key, value) => !value);
                  var activeProjects = _projectsToActive.entries
                      .map((entry) => entry.key)
                      .toList();
                  _filterStateModel.setProjectsQuiet(activeProjects);

                  _surveyorsToActive.removeWhere((key, value) => !value);
                  var activeSurveyors =
                      _surveyorsToActive.entries.map((e) => e.key).toList();
                  _filterStateModel.setSurveyors(activeSurveyors);

                  var mapstateModel =
                      Provider.of<MapstateModel>(context, listen: false);
                  await mapstateModel.getData(context);
                  mapstateModel.fitbounds();
                  mapstateModel.reloadMap();
                  Navigator.pop(context);
                },
              ),
            ],
          )
        ],
      );
    }
  }

  void setSelectionTo(bool selection) {
    if (_doSurveyors) {
      _surveyorsToActive.keys.forEach((key) {
        _surveyorsToActive[key] = selection;
      });
      // var activeSurveyors =
      //     _surveyorsToActive.entries.map((e) => e.key).toList();
      // _filterStateModel.setSurveyors(activeSurveyors);
    } else {
      _projectsToActive.keys.forEach((key) {
        _projectsToActive[key] = selection;
      });
      // var activeProjects =
      //     _projectsToActive.entries.map((entry) => entry.key).toList();
      // _filterStateModel.setProjects(activeProjects);
    }
    setState(() {});
  }
}

class BookmarksWidget extends StatefulWidget {
  BookmarksWidget();
  _BookmarksWidgetState createState() => _BookmarksWidgetState();
}

class _BookmarksWidgetState extends State<BookmarksWidget>
    with AfterLayoutMixin<BookmarksWidget> {
  List<String> bookmarks = [];
  bool _dataLoaded = false;

  @override
  Future<void> afterFirstLayout(BuildContext context) async {
    var bookmarksString = SmashSession.getBookmarks();
    if (bookmarksString.trim().length > 0) {
      bookmarks = bookmarksString.split("@");
    }
    setState(() {
      _dataLoaded = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    MapstateModel mapstateModel =
        Provider.of<MapstateModel>(context, listen: false);

    if (!_dataLoaded) {
      return SmashCircularProgress();
    } else {
      return Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: SmashUI.titleText("Bookmarks", useColor: true),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListView.builder(
                itemCount: bookmarks.length,
                itemBuilder: (BuildContext context, int index) {
                  var bookmark = bookmarks[index];

                  var name2bounds = bookmark.split(":");
                  var name = name2bounds[0];
                  var coordsList = name2bounds[1].split(",");
                  var west = double.parse(coordsList[0]);
                  var east = double.parse(coordsList[1]);
                  var south = double.parse(coordsList[2]);
                  var north = double.parse(coordsList[3]);

                  LatLngBounds b = new LatLngBounds.fromPoints(
                      [LatLng(south, west), LatLng(north, east)]);

                  return ListTile(
                    title: Text(name),
                    subtitle: Text(
                        "w:${west.toStringAsFixed(3)}, e:${east.toStringAsFixed(3)}, s:${south.toStringAsFixed(3)}, n:${north.toStringAsFixed(3)}"),
                    leading: IconButton(
                      icon: Icon(
                        MdiIcons.magnify,
                        color: SmashColors.mainDecorations,
                      ),
                      onPressed: () {
                        mapstateModel.mapController.fitBounds(b);
                        mapstateModel.currentMapBounds = b;
                        Provider.of<AttributesTableStateModel>(context,
                                listen: false)
                            .refresh();
                        Navigator.pop(context);
                      },
                    ),
                    trailing: IconButton(
                      icon: Icon(
                        MdiIcons.trashCan,
                      ),
                      onPressed: () async {
                        bookmarks.removeWhere((element) {
                          return element.startsWith("$name:");
                        });
                        setState(() {});
                        SmashSession.setBookmarks(bookmarks.join("@"));
                      },
                    ),
                  );
                },
              ),
            ),
          ),
          ButtonBar(
            alignment: MainAxisAlignment.spaceEvenly,
            children: [
              TextButton(
                child: const Text('CANCEL'),
                onPressed: () {
                  Navigator.pop(context);
                },
              ),
              TextButton(
                child: const Text('ADD CURRENT'),
                onPressed: () async {
                  String name = await SmashDialogs.showInputDialog(
                      context, "BOOKMARK", "Enter a name for the bookmark.");
                  if (name.trim().isNotEmpty) {
                    var b = mapstateModel.currentMapBounds;
                    String bm =
                        "$name:${b.west},${b.east},${b.south},${b.north}";
                    bookmarks.insert(0, bm);
                    setState(() {});
                    SmashSession.setBookmarks(bookmarks.join("@"));
                  }
                },
              ),
            ],
          )
        ],
      );
    }
  }
}

class Attributes {
  Widget marker;
  int id;
  String text;
  LatLng point;
}

class AttributesTableWidget extends StatefulWidget {
  AttributesTableWidget({Key key}) : super(key: key);

  @override
  _AttributesTableWidgetState createState() => _AttributesTableWidgetState();
}

class _AttributesTableWidgetState extends State<AttributesTableWidget> {
  List<List<Widget>> _dataRows;
  double width;
  double rowHeight = 52.0;
  var headerTexts = [
    "Marker",
    "Actions",
    "Id",
    "Text",
  ];
  var colFactors = [
    0.1,
    0.2,
    0.2,
    0.5,
  ];
  @override
  Widget build(BuildContext context) {
    width = ScreenUtilities.getWidth(context);
    if (width < 1500) {
      width = 1500;
    }

    return Consumer<AttributesTableStateModel>(
        builder: (context, attrState, child) {
      var mapstateModel = Provider.of<MapstateModel>(context, listen: false);
      _dataRows = mapstateModel.attributes.where((arrt) {
        var bounds = mapstateModel.currentMapBounds ??=
            mapstateModel.mapController.bounds;
        return bounds.contains(arrt.point);
      }).map((attr) {
        var textFun = (String str) {
          return Center(child: SmashUI.normalText(str));
        };
        return <Widget>[
          Center(child: attr.marker),
          Center(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: Icon(
                    MdiIcons.magnifyScan,
                    color: SmashColors.mainDecorations,
                  ),
                  tooltip: "Zoom to note.",
                  onPressed: () {
                    if (mapstateModel.mapController != null) {
                      var ll = LatLng(attr.point.latitude - NOTE_ZOOM_BUFFER,
                          attr.point.longitude - NOTE_ZOOM_BUFFER);
                      var ur = LatLng(attr.point.latitude + NOTE_ZOOM_BUFFER,
                          attr.point.longitude + NOTE_ZOOM_BUFFER);

                      mapstateModel.fitbounds(
                          newBounds: LatLngBounds.fromPoints([ll, ur]));
                      attrState.refresh();
                    }
                  },
                ),
                IconButton(
                  icon: Icon(
                    MdiIcons.glasses,
                    color: SmashColors.mainDecorations,
                  ),
                  tooltip: "View note.",
                  onPressed: () {
                    if (attr.marker is Image) {
                      openImageDialog(context, attr.text, attr.id,
                          hideRotate: false);
                    } else {
                      openNoteDialog(context, attr.id);
                    }
                  },
                ),
                // if (SmashSession.isAdmin())
                //   IconButton(
                //     icon: Icon(
                //       MdiIcons.trashCan,
                //       color: SmashColors.mainDanger,
                //     ),
                //     tooltip: "Delete note.",
                //     onPressed: () async {
                //       var doDelete = await SmashDialogs.showConfirmDialog(
                //           context,
                //           "DELETE",
                //           "Are you sure you want to delete the note? This can't be undone.");
                //       if (doDelete) {
                //         var up = SmashSession.getSessionUser();
                //         var response =
                //             await ServerApi.deleteNote(up[0], up[1], attr.id);
                //         await mapstateModel.getData(context);
                //         mapstateModel.reloadMap();
                //         attrState.refresh();
                //         if (response != null) {
                //           SmashDialogs.showErrorDialog(context, response);
                //         }
                //       }
                //     },
                //   ),
              ],
            ),
          ),
          textFun("${attr.id}"),
          textFun(attr.text),
          // textFun(TimeUtilities.ISO8601_TS_FORMATTER
          //     .format(DateTime.fromMillisecondsSinceEpoch(attr.timeStamp))),
          // textFun(attr.user ?? "- nv -"),
          // textFun(attr.project ?? "- nv -"),
        ];
      }).toList();

      var textFun = (String str) {
        return Center(child: SmashUI.normalText(str, bold: true));
      };
      final List headerIndexes =
          Iterable<int>.generate(headerTexts.length).toList();
      var headerWidgets = headerIndexes
          .map((index) => Container(
                child: textFun(
                  headerTexts[index],
                ),
                width: colFactors[index] * width,
                height: rowHeight + 5,
                padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
                alignment: Alignment.centerLeft,
              ))
          .toList();

      return Container(
        width: double.infinity,
        child: HorizontalDataTable(
          leftHandSideColumnWidth: width * colFactors[0],
          rightHandSideColumnWidth: width,
          isFixedHeader: true,
          headerWidgets: headerWidgets,
          leftSideItemBuilder: _generateFirstColumnRow,
          rightSideItemBuilder: _generateRightHandSideColumnRow,
          itemCount: _dataRows.length,
          rowSeparatorWidget: Divider(
            color: SmashColors.mainDecorations,
            height: 1.0,
            thickness: 0.5,
          ),
          leftHandSideColBackgroundColor: SmashColors.mainBackground,
          rightHandSideColBackgroundColor: SmashColors.mainBackground,
        ),
      );
    });
  }

  Widget _generateFirstColumnRow(BuildContext context, int index) {
    return Container(
      child: _dataRows[index][0],
      width: width * colFactors[0],
      height: rowHeight,
      padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
      alignment: Alignment.centerLeft,
    );
  }

  Widget _generateRightHandSideColumnRow(BuildContext context, int index) {
    List<Widget> row = _dataRows[index];

    final List rowIndexes = [1, 2, 3];
    var newRow = rowIndexes
        .map((i) => Container(
              child: row[i],
              width: width * colFactors[i],
              height: rowHeight,
              padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
              alignment: Alignment.centerLeft,
            ))
        .toList();

    return Row(
      children: newRow,
    );
  }
}
