#include <Wire.h>
// #include <Adafruit_MotorShield.h>
#include <SoftwareSerial.h>

String data = String(""); // https://www.arduino.cc/reference/en/language/variables/data-types/stringobject/
int ledL = 3;
int ledR = 11;
int light = 13;

void setup() {
  Serial.begin(9600);

  pinMode(ledL, OUTPUT);
  pinMode(ledR, OUTPUT);
  pinMode(light, OUTPUT);
}

void loop() {
  if(Serial.available() > 0) {
    char c = Serial.read();
    if(c == '|') {
      setLights(data);
      data = String("");
    } else if(c != '\n' && c != '\r') {
      data.concat(c);
    }
  }

}

void setLights(String d) {
  digitalWrite(light, HIGH);
  // 0255-010
  if(d.length() < 8) {
    analogWrite(ledL, 0);
    analogWrite(ledR, 0);
    return;
  }

  int l = d.substring(0, 4).toInt();
  int r = d.substring(4).toInt();

  if(l < 0){
    analogWrite(ledL, l*-1);
    // Serial.println("left negative");
  } else {
    analogWrite(ledL, l);
    // Serial.println("left set " + l);
  }
  
  if(r < 0){
    analogWrite(ledR, r*-1);
    // Serial.println("right negative");
  } else {
    analogWrite(ledR, r);
    // Serial.println("right set " + r);
  }
  digitalWrite(light, LOW);
  
}
