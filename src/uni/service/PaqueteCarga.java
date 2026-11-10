package uni.service;

import uni.entity.EnvioTo;
import uni.entity.EstadoEnvio;

/** PATRÓN COMPOSITE - Leaf: un envío físico individual. */
public class PaqueteCarga implements IUnidadCarga {

    private final EnvioTo envio;

    public PaqueteCarga(EnvioTo envio) {
        this.envio = envio;
    }

    @Override
    public String getCodigo() { return envio.getCodigo(); }

    @Override
    public double getPesoKg() { return envio.getPesoKg(); }

    @Override
    public int contarPaquetes() { return 1; }

    @Override
    public void actualizarEstado(EstadoEnvio estado) {
        envio.setEstado(estado);
    }

    @Override
    public String getEtiqueta() {
        return String.format("Paquete %s  %.1f kg  [%s]", getCodigo(), getPesoKg(), envio.getEstado());
    }

    @Override
    public void recolectarCodigos(java.util.List<String> destino) {
        destino.add(getCodigo());
    }

    public EnvioTo getEnvio() {
        return envio;
    }

    @Override
    public String toString() {
        return getEtiqueta();
    }

    @Override
    public void mostrar(String sangria) {
        System.out.printf("%s- Paquete %s  %.1f kg  [%s]%n",
                sangria, getCodigo(), getPesoKg(), envio.getEstado());
    }
}
