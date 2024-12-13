import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class conexionBD {
    public static void main(String[] args) {
        // Configuración de la conexión
        String url = "jdbc:postgresql://localhost:5432/arquitecturacs"; // Cambia por tu base de datos
        String usuario = "postgres"; // Usuario de la base de datos
        String contraseña = "Aunnolose?P1"; // Contraseña de la base de datos

        Connection conexion = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // Conexión a la base de datos
            conexion = DriverManager.getConnection(url, usuario, contraseña);
            System.out.println("Conexión exitosa a la base de datos.");

            // Creación del Statement
            stmt = conexion.createStatement();

            // Ejecución de la consulta
            String consulta = "SELECT * FROM usuarios";
            rs = stmt.executeQuery(consulta);

            // Procesar los resultados
            while (rs.next()) {
                int id = rs.getInt("ID");
                String nombre = rs.getString("NombreUsuario");
                String email = rs.getString("Contrasenna");

                System.out.println("ID: " + id + ", Usuario: " + nombre + ", contraseña: " + email);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conexion != null) conexion.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
