package uni.util;

/**
 * ADAPTEE (simula el gateway SMS heredado del operador).
 * Acepta solo números de 9 dígitos, textos de hasta 160 caracteres y
 * responde con códigos numéricos (0 = OK).
 */
public class ExtSmsGatewayLegacy {

    public int transmitir(String numero9, String texto) {
        if (numero9 == null || !numero9.matches("9\\d{8}")) {
            return 21;                       // número inválido
        }
        if (texto.length() > 160) {
            return 34;                       // mensaje demasiado largo
        }
        System.out.println("   [SMS-GW] " + numero9 + " <- \"" + texto + "\"");
        return 0;
    }
}
