package uni.service;

import uni.entity.BoletaPagoTo;

/** DIP: GeneradorBoleta guarda la boleta sin conocer el DAO concreto. */
public interface IRegistroBoleta {

    /** @return id de la boleta registrada (o re-emitida para el mismo periodo). */
    int registrar(BoletaPagoTo b, String usuario) throws Exception;

    void cambiarRegimen(String idempleado, String regimen) throws Exception;
}
