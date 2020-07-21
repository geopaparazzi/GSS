/*
 * Copyright (c) 2019-2020. Antonello Andrea (www.hydrologis.com). All rights reserved.
 * Use of this source code is governed by a GPL3 license that can be
 * found in the LICENSE file.
 */

import 'package:flutter/material.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:url_launcher/url_launcher.dart';

openAboutDialog(BuildContext context) async {
  var w = 500.0;
  var h = 500.0;
  Dialog mapSelectionDialog = Dialog(
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
    child: Container(
      height: h,
      width: w,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Padding(
            padding: const EdgeInsets.all(15.0),
            child: SmashUI.titleText("ABOUT GSS", useColor: true),
          ),
          Expanded(child: AboutPage()),
        ],
      ),
    ),
  );
  await showDialog(
      context: context, builder: (BuildContext context) => mapSelectionDialog);
}

class AboutPage extends StatefulWidget {
  @override
  AboutPageState createState() {
    return AboutPageState();
  }
}

class AboutPageState extends State<AboutPage> {
  String _version = "3.0.0";

  @override
  Widget build(BuildContext context) {
    String version = _version;

    return Container(
      padding: SmashUI.defaultPadding(),
      child: ListView(
        children: <Widget>[
          ListTile(
            title: SmashUI.normalText("Geopaparazzi Survey Server",
                bold: true, color: SmashColors.mainDecorations),
            subtitle: SmashUI.smallText(
                "Web application dedicated to the centralization and sync of data from digital field surveys.",
                overflow: null),
          ),
          ListTile(
            title: SmashUI.normalText("Application version",
                bold: true, color: SmashColors.mainDecorations),
            subtitle: SmashUI.smallText(version, overflow: null),
          ),
          ListTile(
            title: SmashUI.normalText("License",
                bold: true, color: SmashColors.mainDecorations),
            subtitle: SmashUI.smallText(
                "The GSS is a free and open source application and is available under the General Public License, version 3.",
                overflow: null),
          ),
          ListTile(
            title: SmashUI.normalText("Source Code",
                bold: true, color: SmashColors.mainDecorations),
            subtitle: SmashUI.smallText(
                "Tap here to visit the source code repository",
                overflow: null),
            onTap: () async {
              if (await canLaunch("https://github.com/moovida/gss")) {
                await launch("https://github.com/moovida/gss");
              }
            },
          ),
          ListTile(
            title: SmashUI.normalText("Legal Information",
                bold: true, color: SmashColors.mainDecorations),
            subtitle: SmashUI.smallText(
                "Copyright 2020, HydroloGIS S.r.l., some rights reserved. Tap to visit.",
                overflow: null),
            onTap: () async {
              if (await canLaunch("http://www.hydrologis.com")) {
                await launch("http://www.hydrologis.com");
              }
            },
          ),
          ListTile(
            title: SmashUI.normalText("SMASH",
                bold: true, color: SmashColors.mainDecorations),
            subtitle: SmashUI.smallText(
                "SMASH is the digital field mapping app that can interact with this server.",
                overflow: null),
            onTap: () async {
              if (await canLaunch(
                  "https://www.geopaparazzi.org/smash/index.html")) {
                await launch("https://www.geopaparazzi.org/smash/index.html");
              }
            },
          ),
        ],
      ),
    );
  }
}
