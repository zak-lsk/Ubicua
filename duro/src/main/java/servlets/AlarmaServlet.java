package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import MQTT.MQTTPublisher;
import MQTT.MQTTBroker;
import Logic.Log;
import Logic.Util; 

/**
 *
 * @author zakil
 */
@WebServlet("/alarma")
public class AlarmaServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        MQTTBroker broker = new MQTTBroker();

        try {
            switch (accion) {
                case "activar":
                    MQTTPublisher.publish(broker, Util.TOPIC_ALARMA_ACTIVAR, "true");
                    Log.logmqtt.info("Se ha actualizado el valor "
                            + "true en el topic {}", Util.TOPIC_ALARMA_ACTIVAR);
                    break;

                case "desactivar":
                    MQTTPublisher.publish(broker, Util.TOPIC_ALARMA_ACTIVAR, "false");
                    Log.logmqtt.info("Se ha actualizado el valor false "
                            + "en el topic {}", Util.TOPIC_ALARMA_ACTIVAR);
                    break;

                default:
                    Log.logmqtt.warn("Acción de alarma no válida: {}", accion);
            }
        } catch (Exception e) {
            Log.log.error("Error al cambiar estado de la alarma: {}", e.getMessage(), e);
        }
    }
}
