package uni.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uni.controller.EnvioController;
import uni.controller.RegistroEnvioFacade;
import uni.dao.EnvioDaoMemoria;
import uni.dao.SeguimientoDaoMemoria;
import uni.database.AccesoDB;
import uni.entity.DireccionTo;
import uni.entity.EmpleadoTo;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.IncidenciaTo;
import uni.entity.RolUsuario;
import uni.entity.SeguimientoTo;
import uni.entity.TipoServicio;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import uni.reportes.SalidaArchivoPdf;
import uni.reportes.Reporte;
import uni.reportes.ReporteCumplimientoTiempos;
import uni.reportes.ReporteIncidenciasZona;
import uni.service.AgrupacionCarga;
import uni.service.ICrudDao;
import uni.service.IFabricaIntegraciones;
import uni.service.INotificador;
import uni.service.ISeguimientoService;
import uni.service.IServicioEnvio;
import uni.service.ITarifa;
import uni.service.PaqueteCarga;
import uni.service.RastreoGpsDecorator;
import uni.service.SeguimientoServiceImpl;
import uni.service.SeguimientoServiceProxy;
import uni.service.SeguroDecorator;
import uni.service.FirmaDigitalDecorator;
import uni.service.EmbalajeFragilDecorator;
import uni.service.TarifaBase;

/**
 * Clase de prueba por consola (equivalente a uni.database.prueba de
 * AppEmpresa). Ejecuta cada patrón sin necesidad de la base de datos,
 * usando los DAO en memoria. Su salida es la evidencia del informe.
 */
public class PruebaPatrones {

    private static final DireccionTo ALMACEN_CALLAO =
            new DireccionTo("Av. Argentina 4521", "Callao", "Callao", "Callao");

    public static void main(String[] args) throws Exception {
        SesionUsuario.getInstancia().iniciar(
                new EmpleadoTo("E0002", "Rosa", "Quispe", RolUsuario.AGENTE_VENTANILLA));
        solidOcpLsp();
        solidDip();
        singleton();
        factory();
        abstractFactory();
        builder();
        prototype();
        adapter();
        facade();
        decorator();
        composite();
        proxy();
        bridge();
        // ---- Capítulo VI: patrones de comportamiento
        SesionUsuario.getInstancia().iniciar(
                new EmpleadoTo("E0004", "Julia", "Torres", RolUsuario.SUPERVISOR_RUTA));
        PruebaComportamiento.observer();
        PruebaComportamiento.state();
        PruebaComportamiento.command();
        PruebaComportamiento.memento();
        PruebaComportamiento.estadoLaboral();
        PruebaComportamiento.nominaSesion13();
        PruebaGrasp.antipatrones();
        PruebaGrasp.grasp();
    }

    private static void seccion(String nombre) {
        System.out.println();
        System.out.println("##SECCION " + nombre);
    }

    private static DireccionTo dir(String calle, String distrito) {
        return new DireccionTo(calle, distrito, "Lima", "Lima");
    }

    // ---------------------------------------------------------------- SOLID
    static void solidOcpLsp() {
        seccion("ocp_lsp");
        ServicioEnvioFactory fabrica = new ServicioEnvioFactory();
        EnvioTo e = new EnvioTo();
        e.setPesoKg(4.0);
        e.setDestino(dir("Jr. Huallaga 320", "Cercado de Lima"));
        e.setFechaProgramada(LocalDate.of(2026, 11, 3));
        e.setIdtiendaRecojo("S003");
        System.out.println("Mismo envío de 4 kg cotizado con cada servicio (polimorfismo):");
        for (TipoServicio t : TipoServicio.values()) {
            IServicioEnvio s = fabrica.crearServicio(t);         // cualquier subtipo sirve
            System.out.printf("  %-16s admite=%-5s tarifa=S/ %6.2f  prioridad=%d  etapas=%d%n",
                    s.getTipo(), s.admite(e), s.calcularTarifa(e), s.getPrioridad(),
                    s.getFlujoEstados().size());
        }
    }

    static void solidDip() throws Exception {
        seccion("dip");
        ICrudDao<EnvioTo> daoPrueba = new EnvioDaoMemoria();     // se inyecta la abstracción
        EnvioController ctrl = new EnvioController(daoPrueba);
        EnvioTo e = new EnvioBuilder("C0001", TipoServicio.ESTANDAR)
                .hacia(dir("Av. Brasil 1200", "Jesús María"))
                .destinatario("Luis Paredes", "987654321", null).peso(2).build();
        ctrl.envioRegistrar(e);
        System.out.println("Controlador usando " + daoPrueba.getClass().getSimpleName()
                + " sin modificar su código.");
        System.out.println("Registrado: " + ctrl.envioBuscar(e.getCodigo()));
        System.out.println("Total en repositorio: " + ctrl.envioListar().size());
    }

    // ---------------------------------------------------------- CREACIONALES
    static void singleton() {
        seccion("singleton");
        AccesoDB a = AccesoDB.getInstancia();
        AccesoDB b = AccesoDB.getInstancia();
        System.out.println("a.hashCode() = " + System.identityHashCode(a));
        System.out.println("b.hashCode() = " + System.identityHashCode(b));
        System.out.println("¿Misma instancia? " + (a == b));
        System.out.println("URL configurada: " + a.getUrl());
        System.out.println("Usuario en sesión (Singleton SesionUsuario): "
                + SesionUsuario.getInstancia().getUsuario().getNombre()
                + " - " + SesionUsuario.getInstancia().getUsuario().getRol());
    }

    static void factory() {
        seccion("factory");
        ServicioEnvioFactory fabrica = new ServicioEnvioFactory();
        for (TipoServicio t : TipoServicio.values()) {
            IServicioEnvio s = fabrica.crearServicio(t);
            System.out.printf("  crearServicio(%-15s) -> %-24s flujo=%s%n",
                    t, s.getClass().getSimpleName(), s.getFlujoEstados());
        }
    }

    static void abstractFactory() {
        seccion("abstract_factory");
        IFabricaIntegraciones[] familias = {
            new FabricaIntegracionProduccion(), new FabricaIntegracionContingencia()};
        for (IFabricaIntegraciones f : familias) {
            System.out.println("Familia: " + f.getNombreFamilia());
            DireccionTo d = f.crearGeolocalizador().geocodificar(dir("Av. La Marina 2355", "San Miguel"));
            System.out.printf("  Geolocalizador -> %s (%.5f, %.5f)%n",
                    f.crearGeolocalizador().getProveedor(), d.getLatitud(), d.getLongitud());
            INotificador n = f.crearNotificador();
            System.out.println("  Notificador    -> " + n.getCanal());
            n.enviar("987654321", "LogiRuta", "Prueba de canal");
            System.out.println("  Firma digital  -> " + f.crearFirmaDigital().getProveedor()
                    + " | código " + f.crearFirmaDigital().firmarEntrega("LR00000001", "45879632"));
        }
    }

    static void builder() {
        seccion("builder");
        EnvioTo programado = new EnvioBuilder("C0002", TipoServicio.PROGRAMADO)
                .desde(ALMACEN_CALLAO)
                .hacia(dir("Calle Las Begonias 415", "San Isidro"))
                .destinatario("Carla Romero", "912345678", "carla.romero@correo.pe")
                .peso(1.2)
                .conSeguro(850.00)
                .fragil()
                .programadoPara(LocalDate.of(2026, 11, 5))
                .build();
        System.out.println("Construido: " + programado);
        System.out.println("  seguro=" + programado.isConSeguro() + " fragil=" + programado.isFragil()
                + " fechaProgramada=" + programado.getFechaProgramada());
        EnvioTo doc = new DirectorEnvio().documentoExpress("C0001", ALMACEN_CALLAO,
                dir("Av. Javier Prado Este 4200", "Surco"), "Mesa de partes", "955112233");
        System.out.println("Director (documentoExpress): " + doc + " firma=" + doc.isRequiereFirma());
        try {
            new EnvioBuilder("C0002", TipoServicio.PROGRAMADO)
                    .hacia(dir("Av. Arequipa 100", "Lince"))
                    .destinatario("Pedro Ruiz", "999888777", null).peso(3).build();
        } catch (IllegalStateException ex) {
            System.out.println("Validación del Builder: " + ex.getMessage());
        }
    }

    static void prototype() {
        seccion("prototype");
        EnvioTo plantilla = new EnvioBuilder("C0005", TipoServicio.ESTANDAR)
                .desde(ALMACEN_CALLAO).hacia(dir("-", "-"))
                .destinatario("-", "900000000", null).peso(1.5).conSeguro(120).build();
        RegistroPlantillas registro = new RegistroPlantillas();
        registro.registrar("ECOMMERCE_STD", plantilla);

        String[][] pedidos = {
            {"Ana Torres", "Av. Universitaria 1801", "San Martín de Porres", "934111222"},
            {"Jorge Lima", "Jr. Cuzco 455", "Breña", "934333444"},
            {"Elena Vega", "Av. Benavides 3020", "Miraflores", "934555666"}};
        ICrudDao<EnvioTo> dao = new EnvioDaoMemoria();
        for (String[] p : pedidos) {
            EnvioTo copia = registro.obtener("ECOMMERCE_STD");       // clon profundo
            copia.setNombreDestinatario(p[0]);
            copia.getDestino().setCalle(p[1]);
            copia.getDestino().setDistrito(p[2]);
            copia.setTelefonoDestinatario(p[3]);
            try {
                dao.create(copia);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("  Clon -> " + copia + " para " + copia.getNombreDestinatario());
        }
        EnvioTo otra = registro.obtener("ECOMMERCE_STD");
        System.out.println("Prototipo intacto: destino=" + plantilla.getDestino().getDistrito()
                + " | ¿destino compartido con clon? " + (otra.getDestino() == plantilla.getDestino()));
    }

    // ---------------------------------------------------------- ESTRUCTURALES
    static void adapter() {
        seccion("adapter");
        List<INotificador> canales = Arrays.asList(
                new CorreoAdapter(new ExtSmtpMailer()),
                new SmsAdapter(new ExtSmsGatewayLegacy()),
                new WhatsAppAdapter(new ExtWhatsAppBusinessApi()));
        String[] destinos = {"carla.romero@correo.pe", "+51 912345678", "912345678"};
        for (int i = 0; i < canales.size(); i++) {
            INotificador n = canales.get(i);                       // el cliente solo ve INotificador
            boolean ok = n.enviar(destinos[i], "Envio LR00000007", "Su paquete está EN_RUTA");
            System.out.println("  " + n.getCanal() + " -> enviado=" + ok);
        }
        boolean fallo = canales.get(0).enviar("correo-invalido", "x", "y");
        System.out.println("  CORREO con destinatario inválido -> enviado=" + fallo);
    }

    static void facade() throws Exception {
        seccion("facade");
        RegistroEnvioFacade fachada = new RegistroEnvioFacade(new EnvioDaoMemoria(),
                new SeguimientoDaoMemoria(), new FabricaIntegracionProduccion());
        EnvioTo e = new EnvioBuilder("C0003", TipoServicio.EXPRESS)
                .desde(ALMACEN_CALLAO).hacia(dir("Av. Larco 880", "Miraflores"))
                .destinatario("Diego Salas", "976543210", null)
                .peso(2.5).conSeguro(400).conFirmaDigital().build();
        String resumen = fachada.registrarEnvio(e, "E0002");       // una sola llamada
        System.out.println("Resultado: " + resumen);
        System.out.printf("Destino geolocalizado: (%.5f, %.5f)%n",
                e.getDestino().getLatitud(), e.getDestino().getLongitud());
    }

    static void decorator() {
        seccion("decorator");
        EnvioTo e = new EnvioTo();
        e.setPesoKg(5.0);
        IServicioEnvio estandar = new ServicioEnvioFactory().crearServicio(TipoServicio.ESTANDAR);

        ITarifa t1 = new TarifaBase(estandar, e);
        ITarifa t2 = new SeguroDecorator(new TarifaBase(estandar, e), 1500.00);
        ITarifa t3 = new RastreoGpsDecorator(new FirmaDigitalDecorator(
                new EmbalajeFragilDecorator(new SeguroDecorator(new TarifaBase(estandar, e), 1500.00))));
        for (ITarifa t : Arrays.asList(t1, t2, t3)) {
            System.out.printf("  S/ %7.2f  <- %s%n", t.getMonto(), t.getDescripcion());
        }
    }

    static void composite() {
        seccion("composite");
        AgrupacionCarga sacoMiraflores = new AgrupacionCarga("SC-101", "SACO", 0.4);
        AgrupacionCarga sacoSurco = new AgrupacionCarga("SC-102", "SACO", 0.4);
        AgrupacionCarga paleta = new AgrupacionCarga("PL-020", "PALETA", 12.0);
        AgrupacionCarga manifiesto = new AgrupacionCarga("MF-2026-0458", "MANIFIESTO", 0.0);

        sacoMiraflores.agregar(paquete("LR00000011", 1.2)).agregar(paquete("LR00000012", 0.8));
        sacoSurco.agregar(paquete("LR00000013", 2.0));
        paleta.agregar(paquete("LR00000014", 35.0)).agregar(sacoSurco);
        manifiesto.agregar(sacoMiraflores).agregar(paleta).agregar(paquete("LR00000015", 6.5));

        System.out.println("Estructura de carga del vehículo:");
        manifiesto.mostrar("  ");
        manifiesto.actualizarEstado(EstadoEnvio.EN_RUTA);          // una llamada, todo el árbol
        System.out.println("Tras manifiesto.actualizarEstado(EN_RUTA):");
        manifiesto.mostrar("  ");
    }

    private static PaqueteCarga paquete(String codigo, double kg) {
        EnvioTo e = new EnvioTo();
        e.setCodigo(codigo);
        e.setPesoKg(kg);
        e.setEstado(EstadoEnvio.CLASIFICADO);
        return new PaqueteCarga(e);
    }

    static void proxy() throws Exception {
        seccion("proxy");
        EnvioDaoMemoria envioDao = new EnvioDaoMemoria();
        SeguimientoDaoMemoria histDao = new SeguimientoDaoMemoria();
        EnvioTo e = new EnvioBuilder("C0001", TipoServicio.ESTANDAR)
                .hacia(dir("Av. Grau 150", "Barranco"))
                .destinatario("Rita Flores", "945678123", null).peso(3).build();
        e.setEstado(EstadoEnvio.CLASIFICADO);       // ya clasificado en almacén (paso 3 del flujo)
        e.setPasoFlujo(3);
        envioDao.create(e);
        String cod = e.getCodigo();

        Map<String, String> asignaciones = new HashMap<>();
        asignaciones.put(cod, "E0010");                            // asignado al repartidor E0010
        ISeguimientoService servicio = new SeguimientoServiceProxy(
                new SeguimientoServiceImpl(envioDao, histDao), asignaciones);

        intentar("Destinatario", new EmpleadoTo("D0001", "Rita", "Flores", RolUsuario.DESTINATARIO),
                servicio, cod, EstadoEnvio.ENTREGADO);
        intentar("Repartidor no asignado", new EmpleadoTo("E0011", "Mario", "Huamán", RolUsuario.REPARTIDOR),
                servicio, cod, EstadoEnvio.EN_RUTA);
        intentar("Repartidor asignado", new EmpleadoTo("E0010", "Iván", "Cárdenas", RolUsuario.REPARTIDOR),
                servicio, cod, EstadoEnvio.EN_RUTA);

        System.out.println("Consulta de tracking (2 veces):");
        List<SeguimientoTo> h = servicio.consultarHistorial(cod);
        servicio.consultarHistorial(cod);
        h.forEach(s -> System.out.println("   " + s));
    }

    private static void intentar(String quien, EmpleadoTo usuario, ISeguimientoService s,
                                 String cod, EstadoEnvio estado) {
        SesionUsuario.getInstancia().iniciar(usuario);
        try {
            s.actualizarEstado(cod, estado, "Barranco", "Actualización desde app móvil");
            System.out.println("   OK: " + quien + " actualizó a " + estado);
        } catch (Exception ex) {
            System.out.println("   DENEGADO: " + ex.getMessage());
        }
    }

    static void bridge() throws Exception {
        seccion("bridge");
        java.io.File carpeta = new java.io.File("salida_reportes");
        // Datos en memoria con los mismos nombres de campo del .jrxml (sin MySQL)
        List<Map<String, ?>> inc = new java.util.ArrayList<>();
        Object[][] filas = {
            {"Lima Norte", "LR00000001", "AUSENCIA_DESTINATARIO", "Nadie en casa", "Iván Cárdenas"},
            {"Lima Norte", "LR00000002", "DIRECCION_INCORRECTA", "Número inexistente", "Iván Cárdenas"},
            {"Lima Norte", "LR00000005", "INCIDENTE_OPERATIVO", "Moto averiada", "Mario Huamán"},
            {"Lima Sur", "LR00000004", "AUSENCIA_DESTINATARIO", "Segundo intento", "Mario Huamán"},
            {"Sierra Central", "LR00000003", "CLIMA", "Huaico en carretera", "Carlos Salazar"}};
        for (Object[] f : filas) {
            Map<String, Object> m = new HashMap<>();
            m.put("zona", f[0]);
            m.put("codigo", f[1]);
            m.put("motivo", f[2]);
            m.put("descripcion", f[3]);
            m.put("registrado_por", f[4]);
            m.put("fecha", new java.sql.Timestamp(System.currentTimeMillis()));
            inc.add(m);
        }
        // Abstracción 1 + implementador "archivo PDF"
        Reporte r1 = new ReporteIncidenciasZona(
                new SalidaArchivoPdf(new java.io.File(carpeta, "incidencias_zona.pdf")));
        System.out.println("   " + r1.generar(new JRMapCollectionDataSource(inc)));
        // Mismo reporte, otro implementador (salida de prueba que solo resume el JasperPrint)
        r1.setSalida((impreso, titulo) -> "Salida de prueba: '" + titulo + "' con "
                + impreso.getPages().size() + " página(s)");
        System.out.println("   " + r1.generar(new JRMapCollectionDataSource(inc)));

        // Abstracción 2 con el mismo implementador PDF
        List<Map<String, ?>> cum = new java.util.ArrayList<>();
        Object[][] servicios = {{"EXPRESS", "Entrega en 24 horas", 1, 420L, 401L},
            {"ESTANDAR", "Entrega en 3 días", 3, 1310L, 1189L}, {"INTERPROVINCIAL", "Entrega a provincias", 5, 275L, 236L}};
        for (Object[] f : servicios) {
            Map<String, Object> m = new HashMap<>();
            m.put("servicio", f[0]);
            m.put("descripcion", f[1]);
            m.put("dias_entrega", f[2]);
            m.put("entregados", f[3]);
            m.put("a_tiempo", f[4]);
            m.put("cumplimiento", java.math.BigDecimal.valueOf(1000L * (Long) f[4] / (Long) f[3], 1));
            cum.add(m);
        }
        Reporte r2 = new ReporteCumplimientoTiempos(
                new SalidaArchivoPdf(new java.io.File(carpeta, "cumplimiento_tiempos.pdf")));
        System.out.println("   " + r2.generar(new JRMapCollectionDataSource(cum)));
    }
}
