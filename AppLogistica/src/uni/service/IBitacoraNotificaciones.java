package uni.service;

/** ISP: registro de las notificaciones enviadas (tabla notificaciones). */
public interface IBitacoraNotificaciones {

    void registrar(String codigo, String canal, String destinatario, String mensaje, boolean enviado)
            throws Exception;
}
