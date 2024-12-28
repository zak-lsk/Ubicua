package Logic;
import java.sql.Timestamp;

public class Movimiento {
    private int hayMovimiento;
    private Timestamp fecha;
    private String zona;
    
    // constructors
    public Movimiento() {
        this.hayMovimiento = 0;
        this.fecha = null;
        this.zona = null;
    }

    public boolean getHayMovimiento() {
        return hayMovimiento == 1;
    }

    public void setHayMovimiento(boolean hayMovimiento) {
        this.hayMovimiento = hayMovimiento ? 1 : 0;
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
