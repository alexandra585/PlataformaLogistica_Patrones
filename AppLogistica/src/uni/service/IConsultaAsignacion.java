package uni.service;

/** ISP: el Proxy solo necesita saber qué repartidor tiene asignado un envío. */
public interface IConsultaAsignacion {

    /** @return idempleado del repartidor asignado o null si no tiene. */
    String repartidorDe(String codigoEnvio) throws Exception;

    /** Envíos asignados al repartidor que aún no se entregan ni devuelven. */
    default java.util.List<String> enviosPendientesDe(String idrepartidor) throws Exception {
        return java.util.Collections.emptyList();
    }
}
