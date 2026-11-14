package uni.util;

import uni.service.DescuentoAFP;
import uni.service.DescuentoSNP;
import uni.service.DescuentoStrategy;

/**
 * Sesión 14: FACTORY METHOD del ejemplo de clase. Es el único lugar que
 * traduce el código guardado en la base de datos a la estrategia concreta;
 * el resto del sistema trabaja con DescuentoStrategy (elimina el GOLDEN
 * HAMMER de los if/else por texto).
 */
public final class DescuentoFactory {

    private DescuentoFactory() {
    }

    public static DescuentoStrategy crear(String tipo) {
        if ("AFP".equalsIgnoreCase(tipo)) {
            return new DescuentoAFP();
        }
        if ("SNP".equalsIgnoreCase(tipo) || "ONP".equalsIgnoreCase(tipo)) {
            return new DescuentoSNP();
        }
        throw new IllegalArgumentException("Régimen pensionario no reconocido: " + tipo);
    }

    public static String[] codigos() {
        return new String[]{"AFP", "SNP"};
    }
}
