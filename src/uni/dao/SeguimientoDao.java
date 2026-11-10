package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.EstadoEnvio;
import uni.entity.SeguimientoTo;
import uni.service.IRegistroHistorico;

/**
 * ISP: el historial de seguimiento solo se inserta y se consulta.
 * A diferencia de ClienteDao de AppEmpresa (que implementaba ICrudDao y
 * lanzaba UnsupportedOperationException), aquí no existen métodos vacíos.
 */
public class SeguimientoDao implements IRegistroHistorico<SeguimientoTo> {

    @Override
    public void registrar(SeguimientoTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Seguimiento_Registrar(?,?,?,?,?)}")) {
            cs.setString(1, o.getCodigoEnvio());
            cs.setString(2, o.getEstado().name());
            cs.setString(3, o.getUbicacion());
            cs.setString(4, o.getObservacion());
            cs.setString(5, o.getIdempleado());
            cs.executeUpdate();
        }
    }

    @Override
    public List<SeguimientoTo> listarPor(String codigo) throws Exception {
        List<SeguimientoTo> lista = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        String sql = "SELECT codigo, estado, ubicacion, observacion, idempleado, fechahora "
                + "FROM seguimientos WHERE codigo = ? ORDER BY fechahora";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new SeguimientoTo(rs.getString(1), EstadoEnvio.valueOf(rs.getString(2)),
                            rs.getString(3), rs.getString(4), rs.getString(5),
                            rs.getTimestamp(6).toLocalDateTime()));
                }
            }
        }
        return lista;
    }
}
