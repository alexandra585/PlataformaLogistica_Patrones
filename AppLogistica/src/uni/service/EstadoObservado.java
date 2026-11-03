package uni.service;

import java.time.LocalDate;
import java.util.List;
import uni.entity.AccionEnvio;
import uni.entity.EstadoEnvio;
import uni.util.TransicionInvalidaException;

/**
 * Observado (dirección incorrecta, ausencia, clima, incidente operativo).
 * Se puede reprogramar mientras no se superen los intentos máximos;
 * al tercer intento fallido solo queda la devolución.
 */
public class EstadoObservado extends EstadoEnvioBase {

    public static final int MAX_INTENTOS = 3;

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.OBSERVADO; }

    @Override
    public void reprogramar(EnvioContexto c, LocalDate nuevaFecha) {
        if (c.getEnvio().getIntentos() >= MAX_INTENTOS) {
            throw new TransicionInvalidaException("El envío ya tuvo " + MAX_INTENTOS
                    + " intentos fallidos: debe devolverse al remitente.");
        }
        if (nuevaFecha == null || nuevaFecha.isBefore(LocalDate.now())) {
            throw new TransicionInvalidaException("La nueva fecha de entrega debe ser hoy o posterior.");
        }
        c.getEnvio().setFechaProgramada(nuevaFecha);
        c.cambiarEstado(EstadoEnvio.REPROGRAMADO);
    }

    @Override
    public void devolver(EnvioContexto c) {
        c.cambiarEstado(EstadoEnvio.DEVUELTO);
    }

    @Override
    public List<AccionEnvio> accionesPermitidas(EnvioContexto c) {
        List<AccionEnvio> l = super.accionesPermitidas(c);
        if (c.getEnvio().getIntentos() < MAX_INTENTOS) {
            l.add(AccionEnvio.REPROGRAMAR);
        }
        l.add(AccionEnvio.DEVOLVER);
        return l;
    }
}
