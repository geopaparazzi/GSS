import 'dart:html';

import 'package:after_layout/after_layout.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:http/http.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:smashlibs/smashlibs.dart';

class ProjectDataView extends StatefulWidget {
  ProjectDataView({Key key}) : super(key: key);

  @override
  _ProjectDataViewState createState() => _ProjectDataViewState();
}

class _ProjectDataViewState extends State<ProjectDataView>
    with AfterLayoutMixin {
  GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  List<String> projects;
  List<String> basemaps;
  List<String> forms;
  List<String> formIds;

  @override
  void afterFirstLayout(BuildContext context) {
    init(context);
  }

  Future<void> init(BuildContext context) async {
    var up = SmashSession.getSessionUser();
    Map<String, List<String>> map =
        await ServerApi.getProjectData(up[0], up[1]);
    projects = map[PROJECTDATA_PROJECTS] ?? [];
    basemaps = map[PROJECTDATA_MAPS] ?? [];
    forms = map[PROJECTDATA_TAGS] ?? [];
    formIds = map[PROJECTDATA_TAGSID] ?? [];
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    var isLargeScreen = ScreenUtilities.isLargeScreen(context);
    var isAdmin = SmashSession.isAdmin();

    List<Widget> bm = [];
    if (basemaps != null) {
      bm = basemaps.map((name) {
        return ListTile(
          leading: Icon(
            SmashIcons.forPath(name),
            color: SmashColors.mainDecorations,
            ),
          title: SmashUI.normalText(name),
        );
      }).toList();
    }

    List<Widget> p = [];
    if (projects != null) {
      p = projects.map((name) {
        return ListTile(
          leading: Icon(
            SmashIcons.forPath(name),
            color: SmashColors.mainDecorations,
          ),
          title: SmashUI.normalText(name),
        );
      }).toList();
    }
    List<Widget> f = [];
    if (forms != null) {
      for (var i = 0; i < forms.length; i++) {
        var name = forms[i];
        var id = formIds[i];
        f.add(ListTile(
          leading: Icon(
            SmashIcons.forPath(name),
            color: SmashColors.mainDecorations,
          ),
          title: SmashUI.normalText(name),
          trailing: isAdmin
              ? IconButton(
                  icon: Icon(MdiIcons.trashCan, color: SmashColors.mainDanger),
                  onPressed: () async {
                    var delete = await showConfirmDialog(context, "DELETE",
                        "Are you sure you want to remove the form: $name");
                    if (delete) {
                      var up = SmashSession.getSessionUser();
                      await ServerApi.deleteProjectForm(up[0], up[1], id);
                      init(context);
                    }
                  },
                )
              : null,
        ));
      }
    }

    var basemapsWidget = Column(
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        SmashUI.titleText("Basemaps", useColor: true),
      ]..addAll(bm),
    );
    var projectsWidget = Column(
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        SmashUI.titleText("Projects", useColor: true),
      ]..addAll(p),
    );
    var formsWidget = Column(
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        SmashUI.titleText("Forms", useColor: true),
      ]..addAll(f),
    );

    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
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
              SmashUI.titleText("Project Data",
                  color: SmashColors.mainBackground),
            ],
          ),
        ),
      ),
      body: projects == null
          ? SmashCircularProgress(label: "Loading data...")
          : isLargeScreen
              ? Padding(
                  padding: SmashUI.defaultPadding(),
                  child: SingleChildScrollView(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Expanded(child: basemapsWidget),
                        Expanded(child: projectsWidget),
                        Expanded(child: formsWidget),
                      ],
                    ),
                  ),
                )
              : Padding(
                  padding: SmashUI.defaultPadding(),
                  child: SingleChildScrollView(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Center(child: basemapsWidget),
                        Center(child: projectsWidget),
                        Center(child: formsWidget),
                      ],
                    ),
                  ),
                ),
      floatingActionButton: isAdmin
          ? FloatingActionButton(
              tooltip: "Upload a new dataset.",
              child: Icon(MdiIcons.plus),
              onPressed: () async {
                InputElement uploadInput = FileUploadInputElement();
                uploadInput.click();

                uploadInput.onChange.listen((e) {
                  // read file content as dataURL
                  final files = uploadInput.files;
                  if (files.length == 1) {
                    final file = files[0];
                    final reader = new FileReader();
                    reader.onLoadStart.listen((e) {
                      final snackBar = SnackBar(
                        content: Text('File upload started...'),
                        duration: Duration(seconds: 3),
                        // backgroundColor: MAIN_COLOR,
                      );
                      _scaffoldKey.currentState.showSnackBar(snackBar);
                    });
                    reader.onLoadEnd.listen((e) async {
                      var result = reader.result;
                      try {
                        var up = SmashSession.getSessionUser();
                        Map<String, String> requestHeaders =
                            ServerApi.getAuthRequestHeader(up[0], up[1]);
                        var postUri = Uri.parse(API_DATA_UPLOAD_PATH);
                        var request = MultipartRequest("POST", postUri);
                        request.headers.addAll(requestHeaders);
                        var multipartFile;
                        if (result is List<int>) {
                          multipartFile = MultipartFile.fromBytes(
                              'file', result,
                              filename: file.name);
                          request.files.add(multipartFile);
                        } else {
                          multipartFile = MultipartFile.fromString(
                              'file', result,
                              filename: file.name);
                          request.files.add(multipartFile);
                        }
                        request.send().then((value) {}).whenComplete(() {
                          final snackBar = SnackBar(
                            content: Text(
                                'File ${file.name} successfully uploaded!'),
                            duration: Duration(seconds: 3),
                            // backgroundColor: MAIN_COLOR,
                          );
                          _scaffoldKey.currentState.showSnackBar(snackBar);
                          init(context);
                        }).catchError((e) {
                          final snackBar = SnackBar(
                            content: Text('An error occurred: ${e.toString()}'),
                            duration: Duration(seconds: 3),
                            backgroundColor: Colors.red,
                          );
                          _scaffoldKey.currentState.showSnackBar(snackBar);
                        });
                      } catch (ex, stack) {
                        print(stack);
                        final snackBar = SnackBar(
                          content: Text('An error occurred: $stack'),
                          duration: Duration(seconds: 3),
                          backgroundColor: Colors.red,
                        );
                        _scaffoldKey.currentState.showSnackBar(snackBar);
                      }
                    });
                    reader.onError.listen((e) {
                      final snackBar = SnackBar(
                        content: Text('An error occurred: ${e.toString()}'),
                        duration: Duration(seconds: 3),
                        backgroundColor: Colors.red,
                      );
                      _scaffoldKey.currentState.showSnackBar(snackBar);
                    });
                    if (file.name.endsWith("tags.json")) {
                      reader.readAsText(file);
                    } else {
                      reader.readAsArrayBuffer(file);
                    }
                  }
                });
              },
            )
          : null,
    );
  }
}
