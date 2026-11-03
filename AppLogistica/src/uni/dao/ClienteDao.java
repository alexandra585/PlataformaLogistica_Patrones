package uni.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.ClienteTo;
import uni.service.ICrudDao;

/** DAO de clientes (molde ClienteDao/EmpleadoDao de AppEmpresa, ahora con CRUD completo). */
public class ClienteDao implements ICrudDao<ClienteTo> {

    @Override
    public void create(ClienteTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try {
            cn.setAutoCommit(false);
            o.setIdcliente(generaCodigo(cn));
            try (CallableStatement cs = cn.prepareCall("{call sp_Cliente_Adicionar(?,?,?,?,?,?)}")) {
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
    public void update(ClienteTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Cliente_Actualizar(?,?,?,?,?,?)}")) {
            cargar(cs, o);
            cs.executeUpdate();
        }
    }

    @Override
    public void delete(ClienteTo o) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Cliente_Eliminar(?)}")) {
            cs.setString(1, o.getIdcliente());
            cs.executeUpdate();
        }
    }

    @Override
    public ClienteTo find(Object id) throws Exception {
        for (ClienteTo c : consultar("SELECT * FROM clientes WHERE idcliente = ?", id.toString())) {
            return c;
        }
        return null;
    }

    @Override
    public List<ClienteTo> readAll() throws Exception {
        return consultar("SELECT * FROM clientes ORDER BY idcliente", null);
    }

    public List<ClienteTo> buscarPorNombre(String texto) throws Exception {
        return consultar("SELECT * FROM clientes WHERE razonsocial LIKE ? ORDER BY razonsocial",
                "%" + texto + "%");
    }

    private void cargar(CallableStatement cs, ClienteTo o) throws SQLException {
        cs.setString(1, o.getIdcliente());
        cs.setString(2, o.getRazonsocial());
        cs.setString(3, o.getTipocliente());
        cs.setString(4, o.getNrodocumento());
        cs.setString(5, o.getEmail());
        cs.setString(6, o.getTelefono());
    }

    private List<ClienteTo> consultar(String sql, String parametro) throws Exception {
        List<ClienteTo> lista = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ClienteTo(rs.getString("idcliente"), rs.getString("razonsocial"),
                            rs.getString("tipocliente"), rs.getString("nrodocumento"),
                            rs.getString("email"), rs.getString("telefono")));
                }
            }
        }
        return lista;
    }

    private String generaCodigo(Connection cn) throws SQLException {
        int cont;
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT valor FROM control WHERE parametro='Clientes' FOR UPDATE");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            cont = rs.getInt(1);
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE control SET valor = valor + 1 WHERE parametro='Clientes'")) {
            ps.executeUpdate();
        }
        return String.format("C%04d", cont);
    }
}
