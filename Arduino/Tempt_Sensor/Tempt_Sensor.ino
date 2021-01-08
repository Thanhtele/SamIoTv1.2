#include <RF24.h>
#include <RF24Network.h>
#include <SPI.h>
#include <avr/sleep.h>
#include <avr/power.h>
#include <dht.h>

dht DHT;
#define DHT11_PIN 4
#define adc_disable() (ADCSRA &= ~(1<<ADEN))

byte wakeUp=22;
RF24 radio(9, 10);               // nRF24L01 (CE,CSN)
RF24Network network(radio);      // Include the radio in the network
const uint16_t this_node = 041;   // Address of our node in Octal format ( 04,031, etc)
const uint16_t master = 00;    // Address of the other node in Octal format
uint16_t rfData[6]={};
typedef enum { wdt_16ms = 0, wdt_32ms, wdt_64ms, wdt_128ms, wdt_250ms, wdt_500ms, wdt_1s, wdt_2s, wdt_4s, wdt_8s } wdt_prescalar_e;
void setup_watchdog(uint8_t prescalar);
void do_sleep(void);

void setup(){
  setup_watchdog(wdt_8s);                   
  adc_disable();
  SPI.begin();
  radio.begin();
  network.begin(90, this_node); //(channel, node address)
  radio.setDataRate(RF24_1MBPS);
  radio.powerDown();
}

void loop(){
  if (wakeUp == 22){  ///8*22=176second=3m
        int chk = DHT.read11(DHT11_PIN);
        rfData[4] = (uint16_t) (DHT.humidity);    
        rfData[5] = (uint16_t) (DHT.temperature);
        radio.powerUp();
        rfData[0]=2;
        RF24NetworkHeader header00(master);             // (Address where the data is going)
        network.write(header00, &rfData, sizeof(rfData)); // Send the data   
        wakeUp = 0;  
        radio.powerDown();   
   } 
   do_sleep();
}
void do_sleep(){
    adc_disable();        /// Tắt ADC
    set_sleep_mode(SLEEP_MODE_PWR_DOWN); // Setup Chế độ ngủ
    sleep_enable();
    sleep_mode();
    sleep_disable();
}
void setup_watchdog(uint8_t prescalar){
    uint8_t wdtcsr = prescalar & 7;
    if ( prescalar & 8 )   wdtcsr |= _BV(WDP3);
    MCUSR &= ~_BV(WDRF);                      
    WDTCSR = _BV(WDCE) | _BV(WDE);            
    WDTCSR = _BV(WDCE) | wdtcsr | _BV(WDIE);  
}
ISR(WDT_vect){
    wakeUp++;
}
