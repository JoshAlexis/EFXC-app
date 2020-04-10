#include <EEPROM.h>
#include <SoftwareSerial.h>
#define LED 13

char data[5];
int pos;
void setup() {
  pinMode(LED,OUTPUT);
  digitalWrite(LED,LOW);
  Serial.begin(9600);
}

void loop() {
  if(Serial.available()>0){
    while(Serial.available()>0){
      data[pos] = Serial.read(); 
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
    Serial.print("LED ON");
  }else if(data[0] == 'a'){
    digitalWrite(LED, LOW);
    Serial.print("LED OFF");
  }
}

void saveRelayStates(){
  char d;
  for(int i=1;i<5;i++){
    d = data[i];
    EEPROM.write(i,d);
    delay(500); 
  }
  Serial.print("s");
  Serial.flush();
  clearInput();
}

void readRelayStates(){
  char c;
  for(int i=1;i<5;i++){
    c = EEPROM.read(i);
    Serial.print(c);
    delay(500);
    Serial.flush();
  }
  clearInput();
}

void clearInput(){
   memset(data,0,sizeof(data));
}
