package uni.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import uni.database.AccesoDB;
import uni.entity.BoletaPagoTo;
import uni.service.IRegistroBoleta;

/**
 * GRASP FABRICACIÓN PURA (Sesión 17): clase inventada que no representa un
 * concepto del negocio; existe para que ni EmpleadoNomina ni la boleta
 * conozcan JDBC (alta cohesión, bajo acoplamiento).
 */
public class BoletaDao implements IRegistroBoleta {

    @Override
    public int registrar(BoletaPagoTo b, String usuario) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Boleta_Registrar(?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setString(2, b.getIdempleado());
            cs.setString(3, b.getPeriodo());
            cs.setBigDecimal(4, dec(b.getSueldoBasico()));
            cs.setInt(5, b.getHorasExtras());
            cs.setBigDecimal(6, dec(b.getMontoHorasExtras()));
            cs.setString(7, b.getRegimen());
            cs.setBigDecimal(8, dec(b.getAportePension()));
            cs.setBigDecimal(9, dec(b.getDescuentoSalud()));
            cs.setBigDecimal(10, dec(b.getTotalIngresos()));
            cs.setBigDecimal(11, dec(b.getTotalDescuentos()));
            cs.setBigDecimal(12, dec(b.getSueldoNeto()));
            cs.setString(13, usuario);
            cs.executeUpdate();
            return cs.getInt(1);
        }
    }

    @Override
    public void cambiarRegimen(String idempleado, String regimen) throws Exception {
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (CallableStatement cs = cn.prepareCall("{call sp_Empleado_CambiarRegimen(?,?)}")) {
            cs.setString(1, idempleado);
            cs.setString(2, regimen);
            cs.executeUpdate();
        }
    }

    /** Filas {idboleta, periodo, horas, regimen, ingresos, descuentos, neto, fecha} del empleado. */
    public List<Object[]> listar(String idempleado) throws Exception {
        List<Object[]> l = new ArrayList<>();
        Connection cn = AccesoDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement("SELECT idboleta, periodo, horas_extras, regimen, "
                + "total_ingresos, total_descuentos, sueldo_neto, fecha_emision FROM boletas_pago "
                + "WHERE idempleado = ? ORDER BY periodo DESC")) {
            ps.setString(1, idempleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4),
                        rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getTimestamp(8)});
                }
            }
        }
        return l;
    }

    private static BigDecimal dec(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
