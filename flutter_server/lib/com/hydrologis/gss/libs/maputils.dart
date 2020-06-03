import 'dart:convert';

import 'package:after_layout/after_layout.dart';
import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:horizontal_data_table/horizontal_data_table.dart';
import 'package:latlong/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

const IMAGE_ID_SEPARATOR = ";";

Marker buildSimpleNote(MapstateModel mapState, var x, var y, String name,
    int noteId, Icon icon, double size, Color color) {
  List lengthHeight = guessTextDimensions(name, size);
  return Marker(
    width: lengthHeight[0],
    height: size + lengthHeight[1],
    point: new LatLng(y, x),
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
          openImageDialog(mapState.currentMapContext, name, dataId);
          // }
        },
        child: imageWidget,
      ),
    ),
  );
}

Marker buildFormNote(MapstateModel mapState, var x, var y, String name,
    String form, var noteId, Icon icon, double size, Color color) {
  List lengthHeight = guessTextDimensions(name, size);

  return Marker(
    width: lengthHeight[0],
    height: size + lengthHeight[1],
    point: new LatLng(y, x),
    builder: (ctx) => new Container(
      child: GestureDetector(
        child: Column(
          children: <Widget>[
            icon,
            Container(
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
          await openNoteDialog(ctx, noteId);
          // }
        },
      ),
    ),
  );
}

openNoteDialog(BuildContext context, int noteId) async {
  var userPwd = SmashSession.getSessionUser();

  var data = await ServerApi.getNoteById(userPwd[0], userPwd[1], noteId);
  Map<String, dynamic> noteItem = jsonDecode(data);
  var id = noteItem[ID];
  var name = noteItem[NAME];
  var ts = noteItem[TS];
  var x = noteItem[X];
  var y = noteItem[Y];
  var p = LatLng(y, x);
  var surveyor = noteItem[SURVEYOR];
  var project = noteItem[PROJECT];
  var form = noteItem[FORM];

  var widget;
  var h = 300.0;
  var w = 400.0;
  if (form != null) {
    h = 900.0;
    w = 900.0;
    var sectionMap = jsonDecode(form);
    var sectionName = sectionMap[ATTR_SECTIONNAME];

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
          child: MasterDetailPage(
            sectionMap,
            SmashUI.titleText(
              sectionName,
              color: SmashColors.mainBackground,
              bold: true,
            ),
            sectionName,
            p,
            noteId,
            null, // TODO add here save function if editing is supported on web
            getThumbnailsFromDb,
            null, // no taking pictures permitted on web
            doScaffold: false,
            isReadOnly: true,
          ),
        ),
      ],
    );
  } else {
    var map = {
      "ID": id,
      "Text": name,
      "Timestamp": TimeUtilities.ISO8601_TS_FORMATTER
          .format(DateTime.fromMillisecondsSinceEpoch(ts)),
      "Project": project,
      "Surveyor": surveyor,
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

  Dialog mapSelectionDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: h,
      width: w,
      child: Center(child: widget),
    ),
  );
  await showDialog(
      context: context, builder: (BuildContext context) => mapSelectionDialog);
}

openImageDialog(BuildContext context, String name, int imageId) {
  var h = MediaQuery.of(context).size.height;
  var size = 600.0;
  Dialog mapSelectionDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: SmashUI.titleText(
                name,
                textAlign: TextAlign.center,
              ),
            ),
            NetworkImageWidget("$API_IMAGEDATA/$imageId", h * 0.6),
          ],
        ),
      ),
    ),
  );
  showDialog(
      context: context, builder: (BuildContext context) => mapSelectionDialog);
}

openMapSelectionDialog(BuildContext context) {
  var size = 400.0;
  Dialog mapSelectionDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: BackgroundMapSelectionWidget(),
    ),
  );
  showDialog(
      context: context, builder: (BuildContext context) => mapSelectionDialog);
}

class BackgroundMapSelectionWidget extends StatefulWidget {
  BackgroundMapSelectionWidget();
  _BackgroundMapSelectionWidgetState createState() =>
      _BackgroundMapSelectionWidgetState();
}

class _BackgroundMapSelectionWidgetState
    extends State<BackgroundMapSelectionWidget> {
  int _index = 0;
  List<TileLayerOptions> _widgets = [];
  List<String> _names = [];
  MapController _mapController = new MapController();

  @override
  void initState() {
    AVAILABLE_MAPS.forEach((name, tilelayer) {
      _names.add(name);
      _widgets.add(tilelayer);
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: SmashUI.titleText(_names[_index]),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Listener(
              // listen to mouse scroll
              onPointerSignal: (e) {
                if (e is PointerScrollEvent) {
                  var delta = e.scrollDelta.direction;
                  _mapController.move(_mapController.center,
                      _mapController.zoom + (delta > 0 ? -0.2 : 0.2));
                }
              },
              child: FlutterMap(
                options: new MapOptions(
                  center: new LatLng(46.47781, 11.33140),
                  zoom: 8,
                ),
                layers: [_widgets[_index]],
                mapController: _mapController,
              ),
            ),
          ),
        ),
        ButtonBar(
          alignment: MainAxisAlignment.spaceEvenly,
          children: [
            FlatButton(
              child: const Text('CANCEL'),
              onPressed: () {
                Navigator.pop(context);
              },
            ),
            FlatButton(
              child: const Text('NEXT'),
              onPressed: () {
                setState(() {
                  _index = _index + 1;
                  if (_index >= _widgets.length) {
                    _index = 0;
                  }
                });
              },
            ),
            FlatButton(
              child: const Text('OK'),
              onPressed: () {
                SmashSession.setBasemap(_names[_index]);
                var mapstateModel =
                    Provider.of<MapstateModel>(context, listen: false);
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
    String responsJson =
        await ServerApi.getProjects(sessionUser[0], sessionUser[1]);

    var jsonMap = jsonDecode(responsJson);

    List<dynamic> projects = jsonMap[KEY_PROJECTS];
    List<String> filterProjects = _filterStateModel.projects;

    Map<String, bool> tmp = {};
    projects.forEach((name) {
      tmp[name] = filterProjects != null ? filterProjects.contains(name) : true;
    });

    _projectsToActive = tmp;
    _projectNames = _projectsToActive.keys.toList();

    responsJson =
        await ServerApi.getSurveyorsJson(sessionUser[0], sessionUser[1]);

    jsonMap = jsonDecode(responsJson);

    List<dynamic> surveyors = jsonMap[KEY_SURVEYORS];
    List<String> filterSurveyors = _filterStateModel.surveyors;

    tmp = {};
    surveyors.forEach((map) {
      var active = map[SURVEYOR_ACTIVE_FIELD_NAME];
      if (active == 1) {
        var name = map[SURVEYOR_NAME_FIELD_NAME];

        tmp[name] =
            filterSurveyors != null ? filterSurveyors.contains(name) : true;
      }
    });

    _surveyorsToActive = tmp;
    _surveyorNames = _surveyorsToActive.keys.toList();

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
              FlatButton(
                child: const Text('CANCEL'),
                onPressed: () {
                  Navigator.pop(context);
                },
              ),
              FlatButton(
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
              FlatButton(
                child: Text(_doSurveyors ? 'PROJECTS' : 'SURVEYORS'),
                onPressed: () {
                  setState(() {
                    _doSurveyors = !_doSurveyors;
                  });
                },
              ),
              FlatButton(
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
}

class Attributes {
  Widget marker;
  int id;
  String text;
  int timeStamp;
  String user;
  String project;
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
    "Timestamp",
    "User",
    "Project",
  ];
  var colFactors = [
    0.05,
    0.1,
    0.05,
    0.2,
    0.2,
    0.2,
    0.2,
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
      _dataRows = mapstateModel.attributes
          .where((arrt) => mapstateModel.currentMapBounds.contains(arrt.point))
          .map((attr) {
        var id = attrState.selectedNoteId;
        bool selected = false;
        if (id != null && id == attr.id) {
          selected = true;
        }

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
                      openImageDialog(context, attr.text, attr.id);
                    } else {
                      openNoteDialog(context, attr.id);
                    }
                  },
                ),
              ],
            ),
          ),
          textFun("${attr.id}"),
          textFun(attr.text),
          textFun(TimeUtilities.ISO8601_TS_FORMATTER
              .format(DateTime.fromMillisecondsSinceEpoch(attr.timeStamp))),
          textFun(attr.user ?? "- nv -"),
          textFun(attr.project ?? "- nv -"),
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

    final List rowIndexes = [1, 2, 3, 4, 5, 6];
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

/// Get thumbnails from the database
Future<List<Widget>> getThumbnailsFromDb(
    BuildContext context, var itemMap, List<String> imageSplit) async {
  List<Widget> thumbList = [];
  String value = ""; //$NON-NLS-1$
  if (itemMap.containsKey(TAG_VALUE)) {
    value = itemMap[TAG_VALUE].trim();
  }
  if (value.isNotEmpty) {
    imageSplit.clear();
    imageSplit.addAll(value.split(IMAGE_ID_SEPARATOR));
  } else {
    return Future.value(thumbList);
  }

  var userPwd = SmashSession.getSessionUser();

  for (int i = 0; i < imageSplit.length; i++) {
    var id = int.parse(imageSplit[i]);
    var bytes =
        await ServerApi.getImageThumbnailById(userPwd[0], userPwd[1], id);
    Image img = Image.memory(bytes);
    Widget withBorder = Container(
      padding: SmashUI.defaultPadding(),
      child: img,
    );
    thumbList.add(withBorder);
  }
  return thumbList;
}
