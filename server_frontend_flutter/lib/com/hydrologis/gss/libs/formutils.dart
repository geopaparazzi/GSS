import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/maputils.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:smashlibs/smashlibs.dart';

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
