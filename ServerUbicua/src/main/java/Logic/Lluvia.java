package Logic;
import java.sql.Timestamp;


public class Lluvia {
    private int hayLluvia;
    private Timestamp fecha;

    // constructores
    public Lluvia() {
        this.hayLluvia = 0;
        this.fecha = null;
    }

    public int getHayLluvia() {
        return hayLluvia;
    }

    public void setHayLluvia(int hayLluvia) {
        this.hayLluvia = hayLluvia;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}
