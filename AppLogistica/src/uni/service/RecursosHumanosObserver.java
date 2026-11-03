package uni.service;

import uni.entity.EmpleadoTo;
import uni.util.SesionUsuario;

/** OBSERVER concreto: Recursos Humanos guarda el estado y su historial laboral. */
public class RecursosHumanosObserver implements IObservadorEmpleado {

    /** Persistencia mínima que necesita RR. HH. (ISP). */
    public interface IRegistroLaboral {
        void guardarEstado(String idempleado, String estado) throws Exception;

        void registrarHistorial(String idempleado, String anterior, String nuevo,
                                String motivo, String usuario) throws Exception;
    }

    private final IRegistroLaboral registro;

    public RecursosHumanosObserver(IRegistroLaboral registro) {
        this.registro = registro;
    }

    @Override
    public void actualizar(EmpleadoTo e, String anterior, String nuevo, String motivo) {
        try {
            String usuario = null;
            try {
                usuario = SesionUsuario.getInstancia().getUsuario().getIdempleado();
            } catch (IllegalStateException sinSesion) {
                // pruebas sin sesión iniciada
            }
            registro.guardarEstado(e.getIdempleado(), nuevo);
            registro.registrarHistorial(e.getIdempleado(), anterior, nuevo, motivo, usuario);
            System.out.println("   [RR.HH.] " + e.getIdempleado() + ": " + anterior + " -> " + nuevo
                    + (motivo == null ? "" : " (" + motivo + ")"));
        } catch (Exception ex) {
            throw new IllegalStateException("RR.HH. no pudo registrar el cambio: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getNombre() {
        return "Recursos Humanos";
    }
}
