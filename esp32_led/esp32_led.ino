#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <ArduinoJson.h>

#define SERVICE_UUID "22bf526e-1f59-40fb-a344-0bea8c1bfef2"
#define CHARACTERISTIC_UUID "cdc7651d-88bd-4c0d-8c90-4572db5aa14b"

const int rledPin = 25;  
const int gledPin = 26;  
const int bledPin = 27; 

const char* currentMode = "WHITE";

BLECharacteristic *pCharacteristic;
bool deviceConnected = false;

void setColor(int R, int G, int B) {
  analogWrite(rledPin, R);  
  analogWrite(gledPin, G);  
  analogWrite(bledPin, B);  
}

void red() {
  setColor(0, 255, 255);  
  delay(100);
}

void yellow() {
  setColor(0, 0, 255);  
  delay(100);
}

void green() {
  setColor(255, 0, 255);  
  delay(100);
}

void cyan() {
  setColor(255, 0, 0);  
  delay(100);
}

void blue() {
  setColor(255, 255, 0);  
  delay(100);
}

void magenta() {
  setColor(0, 255, 0);  
  delay(100);
}

void white() {
  setColor(0, 0, 0);
  delay(100);
}

void turnOff() {
  setColor(255, 255, 255);
  delay(100);
}

void rgb() {
  red();
  delay(1000);    

  green();
  delay(1000);    

  blue();
  delay(1000);    
}

void flash(int R, int G, int B) {
  setColor(R, G, B);
  delay(1000);                       
  setColor(255, 255, 255);
  delay(1000);      
}

void rainbow() {
  red();
  delay(1000);
  
  setColor(255, 165, 0);
  delay(1000);
  
  yellow();
  delay(1000);
  
  green();
  delay(1000);
  
  setColor(0, 255, 165);  
  delay(1000);
  
  cyan();
  delay(1000);
  
  blue();
  delay(1000);

  setColor(165, 0, 255);  
  delay(1000);
  
  magenta();
  delay(1000);
  
  white(); 
  delay(1000);
}

void fade() {
  for (int brightness = 0; brightness <= 255; brightness++) {
    setColor(brightness, 0, 0);  
    delay(10);  
  }
    
  for (int brightness = 0; brightness <= 255; brightness++) {
    setColor(255, brightness, 0);  
    delay(10);  
  }
    
  for (int brightness = 255; brightness >= 0; brightness--) {
    setColor(brightness, 255, 0); 
    delay(10);
  }

  for (int brightness = 0; brightness <= 255; brightness++) {
    setColor(0, 255, brightness);  
    delay(10);
  }

  for (int brightness = 255; brightness >= 0; brightness--) {
    setColor(0, brightness, 255); 
    delay(10);
  }

  for (int brightness = 255; brightness >= 0; brightness--) {
    setColor(0, 0, brightness);  
    delay(10);
  }
}

class MyServerCallbacks: public BLEServerCallbacks { 
  void onConnect(BLEServer* pServer){
    Serial.println("connected");
    deviceConnected = true;
  };

  void onDisconnect(BLEServer *pServer){
    deviceConnected = false;
    
    Serial.println("disconnected");
    delay(500); 

    pServer->getAdvertising()->start();  
    Serial.println("waiting for connection from client");
  }
};

class CharacteristicCallback : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* dhtCharacteristic) {
    String value = pCharacteristic->getValue();
    Serial.print("Received Value: ");
    Serial.println(value.c_str());  

    StaticJsonDocument<200> doc;  
    DeserializationError error = deserializeJson(doc, value);

    if (error) {
      Serial.print(F("Failed to parse JSON: "));
      Serial.println(error.c_str());
      return;
    }

    currentMode = doc["mode"];  
    bool isFlashEnabled = doc["isFlashEnabled"] == NULL ? false : true;
  }
};


void initialize(){
  Serial.begin(9600);
  
  BLEDevice::init("ESP32");

  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);  

  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_NOTIFY | 
    BLECharacteristic::PROPERTY_READ | 
    BLECharacteristic::PROPERTY_WRITE
  );

  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setCallbacks(new CharacteristicCallback());

  pService->start();

  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x12);
  
  pServer->getAdvertising()->start();

  Serial.println("waiting for connection from client");
}

void setup() {
  pinMode(rledPin, OUTPUT);
  pinMode(gledPin, OUTPUT);
  pinMode(bledPin, OUTPUT);
  
  initialize();
}

void loop() {
  if (strcmp(currentMode, "TURN_OFF") == 0) turnOff();
  else if (strcmp(currentMode, "RED") == 0) red();
  else if (strcmp(currentMode, "GREEN") == 0) green();
  else if (strcmp(currentMode, "BLUE") == 0) blue();
  else if (strcmp(currentMode, "MAGENTA") == 0) magenta();
  else if (strcmp(currentMode, "YELLOW") == 0) yellow();
  else if (strcmp(currentMode, "CYAN") == 0) cyan();
  else if (strcmp(currentMode, "WHITE") == 0) white();
  else if (strcmp(currentMode, "FADE") == 0) fade();
  else if (strcmp(currentMode, "RGB") == 0) rgb();
  else if (strcmp(currentMode, "RAINBOW") == 0) rainbow();
}