package Logic;

import java.util.ArrayList;
import java.util.List;
import MQTT.*; 

public class Inteligencia {
    private static boolean VENTANA_ABIERTA = false; //true -> ventana abierta
    private static boolean ESTADO_VENTILACION = false; //true -> ventilacion encendida
    private static boolean MODO_VENTILACION = false; //true -> aire acondicionado; false -> calefaccion
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

            // Definir un rango de temeparatura de confort
            final double TEMP_CONFORT_MIN = 18;
            final double TEMP_CONFORT_MAX = 25;

            // Determinar en que estacion del año nos encontramos
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
                Log.log.info("Alerta: La temperatura interior de casa está"
                        + " fuera del rango de confort.");
                if (mediaApi >= TEMP_CONFORT_MIN && mediaApi <= TEMP_CONFORT_MAX) {
                    //temperatura de fuera dentro del rango de confort 
                    
                    Log.log.info("Abriendo ventanas para ventilar la casa "
                            + "ya que la temperatura exterior es confortable. "
                            + "Temperatura exterior: {} ºC", mediaApi);
                    
                    // Publicar en el topic para abrir ventanas 
                    if (!VENTANA_ABIERTA) {
                        MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "true");
                        VENTANA_ABIERTA = true;
                    }
                    
                    
                    // Apagar la ventilacion
                    if (ESTADO_VENTILACION) { // si ventilacion encendida
                        MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Activado", "false");
                        ESTADO_VENTILACION = false;
                    }
                    
                    
                    return "Ventanas abiertas: Temperatura exterior confortable. "
                            + "\nTemperatura exterior: " + mediaApi + " ºC"
                            + "\nTemperatura interior: " + media_temp + " ºC";
                    
                } else {
                    //temperatura de fuera fuera del rango de confort -> activar calefacción/aire acondicionado
                    
                    if (media_temp < TEMP_CONFORT_MIN && esInvierno) {
                        
                        Log.log.info("Activando calefacción: temperatura "
                                + "interior baja. Temperatura interior: {}", media_temp);
                        
                        // Publicar en el topic para activar calefacción                      
                        if(!ESTADO_VENTILACION){
                            
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Activado", "true");
                           
                            if(MODO_VENTILACION){ //si antes estaba puesto como aire acondicionado
                                
                                // ponerlo en modo calefaccion
                                MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Modo", "false");    
                                MODO_VENTILACION = false; 
                                
                            } // y si ya estaba en modo calefaccion no publicar nada
                            
                        }                        
                        
                        // Cerrar las ventanas
                        if(VENTANA_ABIERTA){
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "false");
                            ESTADO_VENTILACION = false; 
                        }
                        
                        
                        return "Calefacción activada: Temperatura interior baja. "
                                + "\nTemperatura interior: " + media_temp + " ºC"
                                + "\nTemperatura exterior: " + mediaApi + " ºC";
                        
                    } else if (media_temp > TEMP_CONFORT_MAX && esVerano) {
                        
                        Log.log.info("Activando aire acondicionado: "
                                + "temperatura interior alta. Temperatura interior:"
                                + " {}", media_temp);
                        
                        // Publicar en el topic para activar aire acondicionado
                        if (!ESTADO_VENTILACION){ //si la ventilacion estaba apagada
                            
                            // Encenderla
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Activado", "true");
                            ESTADO_VENTILACION = true; 
                            
                            if(!MODO_VENTILACION){ //estaba puesto en modo calefaccion
                                
                                MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Modo", "true");
                                MODO_VENTILACION = true; 
                                
                            }                            
                        }                        
                       
                        // Cerrar las ventanas
                        if(VENTANA_ABIERTA){ //si la ventana estaba abierta
                            
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "false");
                            VENTANA_ABIERTA = false; 
                            
                        }
                        
                        
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
                
                Log.log.info("Alerta: La temperatura interior de casa está"
                        + " dentro del rango de confort. Temperatura interior: {}", media_temp);
                
                if (Math.abs(mediaApi - media_temp) > 2) { 
                    //gran diferencia entre la temperatura de fuera y la temperatura del sensor
                    // Verificar si abrir las ventanas empeoraría la situación
                    
                    if ((esVerano && mediaApi > media_temp) || (esInvierno && mediaApi < media_temp)) {
                        
                        Log.log.info("Mantener ventanas cerradas: La temperatura "
                                + "exterior podría empeorar el confort. Temperatura exterior: {}", mediaApi);
                        
                        // Publicar en el topic para cerrar las ventanas
                        if (VENTANA_ABIERTA){ // si ventana abierta
                            
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "false");
                            VENTANA_ABIERTA = false; 
                            
                        }
                        
                        
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
                        if(!VENTANA_ABIERTA){ //Si ventana cerrada
                            
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventana/Servo", "true");
                            VENTANA_ABIERTA = true; 
                            
                        }
                                                                       
                        // Apagar la ventilacion
                        if(ESTADO_VENTILACION){ //Si ventilacion encendida
                            
                            MQTTPublisher.publish(broker, "Casa/Salon/Ventilacion/Activado", "false"); 
                            ESTADO_VENTILACION = false; 
                            
                        }                        
                        
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
            Log.log.info("Usuario está en casa. Movimiento reciente detectado.");
            return true;
        } else {
            Log.log.info("Usuario no está en casa. No hay movimiento reciente.");
            return false;
        }
    }

    public static boolean usuarioLlegaraPronto() {

        ArrayList<Movimiento> movimientos = Logic.getDataMovimiento("Movimiento");

        // Verificar si hay datos
        if (movimientos.isEmpty()) {
            Log.log.error("No hay datos de movimiento disponibles.");
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

    private static boolean esInvierno() {
        java.util.Calendar calendario = java.util.Calendar.getInstance();
        int mes = calendario.get(java.util.Calendar.MONTH) + 1; // Enero = 0
        return (mes == 11 || mes <= 2); // Invierno: diciembre, enero, febrero
    }

}
