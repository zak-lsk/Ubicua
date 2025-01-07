package Logic;

import java.util.ArrayList;
import java.util.List;
import MQTT.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

                        return "Calefacción activada: Temperatura interior baja. \n"
                                + "Temperatura interior: " + media_temp + " ºC\n"
                                + "Temperatura exterior: " + mediaApi + " ºC";

                    } else if (media_temp > TEMP_CONFORT_MAX && esVerano) {

                        Log.log.info("Activando aire acondicionado: temperatura interior alta. Temperatura interior:{}", media_temp);

                        // Publicar en el topic para activar aire acondicionado
                        encenderVentilacion(broker, true);

                        // Cerrar las ventanas
                        cerrarVentanas(broker);

                        return "Aire acondicionado activado: Temperatura interior alta. \n"
                                + "Temperatura interior: " + media_temp + " ºC.\n"
                                + "Temperatura exterior: " + mediaApi + " ºC.";

                    } else {

                        Log.log.info("No se toma acción: temperatura exterior no presenta ajustes");

                        return "No se toma acción: temperatura exterior no presenta ajustes. \n"
                                + "Temperatura exterior: " + mediaApi + " ºC. \n"
                                + "Temperatura interior: " + media_temp + " ºC. \n";

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

    
    /**
     * Método para cerrar las ventanas
     * @param broker Broker donde se va a publicar el mensaje para cerrar las ventanas
     */
    private static void cerrarVentanas(MQTTBroker broker) {
        if (Util.ESTADO_VENTANA) {
            MQTTPublisher.publish(broker, Util.TOPIC_VENTANA, "false");
            Util.ESTADO_VENTANA = false;
            Log.log.info("Ventanas cerradas correctamente.");
        }
    }

    
    /**
     * Método para abrir las ventanas
     * @param broker Broker donde se va a publicar el mensaje para abrir las ventanas
     */
    private static void abrirVentanas(MQTTBroker broker) {
        apagarVentilacion(broker);
        if (!Util.ESTADO_VENTANA) {
            MQTTPublisher.publish(broker, Util.TOPIC_VENTANA, "true");
            Util.ESTADO_VENTANA = true;
            Log.log.info("Ventanas abiertas correctamente.");
        }
    }

    
    /**
     * Método para encender la ventilación publicando en el topic
     * correspondiente
     *
     * @param broker Broker a donde se va a publicar
     * @param modo Modo de ventilación, true: Aire acondicionado; false:
     * Calefacción
     *
     */
    private static void encenderVentilacion(MQTTBroker broker, boolean modo) {
        Log.log.info("cerrar ventanas desde encenderVentilacion");
        cerrarVentanas(broker);
        if (!Util.ESTADO_VENTILACION) { // si ya estaba apagada

            MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_ACTIVAR, "true");
            Util.ESTADO_VENTILACION = true;
            Log.log.info("Ventilacion encendida correctamente");

        }
        if (Util.MODO_VENTILACION != modo) {

            MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_MODO, String.valueOf(modo));
            Util.MODO_VENTILACION = modo;
            Log.log.info("Modo de ventilacion actualizado correctamente a: "
                    + "{} ", modo);

        }
    }

    /**
     * Método para apagar la ventilación de la casa 
     * @param broker Broker donde se va a publicar el mensaje para apagar la ventilación
     */
    private static void apagarVentilacion(MQTTBroker broker) {

        // en caso de que la ventilacion estaba encendida
        if (Util.ESTADO_VENTILACION) {

            // apagarla
            MQTTPublisher.publish(broker, Util.TOPIC_VENTILACION_ACTIVAR, "false");
            Util.ESTADO_VENTILACION = false;
            Log.log.info("Ventilacion apagada correctamente"); 
        }
    }

    /**
     * Método para determinar si el usuario se encuentra en casa basándose en
     * sus hábitos
     *
     * @return Boolean: true: se encuentra en casa; false: se encuentra fuera de
     * casa
     */
    public static boolean usuarioEnCasa() {
        ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("Movimiento");

        // Verificar si hay datos
        if (movimientos.isEmpty()) {
            Log.log.error("No hay datos de movimiento disponibles.");
            return false;
        }

        // Ordenar por fecha en orden descendente
        movimientos.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        // Tomar el movimiento más reciente
        Movimiento ultimoMovimiento = movimientos.get(0);

        // Obtener la hora actual
        ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
        ZonedDateTime ahora = ZonedDateTime.now(zonaMadrid);
        int horaActual = ahora.getHour();

        // Calcular la diferencia de tiempo
        long diferenciaEnMilisegundos = System.currentTimeMillis() - ultimoMovimiento.getFecha().getTime();
        long diferenciaEnMinutos = diferenciaEnMilisegundos / (1000 * 60);

        // Ajustar el tiempo máximo sin movimiento según la hora del día
        int tiempoMaximoSinMovimiento;

        if (horaActual >= 23 || horaActual < 6) {
            // Durante la noche (23:00 - 6:00), permitir hasta 8 horas sin movimiento
            tiempoMaximoSinMovimiento = 480; // 8 horas en minutos
            Log.log.debug("Horario nocturno: permitiendo {} minutos sin movimiento", tiempoMaximoSinMovimiento);
        } else if (horaActual >= 6 && horaActual < 9) {
            // Durante la mañana temprano (6:00 - 9:00), ser más estricto
            // ya que es cuando la gente suele salir
            tiempoMaximoSinMovimiento = 60; // 1 hora
            Log.log.debug("Horario de mañana: permitiendo {} minutos sin movimiento", tiempoMaximoSinMovimiento);
        } else {
            // Durante el día (9:00 - 23:00), permitir hasta 2 horas sin movimiento
            tiempoMaximoSinMovimiento = 120; // 2 horas en minutos
            Log.log.debug("Horario diurno: permitiendo {} minutos sin movimiento", tiempoMaximoSinMovimiento);
        }

        // Verificar si el último movimiento está dentro del rango permitido
        if (diferenciaEnMinutos <= tiempoMaximoSinMovimiento) {
            Log.log.info("Usuario considerado en casa. Ultimo movimiento hace {} minutos",
                    diferenciaEnMinutos);
            return true;
        } else {
            // Si ha pasado más tiempo del permitido, verificar si es un período de sueño
            if (horaActual >= 23 || horaActual < 7) {
                // Durante la noche, verificar si el último movimiento fue de inactividad
                if (ultimoMovimiento.getHayMovimiento() == 0) {
                    Log.log.info("Usuario probablemente durmiendo. Ultimo movimiento hace {} minutos",
                            diferenciaEnMinutos);
                    return true;
                }
            }

            Log.log.info("Usuario considerado fuera de casa. Ultimo movimiento hace {} minutos",
                    diferenciaEnMinutos);
            return false;
        }
    }

    /**
     * Método para determinar si el usuario va a llegar pronto a casa. 
     * Este método se basa en el estudio previo de los hábitos del usuario
     * @return true: Llegará pronto; false: No llegará pronto
     */
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
    
    
    /**
     * Metodo para gestionar la apertrua y cierre del paraguas basándose en 
     * el estudio previo de los hábitos del usuario.
     * @return String: acción a llevar acabo
     */
    public static String gestionarParaguas() {
        try {
            // Verificar si el usuario está en casa
            boolean usuarioPresente = usuarioEnCasa();

            // Verificar si el usuario saldrá pronto de casa 
            //boolean usuarioSaldraPronto = usuarioSaldraPronto();

            // Obtener los datos del sensor de lluvia
            ArrayList<Lluvia> sensorLluviaDetecta = Logic.getDataLluvia("Lluvia");
            int ultimo = sensorLluviaDetecta.size() - 1;
            // Obtener el último dato registrado de la tabla de lluvia
            boolean hayLluvia = sensorLluviaDetecta.get(ultimo).getHayLluvia() == 1;

            // Obtener el pronóstico del tiempo
            List<WeatherAPI.PronosticoIntervalo> pronosticos = WeatherAPI.obtenerPronosticoPorIntervalos();

            // Verificar si se espera lluvia según el pronóstico
            boolean pronosticoLluvia = pronosticos.stream()
                    .limit(3) // Solo analizar los primeros 3 intervalos (9 horas)
                    .anyMatch(pronostico -> pronostico.getDescripcion().toLowerCase().contains("lluvia"));

            MQTTBroker broker = new MQTTBroker();

            if (usuarioPresente) {
                // El usuario está en casa

                if (hayLluvia || pronosticoLluvia) {
                    if (!Util.ESTADO_PARAGUAS) {
                        abrirParaguas(broker);
                        return "Paraguas abierto debido a detección o pronóstico de lluvia.";
                    } else {
                        return "Paraguas ya está abierto debido a lluvia.";
                    }
                } else {
                    if (Util.MODO_VENTILACION) {
                        cerrarParaguas(broker);
                        return "Paraguas cerrado debido a condiciones favorables.";
                    } else {
                        return "Paraguas ya está cerrado.";
                    }
                }

            } else {
                // El usuario no está en casa, cerrar el paraguas si está abierto
                Log.log.info("El usuario no esta en casa o no se espera "
                        + "que vaya a salir pronto");
                if (Util.MODO_VENTILACION) {
                    cerrarParaguas(broker);
                    return "Paraguas cerrado porque el usuario ya no está en casa.";
                } else {
                    return "El usuario no está en casa y el paraguas ya está cerrado.";
                }
            }
        } catch (Exception e) {
            Log.log.error("Error al gestionar el paraguas: " + e.getMessage());
            return "Error al gestionar el paraguas.";
        }
    }

    /**
     * Método para abrir el paraguas
     * @param broker Broker donde se va a publicar el mensaje para abrir el paraguas
     */
    private static void abrirParaguas(MQTTBroker broker) {
        MQTTPublisher.publish(broker, Util.TOPIC_PARAGUAS, "true");
        Util.MODO_VENTILACION = true;
        Log.log.info("Paraguas abierto correctamente.");
    }

    
    /**
     * Método para cerrar el paraguas 
     * @param broker Broke donde se va publicar el mensaje para cerar el paraguas
     */
    private static void cerrarParaguas(MQTTBroker broker) {
        MQTTPublisher.publish(broker, Util.TOPIC_PARAGUAS, "false");
        Util.MODO_VENTILACION = false;
        Log.log.info("Paraguas cerrado correctamente.");
    }

    
    /**
     * Método para saber si la estación actual del año es invierno
     * @return true: es invierno, false: es verano
     */
    private static boolean esInvierno() {
        java.util.Calendar calendario = java.util.Calendar.getInstance();
        int mes = calendario.get(java.util.Calendar.MONTH) + 1; // Enero = 0
        return (mes == 11 || mes <= 3); // Invierno: diciembre, enero, febrero y marzo
    }
    
    public static String gestionarAlarma() {
        try {
            if (!Util.ESTADO_ALARMA) {
                return "{\"alarmaActivada\": false, \"movimientoDetectado\": false}";
            }
            boolean movimientoDetectado = usuarioEnCasa();
            return "{\"alarmaActivada\": true, \"movimientoDetectado\": " + movimientoDetectado + "}";
        } catch (Exception e) {
            Log.log.error("Error al gestionar la alarma: " + e.getMessage());
            return "{\"error\": \"Error al gestionar la alarma\"}";
        }
    }

    public static void desactivarAlarma() {
        try {
            Util.ESTADO_ALARMA = false;
            MQTTBroker broker = new MQTTBroker();
            MQTTPublisher.publish(broker, Util.TOPIC_ALARMA_ACTIVAR, "false");
            Log.logmqtt.info("Alarma desactivada.");
        } catch (Exception e) {
            Log.log.error("Error al desactivar la alarma: " + e.getMessage());
        }
    }

    public static void sonarAlarma() {
        try {
            MQTTBroker broker = new MQTTBroker();
            MQTTPublisher.publish(broker, Util.TOPIC_ALARMA_SONAR, "true");
            Log.logmqtt.info("Alarma activada y sonando.");
        } catch (Exception e) {
            Log.log.error("Error al hacer sonar la alarma: " + e.getMessage());
        }
    }
    /**
     * Método para predecir si el usuario saldrá de casa en las próximas 
     * 2 horas basándose en patrones históricos de movimiento. 
     * Excluye horario nocturno (23:00-06:00)
     * para no confundir con períodos de sueño.
     *
     * @return true si hay alta probabilidad de salida próxima, false en caso
     * contrario
     */
//    public static boolean usuarioSaldraPronto() {
//        try {
//            ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("Movimiento");
//
//            // Verificar si hay datos
//            if (movimientos.isEmpty()) {
//                Log.log.warn("No hay datos de movimiento disponibles.");
//                return false;
//            }
//
//            // Obtener hora y día actuales
//            ZoneId zonaMadrid = ZoneId.of("Europe/Madrid");
//            ZonedDateTime ahora = ZonedDateTime.now(zonaMadrid);
//            int diaActual = ahora.getDayOfWeek().getValue();
//            int horaActual = ahora.getHour();
//
//            // Si es horario nocturno (21:00 - 06:00), asumir que el usuario va a dormir o está durmiendo
//            if (horaActual >= 21 || horaActual < 6) {
//                Log.log.info("Horario nocturno ({}:00). Usuario probablemente va a dormir o está durmiendo",
//                        horaActual);
//                return false;
//            }
//
//            // Definir ventana de tiempo para analizar (próximas 2 horas)
//            int ventanaAnalisis = 2;
//
//            Log.log.info("Verificando patrones de salida para día {} y hora actual {}",
//                    diaActual, horaActual);
//
//            // Mapa para contar salidas por hora
//            Map<Integer, Integer> salidasPorHora = new HashMap<>();
//            Set<LocalDate> diasAnalizados = new HashSet<>();
//
//            // Analizar los movimientos históricos
//            for (int i = 0; i < movimientos.size() - 1; i++) {
//                Movimiento actual = movimientos.get(i);
//                Movimiento siguiente = movimientos.get(i + 1);
//
//                // Convertir fechas a zona horaria local
//                ZonedDateTime fechaActual = actual.getFecha()
//                        .toInstant()
//                        .atZone(zonaMadrid);
//
//                // Solo analizar el mismo día de la semana y evitar horario nocturno
//                if (fechaActual.getDayOfWeek().getValue() == diaActual
//                        && fechaActual.getHour() >= 6 && fechaActual.getHour() < 21) {
//
//                    diasAnalizados.add(fechaActual.toLocalDate());
//                    int horaMovimiento = fechaActual.getHour();
//
//                    // Detectar patrón de salida (movimiento seguido de no movimiento prolongado)
//                    if (actual.getHayMovimiento() == 1 && siguiente.getHayMovimiento() == 0) {
//                        // Calcular tiempo sin movimiento
//                        long minutosSinMovimiento = java.time.Duration.between(
//                                actual.getFecha().toInstant(),
//                                siguiente.getFecha().toInstant()
//                        ).toMinutes();
//
//                        // Si hay más de 30 minutos sin movimiento, considerar como salida
//                        // excepto si es horario nocturno
//                        if (minutosSinMovimiento >= 30) {
//                            salidasPorHora.merge(horaMovimiento, 1, Integer::sum);
//                        }
//                    }
//                }
//            }
//
//            // Analizar si hay patrones de salida en las próximas horas
//            int totalSalidasProximas = 0;
//            int maxSalidasHora = 0;
//            int horaMasProbable = -1;
//
//            for (int hora = horaActual + 1; hora <= horaActual + ventanaAnalisis; hora++) {
//                // Ignorar análisis si entramos en horario nocturno
//                if (hora >= 23 || hora < 6) {
//                    continue;
//                }
//
//                //contar el número de salidas para cada hora
//                //en caso de no encontrar la hora devolver 0
//                int salidas = salidasPorHora.getOrDefault(hora, 0);
//                totalSalidasProximas += salidas;
//
//                if (salidas > maxSalidasHora) {
//                    maxSalidasHora = salidas;
//                    horaMasProbable = hora;
//                }
//            }
//
//            // Calcular probabilidad de salida
//            if (!diasAnalizados.isEmpty() && horaMasProbable != -1) {
//                double probabilidad = (double) maxSalidasHora / diasAnalizados.size();
//
//                Log.log.info("Probabilidad de salida a las {}:00: {}%",
//                        horaMasProbable,
//                        Math.round(probabilidad * 100));
//
//                // Si la probabilidad es mayor al 30%, considerar que saldrá pronto
//                if (probabilidad > 0.3) {
//                    Log.log.info("Alta probabilidad de salida en la proxima hora");
//                    return true;
//                }
//            }
//
//            Log.log.info("No se detectan patrones claros de salida proxima");
//            return false;
//
//        } catch (Exception e) {
//            Log.log.error("Error al analizar patrones de salida: " + e.getMessage());
//            return false;
//        }
//    }
}
