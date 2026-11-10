package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EmpleadoTo;

/** Inactivo (cesado): conserva su historial, pero no opera; puede reincorporarse. */
public class EstadoLaboralInactivo extends EstadoLaboralBase {

    @Override
    public String getCodigo() { return "INACTIVO"; }

    @Override
    public boolean puedeIniciarSesion() { return false; }

    @Override
    public boolean puedeRecibirEnvios() { return false; }

    @Override
    public String mostrarEstado(EmpleadoTo e) {
        return e.getNombreCompleto() + " está INACTIVO: se conserva su historial de seguimiento.";
    }

    @Override
    public void reincorporar(EmpleadoContexto c) { c.cambiarEstado(new EstadoLaboralActivo()); }

    @Override
    public List<String> accionesPermitidas() { return Arrays.asList("REINCORPORAR"); }

}
