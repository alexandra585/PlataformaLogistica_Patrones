package uni.service;

/** Producto abstracto (Abstract Factory): firma digital de conformidad. */
public interface IFirmaDigital {

    /** @return código de verificación de la firma. */
    String firmarEntrega(String codigoEnvio, String dniReceptor);

    String getProveedor();
}
