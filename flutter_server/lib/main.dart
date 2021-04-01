import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/mapview.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/models.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/session.dart';
import 'package:flutter_server/com/hydrologis/gss/libs/variables.dart';
import 'package:provider/provider.dart';
import 'package:smashlibs/smashlibs.dart';

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
      ],
      child: MaterialApp(
        title: TITLE,
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
            primarySwatch: SmashColors.mainDecorationsMc,
            accentColor: SmashColors.mainSelectionMc,
            canvasColor: SmashColors.mainBackground,
            brightness: Brightness.light,
            fontFamily: 'Arial',
            inputDecorationTheme: InputDecorationTheme(
              border: const OutlineInputBorder(
                borderSide: BorderSide(
                    color: Color.fromARGB(
                        255,
                        SmashColors.mainDecorationsDarkR,
                        SmashColors.mainDecorationsDarkG,
                        SmashColors.mainDecorationsDarkB)),
              ),
              enabledBorder: OutlineInputBorder(
                borderSide: BorderSide(
                    color: Color.fromARGB(
                        255,
                        SmashColors.mainDecorationsDarkR,
                        SmashColors.mainDecorationsDarkG,
                        SmashColors.mainDecorationsDarkB)),
              ),
              disabledBorder: OutlineInputBorder(
                borderSide:
                    BorderSide(color: Color.fromARGB(255, 128, 128, 128)),
              ),
              focusedBorder: const OutlineInputBorder(
                borderSide: BorderSide(
                    color: Color.fromARGB(
                        255,
                        SmashColors.mainSelectionBorderR,
                        SmashColors.mainSelectionBorderG,
                        SmashColors.mainSelectionBorderB)),
              ),

//            labelStyle: const TextStyle(
//              color: Color.fromARGB(255, 128, 128, 128),
//            ),
            )),
        home: MainPage(), //LoginScreen(), //
      ),
    );
  }
}

class MainPage extends StatefulWidget {
  MainPage({Key key}) : super(key: key);

  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  String _errortext = "";
  @override
  Widget build(BuildContext context) {
    var _isLogged = SmashSession.isLogged();
    if (_isLogged) {
      return MainMapView();
    } else {
      return loadLoginView(context);
    }
  }

  Scaffold loadLoginView(BuildContext context) {
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
          String user = userNameController.text;
          String password = passwordController.text;
          var loginOk = await SmashSession.login(user, password);
          if (!loginOk) {
            _errortext = "The used credentials are wrong.";
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
                userNameField,
                SizedBox(height: 25.0),
                passwordField,
                SizedBox(
                  height: 35.0,
                ),
                loginButton,
                SizedBox(
                  height: 15.0,
                ),
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
