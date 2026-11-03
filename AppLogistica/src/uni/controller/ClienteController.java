package uni.controller;

import java.util.List;
import uni.dao.ClienteDao;
import uni.entity.ClienteTo;
import uni.service.ICrudDao;

/** Controlador de clientes (molde ClienteController de AppEmpresa + DIP). */
public class ClienteController {

    private final ICrudDao<ClienteTo> dao;

    public ClienteController() {
        this(new ClienteDao());
    }

    public ClienteController(ICrudDao<ClienteTo> dao) {
        this.dao = dao;
    }

    public void clienteGrabar(ClienteTo c) throws Exception {
        validar(c);
        if (c.getIdcliente() == null || c.getIdcliente().isEmpty()) {
            dao.create(c);
        } else {
            dao.update(c);
        }
    }

    public void clienteEliminar(String id) throws Exception {
        ClienteTo c = new ClienteTo();
        c.setIdcliente(id);
        dao.delete(c);
    }

    public ClienteTo clienteBuscar(String id) throws Exception {
        return dao.find(id);
    }

    public List<ClienteTo> clienteListar() throws Exception {
        return dao.readAll();
    }

    private void validar(ClienteTo c) {
        if (c.getRazonsocial() == null || c.getRazonsocial().trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre o razón social.");
        }
        String doc = c.getNrodocumento() == null ? "" : c.getNrodocumento().trim();
        if ("CORPORATIVO".equals(c.getTipocliente()) && !doc.matches("\\d{11}")) {
            throw new IllegalArgumentException("Un cliente corporativo requiere RUC de 11 dígitos.");
        }
        if ("NATURAL".equals(c.getTipocliente()) && !doc.matches("\\d{8}")) {
            throw new IllegalArgumentException("Un cliente natural requiere DNI de 8 dígitos.");
        }
    }
}
