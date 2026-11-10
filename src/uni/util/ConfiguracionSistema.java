package uni.util;

import uni.controller.GestorComandos;
import uni.controller.RegistroEnvioFacade;
import uni.dao.AlertaDao;
import uni.dao.AsignacionDao;
import uni.dao.AuditoriaDao;
import uni.dao.ComandoLogDao;
import uni.dao.EnvioDao;
import uni.dao.IncidenciaDao;
import uni.dao.NotificacionDao;
import uni.dao.SeguimientoDao;
import uni.database.AccesoDB;
import uni.service.AlertaSupervisorObserver;
import uni.service.IFabricaIntegraciones;
import uni.service.ISeguimientoService;
import uni.service.IndicadoresObserver;
import uni.service.NotificacionDestinatarioObserver;
import uni.service.SeguimientoServiceImpl;
import uni.service.SeguimientoServiceProxy;

/**
 * PATRÓN SINGLETON - "raíz de composición" de la aplicación.
 * Es el único lugar donde se eligen las clases concretas (DAO MySQL,
 * familia de integraciones, observadores). Los formularios piden aquí los
 * servicios ya armados y trabajan solo con interfaces (DIP).
 */
public final class ConfiguracionSistema {

    private static ConfiguracionSistema instancia;

    private final IFabricaIntegraciones integraciones;
    private final SeguimientoServiceImpl seguimientoReal;
    private final ISeguimientoService seguimiento;
    private final RegistroEnvioFacade registroEnvio;
    private final GestorComandos gestorComandos;
    private final HistorialMementos historialMementos = new HistorialMementos();
    private final IndicadoresObserver indicadores = new IndicadoresObserver();
    private final RegistroPlantillas plantillas = new RegistroPlantillas();
    private final uni.service.GestionLaboralService gestionLaboral;

    private ConfiguracionSistema() {
        // Abstract Factory: familia elegida en db.properties (integraciones=PRODUCCION|CONTINGENCIA)
        integraciones = "CONTINGENCIA".equalsIgnoreCase(AccesoDB.getInstancia().getFamiliaIntegraciones())
                ? new FabricaIntegracionContingencia() : new FabricaIntegracionProduccion();

        EnvioDao envioDao = new EnvioDao();
        SeguimientoDao seguimientoDao = new SeguimientoDao();

        // Servicio real + observadores (Observer)
        seguimientoReal = new SeguimientoServiceImpl(envioDao, seguimientoDao, new IncidenciaDao(),
                integraciones.crearFirmaDigital());
        seguimientoReal.agregarObservador(new NotificacionDestinatarioObserver(
                integraciones.crearNotificador(), new NotificacionDao()));
        seguimientoReal.agregarObservador(new AlertaSupervisorObserver(new AlertaDao()));
        seguimientoReal.agregarObservador(indicadores);

        // Proxy de protección delante del servicio real
        seguimiento = new SeguimientoServiceProxy(seguimientoReal, new AsignacionDao(), new AuditoriaDao());

        // Facade de registro de envíos
        registroEnvio = new RegistroEnvioFacade(envioDao, seguimientoDao, integraciones);

        // Invoker de comandos (Command)
        gestorComandos = new GestorComandos(new ComandoLogDao());

        // State + Observer del empleado (ejercicio de la Sesión 12)
        gestionLaboral = new uni.service.GestionLaboralService(new uni.dao.EmpleadoDao());
        gestionLaboral.agregarObservador(new uni.service.RecursosHumanosObserver(new uni.dao.HistorialLaboralDao()));
        gestionLaboral.agregarObservador(new uni.service.OperacionesObserver(new AsignacionDao(), new AlertaDao()));
    }

    public uni.service.GestionLaboralService getGestionLaboral() { return gestionLaboral; }

    public static synchronized ConfiguracionSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracionSistema();
        }
        return instancia;
    }

    public IFabricaIntegraciones getIntegraciones() { return integraciones; }

    /** Servicio real: solo para suscribir/desuscribir observadores de pantalla. */
    public SeguimientoServiceImpl getPublicadorEventos() { return seguimientoReal; }

    /** Servicio protegido por el Proxy: el que usan formularios y comandos. */
    public ISeguimientoService getSeguimiento() { return seguimiento; }

    public RegistroEnvioFacade getRegistroEnvio() { return registroEnvio; }

    public GestorComandos getGestorComandos() { return gestorComandos; }

    public HistorialMementos getHistorialMementos() { return historialMementos; }

    public IndicadoresObserver getIndicadores() { return indicadores; }

    public RegistroPlantillas getPlantillas() { return plantillas; }
}
