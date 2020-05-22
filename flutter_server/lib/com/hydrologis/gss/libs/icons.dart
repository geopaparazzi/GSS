import 'package:flutter/material.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

IconData getSmashIcon(String key) {
  var iconData = MdiIcons.fromString(key);
  if (iconData == null) {
    return MdiIcons.mapMarker;
  }
  return iconData;
}
