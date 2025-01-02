package Logic;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zakil
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        WeatherAPI.obtenerDatosTiempo();
        double tempAct = WeatherAPI.obtenerTemperaturaActual();
        Map<LocalDate, Double> pronosticos = WeatherAPI.obtenerTemperaturaPronostico();
        for (Map.Entry<LocalDate, Double> entrada : pronosticos.entrySet()) {
            System.out.printf("Fecha: %s, Temperatura: %.2f°C%n",
                    entrada.getKey(), entrada.getValue());
        }
        Map<LocalDate, String> tiempo = WeatherAPI.obtenerTiempoPronostico();
        for (Map.Entry<LocalDate, String> entrada : tiempo.entrySet()) {
            System.out.printf("Fecha: %s, Descripción: %s%n",
                    entrada.getKey(), entrada.getValue());
        }
        List<WeatherAPI.PronosticoIntervalo> pronosticosDiarios = WeatherAPI.obtenerPronosticoPorIntervalos(); 
        for(WeatherAPI.PronosticoIntervalo p : pronosticosDiarios){
            System.out.printf("Fecha y hora: %s,  Temperatura: %s,  Descripción: %s\n", 
                    p.getFechaHora(), p.getTemperatura(), p.getDescripcion());
        }
        
        WeatherAPI.actualizarPronosticoDiario(); 
        for(WeatherAPI.PronosticoIntervalo p : pronosticosDiarios){
            System.out.printf("Fecha y hora: %s,  Temperatura: %s,  Descripción: %s\n", 
                    p.getFechaHora(), p.getTemperatura(), p.getDescripcion());
        }
    }

}
