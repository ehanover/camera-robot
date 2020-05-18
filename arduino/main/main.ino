// similar to sketchbook/new_shield_car

#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include "utility/Adafruit_PWMServoDriver.h"
#include <SoftwareSerial.h>

Adafruit_MotorShield AFMS = Adafruit_MotorShield();
Adafruit_DCMotor *leftMotor = AFMS.getMotor(3); // left
Adafruit_DCMotor *rightMotor = AFMS.getMotor(2); // right

//int fullSpeed = 210; // speed when going straight (f,b)
//int backSpeed = 170;
//int turnSpeed = 115; // speed when turning (l,r)
//int diagonalSpeed = 230;
//int diagonalOppositeSpeed = 100; // speed when going diagonally (g,i,h,j)

void setup(){
  mySerial.begin(9600);

  AFMS.begin();
  leftMotor->run(FORWARD);
  leftMotor->setSpeed(0);
  rightMotor->run(FORWARD);
  rightMotor->setSpeed(0);

}

void loop(){
  if(mySerial.available() > 0){
    char data = mySerial.read();
    // int 0-255
    // left motor first 4 bits, right motor remaining 4 bits
    // motorSpeed = (val - 2) * 18

    // int a = (data & 0xF0) >> 4;
    // int b = data & 0xF;

  

  }
}



void setLeft(int targetSpeed, String dir){
  if(dir == "FORWARD"){
    leftMotor->run(FORWARD);
  } else {
    leftMotor->run(BACKWARD);
  }
  leftMotor->setSpeed(targetSpeed);
}

void setRight(int targetSpeed, String dir){
  if(dir == "FORWARD"){
    rightMotor->run(FORWARD);
  } else {
    rightMotor->run(BACKWARD);
  }
  rightMotor->setSpeed(targetSpeed);
}
