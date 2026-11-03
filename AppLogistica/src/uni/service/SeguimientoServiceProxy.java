package uni.service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uni.entity.AccionEnvio;
import uni.entity.EmpleadoTo;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.RolUsuario;
import uni.entity.SeguimientoTo;
import uni.util.SesionUsuario;

/**
 * PATRÓN PROXY (de protección + caché + auditoría).
 * Misma interfaz que el servicio real; controla el acceso a él:
 *  1) Protección: solo los roles operativos cambian estados; un repartidor
 *     solo opera los envíos que tiene asignados; revertir una entrega
 *     (deshacer de Command/Memento) está reservado a supervisores.
 *  2) Caché: las consultas repetidas de tracking no vuelven a la BD.
 *  3) Auditoría: cada intento (permitido o no) queda registrado.
 */
public class SeguimientoServiceProxy implements ISeguimientoService {

    private static final Set<RolUsuario> ROLES_OPERATIVOS = EnumSet.of(
            RolUsuario.REPARTIDOR, RolUsuario.OPERADOR_ALMACEN,
            RolUsuario.SUPERVISOR_RUTA, RolUsuario.ADMINISTRADOR);

    private static final Set<RolUsuario> ROLES_SUPERVISION = EnumSet.of(
            RolUsuario.SUPERVISOR_RUTA, RolUsuario.ADMINISTRADOR);

    private final ISeguimientoService real;
    private final IConsultaAsignacion asignaciones;
    private final IRegistroAuditoria auditoria;          // null = solo consola
    private final Map<String, List<SeguimientoTo>> cache = new HashMap<>();

    /** Constructor usado en las pruebas del Capítulo V (asignaciones en memoria). */
    public SeguimientoServiceProxy(ISeguimientoService real, Map<String, String> asignaciones) {
        this(real, asignaciones::get, null);
    }

    public SeguimientoServiceProxy(ISeguimientoService real, IConsultaAsignacion asignaciones,
                                   IRegistroAuditoria auditoria) {
        this.real = real;
        this.asignaciones = asignaciones;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------ lecturas
    @Override
    public List<SeguimientoTo> consultarHistorial(String codigoEnvio) throws Exception {
        auditar("CONSULTA", codigoEnvio, true);
        if (cache.containsKey(codigoEnvio)) {
            System.out.println("   (respuesta desde caché del proxy)");
            return cache.get(codigoEnvio);
        }
        List<SeguimientoTo> historial = real.consultarHistorial(codigoEnvio);
        cache.put(codigoEnvio, historial);
        return historial;
    }

    @Override
    public EnvioTo buscarEnvio(String codigoEnvio) throws Exception {
        return real.buscarEnvio(codigoEnvio);
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(String codigoEnvio) throws Exception {
        if (!puedeOperar(codigoEnvio)) {
            return java.util.Collections.emptyList();       // la vista deshabilita los botones
        }
        return real.accionesPermitidas(codigoEnvio);
    }

    @Override
    public EnvioMemento crearPuntoRestauracion(String codigoEnvio, String etiqueta) throws Exception {
        return real.crearPuntoRestauracion(codigoEnvio, etiqueta);
    }

    // ----------------------------------------------------------- escrituras
    @Override
    public void actualizarEstado(String codigoEnvio, EstadoEnvio nuevo,
                                 String ubicacion, String observacion) throws Exception {
        verificarOperacion("ACTUALIZAR->" + nuevo, codigoEnvio);
        real.actualizarEstado(codigoEnvio, nuevo, ubicacion, observacion);
        cache.remove(codigoEnvio);   // invalida la caché para no mostrar datos viejos
    }

    @Override
    public void registrarIncidencia(String codigoEnvio, String motivo, String descripcion,
                                    String ubicacion) throws Exception {
        verificarOperacion("INCIDENCIA", codigoEnvio);
        real.registrarIncidencia(codigoEnvio, motivo, descripcion, ubicacion);
        cache.remove(codigoEnvio);
    }

    @Override
    public void reprogramar(String codigoEnvio, LocalDate nuevaFecha, String observacion) throws Exception {
        verificarOperacion("REPROGRAMAR", codigoEnvio);
        real.reprogramar(codigoEnvio, nuevaFecha, observacion);
        cache.remove(codigoEnvio);
    }

    @Override
    public void confirmarEntrega(String codigoEnvio, String dniReceptor, String ubicacion) throws Exception {
        verificarOperacion("ENTREGAR", codigoEnvio);
        real.confirmarEntrega(codigoEnvio, dniReceptor, ubicacion);
        cache.remove(codigoEnvio);
    }

    @Override
    public void restaurar(String codigoEnvio, EnvioMemento memento, String motivo) throws Exception {
        EmpleadoTo u = SesionUsuario.getInstancia().getUsuario();
        EnvioTo actual = real.buscarEnvio(codigoEnvio);
        if (actual != null && actual.getEstado() == EstadoEnvio.ENTREGADO
                && !ROLES_SUPERVISION.contains(u.getRol())) {
            auditar("RESTAURAR(ENTREGADO)", codigoEnvio, false);
            throw new SecurityException("Solo un supervisor puede revertir una entrega confirmada.");
        }
        verificarOperacion("RESTAURAR", codigoEnvio);
        real.restaurar(codigoEnvio, memento, motivo);
        cache.remove(codigoEnvio);
    }

    // ------------------------------------------------------------ control
    private boolean puedeOperar(String codigoEnvio) throws Exception {
        EmpleadoTo u = SesionUsuario.getInstancia().getUsuario();
        if (!ROLES_OPERATIVOS.contains(u.getRol())) {
            return false;
        }
        return u.getRol() != RolUsuario.REPARTIDOR
                || u.getIdempleado().equals(asignaciones.repartidorDe(codigoEnvio));
    }

    private void verificarOperacion(String accion, String codigoEnvio) throws Exception {
        EmpleadoTo usuario = SesionUsuario.getInstancia().getUsuario();
        if (!ROLES_OPERATIVOS.contains(usuario.getRol())) {
            auditar(accion, codigoEnvio, false);
            throw new SecurityException("El rol " + usuario.getRol() + " no puede modificar estados.");
        }
        if (usuario.getRol() == RolUsuario.REPARTIDOR
                && !usuario.getIdempleado().equals(asignaciones.repartidorDe(codigoEnvio))) {
            auditar(accion, codigoEnvio, false);
            throw new SecurityException("El envío " + codigoEnvio
                    + " no está asignado al repartidor " + usuario.getIdempleado());
        }
        auditar(accion, codigoEnvio, true);
    }

    private void auditar(String accion, String codigoEnvio, boolean permitido) {
        EmpleadoTo u = SesionUsuario.getInstancia().getUsuario();
        System.out.printf("[AUDITORÍA] %s (%s) -> %s sobre %s : %s%n",
                u.getIdempleado(), u.getRol(), accion, codigoEnvio, permitido ? "PERMITIDO" : "DENEGADO");
        if (auditoria != null) {
            try {
                auditoria.registrar(u.getIdempleado(), u.getRol().name(), accion, codigoEnvio, permitido);
            } catch (Exception ex) {
                System.out.println("[AUDITORÍA] no se pudo guardar: " + ex.getMessage());
            }
        }
    }
}
