#include <EEPROM.h>
#include <SoftwareSerial.h>
#define LED 13
SoftwareSerial bts(10,11);
char data[5];
int pos;
void setup() {
  pinMode(LED,OUTPUT);
  digitalWrite(LED,LOW);
  bts.begin(38400);
}

void loop() {
  /*if(bts.available())
    Serial.write(bts.read());
  if(Serial.available())
    bts.write(Serial.read());*/
  if(bts.available()>0){
    while(bts.available()>0){
      data[pos] = bts.read(); 
      pos++;
      delay(500);
    }
    if(pos == 4 || pos == 1){
      pos = 0;
    }
    switch(data[0]){
          case 'e':
          case 'a':
            ledChangeState();
            break;
          case 's':
            saveRelayStates();
            break;
          case 'r':
            readRelayStates();
            break;
    }
  }
}

void ledChangeState(){
  if(data[0] == 'e'){
    digitalWrite(LED, HIGH);
    bts.print("LED ON");
  }else if(data[0] == 'a'){
    digitalWrite(LED, LOW);
    bts.print("LED OFF");
  }
}

void saveRelayStates(){
  char d;
  for(int i=1;i<5;i++){
    d = data[i];
    EEPROM.write(i,d);
    delay(500); 
  }
  bts.print("s");
  bts.flush();
  clearInput();
}

void readRelayStates(){
  char c;
  for(int i=1;i<5;i++){
    c = EEPROM.read(i);
    bts.print(c);
    delay(500);
    bts.flush();
  }
  clearInput();
}

void clearInput(){
   memset(data,0,sizeof(data));
}
