package uni.service;

import java.util.Arrays;
import java.util.List;
import uni.entity.EmpleadoTo;

/** Activo: opera normalmente; puede salir de vacaciones o ser cesado. */
public class EstadoLaboralActivo extends EstadoLaboralBase {

    @Override
    public String getCodigo() { return "ACTIVO"; }

    @Override
    public boolean puedeIniciarSesion() { return true; }

    @Override
    public boolean puedeRecibirEnvios() { return true; }

    @Override
    public String mostrarEstado(EmpleadoTo e) {
        return e.getNombreCompleto() + " está ACTIVO: puede operar y recibir envíos asignados.";
    }

    @Override
    public void enviarVacaciones(EmpleadoContexto c) { c.cambiarEstado(new EstadoLaboralVacaciones()); }

    @Override
    public void cesar(EmpleadoContexto c) { c.cambiarEstado(new EstadoLaboralInactivo()); }

    @Override
    public List<String> accionesPermitidas() { return Arrays.asList("VACACIONES", "CESAR"); }
}
