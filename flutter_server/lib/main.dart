import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong/latlong.dart';

//import 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';
//import 'package:atlas/atlas.dart';
//import 'package:google_atlas/google_atlas.dart';
//import 'package:webview_flutter/webview_flutter.dart';

void main() {
//  AtlasProvider.instance = GoogleAtlas();
  runApp(GssApp());
}

class GssApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.green,
      ),
      home: MainPage(),
    );
  }
}

class MainPage extends StatefulWidget {
  MainPage({Key key}) : super(key: key);

  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  double _initLon;
  double _initLat;
  double _initZoom;
  double _currentZoom;

  MapController _mapController;

  @override
  void initState() {
    _initLon = 11.0;
    _initLat = 46.0;
    _initZoom = 13;
    if (_initZoom == 0) _initZoom = 1;
    _currentZoom = _initZoom;
    _mapController = MapController();

    super.initState();
//    WidgetsBinding.instance.addObserver(this);
  }

  @override
  Widget build(BuildContext context) {
    final MAXZOOM = 22.0;
    final MINZOOM = 1.0;

    var markers = <Marker>[
      Marker(
        width: 80.0,
        height: 80.0,
        point: LatLng(_initLat, _initLon),
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

    return Scaffold(
//      appBar: AppBar(
//        title: Text("GSS"),
//      ),
      body: FlutterMap(
        options: new MapOptions(
          center: new LatLng(_initLat, _initLon),
          zoom: _initZoom,
          minZoom: MINZOOM,
          maxZoom: MAXZOOM,
//          plugins: [
//            MarkerClusterPlugin(),
//          ],
        ),
        layers: [
          TileLayerOptions(
            tms: false,
            urlTemplate: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
            subdomains: ['a', 'b', 'c'],
            // For example purposes. It is recommended to use
            // TileProvider with a caching and retry strategy, like
            // NetworkTileProvider or CachedNetworkTileProvider
            tileProvider: NonCachingNetworkTileProvider(),
          ),
          MarkerLayerOptions(markers: markers)
        ],
        mapController: _mapController,
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {},
        tooltip: 'Blah',
        child: Icon(Icons.question_answer),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

// WITH ATLAS
//
//class MainPage extends StatefulWidget {
//  MainPage({Key key}) : super(key: key);
//
//  @override
//  _MainPageState createState() => _MainPageState();
//}
//
//class _MainPageState extends State<MainPage> {
//  double _initLon;
//  double _initLat;
//  double _initZoom;
//  double _currentZoom;
//
//  @override
//  void initState() {
//    _initLon = 11.0;
//    _initLat = 46.0;
//    _initZoom = 13;
//    if (_initZoom == 0) _initZoom = 1;
//    _currentZoom = _initZoom;
//
//    super.initState();
////    WidgetsBinding.instance.addObserver(this);
//  }
//
//  @override
//  Widget build(BuildContext context) {
//    final MAXZOOM = 22.0;
//    final MINZOOM = 1.0;
//
////    var layer = TileLayerOptions(
////      tms: false,
////      urlTemplate: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
////      backgroundColor: Colors.white,
////      maxZoom: 19.0,
////      subdomains: const ['a', 'b', 'c'],
////    );
//
//    final CameraPosition initialCameraPosition = CameraPosition(
//      target: LatLng(
//        latitude: _initLat,
//        longitude: _initLon,
//      ),
//      zoom: 12,
//    );
//    final Set<Marker> _markers = Set<Marker>.from([
//      Marker(
//        id: 'marker-1',
//        position: LatLng(
//          latitude: _initLat,
//          longitude: _initLon,
//        ),
//        onTap: () {
//          print('tapped marker-1');
//        },
//      )
//    ]);
//
//    return Scaffold(
//      appBar: AppBar(
//        title: Text("GSS"),
//      ),
//      body: Atlas(
//        initialCameraPosition: initialCameraPosition,
//        markers: _markers,
//
//      ),
//      floatingActionButton: FloatingActionButton(
//        onPressed: () {},
//        tooltip: 'Blah',
//        child: Icon(Icons.question_answer),
//      ), // This trailing comma makes auto-formatting nicer for build methods.
//    );
//  }
//}

// WITH FLUTTER MAP
//
//class _MainPageState extends State<MainPage> {
//  double _initLon;
//  double _initLat;
//  double _initZoom;
//  double _currentZoom;
//
//  MapController _mapController;
//
//  @override
//  void initState() {
//    _initLon = 11.0;
//    _initLat = 46.0;
//    _initZoom = 13;
//    if (_initZoom == 0) _initZoom = 1;
//    _currentZoom = _initZoom;
//    _mapController = MapController();
//
//    super.initState();
////    WidgetsBinding.instance.addObserver(this);
//  }
//
//  @override
//  Widget build(BuildContext context) {
//    final MAXZOOM = 22.0;
//    final MINZOOM = 1.0;
//
////    this.label: "Open Street Map",
////    this.url: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
////    this.attribution: "OpenStreetMap, ODbL",
////    this.minZoom: 0,
////    this.maxZoom: 19,
////    this.subdomains: const ['a', 'b', 'c'],
////    this.isVisible: true,
////    this.isTms: false,
//    var layer = TileLayerOptions(
//      tms: false,
//      urlTemplate: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
//      backgroundColor: Colors.white,
//      maxZoom: 19.0,
//      subdomains: const ['a', 'b', 'c'],
//    );
//
//    return Scaffold(
//      appBar: AppBar(
//        title: Text("GSS"),
//      ),
//      body: FlutterMap(
//        options: new MapOptions(
//          center: new LatLng(_initLat, _initLon),
//          zoom: _initZoom,
//          minZoom: MINZOOM,
//          maxZoom: MAXZOOM,
//          plugins: [
//            MarkerClusterPlugin(),
//          ],
//        ),
//        layers: [layer],
//        mapController: _mapController,
//      ),
//      floatingActionButton: FloatingActionButton(
//        onPressed: () {},
//        tooltip: 'Blah',
//        child: Icon(Icons.question_answer),
//      ), // This trailing comma makes auto-formatting nicer for build methods.
//    );
//  }
//}
