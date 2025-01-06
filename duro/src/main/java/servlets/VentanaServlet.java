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
@WebServlet("/ventana")
public class VentanaServlet extends HttpServlet {

    
    private static String estadoVentana = "cerrar"; // Estado inicial de la ventana

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion"); // Recoger la acción asociada
        MQTTBroker broker = new MQTTBroker();
        Log.log.info("Acción recibida: " + accion);

        try {
            if (accion == null || (!accion.equals("abrir") && !accion.equals("cerrar"))) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
                Log.log.warn("Acción no válida en abrir/cerrar ventanas");
                return;
            }

            if (!accion.equals(estadoVentana)) {
                // Publica solo si el estado es diferente
                String valor = accion.equals("abrir") ? "true" : "false";
                MQTTPublisher.publish(broker, Util.TOPIC_VENTANA, valor);
                Log.logmqtt.info("Se ha publicado valor {} en el topic {}", valor, Util.TOPIC_VENTANA);
                
                // Actualiza el estado actual de la ventana
                estadoVentana = accion;
            } else {
                Log.logmqtt.info("No se publica nada, el estado ya es: {}", estadoVentana);
            }
        } catch (Exception e) {
            Log.log.warn("Error en VentanaServlet ", e);
        }
    }
}
