package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.service.RecursosHumanosObserver;

/** Estado laboral y su historial (tabla historial_laboral). */
public class HistorialLaboralDao implements RecursosHumanosObserver.IRegistroLaboral {

    @Override
    public void guardarEstado(String idempleado, String estado) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Empleado_CambiarEstadoLaboral(?,?)}")) {
            cs.setString(1, idempleado);
            cs.setString(2, estado);
            cs.executeUpdate();
        }
    }

    @Override
    public void registrarHistorial(String idempleado, String anterior, String nuevo,
                                   String motivo, String usuario) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_HistorialLaboral_Registrar(?,?,?,?,?)}")) {
            cs.setString(1, idempleado);
            cs.setString(2, anterior);
            cs.setString(3, nuevo);
            cs.setString(4, motivo);
            cs.setString(5, usuario);
            cs.executeUpdate();
        }
    }

    /** Filas {fecha, estado anterior, estado nuevo, motivo, usuario}. */
    public List<Object[]> listar(String idempleado) throws Exception {
        List<Object[]> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT fecha, estado_anterior, estado_nuevo, motivo, usuario "
                + "FROM historial_laboral WHERE idempleado = ? ORDER BY idhistorial DESC")) {
            ps.setString(1, idempleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
                }
            }
        }
        return l;
    }
}
