import 'dart:convert';

import 'package:flushbar/flushbar.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_server/com/hydrologis/gss/layers.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:latlong/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:page_slider/page_slider.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

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
  double textHeight = renderParagraph.getMinIntrinsicHeight(36).ceilToDouble();

  textlen = textlen > size ? textlen : size;
  lengthHeight.add(textlen);
  lengthHeight.add(textHeight);
  return lengthHeight;
}

Marker buildImage(BuildContext context, double screenHeight, var x, var y,
    String name, var dataId, var data) {
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
                NetworkImageWidget("$API_IMAGE/$dataId", screenHeight / 2.0),
          )..show(context);
        },
        child: imageWidget,
      ),
    ),
  );
}

Marker buildFormNote(BuildContext context, var x, var y, String name,
    String form, var noteId, String marker, double size, String color) {
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

openMapSelectionDialog(BuildContext context) {
  var size = 400.0;
  Dialog errorDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: size,
      width: size,
      child: BackgroundMapSelectionWidget(),
    ),
  );
  showDialog(context: context, builder: (BuildContext context) => errorDialog);
}

class BackgroundMapSelectionWidget extends StatefulWidget {
  BackgroundMapSelectionWidget();
  _BackgroundMapSelectionWidgetState createState() =>
      _BackgroundMapSelectionWidgetState();
}

class _BackgroundMapSelectionWidgetState
    extends State<BackgroundMapSelectionWidget> {
  int _index = 0;
  List<TileLayerOptions> _widgets = [];
  List<String> _names = [];

  @override
  void initState() {
    AVAILABLE_ONLINE_MAPS.forEach((name, tilelayer) {
      _names.add(name);
      _widgets.add(tilelayer);
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.all(8.0),
          child: SmashUI.titleText(_names[_index]),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: FlutterMap(
              options: new MapOptions(
                center: new LatLng(46.47781, 11.33140),
                zoom: 4.0,
              ),
              layers: [_widgets[_index]],
            ),
          ),
        ),
        ButtonBar(
          alignment: MainAxisAlignment.spaceEvenly,
          children: [
            FlatButton(
              child: const Text('CANCEL'),
              onPressed: () {
                Navigator.pop(context);
              },
            ),
            FlatButton(
              child: const Text('NEXT'),
              onPressed: () {
                setState(() {
                  _index = _index + 1;
                  if (_index >= _widgets.length) {
                    _index = 0;
                  }
                });
              },
            ),
            FlatButton(
              child: const Text('OK'),
              onPressed: () {
                SmashSession.setBasemap(_names[_index]);
                var mapstateModel =
                    Provider.of<MapstateModel>(context, listen: false);
                mapstateModel.reloadMap();
                Navigator.pop(context);
              },
            ),
          ],
        )
      ],
    );
  }
}
