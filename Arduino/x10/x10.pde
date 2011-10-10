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

#define DEVICE_ADDR 1
#define DEBUG_LED 13

bool led_flash = 0;

void setup() {
  // Initialise serial port
  Serial.begin(115200);

  // Initialise I2C bus
  Wire.begin(DEVICE_ADDR);
  Wire.onReceive(&i2cHandler);

  // debug using LED
  digitalWrite(DEBUG_LED, LOW);

  Serial.write("Ready...");
}

// Receive I2C data from DroidOrb
void i2cHandler(int numBytes) {
  int data = Wire.receive();
  if (data == 1) {
    led_flash = 1;
  } else {
    led_flash = 0;
  }
}

void loop() {
  if (led_flash) {
    digitalWrite(DEBUG_LED, HIGH);
  }

  delay(500);

  if (led_flash) {
    digitalWrite(DEBUG_LED, LOW);
    delay(500);
  }
}
