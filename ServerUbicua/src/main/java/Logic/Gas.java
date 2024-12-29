package Logic;

import java.sql.Timestamp;

public class Gas {
    private int hayGas; 
    private Timestamp fecha;
    private String zona;

    // constructors
    public Gas() {
        this.hayGas = 0;
        this.fecha = null;
        this.zona = null;
    }

    public int getHayGas() {
        return hayGas;
    }

    public void setHayGas(int hayGas) {
        this.hayGas = hayGas;
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
