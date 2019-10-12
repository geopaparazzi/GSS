import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

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
      crossAxisAlignment: CrossAxisAlignment.end,
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
