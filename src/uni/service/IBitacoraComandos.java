package uni.service;

import java.util.List;
import uni.entity.ComandoLogTo;

/** Persistencia de la bitácora de comandos (tabla comandos_log). */
public interface IBitacoraComandos {

    long registrar(ComandoLogTo c) throws Exception;

    void marcarDeshecho(long idcomando, boolean deshecho) throws Exception;

    List<ComandoLogTo> listarRecientes(int limite) throws Exception;
}
