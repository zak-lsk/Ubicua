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

    public boolean getHayLluvia() {
        return hayLluvia == 1;
    }

    public void setHayLluvia(boolean hayLluvia) {
        this.hayLluvia = hayLluvia ? 1 : 0;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}
