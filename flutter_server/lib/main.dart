import 'dart:convert';

import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:fab_circular_menu/fab_circular_menu.dart';
import 'package:flushbar/flushbar.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/colors.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/ui.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/form_widgets.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/forms.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:latlong/latlong.dart';
import 'package:provider/provider.dart';
import 'package:transparent_image/transparent_image.dart';

export 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';

void main() {
  runApp(GssApp());
}

class GssApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<MapstateModel>(
      builder: (context) => MapstateModel(),
      child: MaterialApp(
        title: TITLE,
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          primarySwatch: MAIN_COLOR,
        ),
        home: MainPage(), //LoginScreen(),
      ),
    );
  }
}

class MainPage extends StatefulWidget {
  MainPage({Key key}) : super(key: key);

  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  MapController _mapController;
  PolylineLayerOptions _logs;
  List<Marker> _markers;
  double _screenWidth;
  double _screenHeight;

  @override
  void initState() {
    getData();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    if (_mapController == null) {
      _mapController = MapController();
    }
    final MAXZOOM = 22.0;
    final MINZOOM = 1.0;

//    html.IdbFactory fac = html.window.indexedDB;

    var layers = <LayerOptions>[];

//    if (_logs != null) {
//      layers.add(_logs);
//    }
    if (_markers != null && _markers.length > 0) {
      var markerCluster = MarkerClusterLayerOptions(
        maxClusterRadius: 40,
//        height: 40,
//        width: 40,
        size: Size(40, 40),
        fitBoundsOptions: FitBoundsOptions(
          padding: EdgeInsets.all(50),
        ),
        markers: _markers,
        showPolygon: false,
        zoomToBoundsOnClick: true,
//        polygonOptions: PolygonOptions(
//            borderColor: mainDecorationsDark,
//            color: mainDecorations.withOpacity(0.2),
//            borderStrokeWidth: 3),
        builder: (context, markers) {
          return FloatingActionButton(
            child: Text(markers.length.toString()),
            onPressed: null,
            backgroundColor: SmashColors.mainDecorationsDark,
            foregroundColor: SmashColors.mainBackground,
            heroTag: null,
          );
        },
      );
      layers.add(markerCluster);
    }

    var size = MediaQuery.of(context).size;
    _screenWidth = size.width;
    _screenHeight = size.height;

    var ringDiameter = _screenWidth * 0.4;
    var ringWidth = ringDiameter / 3;

    return Scaffold(
      key: _scaffoldKey,
      body: Stack(
        children: <Widget>[
          Consumer<MapstateModel>(
            builder: (context, model, _) => Stack(
              children: <Widget>[
                FlutterMap(
                  options: new MapOptions(
                    center: new LatLng(model.centerLat, model.centerLon),
                    zoom: model.currentZoom,
                    minZoom: MINZOOM,
                    maxZoom: MAXZOOM,
                    plugins: [
                      MarkerClusterPlugin(),
                    ],
                    // TODO check interaction possibilities
                  ),
                  layers: [model.backgroundLayer]..addAll(layers),
                  mapController: _mapController,
                ),
                Align(
                  alignment: Alignment.bottomLeft,
                  child: Padding(
                    padding: const EdgeInsets.all(24.0),
                    child: SelectMapLayerButton(
                      fabButtons: getButtons(model),
                      colorStartAnimation: Colors.green,
                      colorEndAnimation: Colors.red,
                      key: Key("layerMenu"),
                      animatedIconData: AnimatedIcons.menu_close,
                    ),
                  ),
                ),
              ],
            ),
          ),
          Align(
            alignment: Alignment.topRight,
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: FloatingActionButton(
                onPressed: () {},
                child: Icon(MdiIcons.logout),
              ),
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  FloatingActionButton(
                    mini: true,
                    onPressed: () {
                      setState(() {
                        var zoom = _mapController.zoom - 1;
                        if (zoom < MINZOOM) zoom = MINZOOM;
                        _mapController.move(_mapController.center, zoom);
                      });
                    },
                    child: Icon(MdiIcons.magnifyMinus),
                  ),
                  FloatingActionButton(
                    mini: true,
                    onPressed: () {
                      setState(() {
                        var zoom = _mapController.zoom + 1;
                        if (zoom > MAXZOOM) zoom = MAXZOOM;
                        _mapController.move(_mapController.center, zoom);
                      });
                    },
                    child: Icon(MdiIcons.magnifyPlus),
                  ),
                ],
              ),
            ),
          ),
          FabCircularMenu(
            ringDiameter: ringDiameter,
            ringWidth: ringWidth,
            child: Container(),
            ringColor: Colors.white30,
            fabCloseIcon: Icon(
              MdiIcons.close,
            ),
            fabOpenIcon: Icon(
              MdiIcons.settings,
            ),
            options: <Widget>[
              IconButton(
                  icon: Icon(MdiIcons.worker),
                  tooltip: "Manage Surveyors",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
              IconButton(
                  icon: Icon(MdiIcons.accountGroup),
                  tooltip: "Manage Webusers",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
              IconButton(
                  icon: Icon(MdiIcons.export),
                  tooltip: "Export",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
              IconButton(
                  icon: Icon(MdiIcons.bug),
                  tooltip: "Log View",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
            ],
          ),
        ],
      ),
    );
  }

  List<Widget> getButtons(var model) {
    return AVAILABLE_LAYERS_MAP.keys.map((name) {
      return Padding(
        padding: const EdgeInsets.all(8.0),
        child: Container(
          width: 200.0,
          child: FloatingActionButton.extended(
            label: Text(name),
            onPressed: () {
              if (name == MAPSFORGE) {
                model.reset();
              } else {
                model.backgroundLayer = AVAILABLE_LAYERS_MAP[name];
              }
            },
            key: Key(name),
//          tooltip: name,
//          icon: Icon(Icons.map),
          ),
        ),
      );
    }).toList();
  }

  void getData() async {
    var data = await ServerApi.getData();
    Map<String, dynamic> json = jsonDecode(data);
    List<dynamic> logsList = json[LOGS];

    if (logsList != null) {
      List<Polyline> lines = [];
      for (int i = 0; i < logsList.length; i++) {
        dynamic logItem = logsList[i];
//        var id = logItem[ID];
//        var name = logItem[NAME];
        var colorHex = logItem[COLOR];
        var width = logItem[WIDTH];
        var coords = logItem[COORDS];

        List<LatLng> points = [];
        for (int j = 0; j < coords.length; j++) {
          var coord = coords[j];
          points.add(LatLng(coord[Y], coord[X]));
        }

        lines.add(Polyline(
            points: points, strokeWidth: width, color: ColorExt(colorHex)));
      }
      _logs = PolylineLayerOptions(
        polylines: lines,
//        rebuild: true
      );
    }

    List<Marker> markers = <Marker>[];
    List<dynamic> simpleNotesList = json[NOTES];
    for (int i = 0; i < simpleNotesList.length; i++) {
      dynamic noteItem = simpleNotesList[i];
      //      var id = noteItem[ID];
      var name = noteItem[NAME];
      //      var ts = noteItem[TS];
      var x = noteItem[X];
      var y = noteItem[Y];
      markers.add(buildSimpleNote(x, y, name));
    }

    List<dynamic> formNotesList = json[FORMS];
    for (int i = 0; i < formNotesList.length; i++) {
      dynamic formItem = formNotesList[i];
      var id = formItem[ID];
      var name = formItem[NAME];
      var form = formItem[FORM];
      //      var ts = noteItem[TS];
      var x = formItem[X];
      var y = formItem[Y];
      markers.add(buildFormNote(x, y, name, form, id));
    }

    List<dynamic> imagesList = json[IMAGES];
    for (int i = 0; i < imagesList.length; i++) {
      dynamic imageItem = imagesList[i];
      //      var id = imageItem[ID];
      var dataId = imageItem[DATAID];
      var data = imageItem[DATA];
      var name = imageItem[NAME];
      //      var ts = imageItem[TS];
      var x = imageItem[X];
      var y = imageItem[Y];

      var imgData = Base64Decoder().convert(data);
      markers.add(buildImage(x, y, name, dataId, imgData));
    }

    _markers = markers;
    //    _mapController.move(p, 13);

    setState(() {});
  }

  Marker buildSimpleNote(var x, var y, String name) {
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

    return Marker(
      width: textlen,
      height: 180,
      point: new LatLng(y, x),
      builder: (ctx) => new Container(
        child: Column(
          children: <Widget>[
            Icon(
              MdiIcons.noteText,
              size: 48,
              color: Colors.indigo,
            ),
            Container(
              decoration: new BoxDecoration(
                  color: Colors.indigo,
                  borderRadius:
                      new BorderRadius.all(const Radius.circular(10.0))),
              child: new Center(
                child: Padding(
                  padding: const EdgeInsets.all(5.0),
                  child: Text(
                    name,
                    style: TextStyle(
                        fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                ),
              ),
            )
          ],
        ),
      ),
    );
  }

  Marker buildImage(var x, var y, String name, var dataId, var data) {
    var imageWidget = Image.memory(
      data,
      scale: 2.0,
    );

    return Marker(
      width: 180,
      height: 180,
      point: new LatLng(y, x),
      builder: (ctx) => new Container(
        child: GestureDetector(
          onTap: () async {
            Flushbar(
              flushbarPosition: FlushbarPosition.BOTTOM,
              flushbarStyle: FlushbarStyle.GROUNDED,
              backgroundColor: Colors.white.withAlpha(128),
//              isDismissible: true,
//              dismissDirection: FlushbarDismissDirection.HORIZONTAL,
              onTap: (e) {
                Navigator.of(context).pop();
              },
              titleText: Text(
                name,
                style: DEFAULT_BLACKSTYLE,
                textAlign: TextAlign.center,
              ),
              messageText: Container(
                child: Center(
                  child: Stack(
                    children: <Widget>[
                      Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Center(child: CircularProgressIndicator()),
                      ),
                      Center(
                        child: Container(
                          height: _screenHeight / 2.0,
                          decoration: BoxDecoration(
                              border: Border.all(color: MAIN_COLOR)),
                          padding: EdgeInsets.all(5),
                          child: FadeInImage.memoryNetwork(
                              placeholder: kTransparentImage,
                              image: "$API_IMAGE/$dataId",
                              fit: BoxFit.contain),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            )..show(context);
          },
          child: imageWidget,
        ),
      ),
    );
  }

  Marker buildFormNote(var x, var y, String name, String form, var id) {
    LatLng p = LatLng(y, x);
    return Marker(
      width: 180,
      height: 180,
      point: new LatLng(y, x),
      builder: (ctx) => new Container(
        child: GestureDetector(
          onTap: () async {
            var sectionMap = jsonDecode(form);
            var sectionName = sectionMap[ATTR_SECTIONNAME];

            Flushbar(
              flushbarPosition: FlushbarPosition.BOTTOM,
              flushbarStyle: FlushbarStyle.GROUNDED,
              backgroundColor: Colors.white.withAlpha(128),
              onTap: (e) {
                Navigator.of(context).pop();
              },
              titleText: Text(
                name,
                style: DEFAULT_BLACKSTYLE,
                textAlign: TextAlign.center,
              ),
              messageText: Container(
                height: 600,
                child: Center(
                  child: MasterDetailPage(
                    sectionMap,
                    SmashUI.titleText(sectionName,
                        color: SmashColors.mainBackground, bold: true),
                    sectionName,
                    p,
                    id,
                  ),
                ),
              ),
            )..show(context);
          },
          child: Column(
            children: <Widget>[
              Icon(
                MdiIcons.notebook,
                size: 48,
                color: Colors.deepOrange,
              ),
              Container(
                decoration: new BoxDecoration(
                    color: Colors.deepOrange,
                    borderRadius:
                        new BorderRadius.all(const Radius.circular(10.0))),
                child: new Center(
                  child: Padding(
                    padding: const EdgeInsets.all(5.0),
                    child: Text(
                      name,
                      style: TextStyle(
                          fontWeight: FontWeight.bold, color: Colors.white),
                    ),
                  ),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}
