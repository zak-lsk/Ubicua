package Logic;

import db.ConectionDDBB;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Logic {

    public static ArrayList<Temperatura> getDataPrueba(String table) {
        ArrayList<Temperatura> values = new ArrayList<>();

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
                measure.setValor(rs.getFloat("valor"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException | NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }
    
        public static ArrayList<Temperatura> setDataPrueba(String table, float value, String zone) {
        ArrayList<Temperatura> values = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table, zone);
            ps.setString(1, zone);
            ps.setFloat(2, value);
            ps.setTimestamp(3, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Temperatura> getDataTemperatura(String table) {
        ArrayList<Temperatura> values = new ArrayList<>();

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
                measure.setValor(rs.getFloat("valor"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException | NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Temperatura> setDataTemperatura(String table, float value, String zone) {
        ArrayList<Temperatura> values = new ArrayList<>();
        ConectionDDBB conector = new ConectionDDBB();
        Connection con = null;
        try {
            con = conector.obtainConnection(true);
            Log.log.info("Database Connected");
            PreparedStatement ps = ConectionDDBB.SetDataBD(con, table, zone);
            ps.setString(1, zone);
            ps.setFloat(2, value);
            ps.setTimestamp(3, new Timestamp((new Date()).getTime()));
            Log.log.info("Query=>" + ps.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Lluvia> getDataLluvia(String table) {
        ArrayList<Lluvia> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Lluvia> setDataLluvia(String table, int value) {
        ArrayList<Lluvia> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Luz> getDataLuz(String table) {
        ArrayList<Luz> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Luz> setDataLuz(String table, int value) {
        ArrayList<Luz> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Movimiento> getDataMovimiento(String table) {
        ArrayList<Movimiento> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Movimiento> setDataMovimiento(String table, int value, String zone) {
        ArrayList<Movimiento> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Gas> getDataGas(String table) {
        ArrayList<Gas> values = new ArrayList<>();
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
                measure.setHayGas(rs.getInt("hayGas"));
                measure.setFecha(rs.getTimestamp("fecha"));
                values.add(measure);
            }
        } catch (SQLException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

    public static ArrayList<Gas> setDataGas(String table, int value, String zone) {
        ArrayList<Gas> values = new ArrayList<>();
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
            values = new ArrayList<>();
        } catch (NullPointerException e) {
            Log.log.error("Error: " + e);
            values = new ArrayList<>();
        } catch (Exception e) {
            Log.log.error("Error:" + e);
            values = new ArrayList<>();
        } finally {
            conector.closeConnection(con);
        }
        return values;
    }

}
