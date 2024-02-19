import 'dart:async';

import 'package:after_layout/after_layout.dart';
import 'package:dart_hydrologis_utils/dart_hydrologis_utils.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/formutils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/views/about_view.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/main.dart';
import 'package:latlong2/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'dart:html' as html;
import 'package:dart_jts/dart_jts.dart' as JTS;
// import 'package:flutter_map_tappable_polyline/flutter_map_tappable_polyline.dart';

class MainMapView extends StatefulWidget {
  MainMapView({Key? key}) : super(key: key);

  @override
  _MainMapViewState createState() => _MainMapViewState();
}

class _MainMapViewState extends State<MainMapView>
    with AfterLayoutMixin<MainMapView> {
  GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  MapController? _mapController;
  AttributesTableWidget? attributesTableWidget;
  final maxZoom = 25.0;
  final minZoom = 1.0;
  bool _showLastUserPositions = false;
  var userPositionsLayer;
  var formBuilderHelper = FormBuilderFormHelper();

  int _heroCount = 0;

  static const _mapIconsPadding = 16.0;
  static const _mapIconsSizeNormal = 48.0;
  static const _mapIconsSizeMini = 38.0;

  @override
  void afterFirstLayout(BuildContext context) {
    var mapstateModel = Provider.of<MapstateModel>(context, listen: false);
    mapstateModel.getData(context).then((value) {
      mapstateModel.reloadMap();
      // mapstateModel.fitbounds();

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

    var layers = <Widget>[];

    if (mapstateModel.logs != null) {
      layers.add(mapstateModel.logs!);
    }
    if (mapstateModel.mapMarkers.isNotEmpty) {
      var markerCluster = MarkerClusterLayerOptions(
        maxClusterRadius: 20,
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
      layers.add(MarkerClusterLayerWidget(options: markerCluster));
    }
    if (userPositionsLayer != null) {
      layers.add(userPositionsLayer);
    }

    var xyz = SmashSession.getMapcenter();
    if (xyz == null && mapstateModel.dataBounds != null) {
      xyz = [
        mapstateModel.dataBounds!.center.longitude,
        mapstateModel.dataBounds!.center.latitude,
        15
      ];
    }
    if (xyz == null) {
      xyz = [0, 0, 15];
    }
    var lastUsedBasemapNamesStr = SmashSession.getBasemap();
    var lastUsedBasemapNames = lastUsedBasemapNamesStr.split(",");
    var backgroundLayers = mapstateModel.getBackgroundLayers();
    var backgroundLayersToLoad =
        <Widget>[]; // backgroundLayers[lastUsedBasemapNames];
    for (var basemapName in lastUsedBasemapNames) {
      var layer = backgroundLayers[basemapName];
      if (layer != null) {
        backgroundLayersToLoad.add(layer);
      }
    }
    if (backgroundLayersToLoad.isEmpty) {
      if (backgroundLayers.isNotEmpty) {
        // fallback on first
        var fallBackLayerName = backgroundLayers.keys.first;
        backgroundLayersToLoad.add(backgroundLayers[fallBackLayerName]!);
        SmashSession.setBasemap(fallBackLayerName);
      } else {
        // no layer, fallback on default
        backgroundLayersToLoad.add(WebServerApi.getDefaultLayer());
        SmashSession.setBasemap(DEFAULTLAYERNAME);
      }
    }
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
                      if (_mapController != null) {
                        _mapController!.move(_mapController!.center,
                            _mapController!.zoom + (delta > 0 ? -0.2 : 0.2));
                        var bounds = _mapController!.bounds;
                        mapstateModel.currentMapBounds = bounds;
                        Provider.of<AttributesTableStateModel>(context,
                                listen: false)
                            .refresh();
                      }
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
                      // print("onMoveEnd");
                      if (_mapController != null) {
                        var bounds = _mapController!.bounds;
                        mapstateModel.currentMapBounds = bounds;
                        Provider.of<AttributesTableStateModel>(context,
                                listen: false)
                            .refresh();
                      }
                    }
                  },
                  behavior: HitTestBehavior.deferToChild,
                  child: FlutterMap(
                    options: new MapOptions(
                      crs:
                          // backgroundLayersToLoad[0].wmsOptions != null
                          //     ? backgroundLayersToLoad[0].wmsOptions.crs
                          //     :
                          Epsg3857(),
                      center: new LatLng(xyz[1], xyz[0]),
                      zoom: xyz[2],
                      minZoom: minZoom,
                      maxZoom: maxZoom,
                    ),
                    children: []
                      ..addAll(backgroundLayersToLoad)
                      ..addAll(layers),
                    mapController: _mapController,
                  ),
                ),
              ),
              mapstateModel.showAttributes && attributesTableWidget != null
                  ? Expanded(
                      flex: 1,
                      child: attributesTableWidget!,
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
                      backgroundColor: SmashColors.mainBackground,
                      foregroundColor: SmashColors.mainDecorations,
                      onPressed: () async {
                        MapstateModel mapState =
                            Provider.of<MapstateModel>(context, listen: false);
                        var backgroundLayers = mapState.getBackgroundLayers();
                        var names = backgroundLayers.keys.toList();

                        var previousSelectedMapNames =
                            SmashSession.getBasemap().split(",");
                        var selectedMapNames =
                            await SmashDialogs.showMultiSelectionComboDialog(
                                context, "Select background maps", names,
                                selectedItems: previousSelectedMapNames);
                        if (selectedMapNames != null &&
                            selectedMapNames.isNotEmpty) {
                          SmashSession.setBasemap(selectedMapNames.join(","));
                          mapState.reloadMap();
                        }
                      }),
                  // Padding(
                  //   padding: const EdgeInsets.only(top: 8.0),
                  //   child: FloatingActionButton(
                  //       tooltip: "Open data filter dialog.",
                  //       heroTag: "filter_dialog_button",
                  //       child: Icon(
                  //         MdiIcons.filterMenuOutline,
                  //       ),
                  //       backgroundColor: SmashColors.mainDecorations,
                  //       onPressed: () {
                  //         openFilterDialog(context);
                  //       }),
                  // ),
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
                        backgroundColor: SmashColors.mainBackground,
                        foregroundColor: SmashColors.mainDecorations,
                        onPressed: () {
                          setState(() {
                            mapstateModel.showAttributes =
                                !mapstateModel.showAttributes;
                            int delta = -1;
                            if (mapstateModel.showAttributes) {
                              delta = 1;
                            }
                            if (_mapController != null)
                              _mapController!.move(_mapController!.center,
                                  _mapController!.zoom + delta);
                          });
                          // mapstateModel.reloadMap();
                        }),
                  ),
                  Padding(
                    padding: const EdgeInsets.only(top: 8.0),
                    child: FloatingActionButton(
                      tooltip: "Refresh data",
                      heroTag: "refresh_data_button",
                      child: Icon(MdiIcons.refresh),
                      backgroundColor: SmashColors.mainBackground,
                      foregroundColor: SmashColors.mainDecorations,
                      onPressed: () async {
                        var mapstateModel =
                            Provider.of<MapstateModel>(context, listen: false);
                        await mapstateModel.getData(context);
                        mapstateModel.reloadMap();
                      },
                    ),
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
                backgroundColor: SmashColors.mainBackground,
                foregroundColor: SmashColors.mainDecorations,
                onPressed: () {
                  _scaffoldKey.currentState?.openDrawer();
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
            borderRadius: BorderRadius.all(Radius.circular(10)),
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
                      backgroundColor: SmashColors.mainBackground,
                      foregroundColor: SmashColors.mainDecorations,
                      mini: true,
                      onPressed: () {
                        if (_mapController != null) {
                          var zoom = _mapController!.zoom - 1;
                          if (zoom < minZoom) zoom = minZoom;
                          _mapController!.move(_mapController!.center, zoom);
                          var bounds = _mapController!.bounds;
                          mapstateModel.currentMapBounds = bounds;
                          Provider.of<AttributesTableStateModel>(context,
                                  listen: false)
                              .refresh();
                        }
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
                      backgroundColor: SmashColors.mainBackground,
                      foregroundColor: SmashColors.mainDecorations,
                      heroTag: "zoomdata",
                      mini: true,
                      tooltip: "Zoom to data",
                      onPressed: () {
                        if (mapstateModel.dataBounds != null &&
                            _mapController != null) {
                          _mapController!.fitBounds(mapstateModel.dataBounds!);
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
                      backgroundColor: SmashColors.mainBackground,
                      foregroundColor: SmashColors.mainDecorations,
                      heroTag: "zoombookmarks",
                      mini: true,
                      tooltip: "Bookmarks",
                      onPressed: () async {
                        await openBookmarksDialog(context);
                      },
                      child: Icon(MdiIcons.bookmark),
                    ),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: SizedBox(
                    width: _mapIconsSizeMini,
                    height: _mapIconsSizeMini,
                    child: FloatingActionButton(
                      backgroundColor: SmashColors.mainBackground,
                      foregroundColor: SmashColors.mainDecorations,
                      heroTag: "zoomout",
                      mini: true,
                      onPressed: () {
                        if (_mapController != null) {
                          var zoom = _mapController!.zoom + 1;
                          if (zoom > maxZoom) zoom = maxZoom;
                          _mapController!.move(_mapController!.center, zoom);
                          var bounds = _mapController!.bounds;
                          mapstateModel.currentMapBounds = bounds;
                          Provider.of<AttributesTableStateModel>(context,
                                  listen: false)
                              .refresh();
                        }
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
          child: Align(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Image.asset("assets/smash_logo.png"),
            ),
            alignment: Alignment.center,
          ),
        ),
        color: SmashColors.mainDecorations.withAlpha(70),
      ),
      new Container(
        child: new Column(
          children: [
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
            ListTile(
              leading: new Icon(
                MdiIcons.account,
                color: SmashColors.mainDecorations,
                size: iconSize,
              ),
              title: Text("User"),
              subtitle: SmashUI.normalText(SmashSession.getSessionUser()[0],
                  bold: true),
            ),
            ListTile(
              leading: new Icon(
                MdiIcons.folderOutline,
                color: SmashColors.mainDecorations,
                size: iconSize,
              ),
              title: Text("Project"),
              subtitle: SmashUI.normalText(
                  SmashSession.getSessionProject().name ?? "Unknown Project",
                  bold: true),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Divider(
                color: SmashColors.mainDecorations,
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.tools,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Configuration"),
                onTap: () {
                  html.window.open("$WEBAPP_URL$API_CONFIGRATION_URL", 'admin');
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListTile(
                leading: new Icon(
                  MdiIcons.formSelect,
                  color: SmashColors.mainDecorations,
                  size: iconSize,
                ),
                title: Text("Form Builder"),
                onTap: () {
                  Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => MainFormWidget(
                          formBuilderHelper,
                          presentationMode:
                              PresentationMode(isFormbuilder: true),
                          doScaffold: true,
                        ),
                      ));
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Divider(
                color: SmashColors.mainDecorations,
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: CheckboxListTile(
                value: _showLastUserPositions,
                title: Text("Show Active User Positions" +
                    (userPositionsLayer != null
                        ? " (${userPositionsLayer.markers.length})"
                        : "")),
                onChanged: (selection) async {
                  if (selection == null) return;
                  if (selection) {
                    Timer.periodic(const Duration(seconds: 60),
                        (Timer timer) async {
                      if (!_showLastUserPositions) {
                        // print("Cancel timer");
                        timer.cancel();
                      } else {
                        // print("Get user positions.");
                        var lastUserPositions =
                            await WebServerApi.getLastUserPositions();
                        if (lastUserPositions != null &&
                            lastUserPositions.isNotEmpty) {
                          userPositionsLayer = await buildLastUserPositionLayer(
                              lastUserPositions,
                              TimeUtilities.ISO8601_TS_FORMATTER
                                  .format(DateTime.now()));
                          setState(() {});
                        }
                      }
                    });
                    var lastUserPositions =
                        await WebServerApi.getLastUserPositions();
                    if (lastUserPositions != null &&
                        lastUserPositions.isNotEmpty) {
                      userPositionsLayer = await buildLastUserPositionLayer(
                          lastUserPositions,
                          TimeUtilities.ISO8601_TS_FORMATTER
                              .format(DateTime.now()));
                    }
                  } else {
                    userPositionsLayer = null;
                  }
                  setState(() {
                    _showLastUserPositions = !_showLastUserPositions;
                  });
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Divider(
                color: SmashColors.mainDecorations,
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
                  Navigator.of(context).pop();
                  openAboutDialog(context);
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Divider(
                color: SmashColors.mainDecorations,
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
                  _showLastUserPositions = false;
                  SmashSession.logout(
                    mapCenter: _mapController != null
                        ? "${_mapController!.center.longitude};${_mapController!.center.latitude};${_mapController!.zoom}"
                        : null,
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
