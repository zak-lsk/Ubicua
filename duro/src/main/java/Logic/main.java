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

    }

}
