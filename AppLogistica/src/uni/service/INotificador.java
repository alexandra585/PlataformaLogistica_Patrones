package uni.service;

/**
 * PATRÓN ADAPTER - Target.
 * Interfaz que el sistema espera para enviar notificaciones, sin importar
 * el proveedor externo (correo, SMS, WhatsApp...).
 */
public interface INotificador {

    /** @return true si el proveedor aceptó el mensaje. */
    boolean enviar(String destinatario, String asunto, String mensaje);

    String getCanal();
}
