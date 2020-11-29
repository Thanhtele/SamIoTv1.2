#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <EEPROM.h>

#define device1 4
#define device2 8
#define device3 5
#define bt1 2
#define bt2 7
#define bt3 3
#define buzzer 6
/////4-pwm, 5,8 on-off

RF24 radio(9, 10);               // nRF24L01 (CE,CSN)
RF24Network network(radio);      // Include the radio in the network
const uint16_t this_node = 01;   // Address of our node in Octal format ( 04,031, etc)
const uint16_t master = 00;    // Address of the other node in Octal format
uint16_t rfData[6]={};
int flagInterrupt=0;

void controllDevice12(){
      if(digitalRead(bt2)==0){
        Serial.println("Bt 2nd is Click");
        if(rfData[2]) rfData[2]=0; 
        else rfData[2]=1;
        digitalWrite(device2,rfData[2]);
      }
      else{
        Serial.println("Bt 1st is Click");
        if(rfData[1]) rfData[1]=0; 
        else rfData[1]=1;
        digitalWrite(device1,rfData[1]);
      }
      tone(buzzer, 3700, 40);
      flagInterrupt=1;
}
void controllDevice3(){
  if(!digitalRead(3)){
      tone(buzzer, 3700, 40);
      rfData[3]+=20;
      if(rfData[3]>100) rfData[3]=0;
      analogWrite(device3,rfData[3]*255/100);
  } 
  flagInterrupt=1;
}
void setup() {
  Serial.begin(115200);
  SPI.begin();
  radio.begin();
  network.begin(90, this_node); //(channel, node address)
  radio.setDataRate(RF24_1MBPS);
  
  pinMode(device1, OUTPUT);
  pinMode(device2, OUTPUT);
  pinMode(device3, OUTPUT);  
  pinMode(buzzer, OUTPUT);
  pinMode(bt1, INPUT_PULLUP);
  attachInterrupt(0,controllDevice12,FALLING);
  pinMode(bt2,INPUT_PULLUP);
  pinMode(bt3, INPUT_PULLUP);
  attachInterrupt(1,controllDevice3,FALLING);
  
  /////Starting
  digitalWrite(device1,EEPROM.read(1));
  digitalWrite(device2,EEPROM.read(2));
  analogWrite(device3,EEPROM.read(3)*255/100); 
  
  Serial.println("Starting...");
  rfData[0]=3;
  reponseHub();
  flagInterrupt=0;
}

void loop() {
      network.update();
      //===== Receiving =====//
      if(flagInterrupt){
          for(int i=1;i<6;i++){
              if(i==1 || i==2 || i==3) EEPROM.write(i,rfData[i]);
          }
          rfData[0]=3;
          reponseHub();
          flagInterrupt=0;
      }
      while(network.available()) {     // Is there any incoming data? 
            RF24NetworkHeader header;
            network.read(header, &rfData, sizeof(rfData)); // Read the data Received 
           
            if(rfData[0]==0) {
              Serial.println("Hub is pinging ");
              break;
            }
            if(rfData[0]==1){         // saveData, No Reponse
                digitalWrite(device1,rfData[1]);
                digitalWrite(device2,rfData[2]);
                analogWrite(device3,rfData[3]*255/100); 
                for(int i=1;i<6;i++){         
                   if(i==1 || i==2 || i==3) EEPROM.write(i,rfData[i]);
                }
                Serial.println("Hub controlling Devices ");
            }
            if(rfData[0]==3) {
              reponseHub();
              Serial.println("Hub request Status Devices ");        
            }
            
    }    
}
//Reponse status to Hub
void reponseHub(){
        if(rfData[0]==3){
            Serial.println("Sending status Devices to Hub");
            for(int i=1;i<6;i++){
                 rfData[i] = EEPROM.read(i);
            }
        }
        RF24NetworkHeader header00(master);             // (Address where the data is going)
        bool ok = network.write(header00, &rfData, sizeof(rfData)); // Send the data   
}
