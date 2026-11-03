package uni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import uni.database.AccesoDB;
import uni.service.IConsultaAsignacion;

/** Asignaciones envío-repartidor consultadas por el Proxy. */
public class AsignacionDao implements IConsultaAsignacion {

    @Override
    public String repartidorDe(String codigoEnvio) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT idrepartidor FROM asignaciones "
                + "WHERE codigo = ? ORDER BY fecha_asignacion DESC, idruta DESC LIMIT 1")) {
            ps.setString(1, codigoEnvio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    @Override
    public java.util.List<String> enviosPendientesDe(String idrepartidor) throws Exception {
        java.util.List<String> l = new java.util.ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT DISTINCT a.codigo FROM asignaciones a "
                + "JOIN envios e ON e.codigo = a.codigo WHERE a.idrepartidor = ? "
                + "AND e.estado NOT IN ('ENTREGADO','DEVUELTO') ORDER BY a.codigo")) {
            ps.setString(1, idrepartidor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(rs.getString(1));
                }
            }
        }
        return l;
    }
}
