// similar to sketchbook/new_shield_car

#include <Wire.h>
#include <Adafruit_MotorShield.h>
// #include "utility/Adafruit_PWMServoDriver.h"
#include <SoftwareSerial.h>

Adafruit_MotorShield AFMS = Adafruit_MotorShield();
Adafruit_DCMotor *leftMotor = AFMS.getMotor(3); // left
Adafruit_DCMotor *rightMotor = AFMS.getMotor(2); // right
int lightPin = 13;
String data = String("");

void setup(){
  Serial.begin(9600);

  AFMS.begin();
  leftMotor->run(FORWARD);
  leftMotor->setSpeed(0);
  rightMotor->run(FORWARD);
  rightMotor->setSpeed(0);

}

void loop(){
  if(Serial.available() > 0) {
    char c = Serial.read();
    if(c == '|') {
      setMotors(data);
      data = String("");
    } else if(c != '\n' && c != '\r') {
      data.concat(c);
    }
  }
  
}

void setMotors(String d) {
  digitalWrite(lightPin, HIGH);
  // 0255-010
  if(d.length() < 8) {
    setLeft(0, "FORWARD");
    setRight(0, "FORWARD");
    return;
  }

  int l = d.substring(0, 4).toInt();
  int r = d.substring(4).toInt();

  if(l < 0){
    setLeft(-l, "BACKWARD");
  } else {
    setLeft(l, "FORWARD");
  }
  
  if(r < 0){
    setRight(-r, "BACKWARD");
  } else {
    setRight(r, "FORWARD");
  }
  digitalWrite(lightPin, LOW);
  
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
