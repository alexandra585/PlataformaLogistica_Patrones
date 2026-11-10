package uni.util;

import java.util.Map;

/**
 * ADAPTEE (simula el cliente de la API de mensajería WhatsApp Business).
 * Recibe un payload clave-valor con número en formato E.164 (+51...) y
 * devuelve un estado en texto.
 */
public class ExtWhatsAppBusinessApi {

    public String postMessage(Map<String, String> payload) {
        String to = payload.get("to");
        if (to == null || !to.startsWith("+51")) {
            return "{\"status\":\"rejected\"}";
        }
        System.out.println("   [WA-API] " + to + " template=" + payload.get("template")
                + " body=\"" + payload.get("body") + "\"");
        return "{\"status\":\"sent\"}";
    }
}
