package Logic;


/**
 * Clase para almacenar los topics de los sensores y el estado de las cosas
 */
public class Util {
    
    public static final String TOPIC_TEMPERATURA =  "Casa/Salon/Temperatura"; 
    public static final String TOPIC_MOVIMIENTO  = "Casa/Salon/Presencia"; 
    public static final String TOPIC_GAS = "Casa/Salon/Gas"; 
    public static final String TOPIC_VENTANA = "Casa/Salon/Ventana/Servo";
    public static final String TOPIC_ALARMA_ACTIVAR = "Casa/Salon/Alarma/Activada";
    public static final String TOPIC_ALARMA_SONAR = "Casa/Salon/Alarma/Sonar";
    public static final String TOPIC_VENTILACION_ACTIVAR = "Casa/Salon/Ventilacion/Activado";
    public static final String TOPIC_VENTILACION_MODO = "Casa/Salon/Ventilacion/Modo"; 
    public static final String TOPIC_LUZ = "Casa/Exterior/Luz"; 
    public static final String TOPIC_LLUVIA = "Casa/Exterior/Lluvia";
    public static final String TOPIC_PARAGUAS = "Casa/Entrada/Paraguas/Servo";
    public static final String TOPIC_TEST = "Casa/Test"; 
    public static int ESTADO_ANTERIOR_PRESENCIA = -1; 
    public static int ESTADO_PRESENCIA = -1; 
    public static boolean ESTADO_ALARMA = false;  // false -> no activada
    public static boolean ESTADO_ANTERIOR_ALARMA = false; // true -> activada previamente
    public static boolean ESTADO_VENTILACION = false; // true -> encendido
    public static boolean MODO_VENTILACION = true; // true -> aire acondicionado, false -> calefacción
    public static boolean ESTADO_VENTANA = true; // true -> ventana abierta
    public static boolean ESTADO_PARAGUAS = false; // true -> paraguas abierto
    
}
