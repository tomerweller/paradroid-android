#include <Wire.h>
#include <Servo.h>

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

//led messages
#define READY_TO_DROP_MSG 0x10

//navigation messages
#define SERV_RIGHT_MSG    0x22
#define SERV_LEFT_MSG     0x23
#define PULL_RIGHT_MSG    0x24
#define PULL_LEFT_MSG     0x25
#define FLARE_MSG         0x26
#define RESET_MSG         0x27
#define PERM_FLARE_MSG    0x28

//outoging messages
#define RANGE_FINDER_MSG  0x31
#define PHOTO_SENSOR_MSG  0x32

//led pins
#define LED_RED	         48
#define LED_GREEN        46

//reciever pins
#define LEFT_JOY_VER_PIN   3 // +
#define RIGHT_JOY_SIDE_PIN 4 // +
#define RIGHT_JOY_VER_PIN  5 //is dead?
#define LEFT_JOY_SIDE_PIN  6 // +
#define GEARpin            7

//servo pins
#define SERV_RIGHT	  9
#define SERV_LEFT	  8
 
//sensor pins
#define RANGE_FINDER	  A3
#define PHOTO_SENSOR	  A2

//angles
#define SERVO_TOP_ANGLE      40
#define SERVO_BOTTOM_ANGLE  120
#define SERVO_REF           158

//navigation
#define PULL_TIME         2000; 
#define WAIT_TIME         1500; 

//general
#define DELAY_INTERVAL	  100
#define GEAR_THRESHOLD    1500
#define LIGHT_THRESHOLD   3

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

Servo servoRight, servoLeft;
enum STATE {IDLE, PULL, WAIT} state; 
long timer;

void reset();

void setup();
void loop();
void parseMsg(byte msg[]);
void parseRcvr();
bool isManualOverride(); 
void changeServoAngle(Servo servo, byte angle);
void changeLed(byte led, byte value);
void returnMsg();
void setReadyLeds(byte ready);

void resetServos(byte which){ //0=TOP, 1=BOTTOM]
  state = IDLE; 
  if (which==0) { 
    changeServoRightAngle(SERVO_TOP_ANGLE);
    changeServoLeftAngle(SERVO_TOP_ANGLE);
  } else {
    changeServoRightAngle(SERVO_BOTTOM_ANGLE);
    changeServoLeftAngle(SERVO_BOTTOM_ANGLE);    
  }
}

void setup()
{
  Serial.begin(115200);
  Serial.print("\r\nStart");

  servoRight.attach(SERV_RIGHT);
  servoLeft.attach(SERV_LEFT);
  resetServos(1); 
  
  state = IDLE;
  
  pinMode(GEARpin ,INPUT);
  pinMode(RIGHT_JOY_SIDE_PIN ,INPUT);
  pinMode(LEFT_JOY_SIDE_PIN ,INPUT);
  pinMode(LEFT_JOY_VER_PIN ,INPUT);
  pinMode(RIGHT_JOY_VER_PIN ,INPUT);
  
  pinMode(LED_GREEN, OUTPUT); 
  pinMode(LED_RED, OUTPUT);
  setReadyLeds(0);

  isManualOverride();
  
  acc.powerOn();
}

void loop()
{
  byte err;
  byte idle;
  byte msg[2];
 
  if (acc.isConnected()) { //No Manual Override and Accessory is connected	
      if (!isManualOverride()){
        int len = acc.read(msg, sizeof(msg), 1);
    
        if (len > 0) { //if there's a msg parse it          
          parseMsg(msg);
        }
        
        // send back information
        returnMsg();
        
      } else{
        parseRcvr();
      }
      
      //Pulling Shit
      if (state!=IDLE){
        Serial.print("\nState is not IDLE");
        double delta = millis() - timer;
        
        int pullTime = PULL_TIME;
        if (state==PULL && delta >= pullTime){
          resetServos(0); 
          timer = millis(); 
          state=WAIT;        
        }      
        
        int waitTime = WAIT_TIME;      
        if (state==WAIT && delta >= waitTime){          
          timer = millis(); 
          state=IDLE;
        }
        
      }
      
  } else { //No Manual Override and no Accessory
    //  Do Something? 
  }

	delay(DELAY_INTERVAL);
}

void parseMsg(byte msg[]){
        Serial.print("\r\nParsing command of type :");
        Serial.print((int)msg[0]);
        Serial.print(" With Value :"); 
        Serial.print((int)msg[1]);
        
//        TODO: think
//        if (state = FLARE){
//          resetServos();
//          state = IDLE; 
//        }
        
        //
        if (msg[0] == SERV_RIGHT_MSG) {
		changeServoRightAngle(msg[1]);							
	} else if (msg[0] == SERV_LEFT_MSG){
		changeServoLeftAngle(msg[1]);											
	} else if (msg[0] == READY_TO_DROP_MSG){
		setReadyLeds(msg[1]);	
	} else if (msg[0] == PULL_RIGHT_MSG){
		pullRight();
	} else if (msg[0] == PULL_LEFT_MSG){
		pullLeft();
	} else if (msg[0] == FLARE_MSG){
		flare();
	} else if (msg[0] == RESET_MSG){
                resetServos(msg[1]); 
        } else if (msg[0] == PERM_FLARE_MSG){
                permFlare(); 
        }
}

byte angleFromRcvr(int pulse){
  if (pulse<1600) 
    return SERVO_TOP_ANGLE;
  else
    return SERVO_BOTTOM_ANGLE;

//  if (pulse<1500) 
//    return SERVO_TOP_ANGLE;
  
  
//  return map(pulse, 1500, 1700, SERVO_TOP_ANGLE, SERVO_BOTTOM_ANGLE);
}

void parseRcvr(){
//  static int lastRightAngle = 0, lastLeftAngle = 0;  
 
  //update read
  int rightPulse = pulseIn(RIGHT_JOY_SIDE_PIN, HIGH);
  int leftPulse  = pulseIn(LEFT_JOY_SIDE_PIN, HIGH);
  int flarePulse = pulseIn(LEFT_JOY_VER_PIN, HIGH);
  
//  int testPulse = pulseIn(6, HIGH);
  Serial.print("\n Pulse : ");
  Serial.print(flarePulse);
  
  if (flarePulse>1750)
    flare();
  if (rightPulse>1600)
    pullRight();
  if (leftPulse>1600)
    pullLeft();
 }

bool isManualOverride(){
  int value = pulseIn(GEARpin, HIGH);
  Serial.print("\nGear :");
  Serial.print(value);
  return pulseIn(GEARpin, HIGH)>GEAR_THRESHOLD;
}

void changeServoLeftAngle(int angle){
  Serial.print("\nServo Left :");
  Serial.print(angle);
  servoLeft.write(angle);
}

void changeServoRightAngle(int angle){
  Serial.print("\nServo Right :");
  Serial.print(SERVO_REF-angle);
  servoRight.write(SERVO_REF-angle);
}

void pull(void changeAngle(int)){
  Serial.print("\nState is : ");
  Serial.print(state);
  if (state!=IDLE){
    return;
  }
  else{
    timer = millis();
    state = PULL; 
    changeAngle(SERVO_BOTTOM_ANGLE);
  }
}

void pullRight(){
  Serial.print("\nPull Right.");  
  pull(changeServoRightAngle);
}

void pullLeft(){
  Serial.print("\nPull Left.");  
  pull(changeServoLeftAngle);
}

void changeLed(byte led, byte value){
  digitalWrite(led, value==0 ? HIGH : LOW);
}

void setReadyLeds(byte ready){
  if (ready==0){
    digitalWrite(LED_GREEN, LOW);
    digitalWrite(LED_RED, HIGH);
  } else {
    digitalWrite(LED_GREEN, HIGH);
    digitalWrite(LED_RED, LOW);
  }
} 
  
void flare(){
  timer = millis();
  state = PULL;  
  changeServoRightAngle(SERVO_BOTTOM_ANGLE);
  changeServoLeftAngle(SERVO_BOTTOM_ANGLE);
}

void permFlare(){
  timer = millis();
  changeServoRightAngle(SERVO_BOTTOM_ANGLE);
  changeServoLeftAngle(SERVO_BOTTOM_ANGLE);  
}

void returnMsg(){
  static byte count = 0;
  uint16_t val;
  byte msg[2];
  
//  switch (count++ % 2) {
//    case 0:
      val = analogRead(RANGE_FINDER);
      Serial.print("\nValue of Range Finder: ");
      Serial.print(val);
      
      msg[0] = map(val,0, 512, -128, 127);
      Serial.print(" Message : ");
      Serial.print((int)msg[0]); 
//      break;
//    case 1:
      val = analogRead(PHOTO_SENSOR);
      Serial.print("\nValue of Photo Sensor: ");
      Serial.print(val);
      msg[1] = val > LIGHT_THRESHOLD ? 1 : 0;  
      Serial.print(" Message : ");
      Serial.print((int)msg[1]); 
//      break;
//  }
  acc.write(msg, 2);
}
