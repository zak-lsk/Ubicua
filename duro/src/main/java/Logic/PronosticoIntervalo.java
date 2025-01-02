package Logic;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PronosticoIntervalo {
        private LocalDateTime fechaHora;
        private double temperatura;
        private String descripcion;

        public PronosticoIntervalo(LocalDateTime fechaHora, double temperatura, String descripcion) {
            this.fechaHora = fechaHora;
            this.temperatura = temperatura;
            this.descripcion = descripcion;
        }

        // Getters
        public LocalDateTime getFechaHora() { return fechaHora; }
        public double getTemperatura() { return temperatura; }
        public String getDescripcion() { return descripcion; }

        // Obtener la fecha formateadas
        public String getFechaHoraFormateada() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return fechaHora.format(formatter);
        }
    }

    
