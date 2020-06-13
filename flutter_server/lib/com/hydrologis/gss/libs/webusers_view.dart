import 'dart:convert';

import 'package:after_layout/after_layout.dart';
import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:smashlibs/smashlibs.dart';

class WebUsersView extends StatefulWidget {
  WebUsersView({Key key}) : super(key: key);

  @override
  _WebUsersViewState createState() => _WebUsersViewState();
}

class _WebUsersViewState extends State<WebUsersView> with AfterLayoutMixin {
  List<dynamic> webusers = [];

  @override
  Future<void> afterFirstLayout(BuildContext context) async {
    await getWebusers();
  }

  Future getWebusers() async {
    var userPwd = SmashSession.getSessionUser();
    String responsJson =
        await ServerApi.getWebusersJson(userPwd[0], userPwd[1]);
    var jsonMap = jsonDecode(responsJson);
    webusers = jsonMap[KEY_WEBUSERS];
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    List<DataRow> adminsRows = [];
    List<DataRow> normalRows = [];

    var isAdmin = SmashSession.isAdmin();

    webusers.forEach((webuser) {
      var uniqueName = webuser[WEBUSER_UNIQUENAME_FIELD_NAME];
      var name = webuser[WEBUSER_NAME_FIELD_NAME];
      var contact = webuser[WEBUSER_EMAIL_FIELD_NAME] ?? "";
      var group = webuser[WEBUSER_GROUP_FIELD_NAME];

      var inputTitle = "Set new value";
      var cellsList = [
        DataCell(
          SmashUI.normalText(uniqueName),
          showEditIcon: isAdmin,
          onTap: () async {
            if (isAdmin) {
              var result = await showInputDialog(
                context,
                inputTitle,
                "Username",
                defaultText: uniqueName,
              );
              if (result != null && result.length > 0) {
                webuser[WEBUSER_UNIQUENAME_FIELD_NAME] = result;
                var up = SmashSession.getSessionUser();
                String error =
                    await ServerApi.updateOrAddWebuser(up[0], up[1], webuser);
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
                webuser[WEBUSER_NAME_FIELD_NAME] = result;
                var up = SmashSession.getSessionUser();
                String error =
                    await ServerApi.updateOrAddWebuser(up[0], up[1], webuser);
                if (error != null) {
                  showErrorDialog(context, error);
                } else {
                  await getWebusers();
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
                webuser[WEBUSER_EMAIL_FIELD_NAME] = result;
                var up = SmashSession.getSessionUser();
                String error =
                    await ServerApi.updateOrAddWebuser(up[0], up[1], webuser);
                if (error != null) {
                  showErrorDialog(context, error);
                } else {
                  await getWebusers();
                }
              }
            }
          },
        ),
      ];
      if (isAdmin) {
        cellsList.add(DataCell(
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                  icon: Icon(MdiIcons.lockReset),
                  color: SmashColors.mainDanger,
                  tooltip: "Change password for $name.",
                  onPressed: () async {
                    if (isAdmin) {
                      String result = await showInputDialog(
                          context,
                          "CHANGE PASSWORD",
                          "Insert new password for user $name?",
                          isPassword: true);
                      if (result != null && result.length > 0) {
                        webuser[WEBUSER_PASSWORD_FIELD_NAME] = result;
                        var up = SmashSession.getSessionUser();
                        String error = await ServerApi.updateOrAddWebuser(
                            up[0], up[1], webuser);
                        if (error != null) {
                          showErrorDialog(context, error);
                        } else {
                          await getWebusers();
                        }
                      }
                    }
                  }),
              IconButton(
                  icon: Icon(MdiIcons.trashCan),
                  color: SmashColors.mainDanger,
                  tooltip: "Delete user $name",
                  onPressed: () async {
                    if (isAdmin) {
                      bool result = await showConfirmDialog(
                          context,
                          "DELETE USER",
                          "Are you sure you want to delete user $name?");
                      if (result) {
                        var up = SmashSession.getSessionUser();
                        String error = await ServerApi.deleteWebuser(
                            up[0], up[1], webuser);
                        if (error != null) {
                          showErrorDialog(context, error);
                        } else {
                          await getWebusers();
                        }
                      }
                    }
                  }),
            ],
          ),
        ));
      }
      var row = DataRow(cells: cellsList);

      if (group == ADMINGROUP) {
        adminsRows.add(row);
      } else {
        normalRows.add(row);
      }
    });

    bool doAdmins = adminsRows.length > 0;
    bool doNormal = normalRows.length > 0;

    var headerColumns = [
      DataColumn(label: SmashUI.normalText("Username", bold: true)),
      DataColumn(label: SmashUI.normalText("Name", bold: true)),
      DataColumn(label: SmashUI.normalText("Contact", bold: true)),
    ];
    if (isAdmin) {
      headerColumns.add(
        DataColumn(label: SmashUI.normalText("Actions", bold: true)),
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
                  MdiIcons.accountGroup,
                  color: SmashColors.mainBackground,
                ),
              ),
              SmashUI.titleText("Web Users", color: SmashColors.mainBackground),
            ],
          ),
        ),
      ),
      body: webusers.length == 0
          ? SmashCircularProgress(label: "Loading users...")
          : Align(
              alignment: Alignment.topCenter,
              child: Column(
                children: [
                  doAdmins
                      ? Padding(
                          padding: const EdgeInsets.only(top: 25.0),
                          child: SmashUI.titleText("Administrators",
                              useColor: true),
                        )
                      : Container(),
                  doAdmins
                      ? Expanded(
                          child: SingleChildScrollView(
                            child: DataTable(
                              columns: headerColumns,
                              rows: adminsRows,
                            ),
                          ),
                        )
                      : Container(),
                  doNormal
                      ? Padding(
                          padding: const EdgeInsets.only(top: 25.0),
                          child: SmashUI.titleText("Users", useColor: true),
                        )
                      : Container(),
                  doNormal
                      ? Expanded(
                          child: SingleChildScrollView(
                            child: DataTable(
                              columns: headerColumns,
                              rows: normalRows,
                            ),
                          ),
                        )
                      : Container(),
                ],
              ),
            ),
      floatingActionButton: isAdmin
          ? FloatingActionButton(
              tooltip: "Add a new user.",
              child: Icon(MdiIcons.plus),
              onPressed: () async {
                await openNewUserDialog(context);
                await getWebusers();
              },
            )
          : null,
    );
  }

  openNewUserDialog(BuildContext context) async {
    var w = 700.0;
    var h = 350.0;
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
              child: SmashUI.titleText("Add new user", useColor: true),
            ),
            Expanded(child: NewUserForm()),
          ],
        ),
      ),
    );
    await showDialog(
        context: context,
        builder: (BuildContext context) => mapSelectionDialog);
  }
}

class NewUserForm extends StatefulWidget {
  NewUserForm({Key key}) : super(key: key);

  @override
  _NewUserFormState createState() => _NewUserFormState();
}

class _NewUserFormState extends State<NewUserForm> {
  final _formKey = GlobalKey<FormState>();
  String group = USERGROUP;
  TextEditingController nameController = TextEditingController();
  TextEditingController usernameController = TextEditingController();
  TextEditingController emailController = TextEditingController();
  TextEditingController pwdController = TextEditingController();
  TextEditingController groupController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    const pad = 15.0;

    return Form(
        key: _formKey,
        child: Column(
          children: [
            Expanded(
              child: Row(children: <Widget>[
                Expanded(
                  child: Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(pad),
                        child: TextFormField(
                          controller: nameController,
                          decoration: new InputDecoration(labelText: 'Name'),
                          validator: (value) {
                            if (value.isEmpty) {
                              return "This can't be empty";
                            }
                            return null;
                          },
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(pad),
                        child: TextFormField(
                          controller: pwdController,
                          obscureText: true,
                          decoration:
                              new InputDecoration(labelText: 'Password'),
                          validator: (value) {
                            if (value.isEmpty) {
                              return "This can't be empty";
                            }
                            return null;
                          },
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(pad),
                        child: TextFormField(
                          controller: usernameController,
                          decoration:
                              new InputDecoration(labelText: 'Username'),
                          validator: (value) {
                            if (value.isEmpty) {
                              return "This can't be empty";
                            }
                            return null;
                          },
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(pad),
                        child: TextFormField(
                          controller: emailController,
                          decoration: new InputDecoration(labelText: 'Email'),
                          validator: (value) {
                            if (!value.contains("@")) {
                              return "This isn't a valid email.";
                            }
                            return null;
                          },
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(pad),
                        child: DropdownButtonFormField(
                          value: group,
                          items: [
                            DropdownMenuItem(
                              value: ADMINGROUP,
                              child: Text(ADMINGROUP),
                            ),
                            DropdownMenuItem(
                              value: USERGROUP,
                              child: Text(USERGROUP),
                            )
                          ],
                          onChanged: (value) {
                            group = value;
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ]),
            ),
            ButtonBar(
              children: [
                FlatButton(
                  onPressed: () {
                    Navigator.pop(context);
                  },
                  child: Text('CANCEL'),
                ),
                FlatButton(
                  onPressed: () async {
                    if (_formKey.currentState.validate()) {
                      dynamic webuser = {}
                        ..[WEBUSER_UNIQUENAME_FIELD_NAME] =
                            usernameController.text
                        ..[WEBUSER_NAME_FIELD_NAME] = nameController.text
                        ..[WEBUSER_EMAIL_FIELD_NAME] = emailController.text
                        ..[WEBUSER_PASSWORD_FIELD_NAME] = pwdController.text
                        ..[WEBUSER_GROUP_FIELD_NAME] = group;
                      var up = SmashSession.getSessionUser();
                      var error = await ServerApi.updateOrAddWebuser(
                          up[0], up[1], webuser);
                      if (error != null) {
                        showWarningDialog(context, error);
                      } else {
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: Text('SUBMIT'),
                ),
              ],
            )
          ],
        ));
  }
}
