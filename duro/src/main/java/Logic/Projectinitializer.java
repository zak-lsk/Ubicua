package Logic;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import MQTT.MQTTBroker;
import MQTT.MQTTPublisher;
import MQTT.MQTTSubscriber;

@WebListener
public class Projectinitializer implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    @Override
    /**
     * ES: Metodo empleado para detectar la inicializacion del servidor	<br>
     * EN: Method used to detect server initialization
     *
     * @param sce <br>
     * ES: Evento de contexto creado durante el arranque del servidor	<br>
     * EN: Context event created during server launch
     */
    public void contextInitialized(ServletContextEvent sce) {
        Log.log.info("-->Suscribe Topics<--");
        MQTTBroker broker = new MQTTBroker();
        MQTTSubscriber suscriber = new MQTTSubscriber();
        suscriber.suscribeTopic(broker, "Casa/#");
        MQTTPublisher.publish(broker, "Casa/Test", "Hello from Tomcat :)");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
        MQTTPublisher.publish(broker, "Casa/Test", "Hola de nuevo (5segundos después)");
    }
}
