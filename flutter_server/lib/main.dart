import 'dart:convert';

import 'package:fab_circular_menu/fab_circular_menu.dart';
import 'package:flushbar/flushbar.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:flutter_server/com/hydrologis/gss/widgets.dart';
import 'package:latlong/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

export 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';

void main() {
  runApp(GssApp());
}

class GssApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MapstateModel()),
        ChangeNotifierProvider(create: (_) => FilterStateModel()),
      ],
      child: MaterialApp(
        title: TITLE,
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
            primarySwatch: SmashColors.mainDecorationsMc,
            accentColor: SmashColors.mainSelectionMc,
            canvasColor: SmashColors.mainBackground,
            brightness: Brightness.light,
            fontFamily: 'Arial',
            inputDecorationTheme: InputDecorationTheme(
              border: const OutlineInputBorder(
                borderSide: BorderSide(
                    color: Color.fromARGB(
                        255,
                        SmashColors.mainDecorationsDarkR,
                        SmashColors.mainDecorationsDarkG,
                        SmashColors.mainDecorationsDarkB)),
              ),
              enabledBorder: OutlineInputBorder(
                borderSide: BorderSide(
                    color: Color.fromARGB(
                        255,
                        SmashColors.mainDecorationsDarkR,
                        SmashColors.mainDecorationsDarkG,
                        SmashColors.mainDecorationsDarkB)),
              ),
              disabledBorder: OutlineInputBorder(
                borderSide:
                    BorderSide(color: Color.fromARGB(255, 128, 128, 128)),
              ),
              focusedBorder: const OutlineInputBorder(
                borderSide: BorderSide(
                    color: Color.fromARGB(
                        255,
                        SmashColors.mainSelectionBorderR,
                        SmashColors.mainSelectionBorderG,
                        SmashColors.mainSelectionBorderB)),
              ),

//            labelStyle: const TextStyle(
//              color: Color.fromARGB(255, 128, 128, 128),
//            ),
            )),
        home: MainPage(), //LoginScreen(), //
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
  int _heroCount;
  bool _isLogged = false;

  bool _doFirstDataLoading = true;
  LatLngBounds _dataBounds = LatLngBounds();

  static const _mapIconsPadding = 16.0;
  static const _mapIconsSizeNormal = 48.0;
  static const _mapIconsSizeMini = 38.0;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    _isLogged = SmashSession.isLogged();

    if (_isLogged) {
      if (_mapController == null) {
        _mapController = MapController();
      }
      final MAXZOOM = 22.0;
      final MINZOOM = 1.0;
      _heroCount = 0;

      if (_doFirstDataLoading) {
        _doFirstDataLoading = false;
        getData(null);
      }

      var layers = <LayerOptions>[];

      if (_logs != null) {
        layers.add(_logs);
      }
      if (_markers != null && _markers.length > 0) {
        var markerCluster = MarkerClusterLayerOptions(
          maxClusterRadius: 20,
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
              backgroundColor: SmashColors.mainDecorationsDarker,
              foregroundColor: SmashColors.mainBackground,
              heroTag: "${_heroCount++}",
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

      var xyz = SmashSession.getMapcenter();
      var basemap = SmashSession.getBasemap();

      FilterStateModel filterStateModel =
          Provider.of<FilterStateModel>(context);

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
                      layers: [AVAILABLE_LAYERS_MAP[basemap]]..addAll(layers),
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
              alignment: Alignment.bottomLeft,
              child: Padding(
                padding: const EdgeInsets.all(_mapIconsPadding),
                child: SelectMapLayerButton(
                  fabButtons: getButtons(),
                  colorStartAnimation: Colors.green,
                  colorEndAnimation: Colors.red,
                  key: Key("layerMenu"),
                  animatedIconData: AnimatedIcons.menu_close,
                ),
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
                            if (_dataBounds.isValid) {
                              _mapController.fitBounds(_dataBounds);
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
      );
    } else {
      TextStyle loginTextStyle = TextStyle(fontFamily: 'Arial', fontSize: 20.0);

      TextEditingController userNameController = new TextEditingController();
      final userNameField = TextField(
        controller: userNameController,
        obscureText: false,
        style: loginTextStyle,
        decoration: InputDecoration(
            contentPadding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
            hintText: "Username",
            border:
                OutlineInputBorder(borderRadius: BorderRadius.circular(32.0))),
      );

      TextEditingController passwordController = new TextEditingController();
      final passwordField = TextField(
        controller: passwordController,
        obscureText: true,
        style: loginTextStyle,
        decoration: InputDecoration(
            contentPadding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
            hintText: "Password",
            border:
                OutlineInputBorder(borderRadius: BorderRadius.circular(32.0))),
      );

      final loginButon = Material(
        elevation: 5.0,
        borderRadius: BorderRadius.circular(30.0),
        color: SmashColors.mainDecorationsDarker,
        child: MaterialButton(
          minWidth: MediaQuery.of(context).size.width,
          padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
          onPressed: () async {
            // TODO change when not testing
            String user = "god"; //userNameController.text;
            String password = "god"; //passwordController.text;
            await SmashSession.login(user, password);
            setState(() {});
          },
          child: Text("Login",
              textAlign: TextAlign.center,
              style: loginTextStyle.copyWith(
                  color: Colors.white, fontWeight: FontWeight.bold)),
        ),
      );

      return Scaffold(
          body: SingleChildScrollView(
        child: Center(
          child: Container(
            color: Colors.white,
            constraints: BoxConstraints(maxWidth: 400.0),
            child: Padding(
              padding: const EdgeInsets.all(36.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  SizedBox(
                    height: 200.0,
                    child: Image.asset(
                      "assets/smash_logo.png",
                      fit: BoxFit.contain,
                    ),
                  ),
                  SizedBox(height: 45.0),
                  userNameField,
                  SizedBox(height: 25.0),
                  passwordField,
                  SizedBox(
                    height: 35.0,
                  ),
                  loginButon,
                  SizedBox(
                    height: 15.0,
                  ),
                ],
              ),
            ),
          ),
        ),
      ));
    }
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
                  child: FilterSurveyor(filterStateModel, getData),
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
                  child: FilterProject(filterStateModel, getData),
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
          onPressed: () {
            filterStateModel.reset();
            getData(filterStateModel);
          },
          iconSize: 48.0,
          color: SmashColors.mainDecorationsDarker,
        ),
      ],
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
                  MapstateModel model = Provider.of<MapstateModel>(context);
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

  List<Widget> getButtons() {
    return AVAILABLE_LAYERS_MAP.keys.map((name) {
      return Padding(
        padding: const EdgeInsets.all(8.0),
        child: Container(
          width: 200.0,
          child: FloatingActionButton.extended(
            label: Text(name),
            onPressed: () {
              SmashSession.setBasemap(name);
              setState(() {});
            },
            key: Key(name),
            heroTag: name,
//          tooltip: name,
//          icon: Icon(Icons.map),
          ),
        ),
      );
    }).toList();
  }

  void getData(FilterStateModel filterStateModel) async {
    print("Data reload called");
    _dataBounds = LatLngBounds();

    if (filterStateModel == null) {
      filterStateModel = Provider.of<FilterStateModel>(context);
    }

    var userPwd = SmashSession.getSessionUser();

    var data = await ServerApi.getData(
      userPwd[0],
      userPwd[1],
      surveyors: filterStateModel.surveyors,
      projects: filterStateModel.projects,
      matchString: filterStateModel.matchingText,
      fromTo: filterStateModel.fromToTimestamp,
    );
    Map<String, dynamic> json = jsonDecode(data);

    // TODO find a fix for logs
    List<dynamic> logsList = json[LOGS];
    if (logsList != null) {
      List<Polyline> lines = [];
      for (int i = 0; i < logsList.length; i++) {
        dynamic logItem = logsList[i];
//        var id = logItem[ID];
        // var name = logItem[NAME];
        var colorHex = logItem[COLOR];
        var width = logItem[WIDTH];
        var coords = logItem[COORDS];

        List<LatLng> points = [];
        for (int j = 0; j < coords.length; j++) {
          var coord = coords[j];
          var latLng = LatLng(coord[Y], coord[X]);
          _dataBounds.extend(latLng);
          points.add(latLng);
        }

        lines.add(Polyline(
            points: points, strokeWidth: width, color: ColorExt(colorHex)));
      }
      _logs = PolylineLayerOptions(
        polylines: lines,
        polylineCulling: true,
      );
    }

    List<Marker> markers = <Marker>[];

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
      var latLng = LatLng(y, x);
//      print("$latLng : $name");
      _dataBounds.extend(latLng);
      var imgData = Base64Decoder().convert(data);
      markers.add(buildImage(x, y, name, dataId, imgData));
    }

    List<dynamic> simpleNotesList = json[NOTES];
    for (int i = 0; i < simpleNotesList.length; i++) {
      dynamic noteItem = simpleNotesList[i];
//      print(noteItem);
      //      var id = noteItem[ID];
      var name = noteItem[NAME];
      //      var ts = noteItem[TS];
      var x = noteItem[X];
      var y = noteItem[Y];
      var latLng = LatLng(y, x);
//      print("$latLng : $name");
      _dataBounds.extend(latLng);

      var marker = noteItem[MARKER];
      var size = noteItem[SIZE];
      var color = noteItem[COLOR];
      markers.add(buildSimpleNote(x, y, name, marker, size, color));
    }

    List<dynamic> formNotesList = json[FORMS];
    for (int i = 0; i < formNotesList.length; i++) {
      dynamic formItem = formNotesList[i];
//      print(formItem);
      var noteId = formItem[ID];
      var name = formItem[NAME];
      var form = formItem[FORM];
      var userId = formItem[USER];
      //      var ts = noteItem[TS];
      var x = formItem[X];
      var y = formItem[Y];
      var latLng = LatLng(y, x);
//      print("$latLng : $name");
//      print(latLng);
      _dataBounds.extend(latLng);

      var marker = formItem[MARKER];
      var size = formItem[SIZE];
      var color = formItem[COLOR];
      markers.add(buildFormNote(x, y, name, form, noteId, marker, size, color));
    }

    _markers = markers;

    var delta = 0.01;
    _dataBounds = LatLngBounds(
        LatLng(_dataBounds.south - delta, _dataBounds.west - delta),
        LatLng(_dataBounds.north + delta, _dataBounds.east + delta));
    _mapController.fitBounds(_dataBounds);

    setState(() {});
  }

  Marker buildSimpleNote(
      var x, var y, String name, String marker, double size, String color) {
    List lengthHeight = guessTextDimensions(name, size);

    var iconData = getSmashIcon(marker);
    var colorExt = ColorExt(color);

    return Marker(
      width: lengthHeight[0],
      height: size + lengthHeight[1],
      point: new LatLng(y, x),
      builder: (ctx) => new Container(
        child: Column(
          children: <Widget>[
            Icon(
              iconData,
              size: size,
              color: colorExt,
            ),
            FittedBox(
              child: Container(
                decoration: new BoxDecoration(
                    color: colorExt,
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
    double textHeight =
        renderParagraph.getMinIntrinsicHeight(36).ceilToDouble();

    textlen = textlen > size ? textlen : size;
    lengthHeight.add(textlen);
    lengthHeight.add(textHeight);
    return lengthHeight;
  }

  Marker buildImage(var x, var y, String name, var dataId, var data) {
    var imageWidget = Image.memory(
      data,
      scale: 6.0,
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
              titleText: SmashUI.titleText(
                name,
                textAlign: TextAlign.center,
              ),
              messageText:
                  NetworkImageWidget("$API_IMAGE/$dataId", _screenHeight / 2.0),
            )..show(context);
          },
          child: imageWidget,
        ),
      ),
    );
  }

  Marker buildFormNote(var x, var y, String name, String form, var noteId,
      String marker, double size, String color) {
    LatLng p = LatLng(y, x);

    List lengthHeight = guessTextDimensions(name, size);

    var iconData = getSmashIcon(marker);
    var colorExt = ColorExt(color);
    return Marker(
      width: lengthHeight[0],
      height: size + lengthHeight[1],
      point: new LatLng(y, x),
      builder: (ctx) => new Container(
        child: GestureDetector(
          child: Column(
            children: <Widget>[
              Icon(
                iconData,
                size: size,
                color: colorExt,
              ),
              Container(
                decoration: new BoxDecoration(
                    color: colorExt,
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
            var sectionMap = jsonDecode(form);
            var sectionName = sectionMap[ATTR_SECTIONNAME];

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
                  child: MasterDetailPage(
                    sectionMap,
                    SmashUI.titleText(sectionName,
                        color: SmashColors.mainBackground, bold: true),
                    sectionName,
                    p,
                    noteId,
                    null, // TODO add here save function if editing is supported on web
                    null, // TODO add get thumbnails function
                    null, // no taking pictures permitted on web
                  ),
                ),
              ),
            )..show(context);
          },
        ),
      ),
    );
  }
}

class LoginScreen extends StatefulWidget {
  LoginScreen({Key key}) : super(key: key);

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  @override
  Widget build(BuildContext context) {}
}
