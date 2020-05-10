import 'dart:async';
import 'dart:typed_data';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
//import 'package:path/path.dart' show join;
//import 'package:path_provider/path_provider.dart';
//import 'package:simple_permissions/simple_permissions.dart';
//import 'package:permission_handler/permission_handler.dart';

Future<void> main() async {

  WidgetsFlutterBinding.ensureInitialized(); // Ensure that plugin services are initialized so that `availableCameras()` can be called before `runApp()`
  final cameras = await availableCameras(); // Obtain a list of the available cameras on the device.
  final firstCamera = cameras.first; // Get a specific camera from the list of available cameras.

  runApp(
    MaterialApp(
      theme: ThemeData.dark(),
      home: TakePictureScreen(
        camera: firstCamera, // Pass the appropriate camera to the TakePictureScreen widget.
      ),
    ),
  );
}

// A screen that allows users to take a picture using a given camera.
class TakePictureScreen extends StatefulWidget {
  final CameraDescription camera;

  const TakePictureScreen({
    Key key,
    @required this.camera,
  }) : super(key: key);

  @override
  TakePictureScreenState createState() => TakePictureScreenState();
}

class TakePictureScreenState extends State<TakePictureScreen> {
  static const platformMethodChannel = const MethodChannel("com.example.camera_stream_test3");
  CameraController _controller;
  Future<void> _initializeControllerFuture;
  bool stopped;

  @override
  void initState() {
    super.initState();
    stopped = false;

//    SimplePermissions.requestPermission(Permission. WriteExternalStorage).then((PermissionStatus s) {
//      if (s == PermissionStatus.authorized){
//        // code of read or write file in external storage (SD card)
//        print("ASDF got write permission");
//      }
//    });

    // Map<PermissionGroup, PermissionStatus> permissions = await
    // PermissionHandler().requestPermissions([PermissionGroup.storage]);

    // String address = "http://192.168.0.225:5000";
    String address = "http://1faaacb0.ngrok.io";
    platformMethodChannel.invokeMethod("streamStart", {"address":address}); // TODO make sure address is correct
    // To display the current output from the Camera, create a CameraController.
    _controller = CameraController(
      widget.camera, // Get a specific camera from the list of available cameras.
      ResolutionPreset.low, // Define the resolution to use. TODO try changing resolution
    );

    _initializeControllerFuture = _controller.initialize(); // Next, initialize the controller. This returns a Future
  }

  @override
  void dispose() {
    // Dispose of the controller when the widget is disposed.
    stopped = true;
    if(platformMethodChannel != null)
      platformMethodChannel.invokeMethod("streamClose");
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('View the camera')),
      // Wait until the controller is initialized before displaying the camera preview. Use a FutureBuilder to display a loading spinner until the controller has finished initializing.
      body: FutureBuilder<void>(
        future: _initializeControllerFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.done) { // If the Future is complete, display the preview.
            _controller.startImageStream((CameraImage img) {
              _streamImage(img);
            });
            return CameraPreview(_controller);
          } else {
            return Center(child: CircularProgressIndicator());
          }
        },
      ),
      floatingActionButton: FloatingActionButton(
          child: Icon(Icons.block),
          onPressed: () {
            stopped = true;
            platformMethodChannel.invokeMethod('streamClose');
            // _controller.startImageStream(null);
            // TODO navigate to start page
          }
      ),
    );
  }

  _streamImage(CameraImage img) { // format, height, width, planes
    if(stopped || platformMethodChannel == null)
      return;

    List<Uint8List> bytes = img.planes.map((plane) => plane.bytes).toList();
    platformMethodChannel.invokeMethod('streamImage', {"width": img.width, "height": img.height, "bytes": bytes});
  }

}
