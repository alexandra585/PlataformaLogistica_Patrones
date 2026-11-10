package uni.database;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PATRÓN SINGLETON.
 * Punto único de acceso a la base de datos "logistica".
 * Evolución de uni.database.AccesoDB de AppEmpresa: en lugar de abrir una
 * conexión nueva en cada llamada, existe una sola instancia que administra
 * la configuración y reutiliza la conexión mientras esté abierta.
 *
 * La configuración se lee (una sola vez) del archivo db.properties ubicado
 * en la carpeta del proyecto; si no existe se usan los valores por defecto.
 */
public final class AccesoDB {

    // Instancia única. volatile garantiza visibilidad entre hilos.
    private static volatile AccesoDB instancia;

    // Se usa el primer driver JDBC disponible en la carpeta lib.
    private static final String[] DRIVERS = {"com.mysql.cj.jdbc.Driver", "org.mariadb.jdbc.Driver"};

    private String url = "jdbc:mysql://localhost:3306/logistica?useSSL=false"
            + "&allowPublicKeyRetrieval=true&serverTimezone=America/Lima";
    private String usuario = "logistica";
    private String clave = "logistica";
    private String familiaIntegraciones = "PRODUCCION";
    private String driver;
    private Connection cn;

    // Constructor privado: nadie fuera de la clase puede hacer "new AccesoDB()".
    private AccesoDB() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
            url = p.getProperty("db.url", url);
            usuario = p.getProperty("db.usuario", usuario);
            clave = p.getProperty("db.clave", clave);
            familiaIntegraciones = p.getProperty("integraciones", familiaIntegraciones);
        } catch (Exception e) {
            // Sin archivo: se trabaja con los valores por defecto.
        }
        System.out.println("[AccesoDB] Configuración de conexión cargada (una sola vez).");
    }

    // Instanciación perezosa con doble verificación (thread-safe).
    public static AccesoDB getInstancia() {
        if (instancia == null) {
            synchronized (AccesoDB.class) {
                if (instancia == null) {
                    instancia = new AccesoDB();
                }
            }
        }
        return instancia;
    }

    /** Devuelve la conexión activa; si no existe o fue cerrada, la reabre. */
    public synchronized Connection getConexion() throws SQLException, ClassNotFoundException {
        if (cn == null || cn.isClosed() || !cn.isValid(2)) {
            cargarDriver();
            cn = DriverManager.getConnection(url, usuario, clave);
        }
        return cn;
    }

    private void cargarDriver() throws ClassNotFoundException {
        if (driver != null) {
            return;
        }
        for (String d : DRIVERS) {
            try {
                Class.forName(d);
                driver = d;
                return;
            } catch (ClassNotFoundException ignorar) {
                // se prueba el siguiente
            }
        }
        throw new ClassNotFoundException("No se encontró un driver JDBC de MySQL en la carpeta lib.");
    }

    /** Se invoca una sola vez al cerrar la aplicación (PrincipalView). */
    public synchronized void cerrar() throws SQLException {
        if (cn != null && !cn.isClosed()) {
            cn.close();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public String getFamiliaIntegraciones() {
        return familiaIntegraciones;
    }
}
