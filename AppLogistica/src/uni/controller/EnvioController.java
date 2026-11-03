package uni.controller;

import java.util.List;
import uni.dao.EnvioDao;
import uni.entity.EnvioTo;
import uni.service.ICrudDao;

/**
 * DIP: el controlador depende de la abstracción ICrudDao<EnvioTo>,
 * no de la clase concreta EnvioDao (en AppEmpresa: dao = new ClienteDao()).
 */
public class EnvioController {

    private final ICrudDao<EnvioTo> dao;

    /** Constructor usado por los formularios Swing: MySQL por defecto. */
    public EnvioController() {
        this(new EnvioDao());
    }

    /** Inyección por constructor: permite usar otra implementación (pruebas). */
    public EnvioController(ICrudDao<EnvioTo> dao) {
        this.dao = dao;
    }

    public void envioRegistrar(EnvioTo e) throws Exception {
        dao.create(e);
    }

    public EnvioTo envioBuscar(String codigo) throws Exception {
        return dao.find(codigo);
    }

    public List<EnvioTo> envioListar() throws Exception {
        return dao.readAll();
    }
}
