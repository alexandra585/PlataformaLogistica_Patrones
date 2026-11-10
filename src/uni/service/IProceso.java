package uni.service;

/** Contrato heredado de AppEmpresa para procesos transaccionales. */
public interface IProceso<T> {

    void procesar(T o) throws Exception;
}
