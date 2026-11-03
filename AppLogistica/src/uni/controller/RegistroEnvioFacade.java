package uni.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.SeguimientoTo;
import uni.service.EmbalajeFragilDecorator;
import uni.service.FirmaDigitalDecorator;
import uni.service.ICrudDao;
import uni.service.IFabricaIntegraciones;
import uni.service.IGeolocalizador;
import uni.service.INotificador;
import uni.service.IRegistroHistorico;
import uni.service.IServicioEnvio;
import uni.service.ITarifa;
import uni.service.SeguroDecorator;
import uni.service.TarifaBase;
import uni.util.ServicioEnvioFactory;
import uni.util.ValidadorEnvio;

/**
 * PATRÓN FACADE.
 * Un único método registrarEnvio() oculta a los formularios (EnvioView,
 * importación masiva) los 8 subsistemas que intervienen en el registro.
 */
public class RegistroEnvioFacade {

    private final ValidadorEnvio validador = new ValidadorEnvio();
    private final ServicioEnvioFactory fabricaServicios = new ServicioEnvioFactory();
    private final ICrudDao<EnvioTo> envioDao;
    private final IRegistroHistorico<SeguimientoTo> historialDao;
    private final IGeolocalizador geolocalizador;
    private final INotificador notificador;

    public RegistroEnvioFacade(ICrudDao<EnvioTo> envioDao,
                               IRegistroHistorico<SeguimientoTo> historialDao,
                               IFabricaIntegraciones integraciones) {
        this.envioDao = envioDao;
        this.historialDao = historialDao;
        this.geolocalizador = integraciones.crearGeolocalizador();
        this.notificador = integraciones.crearNotificador();
    }

    /** Operación simplificada que usa la capa de presentación. */
    public String registrarEnvio(EnvioTo envio, String idempleado) throws Exception {
        // 1. Validación de datos
        List<String> errores = validador.validar(envio);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(errores.toString());
        }
        // 2. Regla del servicio (Factory)
        IServicioEnvio servicio = fabricaServicios.crearServicio(envio.getServicio());
        if (!servicio.admite(envio)) {
            throw new IllegalArgumentException("El servicio " + servicio.getTipo()
                    + " no admite este envío.");
        }
        // 3. Geolocalización del destino (Adapter vía Abstract Factory)
        if (envio.getDestino() != null) {
            geolocalizador.geocodificar(envio.getDestino());
        }
        // 4. Tarifa con servicios adicionales (Decorator)
        ITarifa tarifa = armarTarifa(servicio, envio);
        envio.setCosto(tarifa.getMonto());
        // 5. Persistencia del envío (DAO)
        envioDao.create(envio);
        // 6. Primer registro de trazabilidad
        historialDao.registrar(new SeguimientoTo(envio.getCodigo(), EstadoEnvio.REGISTRADO,
                "Agencia de origen", "Envío registrado", idempleado, LocalDateTime.now()));
        // 7. Notificación al destinatario (Adapter)
        LocalDate eta = servicio.calcularFechaEstimada(envio, LocalDate.now());
        notificador.enviar(envio.getTelefonoDestinatario(), "LogiRuta " + envio.getCodigo(),
                "Su envio fue registrado. Llegada estimada: " + eta);
        // 8. Resultado para la vista
        return envio.getCodigo() + " | " + tarifa.getDescripcion()
                + " | S/ " + String.format("%.2f", tarifa.getMonto()) + " | ETA " + eta;
    }

    /** Cotización previa para la vista (Factory + Decorator), sin registrar nada. */
    public ITarifa cotizar(EnvioTo envio) {
        IServicioEnvio servicio = fabricaServicios.crearServicio(envio.getServicio());
        return armarTarifa(servicio, envio);
    }

    /** Fecha estimada de entrega según el servicio elegido. */
    public LocalDate fechaEstimada(EnvioTo envio) {
        return fabricaServicios.crearServicio(envio.getServicio()).calcularFechaEstimada(envio, LocalDate.now());
    }

    private ITarifa armarTarifa(IServicioEnvio servicio, EnvioTo envio) {
        ITarifa t = new TarifaBase(servicio, envio);
        if (envio.isConSeguro()) {
            t = new SeguroDecorator(t, envio.getValorDeclarado());
        }
        if (envio.isFragil()) {
            t = new EmbalajeFragilDecorator(t);
        }
        if (envio.isRequiereFirma()) {
            t = new FirmaDigitalDecorator(t);
        }
        return t;
    }
}
