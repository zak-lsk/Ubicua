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
    
    // Clase interna para la respuesta
    private static class TemperaturaResponse {
        private final float temperatura;
        
        public TemperaturaResponse(float temperatura) {
            this.temperatura = temperatura;
        }
    }
}