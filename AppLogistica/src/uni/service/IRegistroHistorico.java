package uni.service;

import java.util.List;

/**
 * ISP: contrato para historiales inmutables (seguimiento de estados,
 * bitácora de auditoría). Solo permite agregar y consultar:
 * no existe update() ni delete(), porque la trazabilidad no se altera.
 */
public interface IRegistroHistorico<T> {

    void registrar(T o) throws Exception;

    List<T> listarPor(String codigo) throws Exception;
}
