package uni.service;

import java.util.EnumSet;
import java.util.Set;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.EventoEnvio;
import uni.entity.TipoServicio;

/**
 * OBSERVER concreto: avisa al destinatario por el canal que entregue la
 * Abstract Factory (WhatsApp o SMS, vía Adapter). Para no saturar al
 * cliente, en servicios no express solo se notifican los hitos visibles;
 * el servicio express notifica todos los cambios (alertas inmediatas).
 */
public class NotificacionDestinatarioObserver implements IObservadorEnvio {

    private static final Set<EstadoEnvio> HITOS = EnumSet.of(EstadoEnvio.RECOGIDO,
            EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO, EstadoEnvio.OBSERVADO,
            EstadoEnvio.REPROGRAMADO, EstadoEnvio.DEVUELTO);

    private final INotificador notificador;
    private final IBitacoraNotificaciones bitacora;   // puede ser null (pruebas)

    public NotificacionDestinatarioObserver(INotificador notificador, IBitacoraNotificaciones bitacora) {
        this.notificador = notificador;
        this.bitacora = bitacora;
    }

    @Override
    public void actualizar(EventoEnvio e) {
        EnvioTo envio = e.getEnvio();
        boolean express = envio.getServicio() == TipoServicio.EXPRESS;
        if (!express && !HITOS.contains(e.getEstadoNuevo()) && !e.isReversion()) {
            return;
        }
        String destino = envio.getTelefonoDestinatario();
        if (destino == null || destino.isEmpty()) {
            return;
        }
        String asunto = "LogiRuta " + envio.getCodigo();
        String mensaje = e.isReversion()
                ? "Corregimos el estado de su envío: " + e.getEstadoNuevo()
                : textoPara(e.getEstadoNuevo(), envio);
        boolean ok = notificador.enviar(destino, asunto, mensaje);
        if (bitacora != null) {
            try {
                bitacora.registrar(envio.getCodigo(), notificador.getCanal(), destino, mensaje, ok);
            } catch (Exception ex) {
                System.out.println("[Notificación] no se pudo registrar: " + ex.getMessage());
            }
        }
    }

    private String textoPara(EstadoEnvio estado, EnvioTo envio) {
        switch (estado) {
            case RECOGIDO:     return "Recogimos su paquete.";
            case EN_RUTA:      return "Su paquete está en ruta y llegará hoy.";
            case ENTREGADO:    return "Su paquete fue entregado. ¡Gracias por confiar en LogiRuta!";
            case OBSERVADO:    return "No pudimos entregar su paquete (intento " + envio.getIntentos()
                                      + "). Lo contactaremos para coordinar.";
            case REPROGRAMADO: return "Entrega reprogramada para el " + envio.getFechaProgramada() + ".";
            case DEVUELTO:     return "Su paquete fue devuelto al remitente.";
            default:           return "Estado actual: " + estado;
        }
    }

    @Override
    public String getNombre() {
        return "Notificación al destinatario (" + notificador.getCanal() + ")";
    }
}
