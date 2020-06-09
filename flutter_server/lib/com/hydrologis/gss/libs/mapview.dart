import 'package:after_layout/after_layout.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/about.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/projectdata.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/surveyors_view.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/webusers_view.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/main.dart';
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
  AttributesTableWidget attributesTableWidget;
  final maxZoom = 22.0;
  final minZoom = 1.0;

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

      Provider.of<AttributesTableStateModel>(context, listen: false).refresh();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<MapstateModel>(builder: (context, mapState, child) {
      mapState.currentMapContext = context;
      return consume(context, mapState);
    });
  }

  Widget consume(BuildContext context, MapstateModel mapstateModel) {
    if (_mapController == null) {
      _mapController = MapController();
      mapstateModel.mapController = _mapController;
    }

    _heroCount = 0;

    var size = MediaQuery.of(context).size;
    var screenHeight = size.height;
    mapstateModel.screenHeight = screenHeight;

    var layers = <LayerOptions>[];

    if (mapstateModel.logs != null) {
      layers.add(mapstateModel.logs);
    }
    if (mapstateModel.mapMarkers != null &&
        mapstateModel.mapMarkers.isNotEmpty) {
      var markerCluster = MarkerClusterLayerOptions(
        maxClusterRadius: 5,
        size: Size(40, 40),
        fitBoundsOptions: FitBoundsOptions(
          padding: EdgeInsets.all(50),
        ),
        markers: mapstateModel.mapMarkers,
        showPolygon: false,
        zoomToBoundsOnClick: true,
        // polygonOptions: PolygonOptions(
        //     borderColor: SmashColors.mainDecorationsDarker,
        //     color: SmashColors.mainDecorationsDarker.withOpacity(0.2),
        //     borderStrokeWidth: 3),
        builder: (context, markers) {
          return FloatingActionButton(
            child: Text(markers.length.toString()),
            onPressed: null,
            backgroundColor: SmashColors.mainDecorationsDarker,
            foregroundColor: SmashColors.mainBackground,
            heroTag: "Cluster_${_heroCount++}",
          );
        },
      );
      layers.add(markerCluster);
    }

    var xyz = SmashSession.getMapcenter();
    var basemap = SmashSession.getBasemap();
    var mapPointers = 0;

    attributesTableWidget =
        mapstateModel.showAttributes ? AttributesTableWidget() : null;
    return Scaffold(
      key: _scaffoldKey,
      body: Stack(
        children: <Widget>[
          Column(
            mainAxisSize: MainAxisSize.max,
            children: [
              Expanded(
                flex: 2,
                child: Listener(
                  // listen to mouse scroll
                  onPointerSignal: (e) {
                    if (e is PointerScrollEvent) {
                      var delta = e.scrollDelta.direction;
                      _mapController.move(_mapController.center,
                          _mapController.zoom + (delta > 0 ? -0.2 : 0.2));
                      var bounds = _mapController.bounds;
                      mapstateModel.currentMapBounds = bounds;
                      Provider.of<AttributesTableStateModel>(context,
                              listen: false)
                          .refresh();
                    }
                  },
                  onPointerDown: (details) {
                    // if (mapPointers == 0) {
                    //   print("onMoveStart");
                    // }
                    mapPointers++;
                  },
                  onPointerUp: (details) {
                    mapPointers--;
                    if (mapPointers == 0) {
                      print("onMoveEnd");
                      var bounds = _mapController.bounds;
                      mapstateModel.currentMapBounds = bounds;
                      Provider.of<AttributesTableStateModel>(context,
                              listen: false)
                          .refresh();
                    }
                  },
                  behavior: HitTestBehavior.deferToChild,
                  child: FlutterMap(
                    options: new MapOptions(
                      center: new LatLng(xyz[1], xyz[0]),
                      zoom: xyz[2],
                      minZoom: minZoom,
                      maxZoom: maxZoom,
                      plugins: [
                        MarkerClusterPlugin(),
                      ],
                    ),
                    layers: [AVAILABLE_MAPS[basemap]]..addAll(layers),
                    mapController: _mapController,
                  ),
                ),
              ),
              mapstateModel.showAttributes
                  ? Expanded(
                      flex: 1,
                      child: attributesTableWidget,
                    )
                  : Container()
            ],
          ),
          Align(
            alignment: Alignment.topRight,
            child: Padding(
              padding: const EdgeInsets.all(_mapIconsPadding),
              child: Column(
                children: [
                  FloatingActionButton(
                      tooltip: "Open background map selector.",
                      heroTag: "map_dialog_button",
                      child: Icon(
                        MdiIcons.map,
                      ),
                      backgroundColor: SmashColors.mainDecorations,
                      onPressed: () {
                        openMapSelectionDialog(context);
                      }),
                  Padding(
                    padding: const EdgeInsets.only(top: 8.0),
                    child: FloatingActionButton(
                        tooltip: "Open data filter dialog.",
                        heroTag: "filter_dialog_button",
                        child: Icon(
                          MdiIcons.filterMenuOutline,
                        ),
                        backgroundColor: SmashColors.mainDecorations,
                        onPressed: () {
                          openFilterDialog(context);
                        }),
                  ),
                  Padding(
                    padding: const EdgeInsets.only(top: 8.0),
                    child: FloatingActionButton(
                        tooltip: mapstateModel.showAttributes
                            ? "Close attributes table."
                            : "Open attributes table.",
                        heroTag: "view_attributes_button",
                        child: Icon(
                          mapstateModel.showAttributes
                              ? MdiIcons.tableLargeRemove
                              : MdiIcons.tableLarge,
                        ),
                        backgroundColor: SmashColors.mainDecorations,
                        onPressed: () {
                          setState(() {
                            mapstateModel.showAttributes =
                                !mapstateModel.showAttributes;
                            int delta = -1;
                            if (mapstateModel.showAttributes) {
                              delta = 1;
                            }
                            _mapController.move(_mapController.center,
                                _mapController.zoom + delta);
                          });
                          // mapstateModel.reloadMap();
                        }),
                  ),
                ],
              ),
            ),
          ),
          getZoomButtons(mapstateModel),
          Align(
            alignment: Alignment.topLeft,
            child: Padding(
              padding: const EdgeInsets.all(_mapIconsPadding),
              child: FloatingActionButton(
                heroTag: "opendrawer",
                backgroundColor: SmashColors.mainDecorations,
                onPressed: () {
                  _scaffoldKey.currentState.openDrawer();
                },
                child: Icon(MdiIcons.menu),
              ),
            ),
          ),
        ],
      ),
      drawer: Drawer(
          child: ListView(
        children: _getDrawerWidgets(context),
      )),
      endDrawerEnableOpenDragGesture: false,
    );
  }

  Align getZoomButtons(MapstateModel mapstateModel) {
    return Align(
      alignment: Alignment.topCenter,
      child: Padding(
        padding: const EdgeInsets.all(_mapIconsPadding / 2.0),
        child: Container(
          decoration: BoxDecoration(
            color: SmashColors.mainBackground.withAlpha(128),
            borderRadius: BorderRadius.all(Radius.circular(40)),
            // border: Border.all(color: SmashColors.mainDecorations, width: 5),
          ),
          child: Padding(
            padding: const EdgeInsets.all(_mapIconsPadding / 2.0),
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
                        if (zoom < minZoom) zoom = minZoom;
                        _mapController.move(_mapController.center, zoom);
                        var bounds = _mapController.bounds;
                        mapstateModel.currentMapBounds = bounds;
                        Provider.of<AttributesTableStateModel>(context,
                                listen: false)
                            .refresh();
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
                          mapstateModel.currentMapBounds =
                              mapstateModel.dataBounds;
                          Provider.of<AttributesTableStateModel>(context,
                                  listen: false)
                              .refresh();
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
                        if (zoom > maxZoom) zoom = maxZoom;
                        _mapController.move(_mapController.center, zoom);
                        var bounds = _mapController.bounds;
                        mapstateModel.currentMapBounds = bounds;
                        Provider.of<AttributesTableStateModel>(context,
                                listen: false)
                            .refresh();
                      },
                      child: Icon(MdiIcons.magnifyPlus),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
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
                  Navigator.of(context).pop();
                  Navigator.push(context,
                      MaterialPageRoute(builder: (context) => SurveyorsView()));
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
                  Navigator.of(context).pop();
                  Navigator.push(context,
                      MaterialPageRoute(builder: (context) => WebUsersView()));
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.database,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Project Data"),
                onTap: () {
                  Navigator.of(context).pop();
                  Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (context) => ProjectDataView()));
                },
              ),
            ),
            // Padding(
            //   padding: const EdgeInsets.all(8.0),
            //   child: ListTile(
            //     leading: new Icon(
            //       MdiIcons.notebook,
            //       color: SmashColors.mainDecorations,
            //       size: iconSize,
            //     ),
            //     title: Text("Form Builder"),
            //     onTap: () {
            //       // TODO
            //       Navigator.of(context).pop();
            //     },
            //   ),
            // ),
            // Padding(
            //   padding: const EdgeInsets.all(8.0),
            //   child: ListTile(
            //     leading: new Icon(
            //       MdiIcons.databaseExport,
            //       color: SmashColors.mainDecorations,
            //       size: iconSize,
            //     ),
            //     title: Text("Export"),
            //     onTap: () {
            //       // TODO
            //       Navigator.of(context).pop();
            //     },
            //   ),
            // ),
            // Padding(
            //   padding: const EdgeInsets.all(8.0),
            //   child: ListTile(
            //     leading: new Icon(
            //       MdiIcons.bug,
            //       color: SmashColors.mainDecorations,
            //       size: iconSize,
            //     ),
            //     title: Text("Log"),
            //     onTap: () {
            //       // TODO
            //       Navigator.of(context).pop();
            //     },
            //   ),
            // ),
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
                  Navigator.of(context).pop();
                  openAboutDialog(context);
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
                  SmashSession.logout(
                    mapCenter:
                        "${_mapController.center.longitude};${_mapController.center.latitude};${_mapController.zoom}",
                  );
                  Navigator.pushReplacement(
                      context, MaterialPageRoute(builder: (_) => MainPage()));
                  // .then((_) => refresh());
                },
              ),
            ),
          ],
        ),
      ),
    ];
  }
}
