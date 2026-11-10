package uni.service;

import java.time.LocalDate;
import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;
import uni.entity.SeguimientoTo;

/**
 * PATRÓN PROXY - Subject: contrato común del servicio real y de su proxy.
 * En el Capítulo VI se amplía con las operaciones que usan los comandos
 * (Command) y los puntos de restauración (Memento).
 */
public interface ISeguimientoService {

    List<SeguimientoTo> consultarHistorial(String codigoEnvio) throws Exception;

    /** Cambio de estado validado por el patrón State. */
    void actualizarEstado(String codigoEnvio, EstadoEnvio nuevo,
                          String ubicacion, String observacion) throws Exception;

    EnvioTo buscarEnvio(String codigoEnvio) throws Exception;

    List<AccionEnvio> accionesPermitidas(String codigoEnvio) throws Exception;

    void registrarIncidencia(String codigoEnvio, String motivo, String descripcion,
                             String ubicacion) throws Exception;

    void reprogramar(String codigoEnvio, LocalDate nuevaFecha, String observacion) throws Exception;

    /** Firma digital (Abstract Factory) + cambio a ENTREGADO. */
    void confirmarEntrega(String codigoEnvio, String dniReceptor, String ubicacion) throws Exception;

    EnvioMemento crearPuntoRestauracion(String codigoEnvio, String etiqueta) throws Exception;

    void restaurar(String codigoEnvio, EnvioMemento memento, String motivo) throws Exception;
}
