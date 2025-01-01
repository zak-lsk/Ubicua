package Logic;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherAPI {
    private static final String API_KEY = "685943c417537f5b313fbfda92eca2b0";
    private static final String CIUDAD = "Alcala de Henares,ES";
    private static final String RUTA_ACTUAL = "Ubicua/Datos/tiempo_actual.json";
    private static final String RUTA_PRONOSTICO = "Ubicua/Datos/pronostico.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void obtenerDatosTiempo() throws Exception {
        // Obtener datos actuales
        String urlActual = String.format(
            "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=es",
            CIUDAD, API_KEY);
        
        // Obtener pronóstico
        String urlPronostico = String.format(
            "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=es",
            CIUDAD, API_KEY);
            
        // Guardar datos actuales
        JsonObject datosActuales = realizarPeticion(urlActual);
        guardarJSON(datosActuales, RUTA_ACTUAL);
        
        // Guardar pronóstico
        JsonObject pronostico = realizarPeticion(urlPronostico);
        guardarJSON(pronostico, RUTA_PRONOSTICO);
    }
    
    private static JsonObject realizarPeticion(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            return JsonParser.parseString(response.toString()).getAsJsonObject();
            
        } finally {
            connection.disconnect();
        }
    }
    
    private static void guardarJSON(JsonObject json, String ruta) throws IOException {
        File file = new File(ruta);
        file.getParentFile().mkdirs(); // Crear directorios si no existen
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        }
    }
    
    // Métodos de utilidad para obtener temperaturas
    public static double obtenerTemperaturaActual() throws Exception {
        try (Reader reader = new FileReader(RUTA_ACTUAL)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.getAsJsonObject("main").get("temp").getAsDouble();
        }
    }
    
    public static double obtenerTemperaturaPronostico() throws Exception {
        try (Reader reader = new FileReader(RUTA_PRONOSTICO)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.getAsJsonArray("list").get(0).getAsJsonObject()
                      .getAsJsonObject("main").get("temp").getAsDouble();
        }
    }
} 