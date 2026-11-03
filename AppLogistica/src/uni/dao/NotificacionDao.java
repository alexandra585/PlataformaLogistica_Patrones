package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.service.IBitacoraNotificaciones;

/** Registro de notificaciones emitidas por NotificacionDestinatarioObserver. */
public class NotificacionDao implements IBitacoraNotificaciones {

    @Override
    public void registrar(String codigo, String canal, String destinatario, String mensaje,
                          boolean enviado) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Notificacion_Registrar(?,?,?,?,?)}")) {
            cs.setString(1, codigo);
            cs.setString(2, canal);
            cs.setString(3, destinatario);
            cs.setString(4, mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje);
            cs.setBoolean(5, enviado);
            cs.executeUpdate();
        }
    }

    /** Filas: {fecha, codigo, canal, destinatario, mensaje, enviado}. */
    public List<Object[]> listarRecientes(int limite) throws Exception {
        List<Object[]> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT fecha, codigo, canal, destinatario, "
                + "mensaje, enviado FROM notificaciones ORDER BY idnotificacion DESC LIMIT " + limite);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                l.add(new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getBoolean(6) ? "Sí" : "No"});
            }
        }
        return l;
    }
}
