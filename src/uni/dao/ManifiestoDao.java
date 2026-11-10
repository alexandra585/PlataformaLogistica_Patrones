package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import uni.database.AccesoDB;
import uni.service.AgrupacionCarga;
import uni.service.IUnidadCarga;
import uni.service.PaqueteCarga;

/**
 * Guarda el árbol del Composite (manifiesto > paletas/sacos > paquetes)
 * en unidades_carga (auto-relación idpadre) y unidad_detalle, crea la ruta
 * y asigna los envíos al repartidor. Todo en una sola transacción.
 */
public class ManifiestoDao {

    public String siguienteCodigo() throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT valor FROM control WHERE parametro='Manifiestos'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("MF-%d-%04d", java.time.LocalDate.now().getYear(), rs.getInt(1));
        }
    }

    /** @return id de la ruta creada. */
    public int guardar(AgrupacionCarga manifiesto, int idzona, String placa,
                       String idsupervisor, String idrepartidor) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE control SET valor = valor + 1 WHERE parametro='Manifiestos'")) {
                ps.executeUpdate();
            }
            int idruta;
            try (CallableStatement cs = cn.prepareCall("{call sp_Ruta_Crear(?,?,?,?)}")) {
                cs.registerOutParameter(1, Types.INTEGER);
                cs.setInt(2, idzona);
                cs.setString(3, placa);
                cs.setString(4, idsupervisor);
                cs.executeUpdate();
                idruta = cs.getInt(1);
            }
            guardarNodo(cn, manifiesto, null, idruta, idrepartidor);
            cn.commit();
            return idruta;
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }

    /** Recorrido recursivo del árbol (mismo recorrido que hace el Composite). */
    private void guardarNodo(Connection cn, AgrupacionCarga nodo, String idpadre, int idruta,
                             String idrepartidor) throws SQLException {
        try (CallableStatement cs = cn.prepareCall("{call sp_Unidad_Registrar(?,?,?,?,?)}")) {
            cs.setString(1, nodo.getCodigo());
            cs.setString(2, nodo.getTipo());
            cs.setDouble(3, nodo.getTaraKg());
            cs.setString(4, idpadre);
            cs.setInt(5, idruta);
            cs.executeUpdate();
        }
        for (IUnidadCarga hijo : nodo.getHijos()) {
            if (hijo instanceof AgrupacionCarga) {
                guardarNodo(cn, (AgrupacionCarga) hijo, nodo.getCodigo(), idruta, idrepartidor);
            } else if (hijo instanceof PaqueteCarga) {
                try (CallableStatement cs = cn.prepareCall("{call sp_UnidadDetalle_Registrar(?,?)}")) {
                    cs.setString(1, nodo.getCodigo());
                    cs.setString(2, hijo.getCodigo());
                    cs.executeUpdate();
                }
                try (CallableStatement cs = cn.prepareCall("{call sp_Asignar_Repartidor(?,?,?)}")) {
                    cs.setString(1, hijo.getCodigo());
                    cs.setString(2, idrepartidor);
                    cs.setInt(3, idruta);
                    cs.executeUpdate();
                }
            }
        }
    }

    /** Códigos de manifiestos registrados (para el reporte Jasper), del más reciente al más antiguo. */
    public java.util.List<String> listarCodigos() throws Exception {
        java.util.List<String> l = new java.util.ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT idunidad FROM unidades_carga WHERE tipo = 'MANIFIESTO' ORDER BY idunidad DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                l.add(rs.getString(1));
            }
        }
        return l;
    }
}
