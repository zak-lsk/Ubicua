package Logic;

import Database.ConectionDDBB;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Logic {

    public static ArrayList<Temperatura> getDataTemperatura(String table) {
        ArrayList<Temperatura> values = new ArrayList<Temperatura>();

        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");

            PreparedStatement ps = ConectionDDBB.GetDataBD(con, table);
            Log.log.info("Query=>" + ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Temperatura measure = new Temperatura();
                measure.setZona(rs.getString("zona"));
                measure.setValor(rs.getInt("valor"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Temperatura>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Temperatura>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Temperatura>();
        }
        conector.closeConnection(con);
        return values;
    }

    public static ArrayList<Temperatura> setDataTemperatura(String table, int value, String zone) {
        ArrayList<Temperatura> values = new ArrayList<Temperatura>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table, zone);
            ps.setString(1, zone);
            ps.setInt(2, value);
            ps.setTimestamp(3, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Temperatura>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Temperatura>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Temperatura>();
        }
        conector.closeConnection(con);
        return values;
    }

    public static ArrayList<Lluvia> getDataLluvia(String table) {
        ArrayList<Lluvia> values = new ArrayList<Lluvia>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.GetDataBD(con, table);
            Log.log.info("Query=>" + ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Lluvia measure = new Lluvia();
                measure.setHayLluvia(rs.getInt("hayLluvia"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Lluvia>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Lluvia>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Lluvia>();
        }
        conector.closeConnection(con);
        return values;
    }

    public static ArrayList<Lluvia> setDataLluvia(String table, int value) {
        ArrayList<Lluvia> values = new ArrayList<Lluvia>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table);
            ps.setInt(1, value);
            ps.setTimestamp(2, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Lluvia>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Lluvia>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Lluvia>();
        }
        conector.closeConnection(con);
        return values;
    }


    public static ArrayList<Luz> getDataLuz(String table) {
        ArrayList<Luz> values = new ArrayList<Luz>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");

            PreparedStatement ps = ConectionDDBB.GetDataBD(con, table);
            Log.log.info("Query=>" + ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Luz measure = new Luz();
                measure.setHayLuz(rs.getInt("hayLuz"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Luz>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Luz>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Luz>();
        }
        conector.closeConnection(con);
        return values;
    }

    public static ArrayList<Luz> setDataLuz(String table, int value) {
        ArrayList<Luz> values = new ArrayList<Luz>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table);
            ps.setInt(1, value);
            ps.setTimestamp(2, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Luz>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Luz>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Luz>();
        }
        conector.closeConnection(con);
        return values;
    }

    public static ArrayList<Movimiento> getDataMovimiento(String table) {
        ArrayList<Movimiento> values = new ArrayList<Movimiento>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");

            PreparedStatement ps = ConectionDDBB.GetDataBD(con, table);
            Log.log.info("Query=>" + ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Movimiento measure = new Movimiento();
                measure.setZona(rs.getString("zona"));
                measure.setHayMovimiento(rs.getInt("hayMovimiento"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Movimiento>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Movimiento>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Movimiento>();
        }
        conector.closeConnection(con);
        return values;
    }


    public static ArrayList<Movimiento> setDataMovimiento(String table, int value, String zone) {
        ArrayList<Movimiento> values = new ArrayList<Movimiento>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table);
            ps.setString(1, zone);
            ps.setInt(2, value);
            ps.setTimestamp(3, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Movimiento>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Movimiento>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Movimiento>();
        }
        conector.closeConnection(con);
        return values;
    }

    
    public static ArrayList<Gas> getDataGas(String table){
        ArrayList<Gas> values = new ArrayList<Gas>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");

            PreparedStatement ps = ConectionDDBB.GetDataBD(con, table);
            Log.log.info("Query=>" + ps.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Gas measure = new Gas();
                measure.setZona(rs.getString("zona"));
                measure.setHayGas(rs.getInt("valor"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Gas>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Gas>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Gas>();
        }
        conector.closeConnection(con);
        return values;
    }

    public static ArrayList<Gas> setDataGas(String table, int value, String zone){
        ArrayList<Gas> values = new ArrayList<Gas>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table);
            ps.setString(1, zone);
            ps.setInt(2, value);
            ps.setTimestamp(3, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Gas>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<Gas>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<Gas>();
        }
        conector.closeConnection(con);
        return values;
    }

    

}
