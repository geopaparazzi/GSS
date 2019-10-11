import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:latlong/latlong.dart';
import 'package:provider/provider.dart';
import 'com/hydrologis/gss/variables.dart';
import 'com/hydrologis/gss/layers.dart';
import 'com/hydrologis/gss/utils.dart';
//import 'package:animated_floatactionbuttons/animated_floatactionbuttons.dart';

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

//  @override
//  void initState() {
//    _mapController = MapController();
//
//    super.initState();
////    WidgetsBinding.instance.addObserver(this);
//  }

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

    return Consumer<MapstateModel>(
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
        floatingActionButton: AnimatedFloatingActionButton(
          fabButtons: getButtons(model),
          colorStartAnimation: Colors.green,
          colorEndAnimation: Colors.red,
          key: Key("layerMenu"),
          animatedIconData:  AnimatedIcons.view_list,
        ),

//        FloatingActionButton(
//          onPressed: () async {
//            var namesList = ["mapsforge"]..addAll(ONLINE_LAYERS_MAP.keys);
//            print("blah");
//            List<SimpleDialogOption> dialogs = namesList.map((name) {
//              return SimpleDialogOption(
//                child: Text(name),
//                onPressed: () {
//                  if (name == MAPSFORGE) {
//                    model.reset();
//                  } else {
//                    model.backgroundLayer = ONLINE_LAYERS_MAP[name];
//                  }
//                },
//              );
//            }).toList();
//
//            SimpleDialog dialog = SimpleDialog(
//              title: const Text('Choose an animal'),
//              children: dialogs,
//            );
//            // show the dialog
//            showDialog(
//              context: context,
//              builder: (BuildContext context) {
//                return dialog;
//              },
//            );
//          },
//          tooltip: 'Select layer',
//          child: Icon(FontAwesomeIcons.layerGroup),
//        ),
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
}
