package uni.util;

import uni.service.INotificador;

/** PATRÓN ADAPTER: adapta ExtSmsGatewayLegacy a INotificador. */
public class SmsAdapter implements INotificador {

    private final ExtSmsGatewayLegacy gateway;

    public SmsAdapter(ExtSmsGatewayLegacy gateway) {
        this.gateway = gateway;
    }

    @Override
    public boolean enviar(String destinatario, String asunto, String mensaje) {
        String numero = destinatario.replace("+51", "").replace(" ", "");   // +51 999.. -> 999..
        String texto = asunto + ": " + mensaje;
        if (texto.length() > 160) {
            texto = texto.substring(0, 157) + "...";                        // límite del gateway
        }
        int codigo = gateway.transmitir(numero, texto);
        return codigo == 0;                                                 // código -> boolean
    }

    @Override
    public String getCanal() {
        return "SMS";
    }
}
