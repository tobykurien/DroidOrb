/*
 *  DroidOrb Android accessory
 * 
 *  This is free software. You can redistribute it and/or modify it under
 *  the terms of Creative Commons Attribution 3.0 United States License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by/3.0/us/ 
 *  or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 *  See Github project http://github.com/tobykurien/DroidOrb for latest
 */

#include <Wire.h>
#include <SPI.h>
#include <Adb.h>

#include "TimerOne.h"
#include "droid_orb.h"

// Adb connection.
Connection * phone;

// Elapsed time for ADC sampling
long lastTime;

// various LED variables
uint8_t red = 0;
uint8_t red_bright = 0;
uint8_t red_pulse = 0;
uint8_t green = 0;
uint8_t green_bright = 0;
uint8_t green_pulse = 0;
uint8_t blue = 0;
uint8_t blue_pulse = 0;
uint8_t blue_bright = 0;
uint8_t white_pulse = 0;

// State variables
bool red_dir = 1; // direction for pulsing, 0 = down, 1 = up
uint8_t red_pulse_val = 0; // value for pulsing
bool green_dir = 1; // direction for pulsing, 0 = down, 1 = up
uint8_t green_pulse_val = 0; // value for pulsing
bool blue_dir = 1; // direction for pulsing, 0 = down, 1 = up
uint8_t blue_pulse_val = 0; // value for pulsing

bool white_on = false;
uint8_t white_pulse_val = 0; // value for pulsing
uint8_t white_pulse_rate = 0;

bool lights_off = false;

bool in_interrupt = false;

// Event handler for the shell connection. 
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  int i;
  String s = "";

  if (event == ADB_CONNECTION_RECEIVE) {
    Serial.print("\r\nGot command from phone: ");
    for (i=0; i<length; i++) s += data[i];
    Serial.println(s);

    byte dev = data[0];
    byte cmd = data[1];

    if (dev == 0) {
      if (length < 3) return; // invalid command

      // DroidOrb command
      switch (cmd) {
        case CMD_SET_LED:  
          print_hex(analogRead(LIGHT_SENSOR), 8);
          red_bright = red = data[2];
          green_bright = green = data[3];
          blue_bright = blue = data[4];
          updateLEDs();
          break;

        case CMD_PULSE_LED:          
          red_pulse = data[2];
          green_pulse = data[3];
          blue_pulse = data[4];
          white_pulse = data[5];
          updateLEDs();
          break;

        case CMD_RESET:
          reset();          
          break;

        case CMD_LIGHT_METER:
          uint8_t light_data[4];
          light_data[0] = DROID_ORB_ADDR;
          light_data[1] = CMD_LIGHT_METER;
          light_data[2] = analogRead(LIGHT_SENSOR);
          light_data[3] = '\n';
          phone->write(4, (uint8_t*)&light_data);
          break;

        case CMD_TEST:          
          white_pulse = 50; // pulse white LED as a test
          red_bright = 0xff; // set red LED on
          blue_pulse = 10; // pulse blue LED
          break;

        default:
          break;
      }
    } else {
      // command for a module, send it through I2C
      Serial.print("\r\nSending I2C addr=");
      print_hex(dev, 8);
      Serial.print(" data=");
      print_hex(data[1], 8);
      Wire.beginTransmission(dev);
      Wire.send(data[1]);
      Wire.endTransmission();
    }

    // send ack/nack back to phone
    // phone->write(length, (uint8_t*)&data);
  } else {
    Serial.print("\r\n" + event);
  }

}

void setup()
{
  // set up led outputs
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  pinMode(LED_WHITE, OUTPUT);

  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_BLUE, LOW);
  digitalWrite(LED_WHITE, LOW);

  // Initialise serial port
  Serial.begin(115200);

  // Initialise I2C bus
  Wire.begin();

  // Note start time
  lastTime = millis();

  // Initialize timer
  Timer1.initialize(100); // 100ns timer resolution
  Timer1.attachInterrupt(timerInterrupt);

  // Initialise the ADB subsystem.  
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect
  phone = ADB::addConnection(ANDROID_CONNECTION_PORT, true, adbEventHandler);

  Serial.println("Init done, waiting for phone command.");
}

// Timer interrupt to handle the fading and pulsing of LED's
void timerInterrupt() 
{
  if (in_interrupt) return; // prevent interrupt overflows
  in_interrupt = true;

  // update state variables for each LED
  pulse(&red, &red_pulse, &red_pulse_val, &red_dir);
  pulse(&green, &green_pulse, &green_pulse_val, &green_dir);
  pulse(&blue, &blue_pulse, &blue_pulse_val, &blue_dir);
  updateLEDs();  

  // white LED is simple ON/OFF
  if (white_pulse_rate == 0) {
    white_pulse_rate = 10;
    if (!lights_off) {
      if (white_pulse > 0) {
        if (white_pulse_val++ >= white_pulse) {
          white_pulse_val = 0;
          digitalWrite(LED_WHITE, white_on = white_on ^ HIGH);
        }
      } else {
        digitalWrite(LED_WHITE, LOW);
      }
    }
  } else {
    white_pulse_rate--;
  }


  in_interrupt = false;
}

// Pulse an LED by using the state variables to set the right PWM value
void pulse(uint8_t* r, uint8_t* r_pulse, uint8_t* r_pulse_val, bool* r_dir) {
  // variables are passed in by reference so they have to be dereferenced
  uint8_t step = 1;

  if (*r_pulse > 0) {
    if (((*r_pulse_val) += step) >= *r_pulse) {
      *r_pulse_val = 0;

      if ((*r_dir) == false) {
        if (((*r) -= step) == 0) {
          *r_dir = true;
          *r = 0;
        }
      } else {
        if ((*r)++ >= 0xff) {
          *r_dir = false;
          *r = 0xff;
        }
      }
    }
  }  
}

// Write current LED brightness values to the actual pins
void updateLEDs() 
{

  if (!lights_off) {
    // update LED
    analogWrite(LED_RED, red);
    analogWrite(LED_GREEN, green);
    analogWrite(LED_BLUE, blue);
  } else {
    analogWrite(LED_RED, 0);
    analogWrite(LED_GREEN, 0);
    analogWrite(LED_BLUE, 0);
    digitalWrite(LED_WHITE, LOW);
  }
}

// reset LEDs to default state
void reset() {
  red_pulse = 0;
  green_pulse = 0;
  blue_pulse = 0;
  white_pulse = 0;

  red_bright = red = 0;
  green_bright = green = 0;
  blue_bright = blue = 0;
  updateLEDs();
}

/* prints hex numbers with leading zeroes */
// copyright, Peter H Anderson, Baltimore, MD, Nov, '07
// source: http://www.phanderson.com/arduino/arduino_display.html
void print_hex(int v, int num_places)
{
  int mask=0, n, num_nibbles, digit;
 
  for (n=1; n<=num_places; n++) {
    mask = (mask << 1) | 0x0001;
  }
  v = v & mask; // truncate v to specified number of places
 
  num_nibbles = num_places / 4;
  if ((num_places % 4) != 0) {
    ++num_nibbles;
  }
  do {
    digit = ((v >> (num_nibbles-1) * 4)) & 0x0f;
    Serial.print(digit, HEX);
  } 
  while(--num_nibbles);
}

// main loop
void loop()
{
  // Poll the ADB subsystem.
  ADB::poll();

  // switch off lights if ambient lighting is dark
  uint8_t lightVal = analogRead(LIGHT_SENSOR);
  if (!lights_off && lightVal <= LIGHT_THRESHOLD_LOW) {
    lights_off = true;

    // let the phone know
    uint8_t len = 3;
    uint8_t light_data[len];
    light_data[0] = DROID_ORB_ADDR;
    light_data[1] = CMD_LIGHTS_OFF;
    light_data[2] = '\n';
    phone->write(len, (uint8_t*)&light_data);
  }

  // and vice versa
  if (lights_off && lightVal > LIGHT_THRESHOLD_HIGH) {
    lights_off = false;

    // let the phone know
    uint8_t len = 3;
    uint8_t light_data[len];
    light_data[0] = DROID_ORB_ADDR;
    light_data[1] = CMD_LIGHTS_ON;
    light_data[2] = '\n';
    phone->write(len, (uint8_t*)&light_data);
  }
}

