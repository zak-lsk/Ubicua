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
@WebServlet("/paraguas")
public class ParaguasServlet extends HttpServlet {

    

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        response.setContentType("application/json");
        MQTTBroker broker = new MQTTBroker();

        try {

            switch (accion) {

                case "abrir":
                    MQTTPublisher.publish(broker, Util.TOPIC_PARAGUAS, "true");
                    Log.logmqtt.info("Se ha actualizado el valor true "
                            + "en el topic {}", Util.TOPIC_PARAGUAS);
                    Util.ESTADO_PARAGUAS = true;
                    break;

                case "cerrar":
                    MQTTPublisher.publish(broker, Util.TOPIC_PARAGUAS, "false");
                    Log.logmqtt.info("Se ha actualizado el valor false "
                            + "en el topic {}", Util.TOPIC_PARAGUAS);
                    Util.ESTADO_PARAGUAS = false;
                    break;

                default:
                    Log.log.warn("Acción de paraguas no válida: {}", accion);
            }
        } catch (Exception e) {
            Log.log.warn("Error en ParaguasServlet ", e);
        }
    }
}
