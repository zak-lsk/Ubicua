package Logic;
import java.sql.Timestamp;


public class Temperatura {
    private float valor; 
    private Timestamp fecha;
    private String zona;

    // constructors
    public Temperatura() {
        this.valor = 0;
        this.fecha = null;
        this.zona = null;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }
}
