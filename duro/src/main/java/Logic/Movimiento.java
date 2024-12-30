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

    public int getHayMovimiento() {
        return hayMovimiento ;
    }

    public void setHayMovimiento(int hayMovimiento) {
        this.hayMovimiento = hayMovimiento ;
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
