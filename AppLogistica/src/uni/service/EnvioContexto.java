package uni.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.EventoEnvio;
import uni.util.TransicionInvalidaException;

/**
 * Clase central del Capítulo VI. Cumple tres roles:
 *  - PATRÓN STATE    -> Context: delega cada acción en el estado actual.
 *  - PATRÓN OBSERVER -> ConcreteSubject: avisa a los observadores de cada cambio.
 *  - PATRÓN MEMENTO  -> Originator: crea y restaura instantáneas de sí mismo.
 */
public class EnvioContexto implements ISujetoEnvio {

    private final EnvioTo envio;
    private final IServicioEnvio servicio;          // flujo de estados (Factory)
    private IEstadoEnvio estado;
    private final List<IObservadorEnvio> observadores = new ArrayList<>();

    // Datos de la operación en curso (quién, dónde, por qué)
    private String idempleado;
    private String ubicacion;
    private String observacion;

    public EnvioContexto(EnvioTo envio, IServicioEnvio servicio) {
        this.envio = envio;
        this.servicio = servicio;
        this.estado = CatalogoEstados.de(envio.getEstado());
    }

    // ============================================================ STATE
    /** Punto de entrada usado por el servicio de seguimiento. */
    public void ejecutar(AccionEnvio accion, String idempleado, String ubicacion,
                         String observacion, LocalDate fecha) {
        this.idempleado = idempleado;
        this.ubicacion = ubicacion;
        this.observacion = observacion;
        switch (accion) {
            case RECOGER:     estado.recoger(this); break;
            case TRASLADAR:   estado.trasladar(this); break;
            case CLASIFICAR:  estado.clasificar(this); break;
            case DESPACHAR:   estado.despachar(this); break;
            case ENTREGAR:    estado.entregar(this); break;
            case OBSERVAR:    estado.observar(this); break;
            case REPROGRAMAR: estado.reprogramar(this, fecha); break;
            case DEVOLVER:    estado.devolver(this); break;
            default: throw new IllegalArgumentException("Acción no soportada: " + accion);
        }
    }

    public List<AccionEnvio> accionesPermitidas() {
        return estado.accionesPermitidas(this);
    }

    public IEstadoEnvio getEstado() {
        return estado;
    }

    /** ¿El siguiente paso del flujo del servicio es 'destino'? */
    boolean flujoPermite(EstadoEnvio destino) {
        List<EstadoEnvio> flujo = servicio.getFlujoEstados();
        int siguiente = envio.getPasoFlujo() + 1;
        return siguiente < flujo.size() && flujo.get(siguiente) == destino;
    }

    boolean flujoContiene(EstadoEnvio e) {
        return servicio.getFlujoEstados().contains(e);
    }

    /** Avance normal dentro del flujo del servicio. */
    void avanzar(EstadoEnvio destino) {
        if (!flujoPermite(destino)) {
            throw new TransicionInvalidaException("El servicio " + servicio.getTipo()
                    + " no pasa de " + envio.getEstado() + " a " + destino
                    + ". Flujo: " + servicio.getFlujoEstados());
        }
        envio.setPasoFlujo(envio.getPasoFlujo() + 1);
        cambiarEstado(destino);
    }

    /** Tras una reprogramación se retoma el flujo en la etapa indicada. */
    void retomarEn(EstadoEnvio etapa) {
        envio.setPasoFlujo(servicio.getFlujoEstados().lastIndexOf(etapa));
        cambiarEstado(etapa);
    }

    void registrarIntentoFallido() {
        envio.setIntentos(envio.getIntentos() + 1);
        cambiarEstado(EstadoEnvio.OBSERVADO);
    }

    void validarFirma() {
        if (envio.isRequiereFirma() && envio.getCodigoFirma() == null) {
            throw new TransicionInvalidaException("El envío " + envio.getCodigo()
                    + " requiere firma digital del receptor antes de marcarse ENTREGADO.");
        }
    }

    /** Cambia el objeto estado y avisa a los observadores. */
    void cambiarEstado(EstadoEnvio nuevo) {
        EstadoEnvio anterior = envio.getEstado();
        envio.setEstado(nuevo);
        estado = CatalogoEstados.de(nuevo);
        notificar(new EventoEnvio(envio, anterior, nuevo, ubicacion, observacion, idempleado, false));
    }

    // ========================================================= OBSERVER
    @Override
    public void suscribir(IObservadorEnvio o) {
        if (!observadores.contains(o)) {
            observadores.add(o);
        }
    }

    @Override
    public void desuscribir(IObservadorEnvio o) {
        observadores.remove(o);
    }

    @Override
    public void notificar(EventoEnvio evento) {
        for (IObservadorEnvio o : new ArrayList<>(observadores)) {
            o.actualizar(evento);
        }
    }

    // ========================================================== MEMENTO
    /** Guarda una instantánea del estado interno (sin exponerlo). */
    public EnvioMemento crearMemento(String etiqueta) {
        return new EnvioMemento(etiqueta, envio.getCodigo(), envio.getEstado(), envio.getPasoFlujo(),
                envio.getIntentos(), envio.getFechaProgramada(), envio.getCodigoFirma());
    }

    /** Vuelve exactamente a la instantánea y deja constancia en el historial. */
    public void restaurar(EnvioMemento m, String idempleado, String motivo) {
        if (!m.getCodigoEnvio().equals(envio.getCodigo())) {
            throw new IllegalArgumentException("El memento pertenece a otro envío.");
        }
        EstadoEnvio anterior = envio.getEstado();
        envio.setEstado(m.estado());
        envio.setPasoFlujo(m.pasoFlujo());
        envio.setIntentos(m.intentos());
        envio.setFechaProgramada(m.fechaProgramada());
        envio.setCodigoFirma(m.codigoFirma());
        estado = CatalogoEstados.de(m.estado());
        notificar(new EventoEnvio(envio, anterior, m.estado(), "Sistema",
                "REVERSIÓN: " + motivo, idempleado, true));
    }

    public EnvioTo getEnvio() {
        return envio;
    }

    public IServicioEnvio getServicio() {
        return servicio;
    }
}
