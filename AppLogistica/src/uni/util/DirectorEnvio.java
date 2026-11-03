package uni.util;

import uni.entity.DireccionTo;
import uni.entity.EnvioTo;
import uni.entity.TipoServicio;

/**
 * PATRÓN BUILDER - Director.
 * Encapsula "recetas" de construcción frecuentes en ventanilla.
 */
public class DirectorEnvio {

    /** Sobre de documentos express con firma de cargo. */
    public EnvioTo documentoExpress(String idcliente, DireccionTo origen, DireccionTo destino,
                                    String receptor, String celular) {
        return new EnvioBuilder(idcliente, TipoServicio.EXPRESS)
                .desde(origen).hacia(destino)
                .destinatario(receptor, celular, null)
                .peso(0.5)
                .conFirmaDigital()
                .build();
    }

    /** Paquete de comercio electrónico estándar asegurado. */
    public EnvioTo paqueteEcommerce(String idcliente, DireccionTo origen, DireccionTo destino,
                                    String receptor, String celular, double kg, double valor) {
        return new EnvioBuilder(idcliente, TipoServicio.ESTANDAR)
                .desde(origen).hacia(destino)
                .destinatario(receptor, celular, null)
                .peso(kg)
                .conSeguro(valor)
                .build();
    }
}
