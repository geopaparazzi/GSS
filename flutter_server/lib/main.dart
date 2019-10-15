import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:latlong/latlong.dart';
import 'package:provider/provider.dart';
import 'com/hydrologis/gss/variables.dart';
import 'com/hydrologis/gss/layers.dart';
import 'com/hydrologis/gss/utils.dart';
import 'package:fab_circular_menu/fab_circular_menu.dart';
import 'package:community_material_icon/community_material_icon.dart';
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
  MapController _mapController;
  PolylineLayerOptions _logs;
  List<Marker> _markers;

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
    if (_logs != null) {
      layers.add(_logs);
    }
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
            backgroundColor: mainDecorationsDark,
            foregroundColor: mainBackground,
            heroTag: null,
          );
        },
      );
      layers.add(markerCluster);
    }

    var ringDiameter = MediaQuery.of(context).size.width * 0.4;
    var ringWidth = ringDiameter / 3;

    return Scaffold(
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
                child: Icon(CommunityMaterialIcons.logout),
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
                    child: Icon(CommunityMaterialIcons.magnify_minus),
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
                    child: Icon(CommunityMaterialIcons.magnify_plus),
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
              CommunityMaterialIcons.close,
            ),
            fabOpenIcon: Icon(
              CommunityMaterialIcons.settings,
            ),
            options: <Widget>[
              IconButton(
                  icon: Icon(CommunityMaterialIcons.worker),
                  tooltip: "Manage Surveyors",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
              IconButton(
                  icon: Icon(CommunityMaterialIcons.account_group),
                  tooltip: "Manage Webusers",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
              IconButton(
                  icon: Icon(CommunityMaterialIcons.export),
                  tooltip: "Export",
                  onPressed: () {},
                  iconSize: 48.0,
                  color: Colors.deepOrange),
              IconButton(
                  icon: Icon(CommunityMaterialIcons.bug),
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
      );
    }

    List<Marker> markers = <Marker>[];
    List<dynamic> notesList = json[NOTES];

    for (int i = 0; i < notesList.length; i++) {
      dynamic noteItem = notesList[i];
//      var id = noteItem[ID];
      var name = noteItem[NAME];
//      var ts = noteItem[TS];
      var x = noteItem[X];
      var y = noteItem[Y];
      markers.add(buildNote(x, y, name));
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

    // TODO add clickable notes, that could show also forms!

    setState(() {});
  }

  Marker buildNote(var x, var y, String name) {
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
              CommunityMaterialIcons.note_text,
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
    var imageWidget = Image.memory(data, scale: 2.0,);

    return Marker(
      width: 180,
      height: 180,
      point: new LatLng(y, x),
      builder: (ctx) => new Container(
        child: imageWidget,
      ),
    );
  }
}
