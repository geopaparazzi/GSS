import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:smashlibs/com/hydrologis/flutterlibs/utils/logging.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:smashlibs/generated/l10n.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

const IMAGE_ID_SEPARATOR = ";";

class ServerFormHelper extends AFormhelper {
  String _sectionName;
  SmashSection _section;
  Widget _titleWidget;
  int _id;
  dynamic _position;

  ServerFormHelper(this._id, this._sectionName, this._section,
      this._titleWidget, this._position);

  @override
  Future<bool> init() async {
    return true;
  }

  @override
  SmashSection getSection() {
    return _section;
  }

  @override
  bool hasForm() {
    // notes always have forms
    return true;
  }

  @override
  Widget getFormTitleWidget() {
    return _titleWidget;
  }

  @override
  int getId() {
    return _id;
  }

  @override
  getPosition() {
    return _position;
  }

  @override
  String getSectionName() {
    return _sectionName;
  }

  /// Get thumbnails from the database
  Future<List<Widget>> getThumbnailsFromDb(BuildContext context,
      SmashFormItem formItem, List<String> imageSplit) async {
    List<Widget> thumbList = [];

    String value = formItem.value ?? ""; //$NON-NLS-1$
    if (value.isNotEmpty) {
      var split = value.split(IMAGE_ID_SEPARATOR);
      split.forEach((v) {
        if (!imageSplit.contains(v)) {
          imageSplit.add(v);
        }
      });
    }

    for (int i = 0; i < imageSplit.length; i++) {
      var id = int.parse(imageSplit[i]);
      var bytes = await WebServerApi.getImageThumbnail(id);
      Image img = Image.memory(bytes!);
      Widget withBorder = Container(
        padding: SmashUI.defaultPadding(),
        child: GestureDetector(
          onDoubleTap: () {
            openImageDialog(context, "", id, hideRotate: true);
          },
          child: img,
        ),
      );
      thumbList.add(withBorder);
    }
    return thumbList;
  }

  @override
  Future onSaveFunction(BuildContext context) async {
    // saving serverside is not implemented yet.
  }

  @override
  Future<String?> takePictureForForms(
      BuildContext context, bool fromGallery, List<String> _position) async {
    // Adding pictures serverside is not implemented yet.
    return null;
  }

  @override
  Future<String?> takeSketchForForms(
      BuildContext context, List<String> imageSplit) {
    // Adding sketches serverside is not implemented yet.
    return Future.value(null);
  }

  @override
  Map<String, dynamic> getFormChangedData() {
    // should not be called on web
    throw UnimplementedError();
  }

  @override
  void setData(Map<String, dynamic> newValues) {
    // forms are readonly on web
  }
}

class FormBuilderFormHelper extends AFormhelper {
  SmashSection? section;
  ServerForm? _serverForm;

  FormBuilderFormHelper();

  @override
  Future<bool> init() async {
    return Future.value(true);
  }

  @override
  Widget getFormTitleWidget() {
    return SmashUI.titleText("Web Formbuilder");
  }

  @override
  int getId() {
    return _serverForm?.id ?? -1;
  }

  @override
  getPosition() {
    return null;
  }

  @override
  SmashSection? getSection() {
    return section;
  }

  @override
  String? getSectionName() {
    return section?.sectionName;
  }

  @override
  Future<List<Widget>> getThumbnailsFromDb(
      BuildContext context, SmashFormItem formItem, List<String> imageSplit) {
    return Future.value([]);
  }

  @override
  bool hasForm() {
    return true;
  }

  @override
  Future<void> onSaveFunction(BuildContext context) async {}

  @override
  Future<String?> takePictureForForms(
      BuildContext context, bool fromGallery, List<String> imageSplit) {
    // TODO: implement takePictureForForms
    throw UnimplementedError();
  }

  @override
  Future<String?> takeSketchForForms(
      BuildContext context, List<String> imageSplit) {
    // TODO: implement takeSketchForForms
    throw UnimplementedError();
  }

  @override
  Widget? getOpenFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    return Tooltip(
      message: SLL.of(context).formbuilder_action_open_existing_tooltip,
      child: IconButton(
          onPressed: () async {
            // gather the existing gss layers
            Map<int, String> id2nameMap = await WebServerApi.getFormNames();

            // create a list of names with id
            List<String> formNames = [];
            id2nameMap.forEach((id, name) {
              formNames.add("$name ($id)");
            });

            String? idNameSelected = await SmashDialogs.showSingleChoiceDialog(
                context, "SELECT FORM", formNames);
            if (idNameSelected != null) {
              var id = int.parse(idNameSelected.split("(")[1].split(")")[0]);
              ServerForm? serverForm = await WebServerApi.getForm(id);
              if (serverForm != null) {
                _serverForm = serverForm;
                var sectionMap = jsonDecode(serverForm.definition);
                section = SmashSection(sectionMap);
                section!.setSectionName(serverForm.name);
                if (postAction != null) postAction();
              }
            }
          },
          icon: Icon(MdiIcons.folderOpenOutline)),
    );
  }

  @override
  Widget? getNewFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    return Tooltip(
      message: SLL.of(context).formbuilder_action_create_new_tooltip,
      child: IconButton(
          onPressed: () async {
            var answer = await SmashDialogs.showInputDialog(
                context,
                SLL.of(context).formbuilder_action_create_new_dialog_title,
                SLL.of(context).formbuilder_action_create_new_dialog_prompt,
                validationFunction: (String? value) {
              if (value == null || value.isEmpty) {
                return SLL
                    .of(context)
                    .formbuilder_action_create_new_error_empty;
              }
              // no spaces
              if (value.contains(" ")) {
                return SLL
                    .of(context)
                    .formbuilder_action_create_new_error_spaces;
              }
              return null;
            });
            if (answer != null) {
              var emptyTagsString = TagsManager.getEmptyTagsString(answer);
              var tm = TagsManager();
              await tm.readTags(tagsString: emptyTagsString);
              section = tm.getTags().getSections()[0];

              _serverForm = ServerForm(section!.sectionName ?? "untitled",
                  jsonEncode(section!.sectionMap), "Point", true, true);

              String? error = await WebServerApi.postForm(_serverForm!);
              if (error != null) {
                SmashDialogs.showErrorDialog(context, error);
              } else {
                if (postAction != null) postAction();
              }
            }
          },
          icon: Icon(MdiIcons.newspaperPlus)),
    );
  }

  @override
  Widget? getSaveFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    return Tooltip(
      message: "Save the form to the server.",
      child: IconButton(
          onPressed: () async {
            if (_serverForm != null) {
              String? error;
              if (_serverForm != null && section != null) {
                // only send if the form has been saved before
                var definition = jsonEncode(section!.sectionMap);
                _serverForm!.definition = definition;
                error = await WebServerApi.putForm(_serverForm!);
              }
              if (error != null) {
                SmashDialogs.showErrorDialog(context, error);
              } else {
                if (postAction != null) postAction();
              }
            } else {
              SmashDialogs.showErrorDialog(context, "Error: No form to save.");
            }
          },
          icon: Icon(MdiIcons.contentSave)),
    );
  }

  @override
  Widget? getRenameFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    return Tooltip(
      message: SLL.of(context).formbuilder_action_rename_tooltip,
      child: IconButton(
          onPressed: () async {
            if (section != null && context.mounted) {
              bool? answer = await SmashDialogs.showConfirmDialog(
                  context,
                  "WARNING",
                  "Renaming tables that contain data leads to the loss of the data. Do you want to continue?");
              if (answer == null || !answer) {
                return;
              }

              // gather the existing gss layers
              Map<int, String> id2nameMap = await WebServerApi.getFormNames();
              // create a list of names
              List<String> formNames = [];
              id2nameMap.forEach((id, name) {
                formNames.add(name);
              });

              var newName = await SmashDialogs.showInputDialog(
                  context,
                  SLL.of(context).formbuilder_action_rename_dialog_title,
                  SLL.of(context).formbuilder_action_create_new_dialog_prompt,
                  defaultText: section!.sectionName, validationFunction: (txt) {
                if (txt == null || txt.isEmpty) {
                  return SLL
                      .of(context)
                      .formbuilder_action_create_new_error_empty;
                }
                if (txt.contains(" ")) {
                  return SLL
                      .of(context)
                      .formbuilder_action_create_new_error_spaces;
                }
                if (id2nameMap.values.contains(txt)) {
                  return SLL.of(context).formbuilder_action_rename_error_empty;
                }
                return null;
              });

              if (newName != null) {
                section!.setSectionName(newName);

                String? error;
                if (_serverForm != null) {
                  // only send if the form has been saved before
                  _serverForm!.name = newName;
                  error = await WebServerApi.putForm(_serverForm!,
                      onlyRename: true);
                }
                if (error != null) {
                  SmashDialogs.showErrorDialog(context, error);
                } else {
                  if (postAction != null) postAction();
                }
              }
            }
          },
          icon: Icon(MdiIcons.rename)),
    );
  }

  Widget? getDeleteFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    return null;
  }

  Widget? getExtraFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    var gType = "Point";
    bool addExtraInfo = true;
    bool isEnabled = true;
    bool showInProjectdataDownloads = true;
    if (_serverForm != null) {
      gType = _serverForm!.geometrytype;
      addExtraInfo = _serverForm!.addTimestamp && _serverForm!.addUserinfo;
      isEnabled = _serverForm!.enabled;
      showInProjectdataDownloads = _serverForm!.showInProjectdataDownload;
    }

    List<Widget> widgets = [
      Padding(
        padding: const EdgeInsets.only(left: 8.0, right: 8.0),
        child: Row(
          children: [
            Padding(
              padding: const EdgeInsets.only(left: 8.0, right: 8.0),
              child: SmashUI.normalText("Geometry type: ",
                  color: SmashColors.mainDecorationsDarker),
            ),
            DropdownButton<String>(
              style: TextStyle(
                  color: SmashColors.mainDecorationsDarker,
                  fontWeight: FontWeight.bold),
              dropdownColor: SmashColors.mainBackground,
              value: gType,
              items: [
                "Point",
                "LineString",
                "Polygon",
              ].map((String value) {
                return DropdownMenuItem<String>(
                  value: value,
                  child: Text(value),
                );
              }).toList(),
              onChanged: (String? value) {
                if (_serverForm != null && value != null) {
                  _serverForm!.geometrytype = value;
                  if (postAction != null) postAction();
                  Navigator.pop(context);
                }
              },
            ),
          ],
        ),
      ),
      Padding(
        padding: const EdgeInsets.only(left: 8.0, right: 8.0),
        child: CheckboxListTile(
            controlAffinity: ListTileControlAffinity.leading,
            title: SmashUI.normalText("Add extra info to the form",
                color: SmashColors.mainDecorationsDarker),
            value: addExtraInfo,
            onChanged: (changed) {
              if (_serverForm != null) {
                _serverForm!.addTimestamp = changed!;
                _serverForm!.addUserinfo = changed;
                if (postAction != null) postAction();
                Navigator.pop(context);
              }
            }),
      ),
      Padding(
        padding: const EdgeInsets.only(left: 8.0, right: 8.0),
        child: CheckboxListTile(
            controlAffinity: ListTileControlAffinity.leading,
            value: isEnabled,
            title: SmashUI.normalText(
                isEnabled ? "Disable the form." : "Enable the form.",
                color: SmashColors.mainDecorationsDarker),
            onChanged: (changed) {
              if (_serverForm != null) {
                _serverForm!.enabled = changed!;
                if (postAction != null) postAction();
                Navigator.pop(context);
              }
            }),
      ),
      Padding(
        padding: const EdgeInsets.only(left: 8.0, right: 8.0),
        child: CheckboxListTile(
            controlAffinity: ListTileControlAffinity.leading,
            value: showInProjectdataDownloads,
            title: SmashUI.normalText("Show form in client project data.",
                color: SmashColors.mainDecorationsDarker),
            onChanged: (changed) {
              if (_serverForm != null) {
                _serverForm!.showInProjectdataDownload = changed!;
                if (postAction != null) postAction();
                Navigator.pop(context);
              }
            }),
      ),
    ];
    // open a dialog with teh widgets
    return IconButton(
      onPressed: () async {
        await SmashDialogs.showWidgetListDialog(
            context, "EXTRA CONFIGURATIONS", widgets);
      },
      icon: Icon(MdiIcons.dotsVertical, color: SmashColors.mainBackground),
    );
  }
}
