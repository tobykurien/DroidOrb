#include <SPI.h>
#include <Adb.h>

// TODO - some pins are used by USB host shield - find out which ones
// pins 13, 12, 11, 10, 9, 8, and 7 are used :-(
//#define LED_WHITE 11
#define LED_RED   6
#define LED_GREEN 5
#define LED_BLUE  3

#define DELAY 1000
#define FADE_DELAY 5

// Adb connection.
Connection * phone;

// Elapsed time for ADC sampling
long lastTime;

char red;
char green;
char blue;

// Event handler for the shell connection. 
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  int i;
  String s = "";

  if (event == ADB_CONNECTION_RECEIVE) {
    Serial.print("\r\nGot command from phone: ");
    for (i=0; i<length; i++) s += data[i];
    Serial.println(s);

    // send it back to phone as ack
    phone->write(length, (uint8_t*)&data);

    if (s.startsWith("test")) {
      fadeIn(LED_RED);
      fadeOut(LED_RED);
    }

    if (length == 3) {
      // command format: RRGGBB
      red = data[0];
      green = data[1];
      blue = data[2];

      analogWrite(LED_RED, red);
      analogWrite(LED_GREEN, green);
      analogWrite(LED_BLUE, blue);
    }

  } else {
    Serial.print("\r\n" + event);
  }

}

void setup()
{
  // set up led outputs
  //pinMode(LED_WHITE, OUTPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  //digitalWrite(LED_WHITE, LOW);
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_BLUE, LOW);

  // Initialise serial port
  Serial.begin(115200);

  // Note start time
  lastTime = millis();

  // Initialise the ADB subsystem.  
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect
  //ADB::addConnection("shell:exec logcat", true, adbEventHandler);  

  // Open an ADB stream to the phone's shell. Auto-reconnect
  phone = ADB::addConnection("tcp:4567", true, adbEventHandler);

  Serial.println("Init done, waiting for phone command.");
}

void loop()
{
  // Poll the ADB subsystem.
  ADB::poll();

  // connection handler will handle incoming requests
}

void fadeIn(int pin) {
  int i = 0;  // We’ll use this to count up and down

  for (i = 0; i < 255; i++) { // loop from 0 to 254 (fade in)
    analogWrite(pin, i);
    delay(FADE_DELAY); // Wait 10ms because analogWrite
    // is instantaneous and we would
    // not see any change
  }
  
}

void fadeOut(int pin) {
  int i = 0;  // We’ll use this to count up and down

  for (i = 254; i >= 0; i--) { // loop from 254 to 0 (fade out)
    analogWrite(pin, i);
    delay(FADE_DELAY); // Wait 10ms because analogWrite
    // is instantaneous and we would
    // not see any change
  }
  
}

