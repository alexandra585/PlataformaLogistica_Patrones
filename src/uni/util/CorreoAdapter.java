package uni.util;

import java.io.IOException;
import uni.service.INotificador;

/** PATRÓN ADAPTER: adapta ExtSmtpMailer a la interfaz INotificador. */
public class CorreoAdapter implements INotificador {

    private static final String REMITENTE = "notificaciones@logiruta.pe";
    private final ExtSmtpMailer mailer;

    public CorreoAdapter(ExtSmtpMailer mailer) {
        this.mailer = mailer;
    }

    @Override
    public boolean enviar(String destinatario, String asunto, String mensaje) {
        try {
            String html = "<p>" + mensaje + "</p>";                     // texto -> HTML
            mailer.sendMail(REMITENTE, new String[]{destinatario}, asunto, html);
            return true;
        } catch (IOException ex) {                                     // excepción -> boolean
            System.out.println("   [CorreoAdapter] rechazo: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String getCanal() {
        return "CORREO";
    }
}
