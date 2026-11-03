package uni.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uni.entity.EstadoEnvio;

/**
 * PATRÓN COMPOSITE - Composite: saco, paleta o manifiesto de vehículo.
 * Puede contener paquetes u otras agrupaciones (estructura de árbol).
 */
public class AgrupacionCarga implements IUnidadCarga {

    private final String codigo;
    private final String tipo;      // SACO | PALETA | MANIFIESTO
    private final double taraKg;    // peso del contenedor vacío
    private final List<IUnidadCarga> hijos = new ArrayList<>();

    public AgrupacionCarga(String codigo, String tipo, double taraKg) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.taraKg = taraKg;
    }

    public AgrupacionCarga agregar(IUnidadCarga unidad) {
        hijos.add(unidad);
        return this;
    }

    public void quitar(IUnidadCarga unidad) {
        hijos.remove(unidad);
    }

    public List<IUnidadCarga> getHijos() {
        return Collections.unmodifiableList(hijos);
    }

    @Override
    public String getCodigo() { return codigo; }

    @Override
    public double getPesoKg() {
        double total = taraKg;
        for (IUnidadCarga u : hijos) {
            total += u.getPesoKg();          // recursión polimórfica
        }
        return total;
    }

    @Override
    public int contarPaquetes() {
        int total = 0;
        for (IUnidadCarga u : hijos) {
            total += u.contarPaquetes();
        }
        return total;
    }

    @Override
    public void actualizarEstado(EstadoEnvio estado) {
        for (IUnidadCarga u : hijos) {
            u.actualizarEstado(estado);      // se propaga a todo el árbol
        }
    }

    @Override
    public String getEtiqueta() {
        return String.format("%s %s  (%d paquete(s), %.1f kg)", tipo, codigo, contarPaquetes(), getPesoKg());
    }

    @Override
    public void recolectarCodigos(List<String> destino) {
        for (IUnidadCarga u : hijos) {
            u.recolectarCodigos(destino);
        }
    }

    public String getTipo() {
        return tipo;
    }

    public double getTaraKg() {
        return taraKg;
    }

    @Override
    public String toString() {
        return getEtiqueta();
    }

    @Override
    public void mostrar(String sangria) {
        System.out.printf("%s+ %s %s  (%d paquete(s), %.1f kg)%n",
                sangria, tipo, codigo, contarPaquetes(), getPesoKg());
        for (IUnidadCarga u : hijos) {
            u.mostrar(sangria + "    ");
        }
    }
}
