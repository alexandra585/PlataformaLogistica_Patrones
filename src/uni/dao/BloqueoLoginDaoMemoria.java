package uni.dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uni.entity.BloqueoLoginTo;
import uni.service.IRegistroIntentosLogin;

/** Registro en memoria para probar ControlAccesoLogin sin MySQL (DIP / LSP). */
public class BloqueoLoginDaoMemoria implements IRegistroIntentosLogin {

    private final Map<String, BloqueoLoginTo> datos = new LinkedHashMap<>();
    private final List<String> bitacora = new ArrayList<>();

    @Override
    public BloqueoLoginTo buscar(String usuario) {
        BloqueoLoginTo b = datos.get(usuario);
        return b == null ? null
                : new BloqueoLoginTo(b.getUsuario(), b.getIntentosFallidos(), b.getBloqueadoHasta(), b.getUltimoFallo());
    }

    @Override
    public void guardar(BloqueoLoginTo b) {
        datos.put(b.getUsuario(), b);
    }

    @Override
    public void registrarIntento(String usuario, boolean exitoso, String detalle) {
        bitacora.add(usuario + " | " + (exitoso ? "OK" : "FALLO") + " | " + detalle);
    }

    @Override
    public List<BloqueoLoginTo> listarConFallos() {
        List<BloqueoLoginTo> l = new ArrayList<>();
        for (BloqueoLoginTo b : datos.values()) {
            if (b.getIntentosFallidos() > 0 || b.getBloqueadoHasta() != null) {
                l.add(b);
            }
        }
        return l;
    }

    @Override
    public void desbloquear(String usuario) {
        BloqueoLoginTo b = datos.get(usuario);
        if (b != null) {
            b.setIntentosFallidos(0);
            b.setBloqueadoHasta(null);
        }
        bitacora.add(usuario + " | FALLO | DESBLOQUEADO POR ADMINISTRADOR");
    }

    public List<String> getBitacora() {
        return bitacora;
    }
}
