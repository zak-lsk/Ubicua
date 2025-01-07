package servlets;

import Logic.Inteligencia;
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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        MQTTBroker broker = new MQTTBroker();

        try {
            switch (accion) {
                case "activar":
                    MQTTPublisher.publish(broker, Util.TOPIC_ALARMA_ACTIVAR, "true");
                    Log.logmqtt.info("Se ha actualizado el valor 'true' en el topic {}", Util.TOPIC_ALARMA_ACTIVAR);
                    response.getWriter().write("{\"mensaje\": \"Alarma activada\"}");
                    break;

                case "desactivar":
                    Inteligencia.desactivarAlarma();
                    response.getWriter().write("{\"mensaje\": \"Alarma desactivada\"}");
                    break;

                case "sonar":
                    Inteligencia.sonarAlarma();
                    response.getWriter().write("{\"mensaje\": \"Alarma sonando\"}");
                    break;

                case "verificar":
                    String estadoAlarma = Inteligencia.gestionarAlarma();
                    response.getWriter().write(estadoAlarma);
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"Acción no válida\"}");
            }
        } catch (Exception e) {
            Log.log.error("Error en el servlet de alarma: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error en el servidor\"}");
        }
    }
}
