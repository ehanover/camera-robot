//import 'package:flutter/material.dart';
//
//void main() => runApp(MyApp());
//
//class MyApp extends StatelessWidget {
//  @override
//  Widget build(BuildContext context) {
//    return MaterialApp(
//      title: 'Camera Stream',
//      home: MyForm(),
//    );
//  }
//}
//
//// Define a custom Form widget.
//class MyForm extends StatefulWidget {
//  @override
//  _MyFormState createState() => _MyFormState();
//}
//
//
//class _MyFormState extends State<MyForm> {
//
//  final myController = TextEditingController(text: "http://");
//
//  @override
//  void dispose() {
//    myController.dispose();
//    super.dispose();
//  }
//
//  @override
//  Widget build(BuildContext context) {
//    return Scaffold(
//      appBar: AppBar(
//        title: Text('Camera Stream Setup'),
//      ),
//      body: Padding(
//        padding: const EdgeInsets.all(16.0),
//        child: Column(
//          children: <Widget>[
//            // Text("IP address:"),
//            TextField(
//              controller: myController,
//              decoration: new InputDecoration(hintText: 'Username'),
//            ),
//          ],
//        )
//      ),
//      floatingActionButton: FloatingActionButton(
//        onPressed: () {
////          Navigator.push(
////            context,
////            MaterialPageRoute(
////               builder: (context) => DetailScreen(todo: todos[index]),
////            ),
////          );
//        },
//        child: Icon(Icons.arrow_forward),
//      ),
//    );
//  }
//}