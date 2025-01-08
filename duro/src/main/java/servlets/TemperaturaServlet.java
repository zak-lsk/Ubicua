package servlets;

import com.google.gson.Gson;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/temperatura")
public class TemperaturaServlet extends HttpServlet {
    
    private static float ultimaTemperatura = 0.0f;
    private final Gson gson = new Gson();
    
    public static void actualizarTemperatura(float temperatura) {
        ultimaTemperatura = temperatura;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        TemperaturaResponse tempResponse = new TemperaturaResponse(ultimaTemperatura);
        String jsonResponse = gson.toJson(tempResponse);
        
        response.getWriter().write(jsonResponse);
    }
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        String dispositivo = request.getParameter("dispositivo");
//        String accion = request.getParameter("accion");
//        response.setContentType("application/json");
//        MQTTBroker broker = new MQTTBroker();
//
//        try {
//            switch (dispositivo) {
//                case "aire":
//                    if ("encender".equals(accion)) {
//                        MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_ACTIVAR, "true");
//                        MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_MODO, "true");
//                        Log.logmqtt.info("Aire acondicionado encendido.");
//                    } else if ("apagar".equals(accion)) {
//                        MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_ACTIVAR, "false");
//                        Log.logmqtt.info("Aire acondicionado apagado.");
//                    }
//                    break;
//
//                case "calefaccion":
//                    if ("encender".equals(accion)) {
//                        MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_ACTIVAR, "true");
//                        MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_MODO, "false");
//                        Log.logmqtt.info("Calefacción encendida.");
//                    } else if ("apagar".equals(accion)) {
//                        MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_ACTIVAR, "false");
//                        Log.logmqtt.info("Calefacción apagada.");
//                    }
//                    break;
//
//                default:
//                    Log.log.warn("Dispositivo no válido: {}", dispositivo);
//            }
//        } catch (Exception e) {
//            Log.log.warn("Error en TemperaturaServlet ", e);
//        }
//    }
    
    // Clase interna para la respuesta
    private static class TemperaturaResponse {
        private final float temperatura;
        
        public TemperaturaResponse(float temperatura) {
            this.temperatura = temperatura;
        }
    }
}
