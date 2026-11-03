package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.IncidenciaTo;
import uni.service.IRegistroHistorico;

/** Incidencias de entrega: solo se agregan y se consultan (ISP). */
public class IncidenciaDao implements IRegistroHistorico<IncidenciaTo> {

    private static final String SELECT = "SELECT i.idincidencia, i.codigo, i.motivo, i.descripcion, "
            + "COALESCE(z.nombre,'Sin zona') AS zona, i.idempleado, i.fecha "
            + "FROM incidencias i LEFT JOIN zonas z ON z.idzona = i.idzona ";

    @Override
    public void registrar(IncidenciaTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Incidencia_Registrar(?,?,?,?)}")) {
            cs.setString(1, o.getCodigoEnvio());
            cs.setString(2, o.getMotivo());
            cs.setString(3, o.getDescripcion());
            cs.setString(4, o.getIdempleado());
            cs.executeUpdate();
        }
    }

    @Override
    public List<IncidenciaTo> listarPor(String codigo) throws Exception {
        return consultar(SELECT + "WHERE i.codigo = ? ORDER BY i.fecha", codigo);
    }

    /** Todas las incidencias (fuente del Reporte de incidencias por zona). */
    public List<IncidenciaTo> listarTodas() throws Exception {
        return consultar(SELECT + "ORDER BY i.fecha", null);
    }

    private List<IncidenciaTo> consultar(String sql, String p) throws Exception {
        List<IncidenciaTo> lista = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (p != null) {
                ps.setString(1, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IncidenciaTo i = new IncidenciaTo(rs.getString("codigo"), rs.getString("motivo"),
                            rs.getString("zona"), rs.getString("descripcion"));
                    i.setIdincidencia(rs.getInt("idincidencia"));
                    i.setIdempleado(rs.getString("idempleado"));
                    i.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    lista.add(i);
                }
            }
        }
        return lista;
    }
}
