package uni.util;

import java.time.LocalDate;
import java.util.List;
import uni.controller.GestorComandos;
import uni.dao.AlertaDaoMemoria;
import uni.dao.EnvioDaoMemoria;
import uni.dao.SeguimientoDaoMemoria;
import uni.entity.AccionEnvio;
import uni.entity.DireccionTo;
import uni.entity.EmpleadoTo;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.EventoEnvio;
import uni.entity.RolUsuario;
import uni.entity.SeguimientoTo;
import uni.entity.TipoServicio;
import uni.service.AlertaSupervisorObserver;
import uni.service.ComandoCambiarEstado;
import uni.service.ComandoConfirmarEntrega;
import uni.service.ComandoRegistrarIncidencia;
import uni.service.EnvioContexto;
import uni.service.EnvioMemento;
import uni.service.IObservadorEnvio;
import uni.service.ISeguimientoService;
import uni.service.IndicadoresObserver;
import uni.service.MacroComando;
import uni.service.NotificacionDestinatarioObserver;
import uni.service.SeguimientoServiceImpl;
import uni.service.SeguimientoServiceProxy;

/**
 * Pruebas por consola del Capítulo VI (Observer, State, Command, Memento).
 * Igual que PruebaPatrones, trabaja con DAO en memoria: no requiere MySQL.
 * Su salida es la evidencia que se incluye en el informe.
 */
public class PruebaComportamiento {

    private static EnvioDaoMemoria envioDao;
    private static SeguimientoDaoMemoria historialDao;
    private static AlertaDaoMemoria alertaDao;
    private static IndicadoresObserver indicadores;
    private static SeguimientoServiceImpl real;

    public static void main(String[] args) throws Exception {
        SesionUsuario.getInstancia().iniciar(
                new EmpleadoTo("E0004", "Julia", "Torres", RolUsuario.SUPERVISOR_RUTA));
        observer();
        state();
        command();
        memento();
        estadoLaboral();
        nominaSesion13();
    }

    private static void seccion(String nombre) {
        System.out.println();
        System.out.println("##SECCION " + nombre);
    }

    /** Arma un entorno nuevo: servicio real + observadores, todo en memoria. */
    private static SeguimientoServiceImpl entorno() {
        envioDao = new EnvioDaoMemoria();
        historialDao = new SeguimientoDaoMemoria();
        alertaDao = new AlertaDaoMemoria();
        indicadores = new IndicadoresObserver();
        real = new SeguimientoServiceImpl(envioDao, historialDao, null, new FirmaDigitalCloud());
        real.agregarObservador(new NotificacionDestinatarioObserver(
                new WhatsAppAdapter(new ExtWhatsAppBusinessApi()), null));
        real.agregarObservador(new AlertaSupervisorObserver(alertaDao));
        real.agregarObservador(indicadores);
        return real;
    }

    private static String nuevoEnvio(TipoServicio tipo, String nombre, String celular, boolean firma)
            throws Exception {
        EnvioBuilder b = new EnvioBuilder("C0005", tipo)
                .desde(new DireccionTo("Av. Argentina 4521", "Callao", "Callao", "Callao"))
                .hacia(new DireccionTo("Av. Brasil 1200", "Jesús María", "Lima", "Lima"))
                .destinatario(nombre, celular, null).peso(2.0);
        if (firma) {
            b.conFirmaDigital();
        }
        EnvioTo e = b.build();
        envioDao.create(e);
        return e.getCodigo();
    }

    private static String estado(String codigo) throws Exception {
        EnvioTo e = envioDao.find(codigo);
        return e.getEstado() + " (paso " + e.getPasoFlujo() + ", intentos " + e.getIntentos() + ")";
    }

    // ================================================================ OBSERVER
    static void observer() throws Exception {
        seccion("observer");
        SeguimientoServiceImpl s = entorno();
        IObservadorEnvio pantalla = new IObservadorEnvio() {           // observador "de pantalla"
            @Override
            public void actualizar(EventoEnvio e) {
                System.out.println("   [Pantalla de seguimiento] " + e);
            }

            @Override
            public String getNombre() {
                return "Pantalla";
            }
        };
        s.agregarObservador(pantalla);
        System.out.println("Observadores suscritos:");
        for (IObservadorEnvio o : s.getObservadores()) {
            System.out.println("   - " + o.getNombre());
        }
        String cod = nuevoEnvio(TipoServicio.EXPRESS, "Elena Vega", "934555666", false);
        System.out.println("Un solo cambio de estado (RECOGER) en " + cod + ":");
        s.actualizarEstado(cod, EstadoEnvio.RECOGIDO, "Agencia Central Lima", "Paquete recogido");
        System.out.println("Despachar y registrar incidencia (express):");
        s.actualizarEstado(cod, EstadoEnvio.EN_RUTA, "Surco", "Salida a reparto");
        s.registrarIncidencia(cod, "AUSENCIA_DESTINATARIO", "Nadie en domicilio", "Surco");
        System.out.println("Historial registrado por HistorialSeguimientoObserver:");
        for (SeguimientoTo h : historialDao.listarPor(cod)) {
            System.out.println("   " + h);
        }
        System.out.println("Alertas generadas por AlertaSupervisorObserver: " + alertaDao.todas());
        System.out.println("Indicadores: " + indicadores.getResumen());
        s.quitarObservador(pantalla);
        System.out.println("Se desuscribe la pantalla y se reprograma (la pantalla ya no imprime):");
        s.reprogramar(cod, LocalDate.now().plusDays(1), "Cliente pidió nueva visita");
        System.out.println("Estado final: " + estado(cod));
    }

    // =================================================================== STATE
    static void state() throws Exception {
        seccion("state");
        SeguimientoServiceImpl s = entorno();
        String est = nuevoEnvio(TipoServicio.ESTANDAR, "Luis Paredes", "987111222", true);
        System.out.println("Recorrido de un envío ESTÁNDAR con firma digital (" + est + "):");
        AccionEnvio[] pasos = {AccionEnvio.RECOGER, AccionEnvio.TRASLADAR, AccionEnvio.CLASIFICAR,
            AccionEnvio.DESPACHAR};
        for (AccionEnvio a : pasos) {
            EnvioContexto ctx = s.cargarContexto(est);
            System.out.printf("   %-12s permite %-45s -> %s%n", ctx.getEstado(),
                    ctx.accionesPermitidas(), a);
            s.actualizarEstado(est, a.getEstadoDestino(), "Lima", a.getEtiqueta());
        }
        intentar("   EN_RUTA sin firma -> ENTREGAR", () -> s.actualizarEstado(est, EstadoEnvio.ENTREGADO, "Jesús María", ""));
        s.confirmarEntrega(est, "45879632", "Jesús María");
        System.out.println("   Con firma -> " + estado(est) + " firma=" + envioDao.find(est).getCodigoFirma());
        System.out.println("   Acciones en ENTREGADO: " + s.cargarContexto(est).accionesPermitidas());

        String exp = nuevoEnvio(TipoServicio.EXPRESS, "Ana Torres", "934111222", false);
        String std = nuevoEnvio(TipoServicio.ESTANDAR, "Jorge Lima", "934333444", false);
        s.actualizarEstado(exp, EstadoEnvio.RECOGIDO, "Agencia", "");
        s.actualizarEstado(std, EstadoEnvio.RECOGIDO, "Agencia", "");
        System.out.println("Mismo estado RECOGIDO, distinto servicio:");
        System.out.println("   EXPRESS  permite " + s.cargarContexto(exp).accionesPermitidas());
        System.out.println("   ESTANDAR permite " + s.cargarContexto(std).accionesPermitidas());
        intentar("   ESTANDAR RECOGIDO -> DESPACHAR", () -> s.actualizarEstado(std, EstadoEnvio.EN_RUTA, "", ""));

        System.out.println("Tres intentos fallidos de entrega (" + exp + "):");
        s.actualizarEstado(exp, EstadoEnvio.EN_RUTA, "Miraflores", "");
        for (int i = 1; i <= 3; i++) {
            s.registrarIncidencia(exp, "AUSENCIA_DESTINATARIO", "Intento " + i, "Miraflores");
            System.out.println("   Incidencia " + i + " -> " + estado(exp) + " permite "
                    + s.cargarContexto(exp).accionesPermitidas());
            if (i < 3) {
                s.reprogramar(exp, LocalDate.now().plusDays(i), "Nueva visita");
                s.actualizarEstado(exp, EstadoEnvio.EN_RUTA, "Miraflores", "Nuevo intento");
            }
        }
        intentar("   Reprogramar por 4.ª vez", () -> s.reprogramar(exp, LocalDate.now().plusDays(4), ""));
        s.actualizarEstado(exp, EstadoEnvio.DEVUELTO, "Almacén Callao", "Devuelto al remitente");
        System.out.println("   Final -> " + estado(exp));
    }

    // ================================================================= COMMAND
    static void command() throws Exception {
        seccion("command");
        SeguimientoServiceImpl s = entorno();
        GestorComandos gestor = new GestorComandos(null);
        String cod = nuevoEnvio(TipoServicio.ESTANDAR, "Rita Flores", "945678123", false);
        gestor.ejecutar(new ComandoCambiarEstado(s, cod, AccionEnvio.RECOGER, "Almacén Callao", "Recojo"));
        gestor.ejecutar(new ComandoCambiarEstado(s, cod, AccionEnvio.TRASLADAR, "Almacén Callao", "Traslado"));
        gestor.ejecutar(new ComandoCambiarEstado(s, cod, AccionEnvio.CLASIFICAR, "Almacén Callao", "Zona Lima Norte"));
        System.out.println("Ejecutados 3 comandos -> " + estado(cod));
        System.out.println("Pila de deshacer: " + gestor.getHistorial());
        System.out.println("Deshacer: " + gestor.deshacer().getDescripcion() + " -> " + estado(cod));
        System.out.println("Deshacer: " + gestor.deshacer().getDescripcion() + " -> " + estado(cod));
        System.out.println("Rehacer:  " + gestor.rehacer().getDescripcion() + " -> " + estado(cod));
        System.out.println("Historial (inmutable, incluye reversiones):");
        for (SeguimientoTo h : historialDao.listarPor(cod)) {
            System.out.println("   " + h);
        }

        System.out.println("Modo sin conexión (repartidor sin señal): se encolan 2 comandos");
        gestor.encolar(new ComandoCambiarEstado(s, cod, AccionEnvio.CLASIFICAR, "Almacén", ""));
        gestor.encolar(new ComandoCambiarEstado(s, cod, AccionEnvio.DESPACHAR, "Comas", "Salida"));
        System.out.println("   Pendientes: " + gestor.getPendientes().size() + " | estado aún " + estado(cod));
        for (String r : gestor.sincronizar()) {
            System.out.println("   " + r);
        }
        System.out.println("   Tras sincronizar -> " + estado(cod));

        System.out.println("Macro-comando: despachar manifiesto de 3 envíos (uno inválido):");
        String a = clasificado(s, "Pedro Ruiz");
        String b = clasificado(s, "Sofía Cáceres");
        String c = nuevoEnvio(TipoServicio.ESTANDAR, "Marco Ibáñez", "911223344", false); // REGISTRADO
        MacroComando macro = new MacroComando("Despachar MF-2026-0460", "MF-2026-0460");
        for (String x : new String[]{a, b, c}) {
            macro.agregar(new ComandoCambiarEstado(s, x, AccionEnvio.DESPACHAR, "Lima Norte", "Manifiesto"));
        }
        intentar("   ejecutar macro", () -> gestor.ejecutar(macro));
        System.out.println("   " + a + " -> " + estado(a) + " | " + b + " -> " + estado(b)
                + " | " + c + " -> " + estado(c));
        MacroComando macro2 = new MacroComando("Despachar MF-2026-0461", "MF-2026-0461");
        macro2.agregar(new ComandoCambiarEstado(s, a, AccionEnvio.DESPACHAR, "Lima Norte", "Manifiesto"))
              .agregar(new ComandoCambiarEstado(s, b, AccionEnvio.DESPACHAR, "Lima Norte", "Manifiesto"));
        gestor.ejecutar(macro2);
        System.out.println("   Macro válida -> " + estado(a) + " | " + estado(b));
        gestor.deshacer();
        System.out.println("   Deshacer macro -> " + estado(a) + " | " + estado(b));
    }

    private static String clasificado(SeguimientoServiceImpl s, String nombre) throws Exception {
        String cod = nuevoEnvio(TipoServicio.ESTANDAR, nombre, "999888777", false);
        s.actualizarEstado(cod, EstadoEnvio.RECOGIDO, "Almacén", "");
        s.actualizarEstado(cod, EstadoEnvio.EN_TRANSITO, "Almacén", "");
        s.actualizarEstado(cod, EstadoEnvio.CLASIFICADO, "Almacén", "");
        return cod;
    }

    // ================================================================= MEMENTO
    static void memento() throws Exception {
        seccion("memento");
        SeguimientoServiceImpl s = entorno();
        HistorialMementos caretaker = new HistorialMementos();
        String cod = clasificado(s, "Carla Romero");
        s.actualizarEstado(cod, EstadoEnvio.EN_RUTA, "San Isidro", "Salida a reparto");
        s.registrarIncidencia(cod, "DIRECCION_INCORRECTA", "Numeración no existe", "San Isidro");
        System.out.println("Estado actual: " + estado(cod));

        EnvioMemento antes = s.crearPuntoRestauracion(cod, "Antes de reprogramar");
        caretaker.guardar(antes);
        System.out.println("Caretaker guarda: " + antes.getResumen());
        s.reprogramar(cod, LocalDate.now().plusDays(2), "Cliente corrigió la dirección");
        System.out.println("Tras reprogramar: " + estado(cod) + " fechaProgramada="
                + envioDao.find(cod).getFechaProgramada());
        System.out.println("El cliente cancela la reprogramación -> restaurar último memento");
        s.restaurar(cod, caretaker.ultimo(cod), "Cliente canceló la reprogramación");
        System.out.println("Restaurado: " + estado(cod) + " fechaProgramada="
                + envioDao.find(cod).getFechaProgramada());

        System.out.println("Deshacer una entrega con firma a través del Proxy:");
        String ent = clasificado(s, "Diego Salas");
        s.actualizarEstado(ent, EstadoEnvio.EN_RUTA, "Barranco", "");
        java.util.Map<String, String> asign = new java.util.HashMap<>();
        asign.put(ent, "E0010");
        ISeguimientoService proxy = new SeguimientoServiceProxy(s, asign);
        GestorComandos gestor = new GestorComandos(null);
        SesionUsuario.getInstancia().iniciar(new EmpleadoTo("E0010", "Iván", "Cárdenas", RolUsuario.REPARTIDOR));
        gestor.ejecutar(new ComandoConfirmarEntrega(proxy, ent, null, "Barranco"));
        System.out.println("   Repartidor entrega -> " + estado(ent));
        intentar("   Repartidor intenta deshacer", gestor::deshacer);
        SesionUsuario.getInstancia().iniciar(new EmpleadoTo("E0004", "Julia", "Torres", RolUsuario.SUPERVISOR_RUTA));
        gestor.deshacer();
        System.out.println("   Supervisor deshace -> " + estado(ent));
        List<SeguimientoTo> h = historialDao.listarPor(ent);
        System.out.println("   Último registro del historial: " + h.get(h.size() - 1));
    }


    // ======================================== STATE + OBSERVER (Sesión 12: Empleado)
    static void estadoLaboral() throws Exception {
        seccion("estado_laboral");
        java.util.Map<String, EmpleadoTo> tabla = new java.util.HashMap<>();
        EmpleadoTo ivan = new EmpleadoTo("E0010", "Iván", "Cárdenas", RolUsuario.REPARTIDOR);
        tabla.put(ivan.getIdempleado(), ivan);
        uni.service.ILectura<EmpleadoTo> dao = new uni.service.ILectura<EmpleadoTo>() {
            @Override
            public EmpleadoTo find(Object id) { return tabla.get(id.toString()); }

            @Override
            public List<EmpleadoTo> readAll() { return new java.util.ArrayList<>(tabla.values()); }
        };
        uni.service.RecursosHumanosObserver.IRegistroLaboral rrhh = new uni.service.RecursosHumanosObserver.IRegistroLaboral() {
            @Override
            public void guardarEstado(String id, String estado) { }

            @Override
            public void registrarHistorial(String id, String a, String n, String m, String u) { }
        };
        uni.service.IConsultaAsignacion asignaciones = new uni.service.IConsultaAsignacion() {
            @Override
            public String repartidorDe(String codigo) { return "E0010"; }

            @Override
            public List<String> enviosPendientesDe(String id) {
                return java.util.Arrays.asList("LR00000004", "LR00000005");
            }
        };
        AlertaDaoMemoria alertas = new AlertaDaoMemoria();
        uni.service.GestionLaboralService gestion = new uni.service.GestionLaboralService(dao);
        gestion.agregarObservador(new uni.service.RecursosHumanosObserver(rrhh));
        gestion.agregarObservador(new uni.service.OperacionesObserver(asignaciones, alertas));

        uni.service.EmpleadoContexto ctx = gestion.cargar("E0010");
        System.out.println("mostrarEstado(): " + ctx.mostrarEstado());
        System.out.println("Acciones permitidas: " + ctx.getEstado().accionesPermitidas());
        System.out.println("Enviar de vacaciones:");
        System.out.println("   " + gestion.cambiarEstado("E0010", "VACACIONES", "Vacaciones programadas"));
        System.out.println("   ¿Puede iniciar sesión? " + uni.service.EstadoLaboralBase.de(ivan.getEstadoLaboral()).puedeIniciarSesion()
                + " | ¿Puede recibir envíos? " + uni.service.EstadoLaboralBase.de(ivan.getEstadoLaboral()).puedeRecibirEnvios());
        System.out.println("   Alertas de Operaciones: " + alertas.todas());
        intentar("Cesar estando de vacaciones", () -> gestion.cambiarEstado("E0010", "CESAR", ""));
        System.out.println("Reincorporar:");
        System.out.println("   " + gestion.cambiarEstado("E0010", "REINCORPORAR", "Fin de vacaciones"));
    }

    /**
     * Sesión 13: el mismo ejercicio de clase (Empleado "Roy Saavedra", sueldo 3000)
     * resuelto dentro de la arquitectura de AppLogistica. Persistencia en memoria.
     */
    static void nominaSesion13() throws Exception {
        seccion("sesion13_command_memento");
        java.util.List<String> bd = new java.util.ArrayList<>();
        uni.service.IRegistroRemuneracion registro = (id, sueldo, area, op, usu) ->
                bd.add(String.format("%s %s -> S/ %.2f, %s", id, op, sueldo, area));
        EmpleadoTo e = new EmpleadoTo("E0099", "Roy", "Saavedra", RolUsuario.OPERADOR_ALMACEN);
        e.setSueldo(3000);
        e.setArea("ALMACEN");
        uni.service.EmpleadoNomina emp = new uni.service.EmpleadoNomina(e, registro, "E0001"); // Receiver/Originator
        uni.controller.MenuComandos menu = new uni.controller.MenuComandos();                  // Invoker
        uni.util.HistorialEmpleado historial = new uni.util.HistorialEmpleado();               // Caretaker

        System.out.println("-- Command (ejemplo de clase) --");
        menu.setComando(new uni.service.MostrarDatosCommand(emp));
        System.out.println("   " + menu.ejecutarComando());
        menu.setComando(new uni.service.CalcularBonoCommand(emp));
        System.out.println("   " + menu.ejecutarComando());
        menu.setComando(new uni.service.CalcularSueldoCommand(emp));
        System.out.println("   " + menu.ejecutarComando());

        System.out.println("-- Ejercicio resuelto: AumentarSueldoCommand --");
        menu.setComando(new uni.service.AumentarSueldoCommand(emp, 10, historial));
        System.out.println("   " + menu.ejecutarComando());
        menu.setComando(new uni.service.CalcularSueldoCommand(emp));
        System.out.println("   " + menu.ejecutarComando());
        intentar("   Aumento de -5 %", () -> {
            menu.setComando(new uni.service.AumentarSueldoCommand(emp, -5, historial));
            menu.ejecutarComando();
        });

        System.out.println("-- Memento (guardar y restaurar) --");
        historial.guardar(emp.guardar("Estado estable"));
        menu.setComando(new uni.service.CambiarAreaCommand(emp, "Supervision", historial));
        System.out.println("   " + menu.ejecutarComando());
        menu.setComando(new uni.service.AumentarSueldoCommand(emp, 20, historial));
        System.out.println("   " + menu.ejecutarComando());
        System.out.println("   Actual: " + emp.mostrarDatos());
        System.out.println("   Puntos guardados por el Caretaker: " + historial.listar().size());
        emp.restaurar(historial.deshacer());
        System.out.println("   Deshacer 1 -> " + emp.mostrarDatos());
        emp.restaurar(historial.deshacer());
        System.out.println("   Deshacer 2 -> " + emp.mostrarDatos());
        emp.restaurar(historial.deshacer());
        System.out.println("   Deshacer 3 -> " + emp.mostrarDatos());
        emp.restaurar(historial.deshacer());
        System.out.println("   Deshacer 4 -> " + emp.mostrarDatos());
        System.out.println("   Sin más estados: " + (historial.deshacer() == null));

        System.out.println("-- Bitácora del Invoker --");
        for (String linea : menu.getBitacora()) {
            System.out.println("   " + linea);
        }
        System.out.println("-- Registros enviados a la BD (historial_remuneracion) --");
        for (String linea : bd) {
            System.out.println("   " + linea);
        }
    }

    // ------------------------------------------------------------- utilidades
    interface Accion {
        void run() throws Exception;
    }

    private static void intentar(String texto, Accion a) {
        try {
            a.run();
            System.out.println(texto + ": OK");
        } catch (Exception ex) {
            System.out.println(texto + ": RECHAZADO -> " + ex.getMessage());
        }
    }

    // Evita advertencia de import no usado en algunos IDE
    @SuppressWarnings("unused")
    private static final Class<?> REF = ComandoRegistrarIncidencia.class;
}
