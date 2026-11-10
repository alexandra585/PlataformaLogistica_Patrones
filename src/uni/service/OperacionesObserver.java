package uni.service;

import java.util.List;
import uni.entity.AlertaTo;
import uni.entity.EmpleadoTo;
import uni.entity.RolUsuario;

/**
 * OBSERVER concreto: Operaciones reacciona cuando un repartidor deja de
 * estar disponible (vacaciones o cese) y genera una alerta por cada envío
 * que tenía asignado, para que el supervisor lo reasigne.
 */
public class OperacionesObserver implements IObservadorEmpleado {

    private final IConsultaAsignacion asignaciones;
    private final IRegistroHistorico<AlertaTo> alertas;

    public OperacionesObserver(IConsultaAsignacion asignaciones, IRegistroHistorico<AlertaTo> alertas) {
        this.asignaciones = asignaciones;
        this.alertas = alertas;
    }

    @Override
    public void actualizar(EmpleadoTo e, String anterior, String nuevo, String motivo) {
        if (e.getRol() != RolUsuario.REPARTIDOR || "ACTIVO".equals(nuevo)) {
            return;
        }
        try {
            List<String> pendientes = asignaciones.enviosPendientesDe(e.getIdempleado());
            for (String codigo : pendientes) {
                alertas.registrar(new AlertaTo(codigo, "ALTA", "Reasignar: el repartidor "
                        + e.getNombreCompleto() + " pasó a " + nuevo + "."));
            }
            System.out.println("   [Operaciones] " + pendientes.size() + " envío(s) por reasignar de "
                    + e.getIdempleado() + ": " + pendientes);
        } catch (Exception ex) {
            System.out.println("   [Operaciones] " + ex.getMessage());
        }
    }

    @Override
    public String getNombre() {
        return "Operaciones (reasignación de envíos)";
    }
}
