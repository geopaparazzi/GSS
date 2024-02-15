import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_map/src/layer/tile_layer/tile_layer.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/network.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/views/map_view.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';
import 'package:smashlibs/generated/l10n.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

export 'package:flutter_map_marker_cluster/flutter_map_marker_cluster.dart';

void main() {
  runApp(GssApp());
}

class GssApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MapstateModel()),
        ChangeNotifierProvider(create: (_) => FilterStateModel()),
        ChangeNotifierProvider(create: (_) => AttributesTableStateModel()),
        ChangeNotifierProvider(create: (_) => FormHandlerState()),
      ],
      child: MaterialApp(
        localizationsDelegates: [
          // SL.delegate,
          // IEL.delegate,
          SLL.delegate,
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        supportedLocales: [
          const Locale('en', ''),
        ],
        routes: {'/': (context) => MainPage()},
        title: TITLE,
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: SmashColors.mainDecorations,
            background: SmashColors.mainBackground,
            primary: SmashColors.mainDecorations,
            // secondary: SmashColors.mainSelection,
            brightness: Brightness.light,
          ),
          appBarTheme: AppBarTheme(
            backgroundColor: SmashColors.mainDecorations,
            foregroundColor: SmashColors.mainBackground,
            titleTextStyle: TextStyle(
              color: SmashColors.mainBackground,
              fontWeight: FontWeight.bold,
              fontSize: SmashUI.BIG_SIZE,
            ),
          ),
          bottomAppBarTheme: BottomAppBarTheme(
              color: SmashColors.mainBackground,
              surfaceTintColor: SmashColors.mainBackground),
          tabBarTheme: TabBarTheme(
              labelColor: SmashColors.mainBackground,
              unselectedLabelColor: Colors.grey.shade400),
          cardTheme: CardTheme(
            surfaceTintColor: SmashColors.mainBackground,
            color: SmashColors.mainBackground,
          ),
          drawerTheme: DrawerThemeData(
            backgroundColor: SmashColors.mainBackground,
            surfaceTintColor: SmashColors.mainBackground,
          ),
          dialogTheme: DialogTheme(
            backgroundColor: SmashColors.mainBackground,
            shape: BeveledRectangleBorder(
              borderRadius: BorderRadius.circular(10.0),
              side: BorderSide(
                color: SmashColors.mainDecorations,
                width: 2.0,
              ),
            ),
            surfaceTintColor: SmashColors.mainBackground,
            titleTextStyle: TextStyle(
              color: SmashColors.mainDecorations,
              fontWeight: FontWeight.bold,
              fontSize: SmashUI.BIG_SIZE,
            ),
          ),
          visualDensity: VisualDensity.adaptivePlatformDensity,
        ),
//          ThemeData(
//             primarySwatch: SmashColors.mainDecorationsMc,
//             accentColor: SmashColors.mainSelectionMc,
//             canvasColor: SmashColors.mainBackground,
//             brightness: Brightness.light,
//             fontFamily: 'Arial',
//             inputDecorationTheme: InputDecorationTheme(
//               border: const OutlineInputBorder(
//                 borderSide: BorderSide(
//                     color: Color.fromARGB(
//                         255,
//                         SmashColors.mainDecorationsDarkR,
//                         SmashColors.mainDecorationsDarkG,
//                         SmashColors.mainDecorationsDarkB)),
//               ),
//               enabledBorder: OutlineInputBorder(
//                 borderSide: BorderSide(
//                     color: Color.fromARGB(
//                         255,
//                         SmashColors.mainDecorationsDarkR,
//                         SmashColors.mainDecorationsDarkG,
//                         SmashColors.mainDecorationsDarkB)),
//               ),
//               disabledBorder: OutlineInputBorder(
//                 borderSide:
//                     BorderSide(color: Color.fromARGB(255, 128, 128, 128)),
//               ),
//               focusedBorder: const OutlineInputBorder(
//                 borderSide: BorderSide(
//                     color: Color.fromARGB(
//                         255,
//                         SmashColors.mainSelectionBorderR,
//                         SmashColors.mainSelectionBorderG,
//                         SmashColors.mainSelectionBorderB)),
//               ),

// //            labelStyle: const TextStyle(
// //              color: Color.fromARGB(255, 128, 128, 128),
// //            ),
//             )),
      ),
    );
  }
}

class MainPage extends StatefulWidget {
  MainPage({Key? key}) : super(key: key);

  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  String _errortext = "";
  WebProject? selectedProject;

  @override
  Widget build(BuildContext context) {
    var _isLogged = SmashSession.isLogged();
    if (_isLogged) {
      return FutureBuilder(
        builder: (context, projectSnap) {
          if (projectSnap.hasError) {
            return SmashUI.errorWidget(projectSnap.error.toString());
          } else if (projectSnap.connectionState == ConnectionState.none ||
              projectSnap.data == null) {
            return SmashCircularProgress(label: "processing...");
          }

          Widget widget = projectSnap.data as Widget;
          return widget;
        },
        future: getMainWidget(context),
      );
    } else {
      return FutureBuilder(
        builder: (context, projectSnap) {
          if (projectSnap.hasError) {
            return SmashUI.errorWidget(projectSnap.error.toString());
          } else if (projectSnap.connectionState == ConnectionState.none ||
              projectSnap.data == null) {
            return SmashCircularProgress(label: "gathering projects...");
          }

          Widget widget = projectSnap.data as Widget;
          return widget;
        },
        future: getLoginWidget(context),
      );
    }
  }

  Future<Widget> getMainWidget(BuildContext context) async {
    await GpPreferences().initialize();
    Map<String, Widget> layersMap = await WebServerApi.getBackGroundLayers();
    MapstateModel mapState = Provider.of<MapstateModel>(context, listen: false);
    mapState.setBackgroundLayers(layersMap);
    return MainMapView();
  }

  Future<Scaffold> getLoginWidget(BuildContext context) async {
    List<WebProject> projectsList = await WebServerApi.getProjects();

    if (projectsList.length == 0) {
      return Scaffold(
        body: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                height: 200.0,
                child: Image.asset(
                  "assets/smash_logo.png",
                  fit: BoxFit.contain,
                ),
              ),
              SizedBox(height: 45.0),
              SmashUI.errorWidget(
                  "No project could be retrieved from the server. Check the server configuration or contact your administrator."),
            ],
          ),
        ),
      );
    }

    if (selectedProject == null) {
      selectedProject = projectsList[0];
    }

    var projectsCombo = DropdownButton<WebProject>(
      isExpanded: true,
      items: projectsList.map((WebProject value) {
        return DropdownMenuItem<WebProject>(
          value: value,
          child: Text(value.name!),
        );
      }).toList(),
      value: selectedProject,
      onChanged: (newProject) {
        selectedProject = newProject;
        setState(() {});
      },
    );

    TextStyle loginTextStyle = TextStyle(fontFamily: 'Arial', fontSize: 20.0);

    TextEditingController userNameController = new TextEditingController();
    final userNameField = TextField(
      controller: userNameController,
      obscureText: false,
      style: loginTextStyle,
      decoration: InputDecoration(
          contentPadding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
          hintText: "Username",
          border:
              OutlineInputBorder(borderRadius: BorderRadius.circular(32.0))),
    );

    TextEditingController passwordController = new TextEditingController();
    final passwordField = TextField(
      controller: passwordController,
      obscureText: true,
      style: loginTextStyle,
      decoration: InputDecoration(
          contentPadding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
          hintText: "Password",
          border:
              OutlineInputBorder(borderRadius: BorderRadius.circular(32.0))),
    );

    final loginButton = Material(
      elevation: 5.0,
      borderRadius: BorderRadius.circular(30.0),
      color: SmashColors.mainDecorationsDarker,
      child: MaterialButton(
        minWidth: MediaQuery.of(context).size.width,
        padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
        onPressed: () async {
          if (selectedProject == null) {
            _errortext = "Please select a project.";
          } else {
            String user = userNameController.text;
            String password = passwordController.text;
            var loginOk =
                await SmashSession.login(user, password, selectedProject!);
            if (loginOk != null) {
              _errortext = loginOk;
            }
          }
          setState(() {});
        },
        child: Text("Login",
            textAlign: TextAlign.center,
            style: loginTextStyle.copyWith(
                color: Colors.white, fontWeight: FontWeight.bold)),
      ),
    );

    return Scaffold(
        body: SingleChildScrollView(
      child: Center(
        child: Container(
          color: Colors.white,
          constraints: BoxConstraints(maxWidth: 400.0),
          child: Padding(
            padding: const EdgeInsets.all(36.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                SizedBox(
                  height: 200.0,
                  child: Image.asset(
                    "assets/smash_logo.png",
                    fit: BoxFit.contain,
                  ),
                ),
                SizedBox(height: 45.0),
                Container(
                  decoration: BoxDecoration(
                      borderRadius: BorderRadius.all(Radius.circular(5)),
                      border: Border.all(
                          color: SmashColors.mainDecorations, width: 1)),
                  child: Padding(
                    padding: const EdgeInsets.only(
                        left: 8.0, right: 8.0, top: 3, bottom: 3),
                    child: projectsCombo,
                  ),
                ),
                SizedBox(height: 25.0),
                userNameField,
                SizedBox(height: 25.0),
                passwordField,
                SizedBox(height: 35.0),
                loginButton,
                SizedBox(height: 15.0),
                SmashUI.normalText(_errortext,
                    color: SmashColors.mainSelectionBorder)
              ],
            ),
          ),
        ),
      ),
    ));
  }
}
