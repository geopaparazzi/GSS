import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/ui.dart';
import 'package:flutter_server/com/hydrologis/gss/models.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/session.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'dart:convert' as JSON;

class FilterSurveyor extends StatefulWidget {
  FilterStateModel _filterStateModel;
  Function _reloadDataFunction;

  FilterSurveyor(this._filterStateModel, this._reloadDataFunction);

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
        heroTag: "filterwurveyorsave",
        mini: true,
        onPressed: () async {
          List<String> filtered = [];
          _surveyorsToActive.forEach((name, active) {
            if (active) {
              filtered.add(name);
            }
          });
          widget._filterStateModel.setSurveyors(filtered);
          widget._reloadDataFunction(context);
          Navigator.pop(context);
        },
        child: Icon(MdiIcons.contentSave),
      ),
    );
  }
}
