package uni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;

/** Lee las vistas estratégicas que alimentan los reportes (Bridge). */
public class ReporteDao {

    /** Filas {servicio, entregados, aTiempo} desde vw_cumplimiento_tiempos. */
    public List<Object[]> cumplimientoTiempos() throws Exception {
        return filas("SELECT servicio, entregados, a_tiempo FROM vw_cumplimiento_tiempos ORDER BY servicio");
    }

    /** Filas {repartidor, entregas, observados} desde vw_productividad_reparto. */
    public List<Object[]> productividadReparto() throws Exception {
        return filas("SELECT repartidor, entregas, observados FROM vw_productividad_reparto ORDER BY entregas DESC");
    }

    private List<Object[]> filas(String sql) throws Exception {
        List<Object[]> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                l.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getInt(3)});
            }
        }
        return l;
    }
}
