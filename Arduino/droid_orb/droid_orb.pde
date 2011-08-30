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
          break;

        case CMD_TEST:          
          white_pulse = 100; // pulse white LED as a test
          red_bright = 0xff;
          blue_pulse = 10;
          break;

        default:
          break;
      }
    } else {
      // command for a module, send it through SPI
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

  // Note start time
  lastTime = millis();

  // Initialize timer
  Timer1.initialize(1000); // 1ms timer resolution
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
  if (white_pulse > 0) {
    if (white_pulse_val++ >= white_pulse) {
      white_pulse_val = 0;
      digitalWrite(LED_WHITE, white_on = white_on ^ HIGH);
    }
  }

  in_interrupt = false;
}

// Pulse an LED by using the state variables to set the right PWM value
void pulse(uint8_t* r, uint8_t* r_pulse, uint8_t* r_pulse_val, bool* r_dir) {
  // variables are passed in by reference so they have to be dereferenced

  if (*r_pulse > 0) {
    if ((*r_pulse_val)++ >= *r_pulse) {
      *r_pulse_val = 0;

      if ((*r_dir) == false) {
        if ((*r)-- == 0) {
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
  // update LED
  analogWrite(LED_RED, red);
  analogWrite(LED_GREEN, green);
  analogWrite(LED_BLUE, blue);
}

// main loop
void loop()
{
  // Poll the ADB subsystem.
  ADB::poll();
}

