package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.EmpleadoTo;
import uni.entity.RolUsuario;
import uni.service.ICrudDao;
import uni.service.IRegistroRemuneracion;

/** DAO de empleados/usuarios (molde EmpleadoDao de AppEmpresa). */
public class EmpleadoDao implements ICrudDao<EmpleadoTo>, IRegistroRemuneracion {

    private static final String SELECT = "SELECT idempleado, nombre, apellidos, email, usuario, "
            + "rol, idsucursal, activo, estado_laboral, sueldo, area, regimen_pensionario FROM empleados ";

    @Override
    public void create(EmpleadoTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try {
            cn.setAutoCommit(false);                    // inicio de la transacción
            o.setIdempleado(generaCodigo(cn));
            try (CallableStatement cs = cn.prepareCall("{call sp_Empleado_Adicionar(?,?,?,?,?,?,?,?)}")) {
                cargar(cs, o);
                cs.executeUpdate();
            }
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }

    @Override
    public void update(EmpleadoTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Empleado_Actualizar(?,?,?,?,?,?,?,?)}")) {
            cargar(cs, o);
            cs.executeUpdate();
        }
    }

    @Override
    public void delete(EmpleadoTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Empleado_Eliminar(?)}")) {
            cs.setString(1, o.getIdempleado());
            cs.executeUpdate();
        }
    }

    @Override
    public EmpleadoTo find(Object id) throws Exception {
        List<EmpleadoTo> l = consultar(SELECT + "WHERE idempleado = ?", id.toString(), null);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public List<EmpleadoTo> readAll() throws Exception {
        return consultar(SELECT + "ORDER BY idempleado", null, null);
    }

    public List<EmpleadoTo> listarPorRol(RolUsuario rol) throws Exception {
        return consultar(SELECT + "WHERE rol = ? AND estado_laboral = 'ACTIVO' ORDER BY nombre", rol.name(), null);
    }

    /** Login: la clave se compara como hash SHA-256 dentro de MySQL. */
    public EmpleadoTo validar(String usuario, String clave) throws Exception {
        List<EmpleadoTo> l = consultar(SELECT + "WHERE usuario = ? AND clave = SHA2(?,256) AND activo = 1",
                usuario, clave);
        return l.isEmpty() ? null : l.get(0);
    }

    private void cargar(CallableStatement cs, EmpleadoTo o) throws SQLException {
        cs.setString(1, o.getIdempleado());
        cs.setString(2, o.getNombre());
        cs.setString(3, o.getApellidos());
        cs.setString(4, o.getEmail());
        cs.setString(5, o.getUsuario());
        cs.setString(6, o.getClave());
        cs.setString(7, o.getRol().name());
        cs.setString(8, o.getIdsucursal());
    }

    private List<EmpleadoTo> consultar(String sql, String p1, String p2) throws Exception {
        List<EmpleadoTo> lista = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (p1 != null) {
                ps.setString(1, p1);
            }
            if (p2 != null) {
                ps.setString(2, p2);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmpleadoTo e = new EmpleadoTo(rs.getString("idempleado"), rs.getString("nombre"),
                            rs.getString("apellidos"), RolUsuario.valueOf(rs.getString("rol")));
                    e.setEmail(rs.getString("email"));
                    e.setUsuario(rs.getString("usuario"));
                    e.setIdsucursal(rs.getString("idsucursal"));
                    e.setActivo(rs.getBoolean("activo"));
                    e.setEstadoLaboral(rs.getString("estado_laboral"));
                    e.setSueldo(rs.getDouble("sueldo"));
                    e.setArea(rs.getString("area"));
                    e.setRegimen(rs.getString("regimen_pensionario"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    private String generaCodigo(Connection cn) throws SQLException {
        int cont;
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT valor FROM control WHERE parametro='Empleados' FOR UPDATE");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            cont = rs.getInt(1);
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE control SET valor = valor + 1 WHERE parametro='Empleados'")) {
            ps.executeUpdate();
        }
        return String.format("E%04d", cont);
    }

    /** Persiste sueldo y área (Command/Memento, Sesión 13) y deja huella en historial_remuneracion. */
    @Override
    public void actualizarRemuneracion(String idempleado, double sueldo, String area, String operacion,
            String usuario) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Empleado_ActualizarRemuneracion(?,?,?,?,?)}")) {
            cs.setString(1, idempleado);
            cs.setBigDecimal(2, java.math.BigDecimal.valueOf(sueldo).setScale(2, java.math.RoundingMode.HALF_UP));
            cs.setString(3, area);
            cs.setString(4, operacion);
            cs.setString(5, usuario);
            cs.executeUpdate();
        }
    }

    /** Filas {fecha, operacion, sueldo_anterior, sueldo_nuevo, area_anterior, area_nueva, usuario}. */
    public List<Object[]> historialRemuneracion(String idempleado) throws Exception {
        List<Object[]> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT fecha, operacion, sueldo_anterior, sueldo_nuevo, "
                + "area_anterior, area_nueva, usuario FROM historial_remuneracion WHERE idempleado = ? "
                + "ORDER BY idhistorial")) {
            ps.setString(1, idempleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getBigDecimal(3),
                        rs.getBigDecimal(4), rs.getString(5), rs.getString(6), rs.getString(7)});
                }
            }
        }
        return l;
    }
}
