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

/**
 *
 * @author zakil
 */
@WebServlet("/paraguas")
public class ParaguasServlet extends HttpServlet {

    private static final String TOPIC = "Casa/Entrada/Paraguas/Servo";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        response.setContentType("application/json");
        MQTTBroker broker = new MQTTBroker();

        try {

            switch (accion) {

                case "abrir":
                    MQTTPublisher.publish(broker, TOPIC, "true");
                    Log.logmqtt.info("Se ha actualizado el valor true "
                            + "en el topic {}", TOPIC);
                    break;

                case "cerrar":
                    MQTTPublisher.publish(broker, TOPIC, "false");
                    Log.logmqtt.info("Se ha actualizado el valor false "
                            + "en el topic {}", TOPIC);
                    break;

                default:
                    Log.log.warn("Acción de paraguas no válida: {}", accion);
            }
        } catch (Exception e) {
            Log.log.warn("Error en ParaguasServlet ", e);
        }
    }
}
