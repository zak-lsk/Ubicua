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

    public boolean getHayLuz() {
        return hayLuz == 1;
    }

    public void setHayLuz(boolean hayLuz) {
        this.hayLuz = hayLuz ? 1 : 0;
    }   

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}
