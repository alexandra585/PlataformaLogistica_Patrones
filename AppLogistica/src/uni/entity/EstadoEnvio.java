package uni.entity;

/** Estados del ciclo de vida de un envío (caso empresarial). */
public enum EstadoEnvio {
    REGISTRADO, RECOGIDO, EN_TRANSITO, CLASIFICADO, EN_RUTA,
    ENTREGADO, OBSERVADO, REPROGRAMADO, DEVUELTO
}
