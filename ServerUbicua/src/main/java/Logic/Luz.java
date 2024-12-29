package Logic;
import java.sql.Timestamp;

public class Luz {
    private int hayLuz;
    private Timestamp fecha;

    // constructors
    public Luz() {
        this.hayLuz = 0;
        this.fecha = null;
    }

    public int getHayLuz() {
        return hayLuz;
    }

    public void setHayLuz(int hayLuz) {
        this.hayLuz = hayLuz;
    }   

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}
