import 'package:camera_robot/main.dart';
import 'package:flutter/material.dart';

import 'main.dart';
//import 'package:path/path.dart' show join;
//import 'package:path_provider/path_provider.dart';
//import 'package:simple_permissions/simple_permissions.dart';
import 'package:permission_handler/permission_handler.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Camera Robot',
      theme: ThemeData.dark(),
      home: MyForm(),
    );
  }
}

// Define a custom Form widget.
class MyForm extends StatefulWidget {
  @override
  _MyFormState createState() => _MyFormState();
}


class _MyFormState extends State<MyForm> {

  final controllerIp = TextEditingController(text: "http://192.168.0.225:5000");
  final controllerMs = TextEditingController(text: "700");

  @override
  void initState() {
    super.initState();

//    SimplePermissions.requestPermission(Permission. WriteExternalStorage).then((PermissionStatus s) {
//      if (s == PermissionStatus.authorized) {
//        // code of read or write file in external storage (SD card)
//        print("ASDF got write permission");
//      }
//    });
//    Map<PermissionGroup, PermissionStatus> permissions = await PermissionHandler().requestPermissions([PermissionGroup.storage]); // outdated?
    Permission.storage.request();
  }

  @override
  void dispose() {
    controllerIp.dispose();
    controllerMs.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Camera Robot Configuration'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: <Widget>[
            Padding(padding: EdgeInsets.all(32.0)),
            Text("Enter the required information and press the check button to continue"),
            TextField(
              controller: controllerIp,
              decoration: new InputDecoration(hintText: 'Server IP'),
            ),
            Padding(padding: EdgeInsets.all(16.0)),
            TextField(
              controller: controllerMs,
              keyboardType: TextInputType.number,
              decoration: new InputDecoration(hintText: 'Send time (ms)'),
            ),
          ],
        )
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
               builder: (context) => TakePictureScreen(address: controllerIp.text, sendTime: int.parse(controllerMs.text)),
            ),
          );
        },
        child: Icon(Icons.check),
      ),
    );
  }
}