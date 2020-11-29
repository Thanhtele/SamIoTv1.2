#include <WiFi.h>
#include <FirebaseESP32.h>
#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>

#define FIREBASE_HOST "iot3005.firebaseio.com"
#define FIREBASE_AUTH "meCbYZV2nmVsA9AGsPMm3xEAPkkRqmV7bnRfFQno"
#define WIFI_SSID "Thanhnv37"
#define WIFI_PASSWORD "12345679"

/*CE -> GPIO4 GPIO
  CSN -> GPIO05 VSPI SS
  MISO -> GPIO19 VSPI MISO
  MOSI -> GPIO23 VSPI MOSI
  CLK -> GPIO18 VSPI CLK
  IRQ-> unconnected*/

RF24 radio(4, 5);               // nRF24L01 (CE,CSN)
RF24Network network(radio);      // Include the radio in the network
const uint16_t this_node = 00;   // Address of this node in Octal format ( 04,031, etc)
const uint16_t node01 = 01;
const uint16_t node02 = 02;
const uint16_t node03 = 041;

//Define FirebaseESP32 data object
FirebaseData firebaseData;
FirebaseData reponseData;
FirebaseJson json;

uint16_t rfData[7] = {};
int newStatus[6] = {},
    preStatus[6] = {};
String uidRoom1[5] = {"-MMtY2dt7F3JoZuyEATc", "-MMtY2t6IU5PI9FSLdbC", "-MMtZJ-B-3JqpIjMAB2D", "-MMtZJ-B-3JqpIjMAB2E", "-MMtZJ2wytDmSBF_Fwrr"},
       uidRoom2[4] = {"-MMtZQgxVrahJy082zFB", "-MMtZQk_y1i8mi5G-hC7", "-MMtaxHAdvZlRwmiz9OZ", "-MMtaxlaIKQPHsWXYXIR"};
String rTime = "00000000";
unsigned long timeStart1 = 0, timeStart2 = 0;
int timeForSensor = 0;
void setup() {
  Serial.begin(115200);
  SPI.begin();
  radio.begin();
  network.begin(90, this_node); //(channel, node address)
  radio.setDataRate(RF24_1MBPS);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  Firebase.setStreamCallback(firebaseData, streamCallback, streamTimeoutCallback);

  if (!Firebase.beginStream(firebaseData, "/User1")) {
    Serial.println("Can't begin stream connection...");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println("------------------------------------");
  }
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[0] + "/isOnline", 0); ////Fan-On/Off Bedroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[3] + "/isOnline", 0); //// Light On-Off Bedroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[4] + "/isOnline", 0); ////Fan-MultiValue Bedroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[1] + "/isOnline", 0); //// TemptSensoor Bedroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[2] + "/isOnline", 0); ////HumiditySensor Bedroom
      
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[2] + "/isOnline", 0); ////LightMultiValue Livingroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[3] + "/isOnline", 0); ////Fan-On/Off Livingroomm      
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[0] + "/isOnline", 1); //// TemptSensoor Livingroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[1] + "/isOnline", 1); ////HumiditySensor Livingroom
  requestStatus(3, 0); ////Require data
}
void loop() {
  network.update();
  ////Recieved Data from Deviecs
  while ( network.available() ) {     // Is there any data from Nodes ?
    RF24NetworkHeader header;
    network.read(header, &rfData, sizeof(rfData)); // Read the incoming data
    if (rfData[0] != 0) { ///Data need sent to Firebase
      switch (header.from_node) {
        case 1: {
            if (preStatus[1] != newStatus[1]) {
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[0] + "/isOnline", newStatus[1]); ////Fan-On/Off Bedroom
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[3] + "/isOnline", newStatus[1]); //// Light On-Off Bedroom
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[4] + "/isOnline", newStatus[1]); ////Fan-MultiValue Bedroom
              preStatus[1] = newStatus[1];
              Serial.println("Update On/Offline Node01");
              timeStart1 = millis();
            }
            if (rfData[0] == 1 || rfData[0] == 3) {
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[0] + "/value/value", rfData[1]); ////On-Off
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[3] + "/value/value", rfData[2]); ////On-Off
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[4] + "/value/value", rfData[3]); ////Multi
            }
            Serial.println("Recived data from node01");
            break;
          }
        case 2: {
            
            if (preStatus[2] != newStatus[2]) {
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[2] + "/isOnline", newStatus[2]); ////LightMultiValue Livingroom
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[3] + "/isOnline", newStatus[2]); ////Fan-On/Off Livingroomm
              preStatus[2] = newStatus[2];
              Serial.println("Update On/Offline Node02");
              timeStart1 = millis();
            }
            if (rfData[0] == 1 || rfData[0] == 3) {
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[2] + "/value/value", rfData[2]); ////Multi
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[3] + "/value/value", rfData[1]); ////On-Off
            }
            Serial.println("Recived data from node02");
            break;
          }
        case 33: {
            Serial.println("Recived data from node041");
            getTime();////////////Get time before update data to cloud
            newStatus[3]=1;
            if (preStatus[3] != newStatus[3]) {
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[1] + "/isOnline", newStatus[3]); //// TemptSensoor Bedroom
              Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[2] + "/isOnline", newStatus[3]); ////HumiditySensor Bedroom
              preStatus[3] = newStatus[3];
              Serial.println("Update On/Offline node041");
            }
            timeForSensor = 0;
            if (rfData[0] == 2 || rfData[0] == 3) {
              getTime();////////////Get time before update data to cloud
              json.clear();
              json.set(rTime, rfData[5]);
              Firebase.updateNode(reponseData, "User1/Home1/" + uidRoom1[1] + "/value", json); ////Tempt-5
              json.clear();
              json.set(rTime, rfData[4]);
              Firebase.updateNode(reponseData, "User1/Home1/" + uidRoom1[2] + "/value", json); ////Humi-4
            }
            break;
        }
      }
    }
  }
  if ((unsigned long) (millis() - timeStart1) >= 10000) {
    byte updated = 0;
    if(timeForSensor==0) newStatus[3]=0;
    timeForSensor++;
    //node01
    newStatus[1]=requestStatus(0,1); //// Ping online or Offline
    if (preStatus[1] != newStatus[1]) {
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[0] + "/isOnline", newStatus[1]); ////Fan-On/Off Bedroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[3] + "/isOnline", newStatus[1]); //// Light On-Off Bedroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[4] + "/isOnline", newStatus[1]); ////Fan-MultiValue Bedroom
      preStatus[1] = newStatus[1];
      updated = 1;
    }
    //node02
    newStatus[2]=requestStatus(0,2); //// Ping online or Offline
    if (preStatus[2] != newStatus[2]) {
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[2] + "/isOnline", newStatus[2]); ////LightMultiValue Livingroom
      Firebase.setInt(reponseData, "User1/Home1/" + uidRoom2[3] + "/isOnline", newStatus[2]); ////Fan-On/Off Livingroomm
      preStatus[2] = newStatus[2];
      updated = 1;
    }
    //node041
    if (timeForSensor == 20) { ///10*20 second = 3m20s
      if (preStatus[3] != newStatus[3]) {
        Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[1] + "/isOnline", newStatus[3]); //// TemptSensoor Bedroom
        Firebase.setInt(reponseData, "User1/Home1/" + uidRoom1[2] + "/isOnline", newStatus[3]); ////HumiditySensor Bedroom
        preStatus[3] = newStatus[3];
        updated = 1;
      }
      timeForSensor = 0;
    }  
    timeStart1 = millis();
    if (updated) Serial.println("Update On/Offline ");
  }
}
////////////// Get Real time from firebase
void getTime() {
  if (Firebase.setTimestamp(reponseData, "RealTime/Timestamp")) {
    rTime = String(reponseData.intData());
  }
}
/////////////// Function when has Event Value Changed
void streamCallback(StreamData data) {
  int node = 0;
  String pathStr = data.dataPath();
  Serial.println("------------------------------------");
  Serial.println("On Event Change data in : /HOME " + data.streamPath() + data.dataPath());

  if (pathStr == "/") {
    Serial.println("Starting !");
  }
  else if (pathStr == "/Home1/" + uidRoom1[0] + "/value/value") {
    rfData[1] = data.intData();
    node = 1;
  }
  else if (pathStr == "/Home1/" + uidRoom1[3] + "/value/value") {
    rfData[2] = data.intData();
    node = 1;
  }
  else if (pathStr == "/Home1/" + uidRoom1[4] + "/value/value") {
    rfData[3] = data.intData();
    node = 1;
  }
  else if (pathStr == "/Home1/" + uidRoom2[2] + "/value/value") {
    rfData[2] = data.intData();
    node = 2;
  }
  else if (pathStr == "/Home1/" + uidRoom2[3] + "/value/value") {
    rfData[1] = data.intData();
    node = 2;
  }

  /////////////////Distributing Data to Devices
  if (node != 0) requestStatus(1, node);
}
int requestStatus(int dataHeader, int node) {
  rfData[0] = dataHeader;
  if(node==0){
    RF24NetworkHeader header;    // (Address where the data is going)
    bool ok = network.multicast(header, &rfData, sizeof(rfData),1); // Send the data devices of Level 1
    if(!ok) network.multicast(header, &rfData, sizeof(rfData),1); // Send the data devices of Level 1
    return 1;
  }
  RF24NetworkHeader headerxx(node);
  bool ok = network.write(headerxx, &rfData, sizeof(rfData));
  if (ok == false) ok = network.write(headerxx, &rfData, sizeof(rfData));
  if(ok==true) return 1;
  return 0;
}
////////////////Time out Stream//Follow
void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    //Serial.println("Stream timeout, resume streaming...");
  }
}
