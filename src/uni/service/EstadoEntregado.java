package uni.service;

import uni.entity.EstadoEnvio;

/** Estado final: ninguna acción operativa es válida. */
public class EstadoEntregado extends EstadoEnvioBase {

    @Override
    public EstadoEnvio getCodigo() { return EstadoEnvio.ENTREGADO; }

    @Override
    public boolean esFinal() { return true; }
}
