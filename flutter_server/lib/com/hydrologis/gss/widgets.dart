import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'dart:convert' as JSON;

import 'package:smashlibs/smashlibs.dart';

class FilterSurveyor extends StatefulWidget {
  FilterStateModel _filterStateModel;

  FilterSurveyor(this._filterStateModel);

  @override
  _FilterSurveyorState createState() => _FilterSurveyorState();
}

class _FilterSurveyorState extends State<FilterSurveyor> {
  Map<String, bool> _surveyorsToActive;

  @override
  void initState() {
    getSurveyors();
    super.initState();
  }

  Future<void> getSurveyors() async {
    var sessionUser = SmashSession.getSessionUser();
    String responsJson =
        await ServerApi.getSurveyors(sessionUser[0], sessionUser[1]);

    var jsonMap = JSON.jsonDecode(responsJson);

    List<dynamic> surveyors = jsonMap[KEY_SURVEYORS];
    List<String> filterSurveyors = widget._filterStateModel.surveyors;

    Map<String, bool> tmp = {};
    surveyors.forEach((name) {
      tmp[name] =
          filterSurveyors != null ? filterSurveyors.contains(name) : true;
    });

    setState(() {
      _surveyorsToActive = tmp;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Filter by Surveyors"),
        actions: <Widget>[
          IconButton(
            icon: Icon(MdiIcons.checkBoxOutline),
            tooltip: "Select all",
            onPressed: () {
              setState(() {
                _surveyorsToActive.forEach((k, v) {
                  _surveyorsToActive[k] = true;
                });
              });
            },
          ),
          IconButton(
            icon: Icon(MdiIcons.checkboxIntermediate),
            tooltip: "Invert selection",
            onPressed: () {
              setState(() {
                _surveyorsToActive.forEach((k, v) {
                  _surveyorsToActive[k] = !v;
                });
              });
            },
          ),
          IconButton(
            icon: Icon(MdiIcons.checkboxBlankOutline),
            tooltip: "Unselect all",
            onPressed: () {
              setState(() {
                _surveyorsToActive.forEach((k, v) {
                  _surveyorsToActive[k] = false;
                });
              });
            },
          )
        ],
      ),
      body: _surveyorsToActive == null
          ? Center(child: CircularProgressIndicator())
          : ListView(
              children: _surveyorsToActive.keys.map((name) {
                return CheckboxListTile(
                  title: SmashUI.normalText(name),
                  value: _surveyorsToActive[name],
                  onChanged: (bool isActive) {
                    setState(() {
                      _surveyorsToActive[name] = isActive;
                    });
                  },
                );
              }).toList(),
            ),
      floatingActionButton: FloatingActionButton(
        heroTag: "filtersurveyorsave",
        mini: true,
        onPressed: () async {
          List<String> filtered = [];
          _surveyorsToActive.forEach((name, active) {
            if (active) {
              filtered.add(name);
            }
          });
          widget._filterStateModel.setSurveyors(filtered);
          Navigator.pop(context);
          var mapstateModel =
              Provider.of<MapstateModel>(context, listen: false);
          await mapstateModel.getData(context);
          mapstateModel.fitbounds();
          mapstateModel.reloadMap();
        },
        child: Icon(MdiIcons.contentSave),
      ),
    );
  }
}

class FilterProject extends StatefulWidget {
  FilterStateModel _filterStateModel;

  FilterProject(this._filterStateModel);

  @override
  _FilterProjectState createState() => _FilterProjectState();
}

class _FilterProjectState extends State<FilterProject> {
  Map<String, bool> _projectsToActive;

  @override
  void initState() {
    getProjects();
    super.initState();
  }

  Future<void> getProjects() async {
    var sessionUser = SmashSession.getSessionUser();
    String responsJson =
        await ServerApi.getProjects(sessionUser[0], sessionUser[1]);

    var jsonMap = JSON.jsonDecode(responsJson);

    List<dynamic> projects = jsonMap[KEY_PROJECTS];
    List<String> filterProjects = widget._filterStateModel.projects;

    Map<String, bool> tmp = {};
    projects.forEach((name) {
      tmp[name] = filterProjects != null ? filterProjects.contains(name) : true;
    });

    if (mounted) {
      setState(() {
        _projectsToActive = tmp;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Filter by Project"),
        actions: <Widget>[
          IconButton(
            icon: Icon(MdiIcons.checkBoxOutline),
            tooltip: "Select all",
            onPressed: () {
              setState(() {
                _projectsToActive.forEach((k, v) {
                  _projectsToActive[k] = true;
                });
              });
            },
          ),
          IconButton(
            icon: Icon(MdiIcons.checkboxIntermediate),
            tooltip: "Invert selection",
            onPressed: () {
              setState(() {
                _projectsToActive.forEach((k, v) {
                  _projectsToActive[k] = !v;
                });
              });
            },
          ),
          IconButton(
            icon: Icon(MdiIcons.checkboxBlankOutline),
            tooltip: "Unselect all",
            onPressed: () {
              setState(() {
                _projectsToActive.forEach((k, v) {
                  _projectsToActive[k] = false;
                });
              });
            },
          )
        ],
      ),
      body: _projectsToActive == null
          ? Center(child: CircularProgressIndicator())
          : ListView(
              children: _projectsToActive.keys.map((name) {
                return CheckboxListTile(
                  title: SmashUI.normalText(name),
                  value: _projectsToActive[name],
                  onChanged: (bool isActive) {
                    setState(() {
                      _projectsToActive[name] = isActive;
                    });
                  },
                );
              }).toList(),
            ),
      floatingActionButton: FloatingActionButton(
        heroTag: "FilterProjectsave",
        mini: true,
        onPressed: () async {
          List<String> filtered = [];
          _projectsToActive.forEach((name, active) {
            if (active) {
              filtered.add(name);
            }
          });
          widget._filterStateModel.setProjects(filtered);
          print(widget._filterStateModel);
          Navigator.pop(context);
          var mapstateModel =
              Provider.of<MapstateModel>(context, listen: false);
          await mapstateModel.getData(context);
          mapstateModel.fitbounds();
          mapstateModel.reloadMap();
        },
        child: Icon(MdiIcons.contentSave),
      ),
    );
  }
}
