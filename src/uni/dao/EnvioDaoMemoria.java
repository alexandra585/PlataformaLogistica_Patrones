package uni.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uni.entity.EnvioTo;
import uni.service.ICrudDao;

/**
 * Implementación en memoria del mismo contrato ICrudDao<EnvioTo>.
 * Sirve para pruebas unitarias y para demostrar DIP/LSP: los controladores
 * funcionan igual con MySQL o con esta clase.
 */
public class EnvioDaoMemoria implements ICrudDao<EnvioTo> {

    private final Map<String, EnvioTo> tabla = new LinkedHashMap<>();
    private int correlativo = 1;

    @Override
    public void create(EnvioTo o) {
        o.setCodigo(String.format("LR%08d", correlativo++));
        tabla.put(o.getCodigo(), o);
    }

    @Override
    public void update(EnvioTo o) {
        tabla.put(o.getCodigo(), o);
    }

    @Override
    public void delete(EnvioTo o) {
        tabla.remove(o.getCodigo());
    }

    @Override
    public EnvioTo find(Object id) {
        return tabla.get(id.toString());
    }

    @Override
    public List<EnvioTo> readAll() {
        return new ArrayList<>(tabla.values());
    }
}
