import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/forms.dart';
import 'package:flutter_server/com/hydrologis/gss/variables.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/ui.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/colors.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/screen.dart';
import 'package:flutter_server/com/hydrologis/gss/utils.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:flutter_tags/tag.dart';

typedef Null ItemSelectedCallback(String selectedFormName);

class FormSectionsWidget extends StatefulWidget {
  final ItemSelectedCallback onItemSelected;
  final String sectionName;
  bool isLargeScreen;
  Map<String, dynamic> sectionMap;

  FormSectionsWidget(this.sectionMap, this.sectionName, this.isLargeScreen,
      this.onItemSelected);

  @override
  State<StatefulWidget> createState() {
    return FormSectionsWidgetState();
  }
}

class FormSectionsWidgetState extends State<FormSectionsWidget> {
  int _selectedPosition = 0;

  @override
  Widget build(BuildContext context) {
    var formNames4Section = TagsManager.getFormNames4Section(widget.sectionMap);

    return ListView.builder(
      itemCount: formNames4Section.length,
      itemBuilder: (context, position) {
        return Ink(
          color: _selectedPosition == position && widget.isLargeScreen
              ? SmashColors.mainDecorations.withAlpha(50)
              : null,
          child: ListTile(
            onTap: () {
              widget.onItemSelected(formNames4Section[position]);
              setState(() {
                _selectedPosition = position;
              });
            },
            title: SmashUI.normalText(formNames4Section[position],
                bold: true, color: SmashColors.mainDecorationsDark),
          ),
        );
      },
    );
  }
}

class FormDetailWidget extends StatefulWidget {
  final String sectionName;
  String formName;
  bool isLargeScreen;
  bool onlyDetail;
  dynamic _position;
  int _noteId;
  int _userId;
  Map<String, dynamic> sectionMap;

  FormDetailWidget(
      this._noteId,
      this._userId,
      this.sectionMap,
      this.sectionName,
      this.formName,
      this.isLargeScreen,
      this.onlyDetail,
      this._position);

  @override
  State<StatefulWidget> createState() {
    return FormDetailWidgetState();
  }
}

class FormDetailWidgetState extends State<FormDetailWidget> {
  List<String> formNames;

  @override
  void initState() {
    formNames = TagsManager.getFormNames4Section(widget.sectionMap);

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    List<ListTile> widgetsList = [];
    if (widget.formName == null) {
      // pick the first of the section
      widget.formName = formNames[0];
    }
    var form4name =
        TagsManager.getForm4Name(widget.formName, widget.sectionMap);
    List<dynamic> formItems = TagsManager.getFormItems(form4name);

    for (int i = 0; i < formItems.length; i++) {
      Widget w = getWidget(context, widget._noteId, widget._userId,
          formItems[i], widget._position);
      if (w != null) {
        widgetsList.add(w);
      }
    }

    return Scaffold(
      appBar: !widget.isLargeScreen && !widget.onlyDetail
          ? AppBar(
              title: Text(widget.formName),
            )
          : null,
      body: Container(
        color: widget.isLargeScreen && !widget.onlyDetail
            ? SmashColors.mainDecorationsMc[50]
            : null,
        child: ListView.builder(
          itemCount: widgetsList.length,
          itemBuilder: (context, index) {
            return Container(
              padding: EdgeInsets.only(top: 10.0),
              child: widgetsList[index],
            );
          },
          padding: EdgeInsets.only(bottom: 10.0),
        ),
      ),
    );
  }
}

class MasterDetailPage extends StatefulWidget {
  Widget title;
  String sectionName;
  dynamic _position;
  int _noteId;
  Map<String, dynamic> sectionMap;
  int _userId;

  MasterDetailPage(this.sectionMap, this.title, this.sectionName,
      this._position, this._noteId, this._userId);

  @override
  _MasterDetailPageState createState() => _MasterDetailPageState();
}

class _MasterDetailPageState extends State<MasterDetailPage> {
  String selectedForm;
  var isLargeScreen = false;

  @override
  Widget build(BuildContext context) {
    var formNames = TagsManager.getFormNames4Section(widget.sectionMap);

    // in case of single tab, display detail directly
    bool onlyDetail = formNames.length == 1;

    return Scaffold(
      appBar: AppBar(
        title: widget.title,
      ),
      body: OrientationBuilder(builder: (context, orientation) {
        isLargeScreen = ScreenUtilities.isLargeScreen(context);

        return Row(children: <Widget>[
          !onlyDetail
              ? Expanded(
                  flex: isLargeScreen ? 4 : 1,
                  child: FormSectionsWidget(
                      widget.sectionMap, widget.sectionName, isLargeScreen,
                      (formName) {
                    if (isLargeScreen) {
                      selectedForm = formName;
                      setState(() {});
                    } else {
                      Navigator.push(context, MaterialPageRoute(
                        builder: (context) {
                          return FormDetailWidget(
                              widget._noteId,
                              widget._userId,
                              widget.sectionMap,
                              widget.sectionName,
                              formName,
                              isLargeScreen,
                              onlyDetail,
                              widget._position);
                        },
                      ));
                    }
                  }),
                )
              : Container(),
          isLargeScreen || onlyDetail
              ? Expanded(
                  flex: 6,
                  child: FormDetailWidget(
                      widget._noteId,
                      widget._userId,
                      widget.sectionMap,
                      widget.sectionName,
                      selectedForm,
                      isLargeScreen,
                      onlyDetail,
                      widget._position))
              : Container(),
        ]);
      }),
    );
  }
}

ListTile getWidget(BuildContext context, int noteId, int userId,
    final Map<String, dynamic> itemMap, dynamic position) {
  String key = "-"; //$NON-NLS-1$
  if (itemMap.containsKey(TAG_KEY)) {
    key = itemMap[TAG_KEY].trim();
  }
  String label = TagsManager.getLabelFromFormItem(itemMap);

  dynamic value = ""; //$NON-NLS-1$
  if (itemMap.containsKey(TAG_VALUE)) {
    value = itemMap[TAG_VALUE].trim();
  }
  String type = TYPE_STRING;
  if (itemMap.containsKey(TAG_TYPE)) {
    type = itemMap[TAG_TYPE].trim();
  }
  String iconStr;
  if (itemMap.containsKey(TAG_ICON)) {
    iconStr = itemMap[TAG_ICON].trim();
  }

  Icon icon;
  if (iconStr != null) {
    var iconData = MdiIcons.fromString(iconStr); // ICONSMAP[iconStr];
    icon = Icon(
      iconData,
      color: SmashColors.mainDecorations,
    );
  }

  bool readonly = false;
  if (itemMap.containsKey(TAG_READONLY)) {
    var readonlyObj = itemMap[TAG_READONLY].trim();
    if (readonlyObj is String) {
      readonly = readonlyObj == 'true';
    } else if (readonlyObj is bool) {
      readonly = readonlyObj;
    } else if (readonlyObj is num) {
      readonly = readonlyObj.toDouble() == 1.0;
    }
  }

  Constraints constraints = new Constraints();
  FormUtilities.handleConstraints(itemMap, constraints);
//    key2ConstraintsMap.put(key, constraints);
//    String constraintDescription = constraints.getDescription();

  var minLines = 1;
  var maxLines = 1;
  var keyboardType = TextInputType.text;
  var textDecoration = TextDecoration.none;
  switch (type) {
    case TYPE_STRINGAREA:
      {
        minLines = 5;
        maxLines = 5;
        continue TYPE_STRING;
      }
    case TYPE_DOUBLE:
      {
        keyboardType =
            TextInputType.numberWithOptions(signed: true, decimal: true);
        continue TYPE_STRING;
      }
    case TYPE_INTEGER:
      {
        keyboardType =
            TextInputType.numberWithOptions(signed: true, decimal: false);
        continue TYPE_STRING;
      }
    TYPE_STRING:
    case TYPE_STRING:
      {
        TextEditingController stringController =
            new TextEditingController(text: value);

        stringController.addListener(() {
          itemMap[TAG_VALUE] = stringController.text;
        });
        TextFormField field = TextFormField(
          validator: (value) {
            if (!constraints.isValid(value)) {
              return constraints.getDescription();
            }
            return null;
          },
          autovalidate: true,
          decoration: InputDecoration(
//            icon: icon,
            labelText: "$label ${constraints.getDescription()}",
          ),
          controller: stringController,
          enabled: !readonly,
          minLines: minLines,
          maxLines: maxLines,
          keyboardType: keyboardType,
        );

        ListTile tile = ListTile(
          title: field,
          leading: icon,
        );
        return tile;
      }
    case TYPE_LABELWITHLINE:
      {
        textDecoration = TextDecoration.underline;
        continue TYPE_LABEL;
      }
    TYPE_LABEL:
    case TYPE_LABEL:
      {
        String sizeStr = "20";
        if (itemMap.containsKey(TAG_SIZE)) {
          sizeStr = itemMap[TAG_SIZE];
        }
        double size = double.parse(sizeStr);
        String url;
        if (itemMap.containsKey(TAG_URL)) {
          url = itemMap[TAG_URL];
          textDecoration = TextDecoration.underline;
        }

        var text = Text(
          value.toString(),
          style: TextStyle(
              fontSize: size,
              decoration: textDecoration,
              color: SmashColors.mainDecorationsDark),
          textAlign: TextAlign.start,
        );

        if (url == null) {
          return ListTile(
            leading: icon,
            title: text,
          );
        } else {
          return ListTile(
            leading: icon,
            title: GestureDetector(
              onTap: () async {
//                if (await canLaunch(url)) {
//                  await launch(url);
//                } else {
                showErrorDialog(context, "Unable to open url: $url");
//                }
              },
              child: text,
            ),
          );
        }
        break;
      }
    case TYPE_DYNAMICSTRING:
      {
        return ListTile(
          leading: icon,
          title: DynamicStringWidget(itemMap, label),
        );
        break;
      }
    case TYPE_DATE:
      {
        return ListTile(
          leading: icon,
          title: DatePickerWidget(itemMap, label),
        );
        break;
      }
    case TYPE_TIME:
      {
        return ListTile(
          leading: icon,
          title: TimePickerWidget(itemMap, label),
        );
        break;
      }
    case TYPE_BOOLEAN:
      {
        return ListTile(
          leading: icon,
          title: CheckboxWidget(itemMap, label),
        );
      }
    case TYPE_STRINGCOMBO:
      {
        return ListTile(
          leading: icon,
          title: ComboboxWidget(itemMap, label),
        );
      }
//      case TYPE_AUTOCOMPLETESTRINGCOMBO: {
//        JSONArray comboItems = TagsManager.getComboItems(jsonObject);
//        String[] itemsArray = TagsManager.comboItems2StringArray(comboItems);
//        addedView = FormUtilities.addAutocompleteComboView(activity, mainView, label, value, itemsArray, constraintDescription);
//        break;
//      }
//      case TYPE_CONNECTEDSTRINGCOMBO: {
//        LinkedHashMap<String, List<String>> valuesMap = TagsManager.extractComboValuesMap(jsonObject);
//        addedView = FormUtilities.addConnectedComboView(activity, mainView, label, value, valuesMap,
//            constraintDescription);
//        break;
//      }
//      case TYPE_AUTOCOMPLETECONNECTEDSTRINGCOMBO: {
//        LinkedHashMap<String, List<String>> valuesMap = TagsManager.extractComboValuesMap(jsonObject);
//        addedView = FormUtilities.addAutoCompleteConnectedComboView(activity, mainView, label, value, valuesMap,
//            constraintDescription);
//        break;
//      }
//      case TYPE_ONETOMANYSTRINGCOMBO:
//        LinkedHashMap<String, List<NamedList<String>>> oneToManyValuesMap = TagsManager.extractOneToManyComboValuesMap(jsonObject);
//        addedView = FormUtilities.addOneToManyConnectedComboView(activity, mainView, label, value, oneToManyValuesMap,
//            constraintDescription);
//        break;
//      case TYPE_STRINGMULTIPLECHOICE: {
//        JSONArray comboItems = TagsManager.getComboItems(jsonObject);
//        String[] itemsArray = TagsManager.comboItems2StringArray(comboItems);
//        addedView = FormUtilities.addMultiSelectionView(activity, mainView, label, value, itemsArray,
//            constraintDescription);
//        break;
//      }
    case TYPE_PICTURES:
      {
        return ListTile(
          leading: icon,
          title: PicturesWidget(noteId, userId, itemMap, label, position),
        );
        break;
      }
    case TYPE_IMAGELIB:
      {
        return ListTile(
          leading: icon,
          title: PicturesWidget(noteId, userId, itemMap, label, position,
              fromGallery: true),
        );
        break;
      }
//      case TYPE_SKETCH:
//        addedView = FormUtilities.addSketchView(noteId, this, requestCode, mainView, label, value, constraintDescription);
//        break;
//      case TYPE_MAP:
//        if (value.length() <= 0) {
//          // need to read image
//          File tempDir = ResourcesManager.getInstance(activity).getTempDir();
//          File tmpImage = new File(tempDir, LibraryConstants.TMPPNGIMAGENAME);
//          if (tmpImage.exists()) {
//            byte[][] imageAndThumbnailFromPath = ImageUtilities.getImageAndThumbnailFromPath(tmpImage.getAbsolutePath(), 1);
//            Date date = new Date();
//            String mapImageName = ImageUtilities.getMapImageName(date);
//
//            IImagesDbHelper imageHelper = DefaultHelperClasses.getDefaulfImageHelper();
//            long imageId = imageHelper.addImage(longitude, latitude, -1.0, -1.0, date.getTime(), mapImageName, imageAndThumbnailFromPath[0], imageAndThumbnailFromPath[1], noteId);
//            value = "" + imageId;
//          }
//        }
//        addedView = FormUtilities.addMapView(activity, mainView, label, value, constraintDescription);
//        break;
//      case TYPE_NFCUID:
//        addedView = new GNfcUidView(this, null, requestCode, mainView, label, value, constraintDescription);
//        break;
    case TYPE_HIDDEN:
      break;
    default:
      print("Type non implemented yet: $type");
      break;
  }

  return null;
}

class CheckboxWidget extends StatefulWidget {
  var _itemMap;
  final String _label;

  CheckboxWidget(this._itemMap, this._label);

  @override
  _CheckboxWidgetState createState() => _CheckboxWidgetState();
}

class _CheckboxWidgetState extends State<CheckboxWidget> {
  @override
  Widget build(BuildContext context) {
    dynamic value = ""; //$NON-NLS-1$
    if (widget._itemMap.containsKey(TAG_VALUE)) {
      value = widget._itemMap[TAG_VALUE].trim();
    }
    bool selected = value == 'true';

    return CheckboxListTile(
      title: SmashUI.normalText(widget._label,
          color: SmashColors.mainDecorationsDark),
      value: selected,
      onChanged: (value) {
        setState(() {
          widget._itemMap[TAG_VALUE] = "$value";
        });
      },
      controlAffinity:
          ListTileControlAffinity.trailing, //  <-- leading Checkbox
    );
  }
}

class ComboboxWidget extends StatefulWidget {
  var _itemMap;
  final String _label;

  ComboboxWidget(this._itemMap, this._label);

  @override
  ComboboxWidgetState createState() => ComboboxWidgetState();
}

class ComboboxWidgetState extends State<ComboboxWidget> {
  @override
  Widget build(BuildContext context) {
    String value = ""; //$NON-NLS-1$
    if (widget._itemMap.containsKey(TAG_VALUE)) {
      value = widget._itemMap[TAG_VALUE].trim();
    }

    var comboItems = TagsManager.getComboItems(widget._itemMap);
    List<String> itemsArray = TagsManager.comboItems2StringArray(comboItems);
    var items = itemsArray
        .map(
          (itemName) => new DropdownMenuItem(
            value: itemName,
            child: new Text(itemName),
          ),
        )
        .toList();

    return Row(
      children: <Widget>[
        Flexible(
          child: Padding(
            padding: SmashUI.defaultRigthPadding(),
            child: SmashUI.normalText(widget._label,
                color: SmashColors.mainDecorationsDark),
          ),
        ),
        Flexible(
          child: Container(
            padding: EdgeInsets.only(
                left: SmashUI.DEFAULT_PADDING, right: SmashUI.DEFAULT_PADDING),
            decoration: BoxDecoration(
              shape: BoxShape.rectangle,
              border: Border.all(
                color: SmashColors.mainDecorations,
              ),
            ),
            child: DropdownButton(
              value: value,
              isExpanded: true,
              items: items,
              onChanged: (selected) {
                setState(() {
                  widget._itemMap[TAG_VALUE] = selected;
                });
              },
            ),
          ),
        ),
      ],
    );
  }
}

class DynamicStringWidget extends StatefulWidget {
  var _itemMap;
  final String _label;

  DynamicStringWidget(this._itemMap, this._label);

  @override
  DynamicStringWidgetState createState() => DynamicStringWidgetState();
}

class DynamicStringWidgetState extends State<DynamicStringWidget> {
  @override
  Widget build(BuildContext context) {
    String value = ""; //$NON-NLS-1$
    if (widget._itemMap.containsKey(TAG_VALUE)) {
      value = widget._itemMap[TAG_VALUE].trim();
    }
    List<String> valuesSplit = value.trim().split(";");
    valuesSplit.removeWhere((s) => s.trim().isEmpty);

    return Tags(
      textField: TagsTextFiled(
        width: 1000,
        hintText: "add new string",
        textStyle: TextStyle(fontSize: SmashUI.NORMAL_SIZE),
        onSubmitted: (String str) {
          valuesSplit.add(str);
          setState(() {
            widget._itemMap[TAG_VALUE] = valuesSplit.join(";");
          });
        },
      ),
      verticalDirection: VerticalDirection.up,
      // text box before the tags
      alignment: WrapAlignment.start,
      // text box aligned left
      itemCount: valuesSplit.length,
      // required
      itemBuilder: (int index) {
        final item = valuesSplit[index];

        return ItemTags(
          key: Key(index.toString()),
          index: index,
          title: item,
          active: true,
          customData: item,
          textStyle: TextStyle(
            fontSize: SmashUI.NORMAL_SIZE,
          ),
          combine: ItemTagsCombine.withTextBefore,
          pressEnabled: true,
          image: null,
          icon: null,
          activeColor: SmashColors.mainDecorations,
          highlightColor: SmashColors.mainDecorations,
          color: SmashColors.mainDecorations,
          textActiveColor: SmashColors.mainBackground,
          textColor: SmashColors.mainBackground,
          removeButton: ItemTagsRemoveButton(),
          onRemoved: () {
            // Remove the item from the data source.
            setState(() {
              valuesSplit.removeAt(index);
              String saveValue = valuesSplit.join(";");
              widget._itemMap[TAG_VALUE] = saveValue;
            });
          },
          onPressed: (item) {
//            var removed = valuesSplit.removeAt(index);
//            valuesSplit.insert(0, removed);
//            String saveValue = valuesSplit.join(";");
//            setState(() {
//              widget._itemMap[TAG_VALUE] = saveValue;
//            });
          },
          onLongPressed: (item) => print(item),
        );
      },
    );
  }
}

class DatePickerWidget extends StatefulWidget {
  var _itemMap;
  final String _label;

  DatePickerWidget(this._itemMap, this._label);

  @override
  DatePickerWidgetState createState() => DatePickerWidgetState();
}

class DatePickerWidgetState extends State<DatePickerWidget> {
  @override
  Widget build(BuildContext context) {
    String value = ""; //$NON-NLS-1$
    if (widget._itemMap.containsKey(TAG_VALUE)) {
      value = widget._itemMap[TAG_VALUE].trim();
    }
    DateTime dateTime;
    if (value.isNotEmpty) {
      try {
        dateTime = TimeUtilities.ISO8601_TS_DAY_FORMATTER.parse(value);
      } catch (e) {
        // ignor eand set to now
      }
    }
    if (dateTime == null) {
      dateTime = DateTime.now();
    }

    return Center(
      child: FlatButton(
          onPressed: () {
//            DatePicker.showDatePicker(
//              context,
//              showTitleActions: true,
//              onChanged: (date) {},
//              onConfirm: (date) {
//                String day =
//                TimeUtilities.ISO8601_TS_DAY_FORMATTER.format(date);
//                setState(() {
//                  widget._itemMap[TAG_VALUE] = day;
//                });
//              },
//              currentTime: dateTime,
//            );
          },
          child: Center(
            child: Padding(
              padding: SmashUI.defaultPadding(),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Padding(
                    padding: SmashUI.defaultRigthPadding(),
                    child: Icon(
                      MdiIcons.calendar,
                      color: SmashColors.mainDecorations,
                    ),
                  ),
                  SmashUI.normalText(
                      value.isNotEmpty
                          ? "${widget._label}: $value"
                          : widget._label,
                      color: SmashColors.mainDecorations,
                      bold: true),
                ],
              ),
            ),
          )),
    );
  }
}

class TimePickerWidget extends StatefulWidget {
  var _itemMap;
  final String _label;

  TimePickerWidget(this._itemMap, this._label);

  @override
  TimePickerWidgetState createState() => TimePickerWidgetState();
}

class TimePickerWidgetState extends State<TimePickerWidget> {
  @override
  Widget build(BuildContext context) {
    String value = ""; //$NON-NLS-1$
    if (widget._itemMap.containsKey(TAG_VALUE)) {
      value = widget._itemMap[TAG_VALUE].trim();
    }
    DateTime dateTime;
    if (value.isNotEmpty) {
      try {
        dateTime = TimeUtilities.ISO8601_TS_TIME_FORMATTER.parse(value);
      } catch (e) {
        // ignore and set to now
      }
    }
    if (dateTime == null) {
      dateTime = DateTime.now();
    }

    return Center(
      child: FlatButton(
          onPressed: () {
//            DatePicker.showTimePicker(
//              context,
//              showTitleActions: true,
//              onChanged: (date) {},
//              onConfirm: (date) {
//                String time =
//                    TimeUtilities.ISO8601_TS_TIME_FORMATTER.format(date);
//                setState(() {
//                  widget._itemMap[TAG_VALUE] = time;
//                });
//              },
//              currentTime: dateTime,
//            );
          },
          child: Center(
            child: Padding(
              padding: SmashUI.defaultPadding(),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Padding(
                    padding: SmashUI.defaultRigthPadding(),
                    child: Icon(
                      MdiIcons.clock,
                      color: SmashColors.mainDecorations,
                    ),
                  ),
                  SmashUI.normalText(
                      value.isNotEmpty
                          ? "${widget._label}: $value"
                          : widget._label,
                      color: SmashColors.mainDecorations,
                      bold: true),
                ],
              ),
            ),
          )),
    );
  }
}

class PicturesWidget extends StatefulWidget {
  var _itemMap;
  final String _label;
  dynamic _position;
  int _noteId;
  int _userId;
  bool fromGallery;

  PicturesWidget(
      this._noteId, this._userId, this._itemMap, this._label, this._position,
      {this.fromGallery = false});

  @override
  PicturesWidgetState createState() => PicturesWidgetState();
}

class PicturesWidgetState extends State<PicturesWidget> {
  @override
  Widget build(BuildContext context) {
    String IMAGE_ID_SEPARATOR = ";";
    List<String> imageSplit = [];
    String value = ""; //$NON-NLS-1$
    if (widget._itemMap.containsKey(TAG_VALUE)) {
      value = widget._itemMap[TAG_VALUE].trim();
    }
    if (value.isNotEmpty) {
      imageSplit = value.split(IMAGE_ID_SEPARATOR);
    }

    List<Widget> thumbList = [];
    for (int i = 0; i < imageSplit.length; i++) {
      var originalImageDataId = int.parse(imageSplit[i]);

      String url = "$API_IMAGEDATA/${widget._userId}/$originalImageDataId";
      NetworkImageWidget niW = NetworkImageWidget(url, 400);

      thumbList.add(Padding(
        padding: const EdgeInsets.all(5.0),
        child: niW,
      ));
    }

    return SingleChildScrollView(
      scrollDirection: Axis.vertical,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: thumbList,
      ),
    );
  }
}
