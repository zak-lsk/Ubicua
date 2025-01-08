
#include <WiFi.h>
#include <SPIFFS.h>
#include <PubSubClient.h>
#include <Wire.h>

#include <ESP32Servo.h>
#include <DHT.h>


#include <WiFi.h>
#include <TaskScheduler.h>



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
//const char* ssid = "iPhone de Zakaria";  // Cambia por tu SSID de WiFi
//const char* password = "zaki2004";       // Cambia por tu clave de WiFi



// Configuración de MQTT
const char* mqtt_server = "192.168.1.133";  // Cambia por la dirección de tu servidor MQTT
const int mqtt_port = 1883;               // Puerto MQTT (por defecto es 1883)

const char* mqtt_user = "ubicua";      // Si tu servidor MQTT requiere autenticación
const char* mqtt_password = "ubicua";  // Si tu servidor MQTT requiere autenticación

// Declaramos la variable para controlar el servo
Servo servo_Ventana, servo_Paraguas, servo_Ventilacion;
// Declaración de pines
const int sensorLuz = 15;
const int sensorMov = 19;
const int sensorGas = 4;
const int sensorLluvia = 32;
const int servoVentana = 13;
const int servoParaguas = 12;
const int ledSalon = 25;
const int buzzer = 27;
const int servoVentilacion = 26;
int valMov = 0;
int valLuz = 0;
float valTemp = 0;
int valGas = 0;
int valLluvia = 0;
int abrir_ventana;
int abrir_paragua;
int encender_luz_salon;
bool sonar_buzzer;
bool encender_ventilacion;
bool modo_ventilacion;
int estadoPrevioLluvia = -1;  // Estado previo del sensor de lluvia
int estadoPrevioLuz = -1;     // Estado previo del sensor de luz
int estadoPrevioMov = -1;     // Estado previo del sensor de movimiento
int estadoPrevioGas = -1;     // Estado previo del sensor de gas


// Declaración de funciones para la lectura de datos
void readDHTtask();
void readSensorGas();
void readSensorLuz();
void readSensorMov();
void readSensorLluvia();
void mover_Servo_Ventana();
void mover_Servo_Paraguas();
void encender_led_salon();
void manejar_buzzer();  //función para activar/desactivar el buzzer
void mover_Servo_Ventilacion();



// Declaración de hilos
Task task_temp(5000, TASK_FOREVER, &readDHTtask, &scheduler, true);  // Hilo encargado de medir la temperatura de la casa
Task task_gas(30000, TASK_FOREVER, &readSensorGas, &scheduler, true);   // Hilo encargado de detectar la presencia de gas
Task task_luz(480000, TASK_FOREVER, &readSensorLuz, &scheduler, true);  // Hilo para detectar la luz
Task task_mov(5000, TASK_FOREVER, &readSensorMov, &scheduler, true);   // Hilo para la detección de movimiento en ciertas partes de la casa
Task task_lluvia(300000, TASK_FOREVER, &readSensorLluvia, &scheduler, true);
Task task_servo_ventana(5000, TASK_FOREVER, &mover_Servo_Ventana, &scheduler, true);
Task task_servo_paraguas(5000, TASK_FOREVER, &mover_Servo_Paraguas, &scheduler, true);
Task task_led_salon(5000, TASK_FOREVER, &encender_led_salon, &scheduler, true);
Task task_buzzer(5000, TASK_FOREVER, &manejar_buzzer, &scheduler, true);                      // Hilo encargado para activar/desactivar el buzzer
Task task_servo_ventilacion(5000, TASK_FOREVER, &mover_Servo_Ventilacion, &scheduler, true);  // Hilo para mover el servomotro que simula la ventilacion


void callback(char* topic, byte* payload, unsigned int length) {
  String mensaje;

  for (unsigned int i = 0; i < length; i++) {
    mensaje += (char)payload[i];
  }

  if (String(topic) == "Casa/Salon/Ventana/Servo") {
    abrir_ventana = (mensaje == "true");
  } else if (String(topic) == "Casa/Entrada/Paraguas/Servo") {
    abrir_paragua = (mensaje == "true");
  } else if (String(topic) == "Casa/Salon/Luz") {
    encender_luz_salon = (mensaje == "true");
  } else if (String(topic) == "Casa/Salon/Alarma/Sonar") {
    sonar_buzzer = (mensaje == "true");
  } else if (String(topic) == "Casa/Salon/Ventilacion/Activado") {
    encender_ventilacion = (mensaje == "true");
  } else if (String(topic) == "Casa/Salon/Ventilacion/Modo") {
    modo_ventilacion = (mensaje == "true");
  }
}


void setup() {  // Void setup is ran only once after each powerup or reset of the Arduino  board.
  Serial.begin(9600);
  Serial.println("Empezando test ");
  initWiFi();
  initMQTTServer();
  servo_Ventana.attach(servoVentana);
  servo_Paraguas.attach(servoParaguas);
  servo_Ventilacion.attach(servoVentilacion);
  pinMode(sensorMov, INPUT);
  pinMode(sensorLuz, INPUT);
  pinMode(sensorGas, INPUT);
  pinMode(sensorLluvia, INPUT);
  pinMode(ledSalon, OUTPUT);
  pinMode(buzzer, OUTPUT);
  dht.begin();
  client.setCallback(callback);
  client.subscribe("Casa/#");
  scheduler.startNow();
}

void loop() {  // Void loop is ran over and  over and consists of the main program.

  if (!client.connected()) {
    Serial.println("Intentando conectarse...");
    reconnect();
  }
  client.loop();
  scheduler.execute();
}


void readDHTtask() {
  float temperatura = dht.readTemperature();

  if (!isnan(temperatura)) {

    char temperaturaStr[10];
    dtostrf(temperatura, 4, 2, temperaturaStr);

    char topic_temperatura[64];
    //strcpy(topic_temperatura, "");
    //strcat(topic_temperatura, "/Sensores/Temperatura");

    client.publish("Casa/Salon/Temperatura", temperaturaStr);
  }
}

void readSensorGas() {
  int estadoActualGas = digitalRead(sensorGas);
  if (estadoActualGas != estadoPrevioGas) {
    char valGasStr[20];
    if (estadoActualGas == LOW) {
      strcpy(valGasStr, "1");  // Gas detectado
    } else {
      strcpy(valGasStr, "0");  // Sin gas
    }
    client.publish("Casa/Salon/Gas", valGasStr);
    estadoPrevioGas = estadoActualGas;  // Actualizar el estado previo
  }
}


void readSensorLuz() {
  int estadoActualLuz = digitalRead(sensorLuz);
  if (estadoActualLuz != estadoPrevioLuz) {
    char valLuzStr[20];
    if (estadoActualLuz == LOW) {
      strcpy(valLuzStr, "1");  // Hay luz
    } else {
      strcpy(valLuzStr, "0");  // No hay luz
    }
    client.publish("Casa/Exterior/Luz", valLuzStr);
    estadoPrevioLuz = estadoActualLuz;  // Actualizar el estado previo
  }
}



void readSensorMov() {
  int estadoActualMov = digitalRead(sensorMov);
  if (estadoActualMov != estadoPrevioMov) {
    char valMovStr[20];
    if (estadoActualMov == HIGH) {
      strcpy(valMovStr, "1");  // Movimiento detectado
    } else {
      strcpy(valMovStr, "0");  // Sin movimiento
    }
    client.publish("Casa/Salon/Presencia", valMovStr);
    estadoPrevioMov = estadoActualMov;  // Actualizar el estado previo
  }
}


void readSensorLluvia() {
  int estadoActualLluvia = digitalRead(sensorLluvia);
  if (estadoActualLluvia != estadoPrevioLluvia) {
    char valLluviaStr[20];
    if (estadoActualLluvia == LOW) {
      strcpy(valLluviaStr, "1");
    } else {
      strcpy(valLluviaStr, "0");
    }
    client.publish("Casa/Exterior/Lluvia", valLluviaStr);
    estadoPrevioLluvia = estadoActualLluvia;  // Actualizar el estado previo
  }
}

void mover_Servo_Ventana() {

  if (abrir_ventana) {
    servo_Ventana.write(100);
  } else {
    servo_Ventana.write(0);
  }
}

void mover_Servo_Paraguas() {

  if (abrir_paragua) {
    servo_Paraguas.write(0);
  } else {
    servo_Paraguas.write(90);
  }
}

void encender_led_salon() {
  if (encender_luz_salon) {
    digitalWrite(ledSalon, HIGH);
  } else {
    digitalWrite(ledSalon, LOW);
  }
}


void manejar_buzzer() {
  if (sonar_buzzer) {
    tone(buzzer, 400);
  } else {
    noTone(buzzer);
  }
}

void mover_Servo_Ventilacion() {
  if (!encender_ventilacion) {
    // Si no está activada la ventilación, detener el servo
    servo_Ventilacion.writeMicroseconds(1500);
    servo_Ventilacion.detach(); 
    Serial.println("Ventilación apagada");
  } else {
    servo_Ventilacion.attach(servoVentilacion); 
      // Si está activada, comprobar el modo para decidir la dirección
      if (modo_ventilacion) {
        // Modo true: Gira en sentido antihorario (simula aire acondicionado)
        servo_Ventilacion.writeMicroseconds(2000);
        Serial.println("Ventilación en modo aire acondicionado (antihorario)");
      } else {
        // Modo false: Gira en sentido horario (simula calefacción)
        servo_Ventilacion.writeMicroseconds(1300);
        Serial.println("Ventilación en modo calefacción (horario)");
    }
  }
}




// para la concexión wifi
void reconnect() {
  while (!client.connected()) {
    Serial.println("Conectando al servidor MQTT...");
    if (client.connect("ESP32Client", mqtt_user, mqtt_password)) {
      Serial.println("Conexión MQTT exitosa");
      client.subscribe("Casa/#");  // Se suscribe al directorio raíz
    } else {
      Serial.println("Error en la conexión MQTT. Reintentando en 5 segundos...");
      delay(5000);
    }
  }
}

void initWiFi() {
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
  Serial.println("Connected to WiFi");
  Serial.println(WiFi.localIP());
}


void initMQTTServer() {
  client.setServer(mqtt_server, mqtt_port);
  reconnect();
  client.publish("Casa/Test", "Hello from ESP32");
}
