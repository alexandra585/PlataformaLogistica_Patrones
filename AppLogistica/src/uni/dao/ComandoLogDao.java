package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.ComandoLogTo;
import uni.service.IBitacoraComandos;

/** Bitácora de comandos ejecutados y deshechos (tabla comandos_log). */
public class ComandoLogDao implements IBitacoraComandos {

    @Override
    public long registrar(ComandoLogTo c) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Comando_Registrar(?,?,?,?,?,?,?)}")) {
            cs.registerOutParameter(1, Types.BIGINT);        // id generado (parámetro de salida)
            cs.setString(2, c.getCodigo());
            cs.setString(3, c.getComando());
            cs.setString(4, c.getDescripcion());
            cs.setString(5, c.getEstadoPrevio());
            cs.setString(6, c.getEstadoNuevo());
            cs.setString(7, c.getIdempleado());
            cs.executeUpdate();
            long id = cs.getLong(1);
            c.setIdcomando(id);
            return id;
        }
    }

    @Override
    public void marcarDeshecho(long idcomando, boolean deshecho) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Comando_MarcarDeshecho(?,?)}")) {
            cs.setLong(1, idcomando);
            cs.setBoolean(2, deshecho);
            cs.executeUpdate();
        }
    }

    @Override
    public List<ComandoLogTo> listarRecientes(int limite) throws Exception {
        List<ComandoLogTo> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT idcomando, codigo, comando, descripcion, "
                + "estado_previo, estado_nuevo, idempleado, deshecho, fecha FROM comandos_log "
                + "ORDER BY idcomando DESC LIMIT " + limite);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ComandoLogTo c = new ComandoLogTo(rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7));
                c.setIdcomando(rs.getLong(1));
                c.setDeshecho(rs.getBoolean(8));
                c.setFecha(rs.getTimestamp(9).toLocalDateTime());
                l.add(c);
            }
        }
        return l;
    }
}
