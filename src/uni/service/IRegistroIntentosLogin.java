package uni.service;

import java.util.List;
import uni.entity.BloqueoLoginTo;

/**
 * DIP: ControlAccesoLogin depende de esta abstracción y no de MySQL.
 * Implementaciones: uni.dao.BloqueoLoginDao (MySQL) y BloqueoLoginDaoMemoria (pruebas).
 */
public interface IRegistroIntentosLogin {

    /** Devuelve el estado del usuario o null si nunca falló. */
    BloqueoLoginTo buscar(String usuario) throws Exception;

    void guardar(BloqueoLoginTo estado) throws Exception;

    /** Bitácora de intentos (exitosos, fallidos y bloqueos). */
    void registrarIntento(String usuario, boolean exitoso, String detalle) throws Exception;

    /** Usuarios con intentos fallidos o bloqueo vigente. */
    List<BloqueoLoginTo> listarConFallos() throws Exception;

    void desbloquear(String usuario) throws Exception;
}
