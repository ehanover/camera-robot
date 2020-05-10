# mobile-camera-robot
This app enables a robot car to carry a cell phone around in order to utilize the phone's camera and cellular capabilities. In theory, the car should be able to travel anywhere where the phone has a data connection.

This project has three components:
1. Server code that displays the camera from the phone on the robot, and that sends back driving commands
2. A phone app that uses sockets to stream the camera preview to the computer and receive the commands which are then sent to an Arduino connected via USB
3. Code running on the Arduino that receives commands from the phone and drives the robot's motors accordingly
