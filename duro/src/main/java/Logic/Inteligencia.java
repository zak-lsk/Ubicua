package Logic;

import java.util.ArrayList;
import java.util.List;

public class Inteligencia {

    public static String compararTemperaturaConSensor() {
        try {
            // Verificar si el usuario está en casa o va a llegar pronto
            boolean usuarioPresente = usuarioEnCasa() || usuarioLlegaraPronto();

            if (!usuarioPresente) {
                Log.log.info("El usuario no está en casa ni se espera que llegue pronto. No se activa el sistema.");
                return "Usuario ausente. No se activan acciones.";
            }

            // Obtener las temperaturas de los últimos 2 intervalos (6 horas) desde la API
            List<WeatherAPI.PronosticoIntervalo> pronosticos = WeatherAPI.obtenerPronosticoPorIntervalos();

            // Tomar las dos primeras temperaturas (6 horas)
            double temp1 = pronosticos.get(0).getTemperatura(); // primeras 3 horas
            double temp2 = pronosticos.get(1).getTemperatura(); // pasadas 6 horas

            // Calcular la media
            double mediaApi = (temp1 + temp2) / 2;

            // Obtener la temperatura del sensor
            ArrayList<Temperatura> sensorTemp = Logic.getDataTemperatura("ubicuabd.Temperatura");

            // Verificar si hay suficientes datos del sensor
            if (sensorTemp.size() < 3) {
                Log.log.warn("No hay suficientes datos del sensor para calcular la media.");
                return "Datos insuficientes del sensor.";
            }

            // Tomar los últimos 3 valores del sensor
            float suma = 0;
            for (int i = sensorTemp.size() - 1; i >= sensorTemp.size() - 3; i--) {
                suma += sensorTemp.get(i).getValor();
            }

            // Calcular la media de la temperatura del sensor
            float media_temp = suma / 3;

            // Comparar la media de la API con la media del sensor
            if (Math.abs(mediaApi - media_temp) > 3) { // Diferencia mayor a 3 grados como ejemplo
                Log.log.info("Alerta: Diferencia significativa entre la API y el sensor.");

                // Activar calefacción o aire acondicionado según la diferencia
                if (mediaApi > media_temp) {
                    Log.log.info("Activando calefacción.");
                    return "Calefacción activada: La temperatura interior es más fría que la predicción.";
                } else {
                    Log.log.info("Activando aire acondicionado.");
                    return "Aire acondicionado activado: La temperatura interior es más cálida que la predicción.";
                }

            } else {
                Log.log.info("La temperatura del sensor coincide con el pronóstico. No se toman acciones.");
                return "Temperatura estable. No se toman acciones.";
            }
        } catch (Exception e) {
            Log.log.error("Error al comparar temperaturas: " + e.getMessage());
            return "Error al procesar la información.";
        }
    }

    
    public static boolean usuarioEnCasa() {
        
        ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("ubicuadb.Movimiento");

        // Verificar si hay datos
        if (movimientos.isEmpty()) {
            Log.log.warn("No hay datos de movimiento disponibles.");
            return false; // Asumimos que el usuario no está en casa
        }

        // Ordenar por fecha en orden descendente
        movimientos.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        // Tomar el movimiento más reciente
        Movimiento ultimoMovimiento = movimientos.get(0);

        // Calcular la diferencia de tiempo (en minutos)
        long diferenciaEnMilisegundos = System.currentTimeMillis() - ultimoMovimiento.getFecha().getTime();
        long diferenciaEnMinutos = diferenciaEnMilisegundos / (1000 * 60);

        // Verificar si el movimiento es reciente (por ejemplo, dentro de los últimos 30 minutos)
        if (diferenciaEnMinutos <= 30 && ultimoMovimiento.getHayMovimiento() == 1) {
            Log.log.info("Usuario está en casa. Movimiento reciente detectado.");
            return true;
        } else {
            Log.log.info("Usuario no está en casa. No hay movimiento reciente.");
            return false;
        }
    }
    
    public static boolean usuarioLlegaraPronto() {
        
        ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("ubicuadb.Movimiento");

        // Verificar si hay datos
        if (movimientos.isEmpty()) {
            Log.log.warn("No hay datos de movimiento disponibles.");
            return false; // Asumimos que el usuario no llegará pronto
        }

        // Filtrar movimientos del mismo día de la semana y rango horario
        java.util.Calendar calendario = java.util.Calendar.getInstance();
        int diaActual = calendario.get(java.util.Calendar.DAY_OF_WEEK);
        int horaActual = calendario.get(java.util.Calendar.HOUR_OF_DAY);

        for (Movimiento movimiento : movimientos) {
            java.util.Calendar fechaMovimiento = java.util.Calendar.getInstance();
            fechaMovimiento.setTime(movimiento.getFecha());

            int diaMovimiento = fechaMovimiento.get(java.util.Calendar.DAY_OF_WEEK);
            int horaMovimiento = fechaMovimiento.get(java.util.Calendar.HOUR_OF_DAY);

            // Comparar día y rango horario
            if (diaMovimiento == diaActual && Math.abs(horaMovimiento - horaActual) <= 1) {
                Log.log.info("Usuario podría llegar pronto basado en patrones previos.");
                return true;
            }
        }

        Log.log.info("Usuario no tiene patrones que indiquen que llegará pronto.");
        return false;
    }

    
}
