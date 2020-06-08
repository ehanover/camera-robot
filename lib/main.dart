import 'dart:async';
import 'dart:typed_data';

import 'package:camera/camera.dart';
import 'package:camera_robot/start.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
// import 'package:path_provider/path_provider.dart'; // Used for debug logging

// A screen that allows users to take a picture using a given camera.
class TakePictureScreen extends StatefulWidget {
  final String address;
  final int sendTime;

  TakePictureScreen({
    Key key,
    @required this.address,
    @required this.sendTime,
  }) : super(key: key);

  @override
  TakePictureScreenState createState() => TakePictureScreenState();
}

class TakePictureScreenState extends State<TakePictureScreen> {
  static const platformMethodChannel = const MethodChannel("com.example.camera_robot");
  CameraDescription camera;
  CameraController _controller;
  Future<void> _initAll;
  bool stopped;

  Future<void> _initCamera() async {
    WidgetsFlutterBinding.ensureInitialized(); // Ensure that plugin services are initialized so that `availableCameras()` can be called before `runApp()`
    final cameras = await availableCameras(); // Obtain a list of the available cameras on the device.
    print("There are ${cameras.length} cameras available");
    camera = cameras[2]; // Get a specific camera from the list of available cameras. Use wide angle camera! (cameras[0] is normal back-facing camera)
  }

  Future<void> _initController() async {
    _controller = CameraController( // To display the current output from the Camera, create a CameraController.
      camera,
      ResolutionPreset.low, // Can change the resolution here
    );
    _controller.initialize();
  }

  Future<void> _initAllFunc() async {
    _initCamera().then((value) {
      _initController();
    });
  }

  /*
  // Used for writing debug log data to a text file while the Arduino is attached
  // to the phone's USB port instead of the computer
  Future<void> _writeToFile(String val) async {
    String dirFolder = "/storage/self/primary/Download";
    String dirFile = "$dirFolder/camera-robot_log.txt";

    File f = File(dirFile);
    f.writeAsString(val);
    print("Successfully wrote data to file at $dirFile");
  }
  */

  @override
  void initState() {
    super.initState();
    stopped = false;
    _initAll = _initAllFunc();
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
        future: _initAll,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.done && stopped == false) { // If the Future is complete, display the preview.
            // platformMethodChannel.invokeMethod("myLog").then((value) {
            //   print("Trying to write value=$value");
            //   _writeToFile("$value");
            // });
            platformMethodChannel.invokeMethod("streamStart", {"address": widget.address, "sendTime": widget.sendTime}).then((val) {
              _controller.startImageStream((CameraImage img) {
                _streamImage(img);
              });
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
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => MyApp(),
              ),
            );
          }
      ),
    );
  }

  _streamImage(CameraImage img) {
    // CameraImage has properties format, height, width, planes
    if(stopped || platformMethodChannel == null)
      return;

    List<Uint8List> bytes = img.planes.map((plane) => plane.bytes).toList();
    platformMethodChannel.invokeMethod('streamImage', {"width": img.width, "height": img.height, "bytes": bytes});
  }

}
