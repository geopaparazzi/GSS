import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:smashlibs/smashlibs.dart';

const IMAGE_ID_SEPARATOR = ";";

class ServerFormHelper implements AFormhelper {
  /// Get thumbnails from the database
  Future<List<Widget>> getThumbnailsFromDb(
      BuildContext context, var itemMap, List<String> imageSplit) async {
    List<Widget> thumbList = [];
    String value = ""; //$NON-NLS-1$
    if (itemMap.containsKey(TAG_VALUE)) {
      value = itemMap[TAG_VALUE].trim();
    }
    if (value.isNotEmpty) {
      imageSplit.clear();
      imageSplit.addAll(value.split(IMAGE_ID_SEPARATOR));
    } else {
      return Future.value(thumbList);
    }

    var userPwd = SmashSession.getSessionUser();

    for (int i = 0; i < imageSplit.length; i++) {
      var id = int.parse(imageSplit[i]);
      var bytes =
          await ServerApi.getImageThumbnailById(userPwd[0], userPwd[1], id);
      Image img = Image.memory(bytes);
      Widget withBorder = Container(
        padding: SmashUI.defaultPadding(),
        child: img,
      );
      thumbList.add(withBorder);
    }
    return thumbList;
  }

  @override
  Future<void> onSaveFunction(
      BuildContext context, noteId, sectionName, sectionMap, _position) {
    // saving serverside is not implemented yet.
  }

  @override
  Future<String> takePictureForForms(BuildContext context, noteId, sectionN,
      bool sectionMap, List<String> _position) {
    // Adding pictures serverside is nor implemented yet.
    return null;
  }
}
