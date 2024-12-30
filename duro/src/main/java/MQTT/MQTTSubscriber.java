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

public class MQTTSubscriber implements MqttCallback {

    private static MqttClient sampleClient;

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
        newTopic.setValue(message.toString());
        newTopic.setIdTopic(topic);

        try {
            String playload = message.toString();               //sacar contenido del mensaje 
            String[] topicParts = topic.split("/");        //dividir el topic en partes
            if (topicParts.length < 2) {                         //todos los topics están hechos para tener 3 secciones
                Log.logmqtt.error("Formato del topic no válido");
                return;
            }
//            String zona = topicParts[1];
//                String sensor = topicParts[2];
            
            System.out.println("Mensaje recibido del topic " + topic + playload);
            //switch (sensor) {
            //    case "Temperatura":
            //        float temperatura = Float.parseFloat(playload);
            //        Logic.setDataTemperatura("Temperatura", temperatura, topicParts[0] + "/" + zona);
            //        Log.logmqtt.info("Temperatura almacenada: "
            //                + temperatura + " ºC" + " en zona " + zona);
            //        break;
//
            //    case "Gas":
            //        int hayGas = Integer.parseInt(playload);
            //        Logic.setDataGas("Gas", hayGas, topicParts[0] + "/" + zona);
            //        Log.logmqtt.info("Valor hayGas " + hayGas + " almacenado");
            //        break;
//
            //    case "Luz":
            //        int hayLuz = Integer.parseInt(playload);
            //        Logic.setDataLuz("Luz", hayLuz);
            //        Log.logmqtt.info("Valor hayLuz " + hayLuz + " almacenado");
            //        break;
//
            //    case "Lluvia":
            //        int hayLluvia = Integer.parseInt(playload);
            //        Logic.setDataLuz("Lluvia", hayLluvia);
            //        Log.logmqtt.info("Valor hayLluvia " + hayLluvia + " almacenado");
            //        break;
//
            //    case "Presencia":
            //        int hayMovimiento = Integer.parseInt(playload);
            //        Logic.setDataMovimiento("Movimiento", hayMovimiento, topicParts[0] + "/" + zona);
            //        Log.logmqtt.info("Valor hayMovimiento " + hayMovimiento + " almacenado");
            //        break;
//
            //    default:
            //        Log.logmqtt.warn("Sensor no reconocido");
            //}
        } catch (NumberFormatException e) {
            Log.logmqtt.error("Valor recibido no válido: " + message.toString(), e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

}
