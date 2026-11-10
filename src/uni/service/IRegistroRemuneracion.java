package uni.service;

/**
 * DIP: el Receiver (EmpleadoNomina) depende de esta abstracción y no del DAO.
 * EmpleadoDao la implementa contra MySQL; las pruebas por consola usan una
 * implementación en memoria.
 */
public interface IRegistroRemuneracion {

    void actualizarRemuneracion(String idempleado, double sueldo, String area, String operacion,
            String usuario) throws Exception;
}
