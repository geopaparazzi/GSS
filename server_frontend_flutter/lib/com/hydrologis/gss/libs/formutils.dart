import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
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
    return 1;
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

            // create a list of id) names
            List<String> formNames = [];
            id2nameMap.forEach((id, name) {
              formNames.add("$id) $name");
            });

            String? idNameSelected = await SmashDialogs.showSingleChoiceDialog(
                context, "SELECT FORM", formNames);
            if (idNameSelected != null) {
              var id = int.parse(idNameSelected.split(")")[0]);
              ServerForm? serverForm = await WebServerApi.getForm(id);
              if (serverForm != null) {
                var sectionMap = jsonDecode(serverForm.definition);
                section = SmashSection(sectionMap);
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

              if (postAction != null) postAction();
            }
          },
          icon: Icon(MdiIcons.newspaperPlus)),
    );
  }

  @override
  Widget? getSaveFormBuilderAction(BuildContext context,
      {Function? postAction}) {
    return Tooltip(
      message: SLL.of(context).formbuilder_action_save_tooltip,
      child: IconButton(
          onPressed: () async {
            // in this demo version we save the form with the name of the section and _tags.json
            // into the forms folder
            // if (section != null) {
            //   Directory formsFolder = await Workspace.getFormsFolder();
            //   var name = section!.sectionName ?? "untitled";
            //   var saveFilePath = FileUtilities.joinPaths(
            //       formsFolder.path, "${name.replaceAll(" ", "_")}_tags.json");
            //   var sectionMap = section!.sectionMap;
            //   var jsonString =
            //       const JsonEncoder.withIndent("  ").convert([sectionMap]);
            //   FileUtilities.writeStringToFile(saveFilePath, jsonString);

            //   if (context.mounted) {
            //     SmashDialogs.showToast(context, "Form saved to $saveFilePath");
            //   }
            // }
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
            // Directory formsFolder = await Workspace.getFormsFolder();
            // if (section != null && context.mounted) {
            //   var newName = await SmashDialogs.showInputDialog(
            //       context,
            //       SLL.of(context).formbuilder_action_rename_dialog_title,
            //       SLL.of(context).formbuilder_action_create_new_dialog_prompt,
            //       validationFunction: (txt) {
            //     var filePath = FileUtilities.joinPaths(
            //         formsFolder.path, "${txt.replaceAll(" ", "_")}_tags.json");
            //     if (File(filePath).existsSync()) {
            //       return SLL.of(context).formbuilder_action_rename_error_empty;
            //     }
            //     return null;
            //   });

            //   if (newName != null) {
            //     section!.setSectionName(newName);
            //     if (postAction != null) postAction();
            //   }
            // }
          },
          icon: Icon(MdiIcons.rename)),
    );
  }
}
