package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.AlertaTo;
import uni.service.IRegistroHistorico;

/** Alertas para supervisores (las genera AlertaSupervisorObserver). */
public class AlertaDao implements IRegistroHistorico<AlertaTo> {

    private static final String SELECT = "SELECT idalerta, codigo, nivel, mensaje, atendida, fecha FROM alertas ";

    @Override
    public void registrar(AlertaTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Alerta_Registrar(?,?,?)}")) {
            cs.setString(1, o.getCodigo());
            cs.setString(2, o.getNivel());
            cs.setString(3, o.getMensaje().length() > 250 ? o.getMensaje().substring(0, 250) : o.getMensaje());
            cs.executeUpdate();
        }
    }

    @Override
    public List<AlertaTo> listarPor(String codigo) throws Exception {
        return consultar(SELECT + "WHERE codigo = ? ORDER BY idalerta DESC", codigo);
    }

    public List<AlertaTo> listarPendientes() throws Exception {
        return consultar(SELECT + "WHERE atendida = 0 ORDER BY idalerta DESC", null);
    }

    public void marcarAtendida(long idalerta) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("UPDATE alertas SET atendida = 1 WHERE idalerta = ?")) {
            ps.setLong(1, idalerta);
            ps.executeUpdate();
        }
    }

    private List<AlertaTo> consultar(String sql, String p) throws Exception {
        List<AlertaTo> lista = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (p != null) {
                ps.setString(1, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlertaTo a = new AlertaTo(rs.getString("codigo"), rs.getString("nivel"), rs.getString("mensaje"));
                    a.setIdalerta(rs.getLong("idalerta"));
                    a.setAtendida(rs.getBoolean("atendida"));
                    a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    lista.add(a);
                }
            }
        }
        return lista;
    }
}
