package Logic;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
    public static final String CIUDAD = "Alcala de Henares,ES";
    public static final String BASE_PATH = System.getProperty("java.io.tmpdir");
    public static final String RUTA_DATOS = Paths.get(BASE_PATH, "SisdogarData").toString();
    public static final String RUTA_ACTUAL = Paths.get(RUTA_DATOS, "tiempo_actual.xml").toString();
    public static final String RUTA_PRONOSTICO_SEMANAL = Paths.get(RUTA_DATOS, "pronostico_semanal.xml").toString();
    public static final String RUTA_PRONOSTICO_DIARIO = Paths.get(RUTA_DATOS, "pronostico_diario.xml").toString();
    private static String CIUDADCODIFICADA, URLACTUUAL, URLPRONOSTICOSEMANAL, URLPRONOSTICODIARIO;

    public static void obtenerDatosTiempo() throws Exception {
        try {
            File directorioDatos = new File(RUTA_DATOS);
            if (!directorioDatos.exists()) {
                if (!directorioDatos.mkdirs()) {
                    throw new IOException("No se pudo crear el directorio en: " + directorioDatos.getAbsolutePath() + 
                                       ". Verificando permisos...");
                }
                Log.log.info("Directorio creado en: " + directorioDatos.getAbsolutePath());
            }

            // Verificar permisos de escritura
            if (!directorioDatos.canWrite()) {
                throw new IOException("No hay permisos de escritura en: " + directorioDatos.getAbsolutePath());
            }

            CIUDADCODIFICADA = URLEncoder.encode(CIUDAD, StandardCharsets.UTF_8.toString());

            // Inicializar URLs si no se ha hecho
            if (URLACTUUAL == null) {
                URLACTUUAL = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=es&mode=xml",
                    CIUDADCODIFICADA, API_KEY);
            }

            if (URLPRONOSTICOSEMANAL == null) {
                URLPRONOSTICOSEMANAL = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast/daily?q=%s&cnt=7&appid=%s&units=metric&lang=es&mode=xml",
                    CIUDADCODIFICADA, API_KEY);
            }

            if (URLPRONOSTICODIARIO == null) {
                URLPRONOSTICODIARIO = String.format(
                    "https://api.openweathermap.org/data/2.5/forecast?q=%s&cnt=40&appid=%s&units=metric&lang=es&mode=xml",
                    CIUDADCODIFICADA, API_KEY);
            }

            // Realizar peticiones y guardar archivos
            Document datosActuales = realizarPeticion(URLACTUUAL);
            guardarXML(datosActuales, RUTA_ACTUAL);

            Document pronosticoSemanal = realizarPeticion(URLPRONOSTICOSEMANAL);
            guardarXML(pronosticoSemanal, RUTA_PRONOSTICO_SEMANAL);

            Document pronosticoDiario = realizarPeticion(URLPRONOSTICODIARIO);
            guardarXML(pronosticoDiario, RUTA_PRONOSTICO_DIARIO);

            Log.log.info("Archivos creados correctamente en: " + RUTA_DATOS);
        } catch (Exception e) {
            Log.log.error("Error en obtenerDatosTiempo: " + e.getMessage());
            throw e;
        }
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
        System.out.println("Archivo creado correctamente en la ruta: " + ruta);
        transformer.transform(source, result);
    }

    public static double obtenerTemperaturaActual() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(RUTA_ACTUAL));

        Element tempElement = (Element) doc.getElementsByTagName("temperature").item(0);
        return Double.parseDouble(tempElement.getAttribute("value"));
    }

    //Método para obtener las temperaturas pronosticadas
    public static Map<LocalDate, Double> obtenerTemperaturaPronostico() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(RUTA_PRONOSTICO_SEMANAL));

        NodeList tiempos = doc.getElementsByTagName("time");
        TreeMap<LocalDate, Double> pronosticos = new TreeMap<>();

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

    //Métodos para obtener la descripción del tiempo pronosticado
    public static Map<LocalDate, String> obtenerTiempoPronostico() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(RUTA_PRONOSTICO_SEMANAL));

        NodeList tiempos = doc.getElementsByTagName("time");
        TreeMap<LocalDate, String> pronosticos = new TreeMap<>();

        for (int i = 0; i < tiempos.getLength(); i++) {
            Element tiempo = (Element) tiempos.item(i);
            String fechaStr = tiempo.getAttribute("day");
            LocalDate fecha = LocalDate.parse(fechaStr);
            Element symbolElement = (Element) tiempo.getElementsByTagName("symbol").item(0);
            String descripcion = symbolElement.getAttribute("name");
            pronosticos.put(fecha, descripcion);
        }
        return pronosticos;
    }

    public static List<PronosticoIntervalo> obtenerPronosticoPorIntervalos() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(RUTA_PRONOSTICO_DIARIO));

        NodeList tiempos = doc.getElementsByTagName("time");
        List<PronosticoIntervalo> pronosticos = new ArrayList<>();

        for (int i = 0; i < tiempos.getLength(); i++) {
            Element tiempo = (Element) tiempos.item(i);

            // Obtener fecha y hora
            String desde = tiempo.getAttribute("from");
            LocalDateTime fechaHora = LocalDateTime.parse(desde);

            // Obtener temperatura
            Element tempElement = (Element) tiempo.getElementsByTagName("temperature").item(0);
            double temperatura = Double.parseDouble(tempElement.getAttribute("value"));

            // Obtener descripción del tiempo
            Element symbolElement = (Element) tiempo.getElementsByTagName("symbol").item(0);
            String descripcion = symbolElement.getAttribute("name");

            pronosticos.add(new PronosticoIntervalo(fechaHora, temperatura, descripcion));
        }

        return pronosticos;
    }
    
    public static void actualizarDatosActuales() throws Exception {
        //Comprobamos si el directorio existe, si no, lo creamos
        File directorioDatos = new File(RUTA_DATOS);
        if (!directorioDatos.exists()) {
            if (!directorioDatos.mkdirs()) {
                throw new IOException("No se pudo crear el directorio: " + RUTA_DATOS);
            }
        }
        // Realizar la petición y guardar el archivo
        Document datosActuales = realizarPeticion(URLACTUUAL);
        guardarXML(datosActuales, RUTA_ACTUAL);
    }

    public static void actualizarPronosticoSemanal() throws Exception {
        //Comprobamos si el directorio existe, si no, lo creamos
        File directorioDatos = new File(RUTA_DATOS);
        if (!directorioDatos.exists()) {
            if (!directorioDatos.mkdirs()) {
                throw new IOException("No se pudo crear el directorio: " + RUTA_DATOS);
            }
        }
        // Realizar la petición y guardar el archivo
        Document pronosticoSemanal = realizarPeticion(URLPRONOSTICOSEMANAL);
        guardarXML(pronosticoSemanal, RUTA_PRONOSTICO_SEMANAL);
    }   
    
    
    
    public static void actualizarPronosticoDiario() throws Exception {
        //Comprobamos si el directorio existe, si no, lo creamos
        File directorioDatos = new File(RUTA_DATOS);
        if (!directorioDatos.exists()) {
            if (!directorioDatos.mkdirs()) {
                throw new IOException("No se pudo crear el directorio: " + RUTA_DATOS);
            }
        }
        // Realizar la petición y guardar el archivo
        Document pronosticoDiario = realizarPeticion(URLPRONOSTICODIARIO);
        guardarXML(pronosticoDiario, RUTA_PRONOSTICO_DIARIO);
    }
    
    public static class PronosticoIntervalo {
        private LocalDateTime fechaHora;
        private double temperatura;
        private String descripcion;

        public PronosticoIntervalo(LocalDateTime fechaHora, double temperatura, String descripcion) {
            this.fechaHora = fechaHora;
            this.temperatura = temperatura;
            this.descripcion = descripcion;
        }

        // Getters necesarios para la serialización JSON
        public LocalDateTime getFechaHora() { return fechaHora; }
        public double getTemperatura() { return temperatura; }
        public String getDescripcion() { return descripcion; }

        // Opcional: método para formatear la fecha/hora de manera más amigable
        public String getFechaHoraFormateada() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaHora.format(formatter);
        }
    }

}
