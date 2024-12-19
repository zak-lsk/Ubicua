
#include <WiFi.h>
#include <SPIFFS.h>
#include <PubSubClient.h>
#include <Wire.h>

#include <ESP32Servo.h>
#include <DHT.h>


#include <WiFi.h>
#include <TaskScheduler.h>

#include <ESP32TIME>

#define DHTPIN 18
#define DHTTYPE DHT11

//inicializar objeto DHT para el sensor temperatura
DHT dht(DHTPIN, DHTTYPE); 

// para el uso de los hilos
Scheduler scheduler; 
// Objeto de cliente WiFi
WiFiClient espClient;
PubSubClient client(espClient);

// Configuración de red WiFi
const char* ssid = "iPhone de Zakaria"; // Cambia por tu SSID de WiFi
const char* password = "zakizaki"; // Cambia por tu clave de WiFi

// Configuración de MQTT
const char* mqtt_server = "172.20.10.6"; // Cambia por la dirección de tu servidor MQTT
const int mqtt_port = 1883; // Puerto MQTT (por defecto es 1883)

const char* mqtt_user = "ubicua"; // Si tu servidor MQTT requiere autenticación
const char* mqtt_password = "ubicua"; // Si tu servidor MQTT requiere autenticación

// Declaramos la variable para controlar el servo
Servo servo_Ventana, servo_Paraguas;
// Declaración de pines
const int sensorLuz = 15; 
const  int sensorMov = 19; 
const int sensorGas = 4; 
const int sensorLluvia = 32; 
const int servoVentana = 13; 
const int servoParaguas = 12; 
const int ledSalon = 25; 
int valMov = 0;  
int valLuz = 0; 
float valTemp = 0; 
int valGas = 0;   
int valLluvia = 0; 
int abrir_ventana; 
int abrir_paragua; 
int encender_luz_salon; 



// Declaración de funciones para la lectura de datos
void readDHTtask(); 
void readSensorGas(); 
void readSensorLuz();
void readSensorMov(); 
void readSensorLluvia();
void mover_Servo_Ventana(); 
void mover_Servo_Paraguas(); 
void encender_led_salon(); 



// Declaración de hilos 
Task task_temp(2000, TASK_FOREVER, &readDHTtask, &scheduler, true);  // Hilo encargado de medir la temperatura de la casa
Task task_gas(5000, TASK_FOREVER, &readSensorGas, &scheduler, true); // Hilo encargado de detectar la presencia de gas 
Task task_luz(5000, TASK_FOREVER, &readSensorLuz, &scheduler, true); // Hilo para detectar la luz
Task task_mov(5000, TASK_FOREVER, &readSensorMov, &scheduler, true); // Hilo para la detección de movimiento en ciertas partes de la casa
Task task_lluvia(5000, TASK_FOREVER, &readSensorLluvia, &scheduler, true); 
Task task_servo_ventana(5000, TASK_FOREVER, &mover_Servo_Ventana, &scheduler, true); 
Task task_servo_paraguas(5000, TASK_FOREVER, &mover_Servo_Paraguas, &scheduler, true); 
Task task_led_salon(5000, TASK_FOREVER, &encender_led_salon, &scheduler, true);


void callback(char* topic, byte* payload, unsigned int length){
  String mensaje; 

  for(unsigned int i = 0; i < length; i++){
    mensaje += (char)payload[i]; 
  }

  if(String(topic) == "Casa/Salon/Ventana/Servo"){
    abrir_ventana = (mensaje == "true");
  }
  else if (String(topic) == "Casa/Entrada/Paraguas/Servo"){
    abrir_paragua = (mensaje == "true");
  }
  else if (String(topic) == "Casa/Salon/Luz"){
    encender_luz_salon = (mensaje == "true");
  }
}


void  setup() { // Void setup is ran only once after each powerup or reset of the Arduino  board.
  Serial.begin(9600);      
  Serial.println("Empezando test ");
  initWiFi();
  initMQTTServer();
  servo_Ventana.attach(servoVentana); 
  servo_Paraguas.attach(servoParaguas); 
  pinMode(sensorMov, INPUT); 
  pinMode(sensorLuz, INPUT); 
  pinMode(sensorGas, INPUT); 
  pinMode(sensorLluvia, INPUT); 
  pinMode(ledSalon, OUTPUT); 
  dht.begin();
  client.setCallback(callback); 
  client.subscribe("Casa/#");
  scheduler.startNow(); 
}

void loop(){ // Void loop is ran over and  over and consists of the main program.
  
  if (!client.connected()) {
    Serial.println("Intentando conectarse..."); 
    reconnect();
  }
  client.loop();
  scheduler.execute(); 
}


void readDHTtask(){
  float temperatura = dht.readTemperature(); 

  if(!isnan(temperatura)){

    char temperaturaStr[10]; 
    dtostrf(temperatura, 4, 2, temperaturaStr);

    char topic_temperatura[64]; 
    //strcpy(topic_temperatura, ""); 
    //strcat(topic_temperatura, "/Sensores/Temperatura");

    client.publish("Casa/Salon/Temperatura", temperaturaStr); 
  }
}

void readSensorGas(){
  // Leer el valor del sensor de gas
  valGas = digitalRead(sensorGas); 

  if(!isnan(valGas)){                       // Si no es NAN
    char topic_gas[64];                     // Array de char para especificar el nombre del topic
    char valGasStr[20]; 

    if(valGas == LOW){
      strcpy(valGasStr, "Hay gas");
    }

    else{
      strcpy(valGasStr, "No hay gas");
    }

    //strcpy(topic_gas, "");         // Copiar el nombre del servidor en el nombre del topic
    //strcat(topic_gas, "/Sensores/Gas");      // Concatenar para crear el nombre del topic completo

    client.publish("Casa/Salon/Gas", valGasStr);
  }
}


void readSensorLuz() {
  // Leer el valor del sensor de luz
  valLuz = digitalRead(sensorLuz);

  // No es necesario verificar si es NaN para digitalRead()
  char topic_Luz[64];  // Array de char para especificar el nombre del topic
  char valLuzStr[20];

  // Invertir la lógica para reflejar la lectura correcta del sensor
  if (valLuz == LOW) {
    Serial.println("Hay luz");
    strcpy(valLuzStr, "Hay luz");
  } else {
    Serial.println("No hay luz");
    strcpy(valLuzStr, "No hay luz");
  }

  // Construir el nombre completo del topic
  //strcpy(topic_Luz, "");         // Copiar el nombre del servidor en el nombre del topic
  //strcat(topic_Luz, "/Sensores/Luz");     // Concatenar para crear el nombre del topic del sensor de luz

  client.publish("Casa/Exterior/Luz", valLuzStr);
}



void readSensorMov(){
  // Leer el valor del sensor de presencia
  valMov = digitalRead(sensorMov); 

  if(!isnan(valMov)){                       // Si no es NAN
    char topic_Mov[64];                     // Array de char para especificar el nombre del topic
    char valMovStr[20]; 

    if(valMov == HIGH){
      strcpy(valMovStr, "Hay movimiento"); 
    }

    else{
      strcpy(valMovStr, "No hay movimiento"); 
    }

    //strcpy(topic_Mov, "");         // Copiar el nombre del servidor en el nombre del topic
    //strcat(topic_Mov, "/Sensores/Presencia");      // Concatenar para crear el nombre del topic del sensor de presencia 

    client.publish("Casa/Salon/Presencia", valMovStr);
  }
}

void readSensorLluvia(){

  valLluvia = digitalRead(sensorLluvia); 
  
  if(!isnan(valLluvia)){

    char topic_lluvia[64]; 
    char valLluviaStr[20]; 

    if(valLluvia == LOW){
      strcpy(valLluviaStr, "lluvia");
      client.publish("Casa/Salon/Ventana/Servo", "false");   //cerrar ventana
      client.publish("Casa/Entrada/Paraguas/Servo", "true");   //sacar paraguas
    }

    else{
      strcpy(valLluviaStr, "no lluvia");
      client.publish("Casa/Salon/Ventana/Servo", "true");    //abrir ventana
      client.publish("Casa/Entrada/Paraguas/Servo", "false");  //cerrar paraguas
    }

    //strcpy(topic_lluvia, "");         // Copiar el nombre del servidor en el nombre del topic
    //strcat(topic_lluvia, "/Sensores/Lluvia");      // Concatenar para crear el nombre del topic del sensor de presencia 

    client.publish("Casa/Exterior/Lluvia", valLluviaStr);
  }
}

void mover_Servo_Ventana(){

  if (abrir_ventana){
    servo_Ventana.write(100); 
  }
  else{
    servo_Ventana.write(0);
  }
}

void mover_Servo_Paraguas(){
  
  if(abrir_paragua){
    servo_Paraguas.write(0);
  }
  else{
    servo_Paraguas.write(90);
  }
}

void encender_led_salon(){
  if(encender_luz_salon){
    digitalWrite(ledSalon, HIGH); 
  }
  else{
    digitalWrite(ledSalon, LOW); 
  }
}

// para la concexión wifi
void reconnect() {
  while (!client.connected()) {
    Serial.println("Conectando al servidor MQTT...");
    if (client.connect("ESP32Client", mqtt_user, mqtt_password)) {
      Serial.println("Conexión MQTT exitosa");
      client.subscribe("Casa/#"); // Se suscribe al directorio raíz
    } else {
      Serial.println("Error en la conexión MQTT. Reintentando en 5 segundos...");
      delay(5000);
    }
  }
}

void initWiFi() {
  WiFi.begin(ssid, password);
  while(WiFi.status()!=WL_CONNECTED){
    delay(500);
  }
  Serial.println("Connected to WiFi");
  Serial.println(WiFi.localIP());
}


void initMQTTServer(){
  client.setServer(mqtt_server, mqtt_port);
  reconnect();
  client.publish("/Casa", "Hello from ESP32");
}

