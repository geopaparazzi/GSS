import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong/latlong.dart';
import 'package:provider/provider.dart';
import 'com/hydrologis/gss/variables.dart';
import 'com/hydrologis/gss/layers.dart';
import 'com/hydrologis/gss/utils.dart';
import 'package:fab_circular_menu/fab_circular_menu.dart';
import 'package:community_material_icon/community_material_icon.dart';

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
  bool _layerFabOpened = false;

  @override
  Widget build(BuildContext context) {
    if (_mapController == null) {
      _mapController = MapController();
    }
    final MAXZOOM = 22.0;
    final MINZOOM = 1.0;

//    html.IdbFactory fac = html.window.indexedDB;

    var markers = <Marker>[
      Marker(
        width: 80.0,
        height: 80.0,
        point: LatLng(46, 11),
        builder: (ctx) => Container(
          child: FlutterLogo(
            colors: Colors.blue,
            key: ObjectKey(Colors.blue),
          ),
        ),
      ),
      Marker(
        width: 80.0,
        height: 80.0,
        point: LatLng(53.3498, -6.2603),
        builder: (ctx) => Container(
          child: FlutterLogo(
            colors: Colors.green,
            key: ObjectKey(Colors.green),
          ),
        ),
      ),
      Marker(
        width: 80.0,
        height: 80.0,
        point: LatLng(48.8566, 2.3522),
        builder: (ctx) => Container(
          child: FlutterLogo(
            colors: Colors.purple,
            key: ObjectKey(Colors.purple),
          ),
        ),
      ),
    ];

    return Stack(
      children: <Widget>[
        Consumer<MapstateModel>(
          builder: (context, model, _) => Scaffold(
//      appBar: AppBar(
//        title: Text("GSS"),
//      ),
            body: FlutterMap(
              options: new MapOptions(
                center: new LatLng(model.centerLat, model.centerLon),
                zoom: model.currentZoom,
                minZoom: MINZOOM,
                maxZoom: MAXZOOM,
//          plugins: [
//            MarkerClusterPlugin(),
//          ],
              ),
              layers: [
                model.backgroundLayer,
                MarkerLayerOptions(markers: markers),
              ],
              mapController: _mapController,
            ),
            floatingActionButton: SelectMapLayerButton(
              fabButtons: getButtons(model),
              colorStartAnimation: Colors.green,
              colorEndAnimation: Colors.red,
              key: Key("layerMenu"),
              animatedIconData: AnimatedIcons.menu_close,
            ),
          ),
        ),
        Scaffold(

          body: Align(
            alignment: Alignment.bottomLeft,
            child: FabCircularMenu(
              child: Container(
//                color: Colors.transparent,
//                child:  Center(
//                    child: Padding(
//                  padding: const EdgeInsets.only(bottom: 8.0),
//                  child: Text('FAB Circle Menu Example',
//                      textAlign: TextAlign.center,
//                      style: TextStyle(color: Colors.red, fontSize: 36.0)),
//                )),
                  ),
              ringColor: Colors.white30,
              fabCloseIcon: Icon(
                CommunityMaterialIcons.close,
//                color: Colors.blueGrey,
              ),
              fabOpenIcon: Icon(
                CommunityMaterialIcons.settings,
//                color: Colors.red,
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
          ),
        ),
      ],
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
}
