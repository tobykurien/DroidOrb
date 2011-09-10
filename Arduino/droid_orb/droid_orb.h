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
#ifndef DROIDORB_h
#define DROIDORB_h

#define ANDROID_CONNECTION_PORT "tcp:4567"

// TODO - some pins are used by USB host shield - find out which ones
// pins 13, 12, 11, 10, 9, 8, and 7 are used :-(
#define LED_RED   6
#define LED_GREEN 5
#define LED_BLUE  3
#define LED_WHITE 4

#define LIGHT_SENSOR A0

#define DELAY 1000
#define FADE_DELAY 5

#define DROID_ORB_ADDR 0

#define CMD_SET_LED 0
#define CMD_PULSE_LED 1
#define CMD_RESET 3
#define CMD_LIGHT_METER 4
#define CMD_LIGHTS_OFF 5
#define CMD_LIGHTS_ON 6
#define CMD_TEST 254

#define LIGHT_THRESHOLD_LOW 0x20
#define LIGHT_THRESHOLD_HIGH 0xA0

#endif
