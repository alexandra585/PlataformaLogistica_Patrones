package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.BloqueoLoginTo;
import uni.service.IRegistroIntentosLogin;

/**
 * GRASP FABRICACIÓN PURA: solo persiste el contador de intentos y la
 * bitácora (tablas bloqueos_login e intentos_login); las reglas están
 * en ControlAccesoLogin.
 */
public class BloqueoLoginDao implements IRegistroIntentosLogin {

    @Override
    public BloqueoLoginTo buscar(String usuario) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT usuario, intentos_fallidos, bloqueado_hasta, "
                + "ultimo_fallo FROM bloqueos_login WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void guardar(BloqueoLoginTo b) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO bloqueos_login "
                + "(usuario, intentos_fallidos, bloqueado_hasta, ultimo_fallo) VALUES (?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE intentos_fallidos = VALUES(intentos_fallidos), "
                + "bloqueado_hasta = VALUES(bloqueado_hasta), ultimo_fallo = VALUES(ultimo_fallo)")) {
            ps.setString(1, b.getUsuario());
            ps.setInt(2, b.getIntentosFallidos());
            ps.setTimestamp(3, ts(b.getBloqueadoHasta()));
            ps.setTimestamp(4, ts(b.getUltimoFallo()));
            ps.executeUpdate();
        }
    }

    @Override
    public void registrarIntento(String usuario, boolean exitoso, String detalle) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO intentos_login (usuario, exitoso, detalle) VALUES (?,?,?)")) {
            ps.setString(1, usuario);
            ps.setBoolean(2, exitoso);
            ps.setString(3, detalle);
            ps.executeUpdate();
        }
    }

    @Override
    public List<BloqueoLoginTo> listarConFallos() throws Exception {
        List<BloqueoLoginTo> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT usuario, intentos_fallidos, bloqueado_hasta, "
                + "ultimo_fallo FROM bloqueos_login WHERE intentos_fallidos > 0 OR bloqueado_hasta IS NOT NULL "
                + "ORDER BY bloqueado_hasta DESC, usuario");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                l.add(mapear(rs));
            }
        }
        return l;
    }

    @Override
    public void desbloquear(String usuario) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Login_Desbloquear(?)}")) {
            cs.setString(1, usuario);
            cs.executeUpdate();
        }
    }

    private static BloqueoLoginTo mapear(ResultSet rs) throws Exception {
        return new BloqueoLoginTo(rs.getString(1), rs.getInt(2), ldt(rs.getTimestamp(3)), ldt(rs.getTimestamp(4)));
    }

    private static Timestamp ts(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    private static LocalDateTime ldt(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}
