import 'package:after_layout/after_layout.dart';
import 'package:fab_circular_menu/fab_circular_menu.dart';
import 'package:flushbar/flushbar.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/widgets.dart';
import 'package:latlong/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

class MainMapView extends StatefulWidget {
  MainMapView({Key key}) : super(key: key);

  @override
  _MainMapViewState createState() => _MainMapViewState();
}

class _MainMapViewState extends State<MainMapView>
    with AfterLayoutMixin<MainMapView> {
  GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  MapController _mapController;

  double _screenWidth;
  int _heroCount;

  static const _mapIconsPadding = 16.0;
  static const _mapIconsSizeNormal = 48.0;
  static const _mapIconsSizeMini = 38.0;

  @override
  void afterFirstLayout(BuildContext context) {
    var mapstateModel = Provider.of<MapstateModel>(context, listen: false);
    mapstateModel.getData(context).then((value) {
      mapstateModel.reloadMap();
      mapstateModel.fitbounds();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<MapstateModel>(builder: (context, mapState, child) {
      return consume(context, mapState);
    });
  }

  Widget consume(BuildContext context, MapstateModel mapstateModel) {
    if (_mapController == null) {
      _mapController = MapController();
      mapstateModel.mapController = _mapController;
    }
    final MAXZOOM = 22.0;
    final MINZOOM = 1.0;
    _heroCount = 0;

    var size = MediaQuery.of(context).size;
    var screenHeight = size.height;
    _screenWidth = size.width;
    mapstateModel.screenHeight = screenHeight;

    var layers = <LayerOptions>[];

    if (mapstateModel.logs != null) {
      layers.add(mapstateModel.logs);
    }
    if (mapstateModel.mapMarkers != null &&
        mapstateModel.mapMarkers.isNotEmpty) {
      var markerCluster = MarkerClusterLayerOptions(
        maxClusterRadius: 20,
        size: Size(40, 40),
        fitBoundsOptions: FitBoundsOptions(
          padding: EdgeInsets.all(50),
        ),
        markers: mapstateModel.mapMarkers,
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
            backgroundColor: SmashColors.mainDecorationsDarker,
            foregroundColor: SmashColors.mainBackground,
            heroTag: "${_heroCount++}",
          );
        },
      );
      layers.add(markerCluster);
    }

    var ringDiameter = _screenWidth * 0.4;
    var ringWidth = ringDiameter / 3;

    var xyz = SmashSession.getMapcenter();
    var basemap = SmashSession.getBasemap();

    FilterStateModel filterStateModel =
        Provider.of<FilterStateModel>(context, listen: false);

    return Scaffold(
      key: _scaffoldKey,
      body: Stack(
        children: <Widget>[
          Consumer<FilterStateModel>(
            builder: (context, model, _) => Stack(
              children: <Widget>[
                Listener(
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
                      center: new LatLng(xyz[1], xyz[0]),
                      zoom: xyz[2],
                      minZoom: MINZOOM,
                      maxZoom: MAXZOOM,
                      plugins: [
                        MarkerClusterPlugin(),
                      ],
                    ),
                    layers: [AVAILABLE_MAPS[basemap]]..addAll(layers),
                    mapController: _mapController,
                  ),
                ),
              ],
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              color: SmashColors.mainBackground.withAlpha(128),
              height: _mapIconsSizeNormal + _mapIconsPadding * 2,
              width: double.infinity,
            ),
          ),
          Align(
            alignment: Alignment.topRight,
            child: Padding(
              padding: const EdgeInsets.all(_mapIconsPadding),
              child: FloatingActionButton(
                  key: Key("map_dialog_button"),
                  child: Icon(
                    MdiIcons.map,
                  ),
                  backgroundColor: SmashColors.mainDecorations,
                  onPressed: () {
                    openMapSelectionDialog(context);
                  }),
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: Padding(
              padding: const EdgeInsets.all(_mapIconsPadding),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: SizedBox(
                      width: _mapIconsSizeMini,
                      height: _mapIconsSizeMini,
                      child: FloatingActionButton(
                        heroTag: "zoomin",
                        backgroundColor: SmashColors.mainDecorations,
                        mini: true,
                        onPressed: () {
                          var zoom = _mapController.zoom - 1;
                          if (zoom < MINZOOM) zoom = MINZOOM;
                          _mapController.move(_mapController.center, zoom);
                        },
                        child: Icon(MdiIcons.magnifyMinus),
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: SizedBox(
                      width: _mapIconsSizeMini,
                      height: _mapIconsSizeMini,
                      child: FloatingActionButton(
                        backgroundColor: SmashColors.mainDecorations,
                        heroTag: "zoomdata",
                        mini: true,
                        tooltip: "Zoom to data",
                        onPressed: () {
                          if (mapstateModel.dataBounds.isValid) {
                            _mapController.fitBounds(mapstateModel.dataBounds);
                          }
                        },
                        child: Icon(MdiIcons.layers),
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: SizedBox(
                      width: _mapIconsSizeMini,
                      height: _mapIconsSizeMini,
                      child: FloatingActionButton(
                        backgroundColor: SmashColors.mainDecorations,
                        heroTag: "zoomout",
                        mini: true,
                        onPressed: () {
                          var zoom = _mapController.zoom + 1;
                          if (zoom > MAXZOOM) zoom = MAXZOOM;
                          _mapController.move(_mapController.center, zoom);
                        },
                        child: Icon(MdiIcons.magnifyPlus),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          Align(
            alignment: Alignment.topLeft,
            child: Padding(
              padding: const EdgeInsets.all(_mapIconsPadding),
              child: FloatingActionButton(
                heroTag: "opendrawer",
                elevation: 1,
                backgroundColor: Colors.transparent,
                foregroundColor: SmashColors.mainDecorationsDarker,
                onPressed: () {
                  _scaffoldKey.currentState.openDrawer();
                },
                child: Icon(MdiIcons.menu),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(_mapIconsPadding),
            child: getCircularMenu(
                context, ringDiameter, ringWidth, filterStateModel),
          ),
        ],
      ),
      drawer: Drawer(
          child: ListView(
        children: _getDrawerWidgets(context),
      )),
      endDrawer: Drawer(
          child: ListView(
        children: _getMapDrawerWidgets(context),
      )),
      endDrawerEnableOpenDragGesture: false,
    );
  }

  _getDrawerWidgets(BuildContext context) {
    double iconSize = 48;
    double textSize = iconSize / 2;
    return [
      new Container(
        margin: EdgeInsets.only(bottom: 20),
        child: new DrawerHeader(
          child: Stack(
            children: <Widget>[
              Align(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Image.asset("assets/smash_logo.png"),
                ),
                alignment: Alignment.center,
              ),
              Text(
                "User: ${SmashSession.getSessionUser()[0]}",
                style: TextStyle(color: Colors.blueGrey),
              )
            ],
          ),
        ),
        color: SmashColors.mainDecorations.withAlpha(70),
      ),
      new Container(
        child: new Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.accountHardHat,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Surveyors"),
                onTap: () {
                  // TODO
                  Navigator.of(context).pop();
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.accountGroup,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Web Users"),
                onTap: () {
                  // TODO
                  Navigator.of(context).pop();
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.notebook,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Form Builder"),
                onTap: () {
                  // TODO
                  Navigator.of(context).pop();
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.databaseExport,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Export"),
                onTap: () {
                  // TODO
                  Navigator.of(context).pop();
                },
              ),
            ),
//            Expanded(
//              child: Container(),
//            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.bug,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Log"),
                onTap: () {
                  // TODO
                  Navigator.of(context).pop();
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.informationOutline,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("About"),
                onTap: () {
                  // TODO
                  Navigator.of(context).pop();
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.logout,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Logout"),
                onTap: () {
                  Navigator.of(context).pop();
                  // MapstateModel model = Provider.of<MapstateModel>(context, listen: false);
                  setState(() {
                    SmashSession.logout(
                      mapCenter:
                          "${_mapController.center.longitude};${_mapController.center.latitude};${_mapController.zoom}",
                    );
                  });
                },
              ),
            ),
          ],
        ),
      ),
    ];
  }

  FabCircularMenu getCircularMenu(BuildContext context, double ringDiameter,
      double ringWidth, FilterStateModel filterStateModel) {
    return FabCircularMenu(
      ringDiameter: ringDiameter,
      ringWidth: ringWidth,
      ringColor: Colors.white30,
      fabCloseIcon: Icon(
        MdiIcons.close,
      ),
      fabOpenIcon: Icon(
        MdiIcons.filterMenuOutline,
      ),
      children: <Widget>[
        IconButton(
          key: Key("filter1"),
          icon: Icon(MdiIcons.accountHardHat),
          tooltip: "Filter by Surveyor",
          onPressed: () {
            Flushbar(
              flushbarPosition: FlushbarPosition.BOTTOM,
              flushbarStyle: FlushbarStyle.GROUNDED,
              backgroundColor: Colors.white.withAlpha(128),
              onTap: (e) {
                Navigator.of(context).pop();
              },
              messageText: Container(
                height: 600,
                child: Center(
                  child: FilterSurveyor(filterStateModel),
                ),
              ),
            )..show(context);
          },
          iconSize: 48.0,
          color: filterStateModel.surveyors != null
              ? SmashColors.mainSelectionBorder
              : SmashColors.mainDecorationsDarker,
        ),
        IconButton(
          key: Key("filter4"),
          icon: Icon(MdiIcons.folderOutline),
          tooltip: "Filter by Project",
          onPressed: () {
            Flushbar(
              flushbarPosition: FlushbarPosition.BOTTOM,
              flushbarStyle: FlushbarStyle.GROUNDED,
              backgroundColor: Colors.white.withAlpha(128),
              onTap: (e) {
                Navigator.of(context).pop();
              },
              messageText: Container(
                height: 600,
                child: Center(
                  child: FilterProject(filterStateModel),
                ),
              ),
            )..show(context);
          },
          iconSize: 48.0,
          color: filterStateModel.projects != null
              ? SmashColors.mainSelectionBorder
              : SmashColors.mainDecorationsDarker,
        ),
        // IconButton(
        //   key: Key("filter2"),
        //   icon: Icon(MdiIcons.calendar),
        //   tooltip: "Filter by Date",
        //   onPressed: () {},
        //   iconSize: 48.0,
        //   color: SmashColors.mainDecorationsDarker,
        // ),
        IconButton(
          key: Key("filter3"),
          icon: Icon(MdiIcons.filterRemove),
          tooltip: "Remove all Filters",
          onPressed: () async {
            filterStateModel.reset();
            var mapstateModel =
                Provider.of<MapstateModel>(context, listen: false);
            await mapstateModel.getData(context);
            mapstateModel.fitbounds();
            mapstateModel.reloadMap();
          },
          iconSize: 48.0,
          color: SmashColors.mainDecorationsDarker,
        ),
      ],
    );
  }

  _getMapDrawerWidgets(BuildContext context) {
    return AVAILABLE_ONLINE_MAPS.keys.map((name) {
      return Padding(
          padding: const EdgeInsets.all(8.0),
          child: OnlineSourceCard(name, AVAILABLE_ONLINE_MAPS[name])

//         Container(
//           width: 200.0,
//           child: FloatingActionButton.extended(
//             label: Text(name),
//             onPressed: () {
//               SmashSession.setBasemap(name);
//               setState(() {});
//             },
//             key: Key(name),
//             heroTag: name,
// //          tooltip: name,
// //          icon: Icon(Icons.map),
//           ),
//         ),
          );
    }).toList();
  }
}
