package uni.dao;

import java.util.ArrayList;
import java.util.List;
import uni.entity.SeguimientoTo;
import uni.service.IRegistroHistorico;

/** Historial en memoria (pruebas). Solo agrega y consulta. */
public class SeguimientoDaoMemoria implements IRegistroHistorico<SeguimientoTo> {

    private final List<SeguimientoTo> registros = new ArrayList<>();

    @Override
    public void registrar(SeguimientoTo o) {
        registros.add(o);
    }

    @Override
    public List<SeguimientoTo> listarPor(String codigo) {
        List<SeguimientoTo> r = new ArrayList<>();
        for (SeguimientoTo s : registros) {
            if (s.getCodigoEnvio().equals(codigo)) {
                r.add(s);
            }
        }
        return r;
    }
}
