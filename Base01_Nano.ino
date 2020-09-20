#include <dht_nonblocking.h>
#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <EEPROM.h>

#define ceilingLight 4
#define ceilingFan 5
#define btLight 7

#define DHT_SENSOR_TYPE DHT_TYPE_11
static const int DHT_SENSOR_PIN = 6;
DHT_nonblocking dht_sensor( DHT_SENSOR_PIN, DHT_SENSOR_TYPE );

RF24 radio(9, 10);               // nRF24L01 (CE,CSN)
RF24Network network(radio);      // Include the radio in the network
const uint16_t this_node = 01;   // Address of our node in Octal format ( 04,031, etc)
const uint16_t master = 00;    // Address of the other node in Octal format
uint16_t rfData[6]={};
int flagInterrupt=0;

static bool measure_environment( float *temperature, float *humidity )
{
  static unsigned long measurement_timestamp = millis( );
  if( millis( ) - measurement_timestamp > 60000ul )
  {
    if( dht_sensor.measure( temperature, humidity ) == true )
    {
      measurement_timestamp = millis( );
      return( true );
    }
  }
  return( false );
}
void controllCeilingLight(){
      flagInterrupt=1;
      if(!digitalRead(btLight)){
         Serial.println("Bt 3rd is Click");
      }
      else{
        Serial.println("Bt 1st is Click");
        if(rfData[4]) rfData[4]=0; 
        else rfData[4]=1;
        digitalWrite(ceilingLight,rfData[4]);
      }
}
void speedCeilingFan(){
  flagInterrupt=1;
  delay(30);
  Serial.println("Bt 2nd is Click");
  if(!digitalRead(3)){
      rfData[1]+=20;
      if(rfData[1]>100) rfData[1]=0;
      analogWrite(ceilingFan,rfData[1]*255/100); 
  }
}
void setup() {
  Serial.begin(115200);
  SPI.begin();
  radio.begin();
  network.begin(90, this_node); //(channel, node address)
  radio.setDataRate(RF24_1MBPS);
  
  pinMode(ceilingLight, OUTPUT);
  pinMode(ceilingFan, OUTPUT);
  pinMode(2, INPUT_PULLUP); // sử dụng điện trở kéo lên cho chân số 13
  attachInterrupt(0,controllCeilingLight,FALLING);
  pinMode(3, INPUT_PULLUP); // sử dụng điện trở kéo lên cho chân
  attachInterrupt(1,speedCeilingFan,FALLING);
  pinMode(btLight, INPUT_PULLUP); // sử dụng điện trở kéo lên cho chân
  /////Starting
  analogWrite(ceilingFan,EEPROM.read(1)*255/100); 
  digitalWrite(ceilingLight,EEPROM.read(4));
  Serial.println("Starting...");
  reponseHub();
  flagInterrupt=0;
}

void loop() {
      network.update();
      //===== Receiving =====//
      int oke=0;
      float temperature;
      float humidity;
      if( measure_environment( &temperature, &humidity ) == true ){
          EEPROM.write(2,humidity);
          EEPROM.write(3,temperature);  
      }
      if(flagInterrupt){
          for(int i=1;i<6;i++){
                  if(i!=2 || i!=3) EEPROM.write(i,rfData[i]);
          }
          flagInterrupt=0;
          Serial.println("On Switch Change");
          reponseHub();
      }
      while ( network.available() ) {     // Is there any incoming data? 
            RF24NetworkHeader header;
            network.read(header, &rfData, sizeof(rfData)); // Read the data Received 
            if(rfData[0]==0){         /// saveData, No Reponse
                analogWrite(ceilingFan,rfData[1]*255/100); 
                digitalWrite(ceilingLight,rfData[4]);
                for(int i=1;i<6;i++){         
                   if(i!=2 || i!=3) EEPROM.write(i,rfData[i]);         //// Save data
                }
                Serial.println(" Hub controlling Devices ");
            }
            else if(rfData[0]==1) {                   ///No saveData, Sent Reponse
                Serial.println(" Hub request Status Devices ");        
                delay(20);
                reponseHub();
            }
            else Serial.println("HUB is pinging");    ///No saveData, No Reponse 
    }    
}
    
/////Reponse status to Hub
void reponseHub(){
        Serial.println("Sending status Devices to Hub");
        for(int i=1;i<6;i++){
             rfData[i]= EEPROM.read(i);
             Serial.print(rfData[i]);
             Serial.print(" ");
        }
        RF24NetworkHeader header00(master);             // (Address where the data is going)
        bool ok = network.write(header00, &rfData, sizeof(rfData)); // Send the data         
        Serial.println("\n-----------------------------------------");
}
