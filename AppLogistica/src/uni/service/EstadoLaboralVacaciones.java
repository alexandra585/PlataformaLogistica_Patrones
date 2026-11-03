package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EmpleadoTo;

/** De vacaciones: no ingresa al sistema ni recibe envíos; solo puede reincorporarse. */
public class EstadoLaboralVacaciones extends EstadoLaboralBase {

    @Override
    public String getCodigo() { return "VACACIONES"; }

    @Override
    public boolean puedeIniciarSesion() { return false; }

    @Override
    public boolean puedeRecibirEnvios() { return false; }

    @Override
    public String mostrarEstado(EmpleadoTo e) {
        return e.getNombreCompleto() + " está DE VACACIONES: sin acceso al sistema ni asignación de envíos.";
    }

    @Override
    public void reincorporar(EmpleadoContexto c) { c.cambiarEstado(new EstadoLaboralActivo()); }

    @Override
    public List<String> accionesPermitidas() { return Arrays.asList("REINCORPORAR"); }
}
