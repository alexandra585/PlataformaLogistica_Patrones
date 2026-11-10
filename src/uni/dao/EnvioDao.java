package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.DireccionTo;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.TipoServicio;
import uni.service.ICrudDao;

/**
 * DAO de envíos sobre MySQL (mismo molde que VentaDao/EmpleadoDao de
 * AppEmpresa: procedimientos almacenados + transacción + correlativo en
 * la tabla control). La conexión se obtiene del Singleton AccesoDB.
 */
public class EnvioDao implements ICrudDao<EnvioTo> {

    private static final String SELECT_BASE =
            "SELECT e.codigo, e.idcliente, e.idservicio, e.nombre_dest, e.telefono_dest, "
            + "e.email_dest, e.peso_kg, e.valor_declarado, e.fragil, e.con_seguro, "
            + "e.requiere_firma, e.fecha_programada, e.idsucursal_recojo, e.estado, e.costo, "
            + "e.idzona_destino, e.paso_flujo, e.intentos, e.fecha_estimada, e.fecha_registro, "
            + "e.codigo_firma, "
            + "d.calle, d.distrito, d.provincia, d.departamento, d.latitud, d.longitud, "
            + "o.calle AS ocalle, o.distrito AS odistrito, o.provincia AS oprovincia, "
            + "o.departamento AS odepartamento "
            + "FROM envios e LEFT JOIN direcciones d ON d.iddireccion = e.iddir_destino "
            + "LEFT JOIN direcciones o ON o.iddireccion = e.iddir_origen ";

    @Override
    public void create(EnvioTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try {
            cn.setAutoCommit(false);                       // inicia la transacción
            o.setCodigo(generaCodigo(cn));
            Integer idOrigen = registrarDireccion(cn, o.getOrigen());
            Integer idDestino = registrarDireccion(cn, o.getDestino());
            try (CallableStatement cs = cn.prepareCall(
                    "{call sp_Envio_Registrar(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
                cs.setString(1, o.getCodigo());
                cs.setString(2, o.getIdcliente());
                cs.setString(3, o.getServicio().name());
                setEntero(cs, 4, idOrigen);
                setEntero(cs, 5, idDestino);
                setEntero(cs, 6, o.getIdzonaDestino());
                cs.setString(7, o.getNombreDestinatario());
                cs.setString(8, o.getTelefonoDestinatario());
                cs.setString(9, o.getEmailDestinatario());
                cs.setDouble(10, o.getPesoKg());
                cs.setDouble(11, o.getValorDeclarado());
                cs.setBoolean(12, o.isFragil());
                cs.setBoolean(13, o.isConSeguro());
                cs.setBoolean(14, o.isRequiereFirma());
                cs.setDate(15, o.getFechaProgramada() == null ? null : Date.valueOf(o.getFechaProgramada()));
                cs.setString(16, o.getIdtiendaRecojo());
                cs.setDouble(17, o.getCosto());
                cs.executeUpdate();
            }
            cn.commit();                                   // confirma la transacción
        } catch (SQLException e) {
            cn.rollback();                                 // deshace la transacción
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }

    /** Persiste estado, paso del flujo, intentos y fecha (patrón State). */
    @Override
    public void update(EnvioTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Envio_ActualizarEstado(?,?,?,?,?,?)}")) {
            cs.setString(1, o.getCodigo());
            cs.setString(2, o.getEstado().name());
            cs.setInt(3, o.getPasoFlujo());
            cs.setInt(4, o.getIntentos());
            cs.setDate(5, o.getFechaProgramada() == null ? null : Date.valueOf(o.getFechaProgramada()));
            cs.setString(6, o.getCodigoFirma());
            cs.executeUpdate();
        }
    }

    /** No se borra físicamente: se marca DEVUELTO (trazabilidad). */
    @Override
    public void delete(EnvioTo o) throws Exception {
        o.setEstado(EstadoEnvio.DEVUELTO);
        update(o);
    }

    @Override
    public EnvioTo find(Object id) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE e.codigo = ?")) {
            ps.setString(1, id.toString().trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<EnvioTo> readAll() throws Exception {
        return listar(SELECT_BASE + "ORDER BY e.codigo DESC", null);
    }

    public List<EnvioTo> listarPorEstado(EstadoEnvio estado) throws Exception {
        return listar(SELECT_BASE + "WHERE e.estado = ? ORDER BY e.codigo", estado.name());
    }

    /** Envíos clasificados de una zona, listos para armar un manifiesto (Composite). */
    public List<EnvioTo> listarParaManifiesto(int idzona) throws Exception {
        return listar(SELECT_BASE + "WHERE e.estado IN ('CLASIFICADO','REPROGRAMADO') "
                + "AND e.idzona_destino = " + idzona + " AND e.idservicio <> 'RECOJO_TIENDA' "
                + "AND e.codigo NOT IN (SELECT codigo FROM unidad_detalle) ORDER BY e.codigo", null);
    }

    private List<EnvioTo> listar(String sql, String parametro) throws Exception {
        List<EnvioTo> lista = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private String generaCodigo(Connection cn) throws SQLException {
        int valor;
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT valor FROM control WHERE parametro='Envios' FOR UPDATE");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            valor = rs.getInt(1);
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE control SET valor = valor + 1 WHERE parametro='Envios'")) {
            ps.executeUpdate();
        }
        return String.format("LR%08d", valor);
    }

    private Integer registrarDireccion(Connection cn, DireccionTo d) throws SQLException {
        if (d == null) {
            return null;
        }
        try (CallableStatement cs = cn.prepareCall("{call sp_Direccion_Registrar(?,?,?,?,?,?,?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);     // parámetro de salida (id generado)
            cs.setString(2, d.getCalle());
            cs.setString(3, d.getDistrito());
            cs.setString(4, d.getProvincia());
            cs.setString(5, d.getDepartamento());
            cs.setDouble(6, d.getLatitud());
            cs.setDouble(7, d.getLongitud());
            cs.executeUpdate();
            return cs.getInt(1);
        }
    }

    private static void setEntero(CallableStatement cs, int i, Integer valor) throws SQLException {
        if (valor == null) {
            cs.setNull(i, Types.INTEGER);
        } else {
            cs.setInt(i, valor);
        }
    }

    private EnvioTo mapear(ResultSet rs) throws SQLException {
        EnvioTo e = new EnvioTo();
        e.setCodigo(rs.getString("codigo"));
        e.setIdcliente(rs.getString("idcliente"));
        e.setServicio(TipoServicio.valueOf(rs.getString("idservicio")));
        e.setNombreDestinatario(rs.getString("nombre_dest"));
        e.setTelefonoDestinatario(rs.getString("telefono_dest"));
        e.setEmailDestinatario(rs.getString("email_dest"));
        e.setPesoKg(rs.getDouble("peso_kg"));
        e.setValorDeclarado(rs.getDouble("valor_declarado"));
        e.setFragil(rs.getBoolean("fragil"));
        e.setConSeguro(rs.getBoolean("con_seguro"));
        e.setRequiereFirma(rs.getBoolean("requiere_firma"));
        Date fp = rs.getDate("fecha_programada");
        e.setFechaProgramada(fp == null ? null : fp.toLocalDate());
        e.setIdtiendaRecojo(rs.getString("idsucursal_recojo"));
        e.setEstado(EstadoEnvio.valueOf(rs.getString("estado")));
        e.setCosto(rs.getDouble("costo"));
        int zona = rs.getInt("idzona_destino");
        e.setIdzonaDestino(rs.wasNull() ? null : zona);
        e.setPasoFlujo(rs.getInt("paso_flujo"));
        e.setIntentos(rs.getInt("intentos"));
        Date fe = rs.getDate("fecha_estimada");
        e.setFechaEstimada(fe == null ? null : fe.toLocalDate());
        Timestamp fr = rs.getTimestamp("fecha_registro");
        e.setFechaRegistro(fr == null ? null : fr.toLocalDateTime());
        e.setCodigoFirma(rs.getString("codigo_firma"));
        if (rs.getString("calle") != null) {
            DireccionTo d = new DireccionTo(rs.getString("calle"), rs.getString("distrito"),
                    rs.getString("provincia"), rs.getString("departamento"));
            d.setLatitud(rs.getDouble("latitud"));
            d.setLongitud(rs.getDouble("longitud"));
            e.setDestino(d);
        }
        if (rs.getString("ocalle") != null) {
            e.setOrigen(new DireccionTo(rs.getString("ocalle"), rs.getString("odistrito"),
                    rs.getString("oprovincia"), rs.getString("odepartamento")));
        }
        return e;
    }
}
