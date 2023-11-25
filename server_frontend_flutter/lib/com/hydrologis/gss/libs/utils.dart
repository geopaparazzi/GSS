import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:http/http.dart';
import 'package:latlong2/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

class NetworkImageWidget extends StatefulWidget {
  final double _height;
  final String _imageUrl;
  final String? _title;
  final bool hideRotate;
  NetworkImageWidget(this._imageUrl, this._title, this._height,
      {this.hideRotate = true, Key? key})
      : super(key: key);

  @override
  _NetworkImageWidgetState createState() =>
      _NetworkImageWidgetState(_imageUrl, _height);
}

class _NetworkImageWidgetState extends State<NetworkImageWidget> {
  final double _height;
  final String _imageUrl;

  bool _imageReady = false;
  List<int>? _bytes;
  String? error;
  int quarterTurns = 0;

  _NetworkImageWidgetState(this._imageUrl, this._height);

  @override
  void initState() {
    super.initState();

    downloadImage();
  }

  void downloadImage() async {
    var tokenHeader = WebServerApi.getTokenHeader();
    var project = SmashSession.getSessionProject();
    var uri = Uri.parse(_imageUrl + "?$API_PROJECT_PARAM${project.id}");

    var response = await get(uri, headers: tokenHeader);
    if (response.statusCode == 200) {
      Map<String, dynamic> imageMap = jsonDecode(response.body);
      var dataString = imageMap[IMAGEDATA][DATA];
      _bytes = Base64Decoder().convert(dataString);
      _imageReady = true;
    } else {
      error = "An error occurred while retrieving the image.";
    }
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    if (error != null) {
      return Center(child: SmashUI.errorWidget(error!));
    } else if (!_imageReady) {
      return Padding(
        padding: EdgeInsets.only(top: _height / 2),
        child: Center(child: SmashCircularProgress()),
      );
    } else {
      Image image = Image.memory(
        Uint8List.fromList(_bytes!),
        fit: BoxFit.contain,
      );

      var height = ScreenUtilities.getHeight(context);
      var width = ScreenUtilities.getWidth(context);
      var delta = 200;

      return Container(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            if (widget._title != null && widget._title!.length > 0)
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: SmashUI.titleText(widget._title!),
              ),
            Padding(
              padding: const EdgeInsets.all(18.0),
              child: RotatedBox(
                quarterTurns: quarterTurns,
                child: SizedBox(
                  height: height - delta,
                  width: width - delta,
                  child: image,
                ),
              ),
            ),
            if (!widget.hideRotate)
              Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: FloatingActionButton(
                  mini: true,
                  backgroundColor: SmashColors.mainDecorations,
                  child: Icon(MdiIcons.rotateRight),
                  onPressed: () {
                    setState(() {
                      quarterTurns++;
                      if (quarterTurns > 3) {
                        quarterTurns = 0;
                      }
                    });
                  },
                ),
              )
          ],
        ),
      );

      // return Stack(
      //   children: [
      //     Container(
      //       child: Expanded(
      //         flex: 1,
      //         child: Padding(
      //           padding: const EdgeInsets.all(8.0),
      //           child: RotatedBox(
      //             quarterTurns: 0,
      //             child: image,
      //           ),
      //         ),
      //       ),
      //     ),
      //     Align(
      //       alignment: Alignment.bottomRight,
      //       child: FloatingActionButton(
      //         child: Icon(MdiIcons.rotateRight),
      //         onPressed: () {
      //           setState(() {
      //             quarterTurns++;
      //             if (quarterTurns > 3) {
      //               quarterTurns = 0;
      //             }
      //           });
      //         },
      //       ),
      //     )
      //   ],
      // );
    }
  }
}

class OnlineSourceCard extends StatefulWidget {
  final name;
  final layer;
  OnlineSourceCard(this.name, this.layer, {Key? key}) : super(key: key);

  @override
  _OnlineSourceCardState createState() => _OnlineSourceCardState();
}

class _OnlineSourceCardState extends State<OnlineSourceCard> {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Card(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            ListTile(
              leading: Icon(MdiIcons.layersTripleOutline),
              title: Text(widget.name),
              subtitle: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Container(
                    padding: EdgeInsets.only(top: 15),
                    height: 100,
                    child: FlutterMap(
                      options: new MapOptions(
                        center: new LatLng(46.47781, 11.33140),
                        zoom: 13.0,
                      ),
                      children: [widget.layer],
                    ),
                  )
                ],
              ),
            ),
            ButtonBar(
              children: <Widget>[
                TextButton(
                  style: SmashUI.defaultFlatButtonStyle(),
                  child: const Text('SELECT'),
                  onPressed: () {
                    SmashSession.setBasemap(widget.name);
                    Navigator.pop(context);
                    var mapstateModel =
                        Provider.of<MapstateModel>(context, listen: false);
                    mapstateModel.reloadMap();
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// Helper class for latlong related issues.
class LatLongHelper {
  /// Create [LatLng] from lon and lat with check.
  static LatLng fromLatLon(double lat, double lon) {
    if (lon < -180) lon = -180;
    if (lon > 180) lon = 180;
    if (lat < -90) lat = -90;
    if (lat > 90) lat = 90;
    return LatLng(lat, lon);
  }
}
