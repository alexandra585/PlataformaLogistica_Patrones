package uni.util;

import java.util.HashMap;
import java.util.Map;
import uni.service.INotificador;

/** PATRÓN ADAPTER: adapta ExtWhatsAppBusinessApi a INotificador. */
public class WhatsAppAdapter implements INotificador {

    private final ExtWhatsAppBusinessApi api;

    public WhatsAppAdapter(ExtWhatsAppBusinessApi api) {
        this.api = api;
    }

    @Override
    public boolean enviar(String destinatario, String asunto, String mensaje) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", destinatario.startsWith("+51") ? destinatario : "+51" + destinatario);
        payload.put("template", "estado_envio");
        payload.put("body", asunto + " - " + mensaje);
        String respuesta = api.postMessage(payload);
        return respuesta.contains("\"sent\"");                             // JSON -> boolean
    }

    @Override
    public String getCanal() {
        return "WHATSAPP";
    }
}
