package uni.service;

import uni.util.ParametrosPlanilla;

/** Estrategia concreta: aporte al sistema privado de pensiones (11 %). */
public class DescuentoAFP implements DescuentoStrategy {

    @Override
    public String getCodigo() {
        return "AFP";
    }

    @Override
    public String getNombre() {
        return "AFP (sistema privado de pensiones)";
    }

    @Override
    public double getTasa() {
        return ParametrosPlanilla.TASA_AFP;
    }
}
