package uni.service;

import uni.util.ParametrosPlanilla;

/** Estrategia concreta: aporte al sistema nacional de pensiones, ONP (6 %). */
public class DescuentoSNP implements DescuentoStrategy {

    @Override
    public String getCodigo() {
        return "SNP";
    }

    @Override
    public String getNombre() {
        return "SNP / ONP (sistema nacional de pensiones)";
    }

    @Override
    public double getTasa() {
        return ParametrosPlanilla.TASA_SNP;
    }
}
