package uni.database;

import java.sql.Connection;

/**
 * Prueba de conexión (igual que uni.database.prueba de AppEmpresa).
 * Clic derecho > Run File para verificar que MySQL responde.
 */
public class prueba {

    public static void main(String[] args) {
        try {
            Connection cn = AccesoDB.getInstancia().getConexion();
            System.out.println("Conexion conforme.... " + cn.getMetaData().getDatabaseProductName()
                    + " " + cn.getMetaData().getDatabaseProductVersion()
                    + " (driver " + AccesoDB.getInstancia().getDriver() + ")");
            AccesoDB.getInstancia().cerrar();
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
        }
    }
}
