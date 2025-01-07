package MQTT;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import db.Topics;
import Logic.*;

public class MQTTSuscriber implements MqttCallback {

    private static MqttClient sampleClient;
    private MQTTBroker broker = new MQTTBroker();

    public void suscribeTopic(MQTTBroker broker, String topic) {
        Log.logmqtt.debug("Suscribe to topics");
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            // Crear un ID único para el subscriber
            String clientId = MQTTBroker.getClientId() + "_sub_" + System.currentTimeMillis();

            sampleClient = new MqttClient(MQTTBroker.getBroker(), clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(MQTTBroker.getUsername());
            connOpts.setPassword(MQTTBroker.getPassword().toCharArray());
            connOpts.setCleanSession(false);
            connOpts.setKeepAliveInterval(120);
            connOpts.setConnectionTimeout(60);
            connOpts.setAutomaticReconnect(true);  // Añadir reconexión automática

            // Establecer el callback antes de conectar
            sampleClient.setCallback(this);

            Log.logmqtt.debug("Mqtt Connecting to broker: " + MQTTBroker.getBroker());
            sampleClient.connect(connOpts);
            Log.logmqtt.debug("Mqtt Connected");

            sampleClient.subscribe(topic, 2);
            Log.logmqtt.info("Subscribed to {}", topic);

        } catch (MqttException me) {
            Log.logmqtt.error("Error suscribing topic: {}", me);
        } catch (Exception e) {
            Log.logmqtt.error("Error suscribing topic: {}", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.logmqtt.warn("Connection lost :", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.logmqtt.info("Mensaje recibido {}: {}", topic, message.toString());
        Topics newTopic = new Topics();
        String payload = message.toString();               //sacar contenido del mensaje 
        newTopic.setValue(message.toString());
        newTopic.setIdTopic(topic);

        try {
            String[] topicParts = topic.split("/");        //dividir el topic en partes
            String zona, sensor;

            // Verificar el formato del topic
            if (topicParts.length < 2) {
                Log.logmqtt.error("Formato del topic no válido");
                return;
            }

            // Topic de prueba
            if (topic.equals("Casa/Test")) {
                Log.logmqtt.info("Test message received: {}", payload);
                return;
            }

            //Estructura de los topics : Casa/[Zona]/[Sensor:ya sea un sensor o un elemento]/[Tipo]
            zona = topicParts[1];
            sensor = topicParts[2];

            switch (zona) {
                case "Salon":
                    handleSalon(sensor, topicParts, payload);
                    break;

                case "Exterior":
                    handleExterior(sensor, payload);
                    break;

                case "Entrada":
                    handleEntrada(sensor, topicParts, payload);
                    break;

                default:
                    Log.logmqtt.warn("Zona no reconocida: {}", zona);
            }
        } catch (NumberFormatException e) {
            Log.logmqtt.error("Valor recibido no válido: " + message.toString(), e);
        }
    }

    private void handleSalon(String sensor, String[] topicParts, String payload) {
        switch (sensor) {
            case "Temperatura":
                float temperatura = Float.parseFloat(payload);
                Logic.setDataTemperatura("Temperatura", temperatura, topicParts[0] + "/" + topicParts[1]);
                Logic.setDataPrueba("prueba", temperatura, "Casa/Salon");
                Log.logmqtt.info("Temperatura almacenada: "
                        + temperatura + " ºC" + " en salón ");
                Log.logdb.info("Valor de temperatura almacenado en la base de datos");
                servlets.TemperaturaServlet.actualizarTemperatura(temperatura);
                Log.log.info("Valor de temperatura actualizado en el html");
                break;

            case "Gas":
                int hayGas = Integer.parseInt(payload);
                Logic.setDataGas("Gas", hayGas, topicParts[0] + "/" + topicParts[1]);
                Log.logmqtt.info("Valor hayGas " + hayGas + " almacenado");
                Log.logdb.info("Valor de gas almacenado en la base de datos");
                break;

            case "Presencia":
                int hayMovimiento = Integer.parseInt(payload);
                Util.ESTADO_PRESENCIA = hayMovimiento; 
                //Guardar el valor de movimiento en caso de que haya cambiado
                if (hayMovimiento != Util.ESTADO_ANTERIOR_PRESENCIA) {
                    Logic.setDataMovimiento("Movimiento", hayMovimiento, topicParts[0] + "/" + topicParts[1]);
                    Log.logmqtt.info("Valor hayMovimiento " + hayMovimiento + " almacenado");
                    Log.logdb.info("Valor de movimiento almacenado en la base de datos");
                    Util.ESTADO_ANTERIOR_PRESENCIA = hayMovimiento;
                }

                break;

            case "Ventana":
                if (topicParts.length > 3 && topicParts[3].equals("Servo")) {
                    boolean estadoVentana = Boolean.parseBoolean(payload);
                    Util.ESTADO_VENTANA = estadoVentana; 
                    Log.logmqtt.info("Estado de ventana actualizado ", estadoVentana);
                }
                break;

            case "Alarma":
                if (topicParts.length > 3) {
                    switch (topicParts[3]) {
                        case "Activada":
                            Util.ESTADO_ALARMA = Boolean.parseBoolean(payload);
                            Log.logmqtt.info("Estado de alarma actualizado: {}", Util.ESTADO_ALARMA);
                            //Si se desactiva y antes estaba activada
                            if (!Util.ESTADO_ALARMA && Util.ESTADO_ANTERIOR_ALARMA) {
                                try {
                                    MQTTPublisher.publish(broker, Util.TOPIC_ALARMA_SONAR, "false");
                                    Util.ESTADO_ANTERIOR_ALARMA = false;
                                    Log.logmqtt.info("Sonido de alarma detenido "
                                            + "por desactivación");
                                } catch (Exception e) {
                                    Log.log.warn("Error al detener sonido de "
                                            + "alarma: {}", e.getMessage());
                                }
                            }
                            break;
                    }

                }
                break;
            
            case "Ventilacion": 
                switch (topicParts[3]) {
                    case "Activado": 
                        Util.ESTADO_VENTILACION = Boolean.parseBoolean(payload); 
                        Log.logmqtt.info("Estado de ventilación actualizado: "
                                + "{}", Util.ESTADO_VENTILACION);
                        break; 
                    case "Modo": 
                        Util.MODO_VENTILACION = Boolean.parseBoolean(payload); 
                        Log.logmqtt.info("Modo de ventilación actualizado: "
                                + "{}", Util.MODO_VENTILACION); 
                }
                break; 
                
            default:
                Log.logmqtt.warn("Sensor no reconocido");
        }
    }

    private void handleExterior(String sensor, String payload) {
        switch (sensor) {
            case "Luz":
                int hayLuz = Integer.parseInt(payload);
                Logic.setDataLuz("Luz", hayLuz);
                Log.logmqtt.info("Valor hayLuz " + hayLuz + " almacenado");
                Log.logdb.info("Valor de luz almacenado en la base de datos");
                break;

            case "Lluvia":
                int hayLluvia = Integer.parseInt(payload);
                Logic.setDataLluvia("Lluvia", hayLluvia);
                Log.logmqtt.info("Valor hayLluvia " + hayLluvia + " almacenado");
                Log.logdb.info("Valor de lluvia almacenado en la base de datos");
                break;

            default:
                Log.logmqtt.warn("Sensor no reconocido en Exterior: {}", sensor);

        }
    }

    private void handleEntrada(String sensor, String[] topicParts, String payload) {
        if (sensor.equals("Paraguas")) {

            if (topicParts.length > 3 && topicParts[3].equals("Servo")) {

                boolean estadoParaguas = Boolean.parseBoolean(payload);

                Log.logmqtt.info("Estado Paraguas actualizado : {}",
                        estadoParaguas);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}
