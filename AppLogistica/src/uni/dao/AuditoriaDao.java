package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import uni.database.AccesoDB;
import uni.service.IRegistroAuditoria;

/** Bitácora de accesos del Proxy de seguimiento (tabla auditoria). */
public class AuditoriaDao implements IRegistroAuditoria {

    @Override
    public void registrar(String idusuario, String rol, String accion, String codigo,
                          boolean permitido) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Auditoria_Registrar(?,?,?,?,?)}")) {
            cs.setString(1, idusuario);
            cs.setString(2, rol);
            cs.setString(3, accion.length() > 40 ? accion.substring(0, 40) : accion);
            cs.setString(4, codigo);
            cs.setBoolean(5, permitido);
            cs.executeUpdate();
        }
    }
}
