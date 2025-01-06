package Logic;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.File;

import MQTT.MQTTBroker;
import MQTT.MQTTPublisher;
import MQTT.MQTTSuscriber;
import Logic.Util; 

@WebListener
public class Projectinitializer implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //Al cerrar el servidor, eliminar los archivos de datos
        File directorioDatos = new File(WeatherAPI.RUTA_DATOS);
        if (directorioDatos.exists()) {
            directorioDatos.delete();
        }
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
        MQTTSuscriber suscriber = new MQTTSuscriber();
        suscriber.suscribeTopic(broker, "Casa/#");
        MQTTPublisher.publish(broker, Util.TOPIC_TEST, "Hello from Tomcat :)");
        
        //Despues de 5 segundos publicar un mensaje de prueba
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
        MQTTPublisher.publish(broker, Util.TOPIC_TEST, "Hola de nuevo (5segundos después)");
    }

    
}
