package uni.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.ItemCombo;

/** Tablas maestras pequeñas que llenan los JComboBox de los formularios. */
public class CatalogoDao {

    public List<ItemCombo> zonas() throws Exception {
        return items("SELECT idzona, nombre FROM zonas ORDER BY idzona");
    }

    public List<ItemCombo> tiendasRecojo() throws Exception {
        return items("SELECT idsucursal, CONCAT(nombre,' - ',direccion) FROM sucursales "
                + "WHERE tipo='TIENDA_RECOJO' AND activo=1");
    }

    public List<ItemCombo> sucursales() throws Exception {
        return items("SELECT idsucursal, nombre FROM sucursales WHERE activo=1 ORDER BY idsucursal");
    }

    public List<ItemCombo> vehiculos() throws Exception {
        return items("SELECT placa, CONCAT(placa,' - ',tipo,' (',capacidad_kg,' kg)') FROM vehiculos WHERE activo=1");
    }

    public List<ItemCombo> repartidores() throws Exception {
        return items("SELECT idempleado, CONCAT(idempleado,' - ',nombre,' ',apellidos) FROM empleados "
                + "WHERE rol='REPARTIDOR' AND estado_laboral='ACTIVO' ORDER BY nombre");
    }

    public List<ItemCombo> clientes() throws Exception {
        return items("SELECT idcliente, CONCAT(idcliente,' - ',razonsocial) FROM clientes ORDER BY idcliente");
    }

    private List<ItemCombo> items(String sql) throws Exception {
        List<ItemCombo> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                l.add(new ItemCombo(rs.getString(1), rs.getString(2)));
            }
        }
        return l;
    }
}
