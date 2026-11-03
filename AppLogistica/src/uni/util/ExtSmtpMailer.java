package uni.util;

import java.io.IOException;

/**
 * ADAPTEE (simula una librería externa de correo que NO podemos modificar).
 * Firma incompatible: arreglo de destinatarios, cuerpo HTML y excepción
 * verificada.
 */
public class ExtSmtpMailer {

    public void sendMail(String from, String[] to, String subject, String htmlBody)
            throws IOException {
        if (to == null || to.length == 0 || !to[0].contains("@")) {
            throw new IOException("550 Invalid recipient");
        }
        System.out.println("   [SMTP] from=" + from + " to=" + to[0] + " subject=\"" + subject + "\"");
    }
}
