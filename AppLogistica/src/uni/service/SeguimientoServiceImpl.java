package uni.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import uni.entity.AccionEnvio;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.IncidenciaTo;
import uni.entity.SeguimientoTo;
import uni.util.ServicioEnvioFactory;
import uni.util.SesionUsuario;

/**
 * PATRÓN PROXY - RealSubject: lógica real de seguimiento.
 * Desde el Capítulo VI ya no cambia el estado "a mano": arma un
 * EnvioContexto (State), le suscribe los observadores (Observer) y deja que
 * el estado actual valide la transición. Persistir, registrar historial,
 * notificar y alertar son reacciones de los observadores.
 */
public class SeguimientoServiceImpl implements ISeguimientoService {

    private final ICrudDao<EnvioTo> envioDao;
    private final IRegistroHistorico<SeguimientoTo> historialDao;
    private final IRegistroHistorico<IncidenciaTo> incidenciaDao;
    private final IFirmaDigital firmaDigital;
    private final ServicioEnvioFactory fabricaServicios = new ServicioEnvioFactory();
    private final List<IObservadorEnvio> observadores = new CopyOnWriteArrayList<>();

    public SeguimientoServiceImpl(ICrudDao<EnvioTo> envioDao,
                                  IRegistroHistorico<SeguimientoTo> historialDao) {
        this(envioDao, historialDao, null, null);
    }

    public SeguimientoServiceImpl(ICrudDao<EnvioTo> envioDao,
                                  IRegistroHistorico<SeguimientoTo> historialDao,
                                  IRegistroHistorico<IncidenciaTo> incidenciaDao,
                                  IFirmaDigital firmaDigital) {
        this.envioDao = envioDao;
        this.historialDao = historialDao;
        this.incidenciaDao = incidenciaDao;
        this.firmaDigital = firmaDigital;
        // Observadores obligatorios: sin ellos el cambio no quedaría guardado ni trazado.
        observadores.add(new PersistenciaEnvioObserver(envioDao));
        observadores.add(new HistorialSeguimientoObserver(historialDao));
    }

    /** Registro de observadores adicionales (notificación, alertas, pantallas...). */
    public void agregarObservador(IObservadorEnvio o) {
        observadores.add(o);
    }

    public void quitarObservador(IObservadorEnvio o) {
        observadores.remove(o);
    }

    public List<IObservadorEnvio> getObservadores() {
        return observadores;
    }

    /** Arma el contexto State/Observer de un envío. */
    public EnvioContexto cargarContexto(String codigoEnvio) throws Exception {
        EnvioTo envio = envioDao.find(codigoEnvio);
        if (envio == null) {
            throw new IllegalArgumentException("Envío no existe: " + codigoEnvio);
        }
        EnvioContexto ctx = new EnvioContexto(envio, fabricaServicios.crearServicio(envio.getServicio()));
        for (IObservadorEnvio o : observadores) {
            ctx.suscribir(o);
        }
        return ctx;
    }

    private String usuarioActual() {
        return SesionUsuario.getInstancia().getUsuario().getIdempleado();
    }

    @Override
    public List<SeguimientoTo> consultarHistorial(String codigoEnvio) throws Exception {
        System.out.println("   (consulta a la base de datos: historial de " + codigoEnvio + ")");
        return historialDao.listarPor(codigoEnvio);
    }

    @Override
    public void actualizarEstado(String codigoEnvio, EstadoEnvio nuevo,
                                 String ubicacion, String observacion) throws Exception {
        EnvioContexto ctx = cargarContexto(codigoEnvio);
        ctx.ejecutar(AccionEnvio.hacia(nuevo), usuarioActual(), ubicacion, observacion, null);
    }

    @Override
    public EnvioTo buscarEnvio(String codigoEnvio) throws Exception {
        return envioDao.find(codigoEnvio);
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(String codigoEnvio) throws Exception {
        return cargarContexto(codigoEnvio).accionesPermitidas();
    }

    @Override
    public void registrarIncidencia(String codigoEnvio, String motivo, String descripcion,
                                    String ubicacion) throws Exception {
        EnvioContexto ctx = cargarContexto(codigoEnvio);
        String texto = motivo + (descripcion == null || descripcion.isEmpty() ? "" : ": " + descripcion);
        ctx.ejecutar(AccionEnvio.OBSERVAR, usuarioActual(), ubicacion, texto, null); // valida primero
        if (incidenciaDao != null) {
            IncidenciaTo inc = new IncidenciaTo(codigoEnvio, motivo, null, descripcion);
            inc.setIdempleado(usuarioActual());
            incidenciaDao.registrar(inc);
        }
    }

    @Override
    public void reprogramar(String codigoEnvio, LocalDate nuevaFecha, String observacion) throws Exception {
        EnvioContexto ctx = cargarContexto(codigoEnvio);
        ctx.ejecutar(AccionEnvio.REPROGRAMAR, usuarioActual(), "Central de operaciones",
                observacion, nuevaFecha);
    }

    @Override
    public void confirmarEntrega(String codigoEnvio, String dniReceptor, String ubicacion) throws Exception {
        EnvioContexto ctx = cargarContexto(codigoEnvio);
        EnvioTo envio = ctx.getEnvio();
        String obs = "Entregado al destinatario";
        if (envio.isRequiereFirma()) {
            if (firmaDigital == null) {
                throw new IllegalStateException("No hay proveedor de firma digital configurado.");
            }
            if (dniReceptor == null || !dniReceptor.matches("\\d{8}")) {
                throw new IllegalArgumentException("Ingrese el DNI (8 dígitos) de quien recibe.");
            }
            envio.setCodigoFirma(firmaDigital.firmarEntrega(codigoEnvio, dniReceptor));
            obs = "Entregado con firma digital " + envio.getCodigoFirma()
                    + " (" + firmaDigital.getProveedor() + ")";
        }
        ctx.ejecutar(AccionEnvio.ENTREGAR, usuarioActual(), ubicacion, obs, null);
    }

    @Override
    public EnvioMemento crearPuntoRestauracion(String codigoEnvio, String etiqueta) throws Exception {
        return cargarContexto(codigoEnvio).crearMemento(etiqueta);
    }

    @Override
    public void restaurar(String codigoEnvio, EnvioMemento memento, String motivo) throws Exception {
        cargarContexto(codigoEnvio).restaurar(memento, usuarioActual(), motivo);
    }
}
