package uni.reportes;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import uni.database.AccesoDB;

/**
 * PATRÓN BRIDGE - Abstraction.
 * Sabe QUÉ reporte se genera (plantilla .jrxml y parámetros) y mantiene el
 * "puente" hacia la salida (ISalidaReporte), que puede cambiarse en tiempo
 * de ejecución. Igual que InformeView de AppEmpresa, el .jrxml se compila
 * en tiempo de ejecución (aquí con caché) y se llena con la conexión de AccesoDB.
 */
public abstract class Reporte {

    private static final Map<String, JasperReport> COMPILADOS = new ConcurrentHashMap<>();

    protected ISalidaReporte salida;

    protected Reporte(ISalidaReporte salida) {
        this.salida = salida;
    }

    public void setSalida(ISalidaReporte salida) {
        this.salida = salida;
    }

    /** Llena el reporte con los datos de MySQL y lo entrega por la salida elegida. */
    public final String generar() throws Exception {
        return salida.emitir(llenar(), getTitulo());
    }

    /** Variante sin base de datos (pruebas por consola): los campos vienen del datasource. */
    public final String generar(JRDataSource datos) throws Exception {
        return salida.emitir(JasperFillManager.fillReport(compilar(), getParametros(), datos), getTitulo());
    }

    public final JasperPrint llenar() throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        return JasperFillManager.fillReport(compilar(), getParametros(), cn);
    }

    private JasperReport compilar() throws Exception {
        String archivo = getPlantilla();
        JasperReport r = COMPILADOS.get(archivo);
        if (r == null) {
            try (InputStream in = Reporte.class.getResourceAsStream("/uni/reportes/" + archivo)) {
                if (in == null) {
                    throw new java.io.FileNotFoundException("No se encontró la plantilla " + archivo);
                }
                r = JasperCompileManager.compileReport(in);
            }
            COMPILADOS.put(archivo, r);
        }
        return r;
    }

    /** Parámetros $P{...} del .jrxml; por defecto ninguno. */
    protected Map<String, Object> getParametros() {
        return new HashMap<>();
    }

    public abstract String getTitulo();

    /** Nombre del .jrxml dentro de uni/reportes. */
    protected abstract String getPlantilla();
}
