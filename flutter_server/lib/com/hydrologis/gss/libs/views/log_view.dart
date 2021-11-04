import 'dart:convert';
import 'dart:html';

import 'package:after_layout/after_layout.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:horizontal_data_table/horizontal_data_table.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:smashlibs/smashlibs.dart';

class LogView extends StatefulWidget {
  LogView({Key key}) : super(key: key);

  @override
  _LogViewState createState() => _LogViewState();
}

class _LogViewState extends State<LogView> with AfterLayoutMixin {
  List<LogItem> logList;
  String type = "ALL";
  var typeList = [
    "ALL",
    "INFO",
    "WARNING",
    "ERROR",
    "ACCESS",
    "DEBUG",
  ];

  @override
  void afterFirstLayout(BuildContext context) {
    loadData();
  }

  Future<void> loadData() async {
    var up = SmashSession.getSessionUser();
    String dataJson = await ServerApi.getLog(up[0], up[1], type: type);

    var logJsonMap = jsonDecode(dataJson ?? "");
    List<dynamic> logListTmp = logJsonMap[LOG];
    logList = [];
    logListTmp.forEach((map) {
      logList.add(
        LogItem()
          ..ts = map[LOGTS]
          ..type = map[LOGTYPE]
          ..msg = map[LOGMSG],
      );
    });
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    var isAdmin = SmashSession.isAdmin();

    return Scaffold(
      appBar: AppBar(
        actions: [
          DropdownButton(
            dropdownColor: SmashColors.mainDecorations,
            items: typeList
                .map((e) => DropdownMenuItem(
                      child: SmashUI.normalText(e,
                          color: SmashColors.mainBackground),
                      value: e,
                    ))
                .toList(),
            value: type,
            onChanged: (newSel) {
              type = newSel;
              loadData();
            },
          )
        ],
        title: Center(
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.only(right: 8.0),
                child: new Icon(
                  MdiIcons.database,
                  color: SmashColors.mainBackground,
                ),
              ),
              SmashUI.titleText("Debug Log", color: SmashColors.mainBackground),
            ],
          ),
        ),
      ),
      body: logList == null
          ? SmashCircularProgress(label: "Loading data...")
          : Padding(
              padding: SmashUI.defaultPadding(),
              child: LogTableWidget(logList),
            ),
    );
  }
}

class LogItem {
  String ts;
  String type;
  String msg;
}

class LogTableWidget extends StatefulWidget {
  final List<LogItem> logList;
  LogTableWidget(this.logList, {Key key}) : super(key: key);

  @override
  _LogTableWidgetState createState() => _LogTableWidgetState();
}

class _LogTableWidgetState extends State<LogTableWidget> {
  List<List<Widget>> _dataRows;
  double width;
  double rowHeight = 52.0;
  var headerTexts = [
    "Timestamp",
    "Type",
    "Message",
  ];
  var colFactors = [
    0.15,
    0.1,
    0.8,
  ];

  var iconsMap = {
    "INFO": Icon(
      MdiIcons.informationOutline,
      color: SmashColors.mainDecorations,
    ),
    "WARNING": Icon(
      MdiIcons.alert,
      color: Colors.orangeAccent,
    ),
    "ERROR": Icon(
      MdiIcons.alert,
      color: SmashColors.mainDanger,
    ),
    "ACCESS": Icon(
      MdiIcons.keyArrowRight,
      color: Colors.greenAccent,
    ),
    "DEBUG": Icon(
      MdiIcons.bug,
      color: SmashColors.mainTextColorNeutral,
    ),
  };

  @override
  Widget build(BuildContext context) {
    width = ScreenUtilities.getWidth(context);
    if (width < 1500) {
      width = 1500;
    }

    _dataRows = widget.logList.map((log) {
      var icon = iconsMap[log.type];

      return <Widget>[
        SmashUI.normalText(log.ts),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            icon,
            Center(child: SmashUI.normalText(log.type)),
          ],
        ),
        SelectableText(log.msg),
      ];
    }).toList();

    var textFun = (String str) {
      return Center(child: SmashUI.normalText(str, bold: true));
    };
    final List headerIndexes =
        Iterable<int>.generate(headerTexts.length).toList();
    var headerWidgets = headerIndexes
        .map((index) => Container(
              child: textFun(
                headerTexts[index],
              ),
              width: colFactors[index] * width,
              height: rowHeight + 5,
              padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
              alignment: Alignment.centerLeft,
            ))
        .toList();

    return Container(
      width: double.infinity,
      child: HorizontalDataTable(
        leftHandSideColumnWidth: width * colFactors[0],
        rightHandSideColumnWidth: width,
        isFixedHeader: true,
        headerWidgets: headerWidgets,
        leftSideItemBuilder: _generateFirstColumnRow,
        rightSideItemBuilder: _generateRightHandSideColumnRow,
        itemCount: _dataRows.length,
        rowSeparatorWidget: Divider(
          color: SmashColors.mainDecorations,
          height: 1.0,
          thickness: 0.5,
        ),
        leftHandSideColBackgroundColor: SmashColors.mainBackground,
        rightHandSideColBackgroundColor: SmashColors.mainBackground,
      ),
    );
  }

  Widget _generateFirstColumnRow(BuildContext context, int index) {
    return Container(
      child: _dataRows[index][0],
      width: width * colFactors[0],
      height: rowHeight,
      padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
      alignment: Alignment.centerLeft,
    );
  }

  Widget _generateRightHandSideColumnRow(BuildContext context, int index) {
    List<Widget> row = _dataRows[index];

    final List rowIndexes = [1, 2];
    var newRow = rowIndexes
        .map((i) => Container(
              child: row[i],
              width: width * colFactors[i],
              height: rowHeight,
              padding: EdgeInsets.fromLTRB(5, 0, 0, 0),
              alignment: Alignment.centerLeft,
            ))
        .toList();

    return Row(
      children: newRow,
    );
  }
}
