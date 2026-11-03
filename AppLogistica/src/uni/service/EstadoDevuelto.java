package uni.service;

import uni.entity.EstadoEnvio;

/** Estado final: el paquete regresó al remitente. */
public class EstadoDevuelto extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.DEVUELTO; }

    @Override
    public boolean esFinal() { return true; }
}
