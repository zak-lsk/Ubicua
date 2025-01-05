package Logic;

import java.util.ArrayList;
import java.util.List;
import MQTT.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Inteligencia {

    private static boolean VENTANA_ABIERTA = true; //true -> ventana abierta
    private static boolean ESTADO_VENTILACION = true; //true -> ventilacion encendida
    private static boolean MODO_VENTILACION = true; //true -> aire acondicionado; false -> calefaccion

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

            // Definir un rango de temperatura de confort
            final double TEMP_CONFORT_MIN = 18;
            final double TEMP_CONFORT_MAX = 25;

            // Determinar en qué estación del año nos encontramos
            boolean esInvierno = esInvierno();
            boolean esVerano = !esInvierno;

            // Tomar las dos primeras temperaturas (6 horas)
            double temp1 = pronosticos.get(0).getTemperatura(); // primeras 3 horas
            double temp2 = pronosticos.get(1).getTemperatura(); // pasadas 6 horas

            // Definir el broker donde se publicarán los mensajes
            MQTTBroker broker = new MQTTBroker();

            // Calcular la media y redondear a 2 decimales
            double mediaApi = Math.round((temp1 + temp2) / 2 * 100.0) / 100.0;

            // Obtener la temperatura del sensor
            ArrayList<Temperatura> sensorTemp = Logic.getDataTemperatura("Temperatura");

            // Verificar si hay suficientes datos del sensor
            if (sensorTemp.size() < 3) {
                Log.log.error("No hay suficientes datos del sensor para calcular la media.");
                return "Datos insuficientes del sensor.";
            }

            // Tomar los últimos 3 valores del sensor
            float suma = 0;
            for (int i = sensorTemp.size() - 1; i >= sensorTemp.size() - 3; i--) {
                suma += sensorTemp.get(i).getValor();
            }

            // Calcular la media de la temperatura del sensor y redondear a 2 decimales
            double media_temp = Math.round((suma / 3) * 100.0) / 100.0;

            // Lógica para decidir
            if (media_temp < TEMP_CONFORT_MIN || media_temp > TEMP_CONFORT_MAX) {
                // Temperatura interior de casa, está fuera del rango de confort
                Log.log.info("Alerta: La temperatura interior de casa está fuera del rango de confort.");

                if (mediaApi >= TEMP_CONFORT_MIN && mediaApi <= TEMP_CONFORT_MAX) {
                    // Temperatura de fuera dentro del rango de confort

                    Log.log.info("Abriendo ventanas para ventilar la casa ya que la temperatura exterior es confortable. Temperatura exterior: {} ºC", mediaApi);

                    // Publicar en el topic para abrir ventanas 
                    abrirVentanas(broker);

                    // Apagar la ventilación
                    apagarVentilacion(broker);

                    return "Ventanas abiertas: Temperatura exterior confortable. "
                            + "\nTemperatura exterior: " + mediaApi + " ºC"
                            + "\nTemperatura interior: " + media_temp + " ºC";

                } else {
                    // Temperatura de fuera fuera del rango de confort -> activar calefacción/aire acondicionado

                    if (media_temp < TEMP_CONFORT_MIN && esInvierno) {

                        Log.log.info("Activando calefacción: temperatura interior baja. Temperatura interior: {}", media_temp);

                        // Publicar en el topic para activar calefacción                      
                        encenderVentilacion(broker, false);

                        // Cerrar las ventanas
                        cerrarVentanas(broker);

                        return "Calefacción activada: Temperatura interior baja. "
                                + "\nTemperatura interior: " + media_temp + " ºC"
                                + "\nTemperatura exterior: " + mediaApi + " ºC";

                    } else if (media_temp > TEMP_CONFORT_MAX && esVerano) {

                        Log.log.info("Activando aire acondicionado: temperatura interior alta. Temperatura interior:{}", media_temp);

                        // Publicar en el topic para activar aire acondicionado
                        encenderVentilacion(broker, true);

                        // Cerrar las ventanas
                        cerrarVentanas(broker);

                        return "Aire acondicionado activado: Temperatura interior alta. "
                                + "\nTemperatura interior: " + media_temp + " ºC."
                                + "\nTemperatura exterior: " + mediaApi + " ºC.";

                    } else {

                        Log.log.info("No se toma acción: temperatura exterior no presenta ajustes");

                        return "No se toma acción: temperatura exterior no presenta ajustes. "
                                + "Temperatura exterior: " + mediaApi + " ºC. \n"
                                + "Temperatura interior: " + media_temp + " ºC. ";

                    }
                }
            } else {
                // Temperatura interior dentro del rango de confort

                Log.log.info("Alerta: La temperatura interior de casa está dentro del rango de confort. Temperatura interior: {}", media_temp);

                if (Math.abs(mediaApi - media_temp) > 2) {
                    // Gran diferencia entre la temperatura de fuera y la temperatura del sensor
                    // Verificar si abrir las ventanas empeoraría la situación

                    if ((esVerano && mediaApi > media_temp) || (esInvierno && mediaApi < media_temp)) {

                        Log.log.info("Mantener ventanas cerradas: La temperatura exterior podría empeorar el confort. Temperatura exterior: {}", mediaApi);

                        // Publicar en el topic para cerrar las ventanas
                        cerrarVentanas(broker);
                        apagarVentilacion(broker);

                        return "Ventanas cerradas: Temperatura exterior no es "
                                + "favorable (" + mediaApi + "°C)."
                                + "\nTemperatura interior(" + media_temp + "°C)";

                    }

                    // Solo abrir ventanas si la temperatura exterior ayudaría a mantener el confort
                    if ((esVerano && mediaApi < media_temp && mediaApi >= TEMP_CONFORT_MIN)
                            || (esInvierno && mediaApi > media_temp && mediaApi <= TEMP_CONFORT_MAX)) {

                        Log.log.info("Ventanas abiertas: La temperatura exterior (" + mediaApi
                                + " °C) ayudará a mantener el confort interior (" + media_temp + " °C).");

                        // Publicar en el topic para abrir las ventanas
                        abrirVentanas(broker);

                        // Apagar la ventilación
                        apagarVentilacion(broker);

                        return "Ventanas abiertas: La temperatura exterior (" + mediaApi
                                + " °C) ayudará a mantener el confort interior (" + media_temp + " °C).";

                    }
                }

                Log.log.info("La temperatura del sensor está dentro del rango de confort. No se toman acciones.");
                return "Temperatura estable (" + media_temp + " °C). No se toman acciones.";

            }
        } catch (Exception e) {
            Log.log.error("Error al comparar temperaturas: " + e.getMessage());
            return "Error al procesar la información.";
        }
    }

    private static void cerrarVentanas(MQTTBroker broker) {
        if (VENTANA_ABIERTA) {
            MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "false");
            VENTANA_ABIERTA = false;
            Log.log.info("Ventanas cerradas correctamente.");
        }
    }

    private static void abrirVentanas(MQTTBroker broker) {
        if (!VENTANA_ABIERTA) {
            MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "true");
            VENTANA_ABIERTA = true;
            Log.log.info("Ventanas abiertas correctamente.");
        }
    }

    /**
     * Método para encender la ventilación publicando en el topic correspondiente
     * @param broker Broker a donde se va a publicar
     * @param modo Modo de ventilación,
     * true: Aire acondicionado; false: Calefacción
     * 
     */
    private static void encenderVentilacion(MQTTBroker broker, boolean modo) {
        cerrarVentanas(broker);
        if (!ESTADO_VENTILACION) { // si ya estaba apagada

            MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Activado", "true");
            ESTADO_VENTILACION = true;
            Log.log.info("Ventilacion encendida correctamente");

        }
        if (MODO_VENTILACION != modo) {

            MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Modo", String.valueOf(modo));
            MODO_VENTILACION = modo;
            Log.log.info("Modo de ventilacion actualizado correctamente a: "
                    + "{} ", modo);

        }
    }

    private static void apagarVentilacion(MQTTBroker broker) {
        
        // en caso de que la ventilacion estaba encendida
        if (ESTADO_VENTILACION) {
            
            // apagarla
            MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Activado", "false");
            ESTADO_VENTILACION = false;

        }
    }

    /**
     * Método para determinar si el usuario se encuentra en casa basándose en 
     * sus hábitos 
     * @return Boolean: true: se encuentra en casa; false: se encuentra fuera de casa
     */
    public static boolean usuarioEnCasa() {

        ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("Movimiento");

        // Verificar si hay datos
        if (movimientos.isEmpty()) {
            Log.log.error("No hay datos de movimiento disponibles.");
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
            
            //en caso de que hay movimiento durante 30 minutos
            Log.log.info("Usuario está en casa. Movimiento reciente detectado.");
            return true;
            
        } else {
            
            //en caso de que no haya movimiento durante 30 minutos 
            Log.log.info("Usuario no está en casa. No hay movimiento reciente.");
            return false;
            
        }
    }

    public static boolean usuarioLlegaraPronto() {
        try {
            ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("Movimiento");

            // Verificar si hay datos
            if (movimientos.isEmpty()) {
                Log.log.warn("No hay datos de movimiento disponibles.");
                return false;
            }

            // Obtener hora y día actuales
            ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
            ZonedDateTime ahora = ZonedDateTime.now(zonaMadrid);
            int diaActual = ahora.getDayOfWeek().getValue();
            int horaActual = ahora.getHour();

            // Definir ventana de tiempo para llegada próxima (1 horas)
            int ventanaProxima = 1;

            Log.log.info("Verificando patrones de llegada para día {} y hora {}", diaActual, horaActual);

            // Contar ocurrencias de movimiento en el rango horario próximo
            int ocurrenciasEnRango = 0;
            int totalDiasAnalizados = 0;

            // Mapa para contar ocurrencias por hora
            Map<Integer, Integer> ocurrenciasPorHora = new HashMap<>();

            // Analizar los movimientos históricos
            for (Movimiento movimiento : movimientos) {

                // convertir la fecha/hora de movimiento a la zona horaria local
                ZonedDateTime fechaMovimiento = movimiento.getFecha()
                        .toInstant()
                        .atZone(zonaMadrid);

                int diaMovimiento = fechaMovimiento.getDayOfWeek().getValue();
                int horaMovimiento = fechaMovimiento.getHour();

                // Solo analizar movimientos del mismo día de la semana
                if (diaMovimiento == diaActual) {
                    totalDiasAnalizados++;

                    // Registrar movimiento en las próximas horas
                    for (int i = horaActual + 1; i <= horaActual + ventanaProxima; i++) {
                        if (horaMovimiento == i && movimiento.getHayMovimiento() == 1) {
                            ocurrenciasEnRango++;
                            // Contar ocurrencias por hora
                            ocurrenciasPorHora.merge(horaMovimiento, 1, Integer::sum);
                        }
                    }
                }
            }

            // Calcular probabilidad de llegada
            if (totalDiasAnalizados > 0) {

                double probabilidad = (double) ocurrenciasEnRango / totalDiasAnalizados;

                // Encontrar la hora más probable de llegada
                int horaMasProbable = -1;
                int maxOcurrencias = 0;

                for (Map.Entry<Integer, Integer> entry : ocurrenciasPorHora.entrySet()) {

                    if (entry.getValue() > maxOcurrencias) {

                        maxOcurrencias = entry.getValue();
                        horaMasProbable = entry.getKey();

                    }

                }

                // Si hay un patrón significativo (más del 50% de probabilidad)
                if (probabilidad > 0.5) {

                    Log.log.info("Alta probabilidad de llegada ({}%) en las próxima {} hora. "
                            + "Hora más probable: {}:00",
                            Math.round(probabilidad * 100),
                            ventanaProxima,
                            horaMasProbable);

                    return true;

                } else {

                    Log.log.info("Baja probabilidad de llegada ({}%) en las próxima {} hora.",
                            Math.round(probabilidad * 100),
                            ventanaProxima);

                    return false;

                }
            }

            Log.log.info("No hay suficientes datos históricos para este día de la semana.");
            return false;

        } catch (Exception e) {
            Log.log.error("Error al analizar patrones de llegada: " + e.getMessage());
            return false;
        }
    }

    public static String analizarHabitosSalida() {

        try {

            // Obtener los datos históricos de movimiento y pronóstico del tiempo
            ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("Movimiento");
            List<WeatherAPI.PronosticoIntervalo> pronosticos = WeatherAPI.obtenerPronosticoPorIntervalos();

            // Verificar si hay datos disponibles
            if (movimientos.isEmpty()) {
                Log.log.warn("No hay datos de movimiento para analizar");
                return "No hay suficientes datos para analizar hábitos.";
            }

            // Configurar zona horaria de Madrid para análisis preciso
            ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
            ZonedDateTime ahora = ZonedDateTime.now(zonaMadrid);
            int diaActual = ahora.getDayOfWeek().getValue(); // 1 = Lunes, 7 = Domingo

            // Estructuras de datos para el análisis
            Map<Integer, Integer> patronesSalida = new HashMap<>(); // Hora -> Número de salidas
            Set<LocalDate> diasAnalizados = new HashSet<>(); // Conjunto de días únicos analizados
            ZonedDateTime ultimoMovimiento = null;
            boolean huboMovimiento = false;
            final int MINUTOS_SIN_MOVIMIENTO = 30; // Tiempo mínimo sin movimiento para considerar salida

            // Registrar inicio del análisis
            Log.log.info("Iniciando análisis de hábitos para {}",
                    ahora.format(DateTimeFormatter.ofPattern("EEEE")));

            // Analizar cada movimiento registrado
            for (Movimiento movimiento : movimientos) {
                // Convertir la fecha del movimiento a zona horaria de Madrid
                ZonedDateTime fechaMovimiento = movimiento.getFecha()
                        .toInstant()
                        .atZone(zonaMadrid);

                // Ignorar datos más antiguos de 3 meses para mantener relevancia
                if (fechaMovimiento.isBefore(ahora.minusMonths(3))) {
                    continue;
                }

                // Analizar solo movimientos del mismo día de la semana
                if (fechaMovimiento.getDayOfWeek().getValue() == diaActual) {
                    // Registrar día único para estadísticas
                    diasAnalizados.add(fechaMovimiento.toLocalDate());

                    // Detectar patrón de salida (movimiento seguido de no movimiento prolongado)
                    if (movimiento.getHayMovimiento() == 1) {
                        huboMovimiento = true;
                        ultimoMovimiento = fechaMovimiento;
                    } else if (huboMovimiento && ultimoMovimiento != null) {
                        // Calcular tiempo transcurrido sin movimiento
                        long minutosSinMovimiento = java.time.Duration.between(
                                ultimoMovimiento,
                                fechaMovimiento
                        ).toMinutes();

                        // Solo considerar como salida si han pasado al menos 30 minutos sin movimiento
                        if (minutosSinMovimiento >= MINUTOS_SIN_MOVIMIENTO) {
                            int horaSalida = ultimoMovimiento.getHour();
                            patronesSalida.merge(horaSalida, 1, Integer::sum);
                            Log.log.debug("Detectada posible salida a las {}:00 ({} minutos sin movimiento)",
                                    horaSalida,
                                    minutosSinMovimiento);
                        }
                        huboMovimiento = false;
                    }
                }
            }

            // Registrar resumen del análisis
            Log.log.info("Analizados {} días únicos para {}",
                    diasAnalizados.size(),
                    ahora.format(DateTimeFormatter.ofPattern("EEEE")));

            // Procesar resultados si hay datos suficientes
            if (!patronesSalida.isEmpty() && !diasAnalizados.isEmpty()) {
                // Obtener las 3 horas con más salidas registradas
                List<Map.Entry<Integer, Integer>> horasMasProbables = patronesSalida.entrySet().stream()
                        .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                        .limit(3)
                        .collect(Collectors.toList());

                // Analizar la hora más frecuente
                Map.Entry<Integer, Integer> horaMasProbable = horasMasProbables.get(0);
                int horaSalidaProbable = horaMasProbable.getKey();
                // Calcular probabilidad dividiendo ocurrencias entre días totales
                double probabilidad = (double) horaMasProbable.getValue() / diasAnalizados.size();

                // Registrar las horas más probables para debug
                Log.log.info("Top 3 horas más probables para {}:",
                        ahora.format(DateTimeFormatter.ofPattern("EEEE")));

                for (Map.Entry<Integer, Integer> hora : horasMasProbables) {
                    double prob = (double) hora.getValue() / diasAnalizados.size();
                    Log.log.info("  {}:00 - {}%",
                            hora.getKey(),
                            Math.round(prob * 100));
                }

                // Si la probabilidad es significativa (>30%)
                if (probabilidad > 0.3) {
                    // Variables para el pronóstico
                    boolean lloveraProbable = false;
                    String descripcionTiempo = "";
                    double temperatura = 0;

                    // Tomar solo los primeros 3 intervalos del pronóstico (9 horas)
                    List<WeatherAPI.PronosticoIntervalo> pronosticosProximos = pronosticos.stream()
                            .limit(3)
                            .collect(Collectors.toList());

                    Log.log.info("Analizando los próximos {} intervalos de pronóstico", pronosticosProximos.size());

                    // Buscar pronóstico en los intervalos próximos
                    boolean pronosticoEncontrado = false;

                    for (WeatherAPI.PronosticoIntervalo pronostico : pronosticosProximos) {

                        int horaPronostico = pronostico.getFechaHora().getHour();

                        if (Math.abs(horaPronostico - horaSalidaProbable) <= 1) {

                            String descripcion = pronostico.getDescripcion().toLowerCase();

                            // Detectar condiciones de lluvia
                            lloveraProbable = descripcion.contains("lluvia")
                                    || descripcion.contains("tormenta")
                                    || descripcion.contains("precipitación");

                            descripcionTiempo = pronostico.getDescripcion();

                            temperatura = pronostico.getTemperatura();

                            pronosticoEncontrado = true;

                            break;

                        }
                    }

                    // Construir recomendación personalizada
                    StringBuilder recomendacion = new StringBuilder();

                    // Añadir contexto del análisis
                    recomendacion.append(String.format(
                            "Según el análisis de los últimos %d %s, ",
                            diasAnalizados.size(),
                            diasAnalizados.size() == 1 ? "día" : "días"
                    ));

                    // Añadir hora probable y confianza
                    recomendacion.append(String.format(
                            "sueles salir sobre las %d:00 (probabilidad: %d%%). ",
                            horaSalidaProbable,
                            Math.round(probabilidad * 100)
                    ));

                    // Añadir pronóstico y recomendación
                    if (lloveraProbable) {

                        recomendacion.append(String.format(
                                "Se prevé %s con %.1f°C. ",
                                descripcionTiempo.toLowerCase(),
                                temperatura
                        ));

                        recomendacion.append("Te recomiendo llevar paraguas.");
                        
                        // publicar en el topic para sacar paraguas 

                    } else {

                        recomendacion.append(String.format(
                                "Se prevé %s con %.1f°C. ",
                                descripcionTiempo.toLowerCase(),
                                temperatura
                        ));

                        recomendacion.append("No necesitarás paraguas.");
                        
                        // publicar en el topic para cerrar el paraguas
                        
                    }

                    return recomendacion.toString();

                } else {
                    // Si la probabilidad es baja, informar que no hay patrón claro

                    return String.format(
                            "Los patrones de salida para %s son variables (probabilidad: %d%%). "
                            + "No se puede hacer una predicción confiable.",
                            ahora.format(DateTimeFormatter.ofPattern("EEEE")),
                            Math.round(probabilidad * 100)
                    );
                    
                    // en todo caso, publicar en el topic para cerrar el paraguas
                    

                }
            }

            return "No hay suficientes datos recientes para predecir patrones de salida.";

        } catch (Exception e) {
            Log.log.error("Error al analizar hábitos de salida: " + e.getMessage());
            return "Error al analizar hábitos de salida.";
        }
    }

    private static boolean esInvierno() {
        java.util.Calendar calendario = java.util.Calendar.getInstance();
        int mes = calendario.get(java.util.Calendar.MONTH) + 1; // Enero = 0
        return (mes == 11 || mes <= 2); // Invierno: diciembre, enero, febrero
    }
}
