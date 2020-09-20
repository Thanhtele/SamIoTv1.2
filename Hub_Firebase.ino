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
  const uint16_t node01 = 01;      // Address of the other node in Octal format
  const uint16_t node02 = 02;      // Address of the other node in Octal format

  unsigned long timeStart1=0,timeStart2=0;
   
//Define FirebaseESP32 data object
  FirebaseData firebaseData;
  FirebaseData reponseData;
  FirebaseJson json;
   
  uint16_t  statusLivingRoom[6]={},
            statusBedRoom[6]={},
            rfData[6]={};      
  int deviceIsOnine[6]={};
  int timeRequeted=0;
  String rTime="00000000";     
  
void setup() {

  Serial.begin(115200);
  SPI.begin();
  radio.begin();
  network.begin(90, this_node); //(channel, node address)
  radio.setDataRate(RF24_1MBPS);
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED){
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
  
  if (!Firebase.beginStream(firebaseData, "/User1")){
      Serial.println("Can't begin stream connection...");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
  }
  timeRequeted=1;
  requestStatus(1);  ///Require data
  
}
void loop() {
    network.update();
///Request Status Devices
    if((unsigned long) (millis()-timeStart1)>=10000){
      requestStatus(2); /// Ping online or Offline
      updataOnOffline();
      timeStart1=millis();
    }
///////////////// Update Value Sensor
    if((unsigned long) (millis()-timeStart2)>=180000){
      timeRequeted=1; 
      requestStatus(1);  ///Require data
      timeStart2=millis();
    }


///////////////////// Recieved Data from Deviecs
    int room=0;  
    while ( network.available() ) {     // Is there any data from Nodes ?
        RF24NetworkHeader header;
        network.read(header, &rfData, sizeof(rfData)); // Read the incoming data
        
        
        if (header.from_node == 1) {    // If data comes from LivingRoom
            room=1;
            deviceIsOnine[1]=1;
            for(int i=1;i<6;i++){
              statusBedRoom[i]=rfData[i];
            }
        }
        else if (header.from_node == 2) {    // If data comes from BedRoom
          room=2;
          deviceIsOnine[2]=1;
          for(int i=1;i<6;i++){
              statusLivingRoom[i]=rfData[i];
          }
        }
     }
     ////////////Get time before update data to cloud
     if(room!=0){
         if(timeRequeted) getTime();
         switch (room){
               case 1:{
                if(timeRequeted){
                      json.clear(); 
                      json.set(rTime,statusBedRoom[2]);
                      Firebase.updateNode(reponseData, "User1/Room1/Devices/Device2/value",json);
                      json.clear(); 
                      json.set(rTime,statusBedRoom[3]);       
                      Firebase.updateNode(reponseData, "User1/Room1/Devices/Device3/value",json);
                      timeRequeted=0;
                  }
                  Firebase.setInt(reponseData, "User1/Room1/Devices/Device1/value/value",statusBedRoom[1]);
                  Firebase.setInt(reponseData, "User1/Room1/Devices/Device4/value/value",statusBedRoom[4]);
                  
                  Serial.println("Received Data from BedRoom");
                  for(int i=1;i<6;i++){
                        Serial.print(statusBedRoom[i]);
                        Serial.print("  -  ");   
                  }
                  Serial.println();
                  break;
               }
               case 2:{
                 if(timeRequeted){
                        json.clear(); 
                        json.set(rTime,statusLivingRoom[2]);
                        Firebase.updateNode(reponseData, "User1/Room2/Devices/Device2/value",json);
                        json.clear(); 
                        json.set(rTime,statusLivingRoom[3]);
                        Firebase.updateNode(reponseData, "User1/Room2/Devices/Device3/value",json);
                        timeRequeted=0;
                  }
                  Firebase.setInt(reponseData, "User1/Room2/Devices/Device1/value/value",statusLivingRoom[1]);
                  Firebase.setInt(reponseData, "User1/Room2/Devices/Device4/value/value",statusLivingRoom[4]);
                  
                  Serial.println("Received Data from LivingRoom");
                  for(int i=1;i<6;i++){
                        Serial.print(statusLivingRoom[i]);
                        Serial.print("  _  ");   
                  }
                  Serial.println();
                  break;
               }
         }
     }
}
////////////// Get Real time from firebase
void getTime(){
  if (Firebase.setTimestamp(reponseData, "RealTime/Timestamp")){
    rTime = String(reponseData.intData());
    Serial.println(rTime);
  }
}
/////////////// Function when has Event Value Changed
void streamCallback(StreamData data){
          int room=0;
          String pathStr = data.dataPath();
          Serial.println("------------------------------------");
          Serial.println("On Event Change data in : /HOME " + data.streamPath() + data.dataPath());
          Serial.println(pathStr+"----");
          if(pathStr=="/"){
            Serial.println("Starting !");
          }
          else if(pathStr == "/Room1/Devices/Device1/value/value"){   //Vslue can change
             statusBedRoom[1]=data.intData();
             room=1;
          }
          else if(pathStr == "/Room1/Devices/Device4/value/value"){   //Value can change
             statusBedRoom[4]=data.intData();
             room=1;
          }
          
          else if(pathStr == "/Room2/Devices/Device1/value/value"){   //Value can change
             statusLivingRoom[1]=data.intData();
             room=2;
          }  
          else if(pathStr == "/Room2/Devices/Device4/value/value"){   //Value can change
             statusLivingRoom[4]=data.intData();
             room=2;
          }      
    
/////////////////Distributing Data to Devices
          switch (room){
               case 1 :{
                    statusBedRoom[0]=0;
                    RF24NetworkHeader heade01(node01);             // (Address where the data is going)
                    bool ok = network.write(heade01, &statusBedRoom, sizeof(statusBedRoom)); // Send the data    
                    deviceIsOnine[1]=ok;
                    if(ok){
                      Serial.println("Transmited Data to BedRoom");
                      for(int i=1;i<6;i++){
                        Serial.print(statusBedRoom[i]);
                        Serial.print("   ");
                      }
                    }
                    else Serial.println("Transmited Data BedRoom Failllllllll");
                    break;
               }

               case 2:{
                    statusLivingRoom[0]=0;
                    RF24NetworkHeader heade02(node02);             // (Address where the data is going)
                    bool ok = network.write(heade02, &statusLivingRoom, sizeof(statusLivingRoom)); // Send the data    
                    deviceIsOnine[2]=ok;
                    if(ok){
                      Serial.println("Transmited Data to LivingRoom");
                      for(int i=1;i<6;i++){
                        Serial.print(statusLivingRoom[i]);
                        Serial.print("   ");
                      }
                    }
                    else Serial.println("Transmited Data LivingRoom Failllllllll");
                    break;
                }
          }
          Serial.println();
          updataOnOffline();    
}

////////////Function Update Online or Offline
void updataOnOffline(){     
      Firebase.setInt(reponseData, "User1/Room1/Devices/Device1/isOnline",deviceIsOnine[1]);
      Firebase.setInt(reponseData, "User1/Room1/Devices/Device2/isOnline",deviceIsOnine[1]);
      Firebase.setInt(reponseData, "User1/Room1/Devices/Device3/isOnline",deviceIsOnine[1]);
      Firebase.setInt(reponseData, "User1/Room1/Devices/Device4/isOnline",deviceIsOnine[1]);
      
      Firebase.setInt(reponseData, "User1/Room2/Devices/Device1/isOnline",deviceIsOnine[2]);
      Firebase.setInt(reponseData, "User1/Room2/Devices/Device2/isOnline",deviceIsOnine[2]);
      Firebase.setInt(reponseData, "User1/Room2/Devices/Device3/isOnline",deviceIsOnine[2]);
      Firebase.setInt(reponseData, "User1/Room2/Devices/Device4/isOnline",deviceIsOnine[2]);
      
      Serial.println("Updated Status Online/OffLine");
}
////////////Function call Devices reponse Status
void requestStatus(int header){
      Serial.println("Requesting Status Devices...............");
      rfData[0]=header;
      ///////////////////
      RF24NetworkHeader heade01(node01);             // (Address where the data is going)
      bool ok1 = network.write(heade01, &rfData, sizeof(rfData)); // Send the data    
      deviceIsOnine[1]=ok1;
      ///////////////////
      RF24NetworkHeader heade02(node02);             // (Address where the data is going)
      bool ok2 = network.write(heade02, &rfData, sizeof(rfData)); // Send the data    
      deviceIsOnine[2]=ok2;
      ////////////////////
      rfData[0]=0;
}
////////////////Time out Stream//Follow
void streamTimeoutCallback(bool timeout){
  if(timeout){
    //Stream timeout occurred
    //Serial.println("Stream timeout, resume streaming...");
  }  
}
