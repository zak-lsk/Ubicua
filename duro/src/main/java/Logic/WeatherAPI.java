package Logic;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WeatherAPI {
    private static final String API_KEY = "685943c417537f5b313fbfda92eca2b0";
    private static final String CIUDAD = "Alcala de Henares,ES";
    private static final String BASE_PATH = System.getProperty("user.dir");
    private static final String RUTA_DATOS = Paths.get(BASE_PATH, "Datos").toString();
    private static final String RUTA_ACTUAL = Paths.get(RUTA_DATOS, "tiempo_actual.xml").toString();
    private static final String RUTA_PRONOSTICO = Paths.get(RUTA_DATOS, "pronostico.xml").toString();
    
    public static void obtenerDatosTiempo() throws Exception {
        File directorioDatos = new File(RUTA_DATOS);
        if (!directorioDatos.exists()) {
            if (!directorioDatos.mkdirs()) {
                throw new IOException("No se pudo crear el directorio: " + RUTA_DATOS);
            }
            System.out.println("Directorio creado: " + RUTA_DATOS);
        }

        String ciudadCodificada = URLEncoder.encode(CIUDAD, StandardCharsets.UTF_8.toString());
        
        String urlActual = String.format(
            "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=es&mode=xml",
            ciudadCodificada, API_KEY);
        
        String urlPronostico = String.format(
            "https://api.openweathermap.org/data/2.5/forecast/daily?q=%s&cnt=7&appid=%s&units=metric&lang=es&mode=xml",
            ciudadCodificada, API_KEY);
            
        Document datosActuales = realizarPeticion(urlActual);
        guardarXML(datosActuales, RUTA_ACTUAL);
        
        Document pronostico = realizarPeticion(urlPronostico);
        guardarXML(pronostico, RUTA_PRONOSTICO);
        
        System.out.println("Datos guardados en:");
        System.out.println("- Tiempo actual: " + new File(RUTA_ACTUAL).getAbsolutePath());
        System.out.println("- Pronóstico: " + new File(RUTA_PRONOSTICO).getAbsolutePath());
    }
    
    private static Document realizarPeticion(String urlString) throws Exception {
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
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(response.toString())));
            
        } finally {
            connection.disconnect();
        }
    }
    
    private static void guardarXML(Document doc, String ruta) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(ruta));
        transformer.transform(source, result);
    }
    
    public static double obtenerTemperaturaActual() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(RUTA_ACTUAL));
        
        Element tempElement = (Element) doc.getElementsByTagName("temperature").item(0);
        return Double.parseDouble(tempElement.getAttribute("value"));
    }
    
    public static Map<LocalDate, Double> obtenerTemperaturaPronostico() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(RUTA_PRONOSTICO));
        
        NodeList tiempos = doc.getElementsByTagName("time");
        Map<LocalDate, Double> pronosticos = new HashMap<>();
        
        for (int i = 0; i < tiempos.getLength(); i++) {
            Element tiempo = (Element) tiempos.item(i);
            String fechaStr = tiempo.getAttribute("day");
            LocalDate fecha = LocalDate.parse(fechaStr);
            
            Element tempElement = (Element) tiempo.getElementsByTagName("temperature").item(0);
            double temperatura = Double.parseDouble(tempElement.getAttribute("day"));
            
            pronosticos.put(fecha, temperatura);
        }
        
        return pronosticos;
    }
} 