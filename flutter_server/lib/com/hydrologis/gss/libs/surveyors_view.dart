import 'dart:convert';

import 'package:after_layout/after_layout.dart';
import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:smashlibs/smashlibs.dart';

class SurveyorsView extends StatefulWidget {
  SurveyorsView({Key key}) : super(key: key);

  @override
  _SurveyorsViewState createState() => _SurveyorsViewState();
}

class _SurveyorsViewState extends State<SurveyorsView> with AfterLayoutMixin {
  List<dynamic> surveyors;

  @override
  Future<void> afterFirstLayout(BuildContext context) async {
    await getSurveyors();
  }

  Future getSurveyors() async {
    var userPwd = SmashSession.getSessionUser();
    String responsJson =
        await ServerApi.getSurveyorsJson(userPwd[0], userPwd[1]);
    var jsonMap = jsonDecode(responsJson);
    surveyors = jsonMap[KEY_SURVEYORS];
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    List<DataRow> activeRows = [];
    List<DataRow> disabledRows = [];

    var isAdmin = SmashSession.isAdmin();

    if (surveyors != null)
      surveyors.forEach((surveyor) {
        var deviceId = surveyor[SURVEYOR_DEVICE_FIELD_NAME];
        var name = surveyor[SURVEYOR_NAME_FIELD_NAME];
        var contact = surveyor[SURVEYOR_CONTACT_FIELD_NAME] ?? "";
        int active = surveyor[SURVEYOR_ACTIVE_FIELD_NAME];

        var inputTitle = "Set new value";
        var dataCells = [
          DataCell(
            SmashUI.normalText(deviceId),
            showEditIcon: isAdmin,
            onTap: () async {
              if (isAdmin) {
                var result = await showInputDialog(
                  context,
                  inputTitle,
                  "Device id",
                  defaultText: deviceId,
                );
                if (result != null && result.length > 0) {
                  surveyor[SURVEYOR_DEVICE_FIELD_NAME] = result;
                  var up = SmashSession.getSessionUser();
                  String error = await ServerApi.updateOrAddSurveyor(
                      up[0], up[1], surveyor);
                  if (error != null) {
                    showErrorDialog(context, error);
                  } else {
                    setState(() {});
                  }
                }
              }
            },
          ),
          DataCell(
            SmashUI.normalText(name),
            showEditIcon: isAdmin,
            onTap: () async {
              if (isAdmin) {
                var result = await showInputDialog(
                  context,
                  inputTitle,
                  "Name",
                  defaultText: name,
                );
                if (result != null && result.length > 0) {
                  surveyor[SURVEYOR_NAME_FIELD_NAME] = result;
                  var up = SmashSession.getSessionUser();
                  String error = await ServerApi.updateOrAddSurveyor(
                      up[0], up[1], surveyor);
                  if (error != null) {
                    showErrorDialog(context, error);
                  } else {
                    await getSurveyors();
                  }
                }
              }
            },
          ),
          DataCell(
            SmashUI.normalText(contact),
            showEditIcon: isAdmin,
            onTap: () async {
              if (isAdmin) {
                var result = await showInputDialog(
                  context,
                  inputTitle,
                  "Contact",
                  defaultText: contact,
                );
                if (result != null && result.length > 0) {
                  surveyor[SURVEYOR_CONTACT_FIELD_NAME] = result;
                  var up = SmashSession.getSessionUser();
                  String error = await ServerApi.updateOrAddSurveyor(
                      up[0], up[1], surveyor);
                  if (error != null) {
                    showErrorDialog(context, error);
                  } else {
                    await getSurveyors();
                  }
                }
              }
            },
          ),
        ];
        if (isAdmin) {
          dataCells.add(
            DataCell(
              Checkbox(
                  value: active == 1 ? true : false,
                  onChanged: (selected) async {
                    if (isAdmin) {
                      surveyor[SURVEYOR_ACTIVE_FIELD_NAME] = selected ? 1 : 0;
                      var up = SmashSession.getSessionUser();
                      String error = await ServerApi.updateOrAddSurveyor(
                          up[0], up[1], surveyor);
                      if (error != null) {
                        showErrorDialog(context, error);
                      } else {
                        await getSurveyors();
                      }
                    }
                  }),
            ),
          );
        }
        var row = DataRow(cells: dataCells);

        if (active == 1) {
          activeRows.add(row);
        } else {
          disabledRows.add(row);
        }
      });

    bool doActive = activeRows.length > 0;
    bool doDisabled = disabledRows.length > 0;

    var dataHeader = [
      DataColumn(label: SmashUI.normalText("Device Id", bold: true)),
      DataColumn(label: SmashUI.normalText("Name", bold: true)),
      DataColumn(label: SmashUI.normalText("Contact", bold: true)),
    ];
    if (isAdmin) {
      dataHeader.add(
        DataColumn(label: SmashUI.normalText("Active", bold: true)),
      );
    }
    return Scaffold(
      appBar: AppBar(
        title: Center(
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.only(right: 8.0),
                child: new Icon(
                  MdiIcons.accountHardHat,
                  color: SmashColors.mainBackground,
                ),
              ),
              SmashUI.titleText("Surveyors", color: SmashColors.mainBackground),
            ],
          ),
        ),
      ),
      body: surveyors == null
          ? SmashCircularProgress(label: "Loading surveyors...")
          : surveyors.length == 0
              ? Center(
                  child: SmashUI.titleText("No surveyors present yet.",
                      useColor: true))
              : Align(
                  alignment: Alignment.topCenter,
                  child: Column(
                    children: [
                      doActive
                          ? Padding(
                              padding: const EdgeInsets.only(top: 25.0),
                              child: SmashUI.titleText("Active Surveyors"),
                            )
                          : Container(),
                      doActive
                          ? Expanded(
                              child: SingleChildScrollView(
                                child: DataTable(
                                  columns: dataHeader,
                                  rows: activeRows,
                                ),
                              ),
                            )
                          : Container(),
                      doDisabled
                          ? Padding(
                              padding: const EdgeInsets.only(top: 25.0),
                              child: SmashUI.titleText("Disabled Surveyors"),
                            )
                          : Container(),
                      doDisabled
                          ? Expanded(
                              child: SingleChildScrollView(
                                child: DataTable(
                                  columns: [
                                    DataColumn(
                                        label: SmashUI.normalText("Device Id",
                                            bold: true)),
                                    DataColumn(
                                        label: SmashUI.normalText("Name",
                                            bold: true)),
                                    DataColumn(
                                        label: SmashUI.normalText("Contact",
                                            bold: true)),
                                    DataColumn(
                                        label: SmashUI.normalText("Active",
                                            bold: true)),
                                  ],
                                  rows: disabledRows,
                                ),
                              ),
                            )
                          : Container(),
                    ],
                  ),
                ),
      floatingActionButton: SmashSession.isAdmin()
          ? Builder(
              builder: (BuildContext context) {
                return FloatingActionButton.extended(
                    onPressed: () async {
                      var sessionUser = SmashSession.getSessionUser();
                      await ServerApi.enableAutomaticRegistration(
                          sessionUser[0], sessionUser[1]);
                      final snackBar = SnackBar(
                          content: Text(
                              'Automatic registration of surveyor devices enabled for 30 seconds.'));
                      Scaffold.of(context).showSnackBar(snackBar);
                    },
                    icon: Icon(MdiIcons.doorOpen),
                    tooltip: "Activate automatic registration for 30 seconds.",
                    label: Text("30 sec automatic"));
              },
            )
          : null,
    );
  }
}
