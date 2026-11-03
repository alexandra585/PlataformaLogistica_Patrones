package uni.dao;

import java.util.ArrayList;
import java.util.List;
import uni.entity.AlertaTo;
import uni.service.IRegistroHistorico;

/** Alertas en memoria (pruebas sin base de datos). */
public class AlertaDaoMemoria implements IRegistroHistorico<AlertaTo> {

    private final List<AlertaTo> alertas = new ArrayList<>();

    @Override
    public void registrar(AlertaTo o) {
        alertas.add(o);
    }

    @Override
    public List<AlertaTo> listarPor(String codigo) {
        List<AlertaTo> r = new ArrayList<>();
        for (AlertaTo a : alertas) {
            if (a.getCodigo().equals(codigo)) {
                r.add(a);
            }
        }
        return r;
    }

    public List<AlertaTo> todas() {
        return alertas;
    }
}
