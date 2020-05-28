import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_map/plugin_api.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:intl/intl.dart';
import 'package:latlong/latlong.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:transparent_image/transparent_image.dart';

class SelectMapLayerButton extends StatefulWidget {
  final List<Widget> fabButtons;
  final Color colorStartAnimation;
  final Color colorEndAnimation;
  final AnimatedIconData animatedIconData;

  SelectMapLayerButton(
      {Key key,
      this.fabButtons,
      this.colorStartAnimation,
      this.colorEndAnimation,
      this.animatedIconData})
      : super(key: key);

  @override
  _SelectMapLayerButtonState createState() => _SelectMapLayerButtonState();
}

class _SelectMapLayerButtonState extends State<SelectMapLayerButton>
    with SingleTickerProviderStateMixin {
  bool isOpened = false;
  AnimationController _animationController;
  Animation<Color> _buttonColor;
  Animation<double> _animateIcon;
  Animation<double> _translateButton;
  Curve _curve = Curves.easeOut;
  double _fabHeight = 56.0;

  @override
  initState() {
    _animationController =
        AnimationController(vsync: this, duration: Duration(milliseconds: 500))
          ..addListener(() {
            setState(() {});
          });
    _animateIcon =
        Tween<double>(begin: 0.0, end: 1.0).animate(_animationController);
    _buttonColor = ColorTween(
      begin: widget.colorStartAnimation,
      end: widget.colorEndAnimation,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Interval(
        0.00,
        1.00,
        curve: Curves.linear,
      ),
    ));
    _translateButton = Tween<double>(
      begin: _fabHeight,
      end: -14.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Interval(
        0.0,
        0.75,
        curve: _curve,
      ),
    ));
    super.initState();
  }

  @override
  dispose() {
    _animationController.dispose();
    super.dispose();
  }

  animate() {
    if (!isOpened) {
      _animationController.forward();
    } else {
      _animationController.reverse();
    }
    isOpened = !isOpened;
  }

  Widget toggle() {
    return Container(
      child: FloatingActionButton(
        backgroundColor: _buttonColor.value,
        onPressed: animate,
        tooltip: 'Toggle',
        child: AnimatedIcon(
          icon: widget.animatedIconData,
          progress: _animateIcon,
        ),
      ),
    );
  }

  List<Widget> _setFabButtons() {
    List<Widget> processButtons = List<Widget>();
    for (int i = 0; i < widget.fabButtons.length; i++) {
      processButtons.add(TransformMapLayerButton(
        floatButton: widget.fabButtons[i],
        translateValue: _translateButton.value * (widget.fabButtons.length - i),
        isVisbile: isOpened,
      ));
    }
    processButtons.add(toggle());
    return processButtons;
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.end,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: _setFabButtons(),
    );
  }
}

class TransformMapLayerButton extends StatelessWidget {
  final Widget floatButton;
  final double translateValue;
  final bool isVisbile;

  TransformMapLayerButton(
      {this.floatButton, this.translateValue, this.isVisbile})
      : super(key: ObjectKey(floatButton));

  @override
  Widget build(BuildContext context) {
    return Transform(
      transform: Matrix4.translationValues(
        0.0,
        translateValue,
        0.0,
      ),
      child: AnimatedOpacity(
        // If the widget is visible, animate to 0.0 (invisible).
        // If the widget is hidden, animate to 1.0 (fully visible).
        opacity: isVisbile ? 1.0 : 0.0,
        duration: Duration(milliseconds: 500),
        // The green box must be a child of the AnimatedOpacity widget.
        child: floatButton,
      ),
    );
  }
}

class NetworkImageWidget extends StatelessWidget {
  double _height;
  String _imageUrl;

  NetworkImageWidget(this._imageUrl, this._height);

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Center(
        child: Stack(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Center(child: CircularProgressIndicator()),
            ),
            Center(
              child: Container(
                height: _height,
                decoration: BoxDecoration(
                    border: Border.all(color: SmashColors.mainDecorations)),
                padding: EdgeInsets.all(5),
                child: FadeInImage.memoryNetwork(
                    placeholder: kTransparentImage,
                    image: _imageUrl,
                    fit: BoxFit.contain),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class OnlineSourceCard extends StatefulWidget {
  final name;
  final layer;
  OnlineSourceCard(this.name, this.layer, {Key key}) : super(key: key);

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
                      layers: [widget.layer],
                    ),
                  )
                ],
              ),
            ),
            ButtonBar(
              children: <Widget>[
                FlatButton(
                  child: const Text('SELECT'),
                  onPressed: () {
                    SmashSession.setBasemap(widget.name);
                    Navigator.pop(context);
                    var mapstateModel = Provider.of<MapstateModel>(context, listen: false);
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
