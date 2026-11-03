package uni.util;

import java.time.LocalDate;
import java.util.List;
import uni.entity.DireccionTo;
import uni.entity.EnvioTo;
import uni.entity.TipoServicio;

/**
 * PATRÓN BUILDER.
 * Construye un EnvioTo paso a paso. Evita el "constructor telescópico" de
 * 17 parámetros y valida el objeto completo antes de entregarlo.
 */
public class EnvioBuilder {

    private final EnvioTo envio = new EnvioTo();

    public EnvioBuilder(String idcliente, TipoServicio servicio) {
        envio.setIdcliente(idcliente);
        envio.setServicio(servicio);
    }

    public EnvioBuilder desde(DireccionTo origen) {
        envio.setOrigen(origen);
        return this;
    }

    public EnvioBuilder hacia(DireccionTo destino) {
        envio.setDestino(destino);
        return this;
    }

    public EnvioBuilder destinatario(String nombre, String celular, String email) {
        envio.setNombreDestinatario(nombre);
        envio.setTelefonoDestinatario(celular);
        envio.setEmailDestinatario(email);
        return this;
    }

    public EnvioBuilder peso(double kg) {
        envio.setPesoKg(kg);
        return this;
    }

    public EnvioBuilder conSeguro(double valorDeclarado) {
        envio.setConSeguro(true);
        envio.setValorDeclarado(valorDeclarado);
        return this;
    }

    public EnvioBuilder fragil() {
        envio.setFragil(true);
        return this;
    }

    public EnvioBuilder conFirmaDigital() {
        envio.setRequiereFirma(true);
        return this;
    }

    public EnvioBuilder programadoPara(LocalDate fecha) {
        envio.setFechaProgramada(fecha);
        return this;
    }

    public EnvioBuilder recogerEnTienda(String idtienda) {
        envio.setIdtiendaRecojo(idtienda);
        return this;
    }

    /** Valida y entrega el producto terminado. */
    public EnvioTo build() {
        List<String> errores = new ValidadorEnvio().validar(envio);
        if (envio.getServicio() == TipoServicio.PROGRAMADO && envio.getFechaProgramada() == null) {
            errores.add("La mensajería programada requiere fecha.");
        }
        if (envio.getServicio() == TipoServicio.RECOJO_TIENDA && envio.getIdtiendaRecojo() == null) {
            errores.add("El recojo en tienda requiere indicar la agencia.");
        }
        if (envio.getServicio() != TipoServicio.RECOJO_TIENDA && envio.getDestino() == null) {
            errores.add("La dirección de destino es obligatoria.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalStateException("Envío inválido: " + errores);
        }
        return envio;
    }
}
