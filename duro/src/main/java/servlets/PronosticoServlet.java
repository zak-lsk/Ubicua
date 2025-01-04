package servlets;

import Logic.Log;
import Logic.WeatherAPI;
import Logic.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.ProcessBuilder.Redirect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/pronostico")
public class PronosticoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            //Siempre obtener los últimos datos de la API
            Log.log.info("Obteniendo nuevos datos del pronóstico del tiempo...");
            WeatherAPI.obtenerDatosTiempo();

            try {
                List<WeatherAPI.PronosticoIntervalo> pronosticos
                        = WeatherAPI.obtenerPronosticoPorIntervalos();

                if (pronosticos == null || pronosticos.isEmpty()) {
                    Log.log.warn("No se encontraron pronósticos");
                    response.getWriter().write("{\"error\": \"No se encontraron pronósticos\"}");
                    return;
                }
                // Registrar el adaptador para LocalDateTime
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .create();

                String jsonResponse = gson.toJson(pronosticos);
                response.getWriter().write(jsonResponse);
                Log.log.info("Pronóstico enviado correctamente");
            } catch (Exception e) {
                Log.log.error("Error al procesar el pronóstico: " + e.getMessage());
                response.getWriter().write("{\"error\": \"Error al procesar el pronóstico: " + e.getMessage() + "\"}");
            }

        } catch (Exception e) {
            Log.log.error("Error general: " + e.getMessage());
            response.getWriter().write("{\"error\": \"Error general: " + e.getMessage() + "\"}");
        }
    }
}
