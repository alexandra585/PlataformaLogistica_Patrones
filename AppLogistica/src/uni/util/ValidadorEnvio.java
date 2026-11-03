package uni.util;

import java.util.ArrayList;
import java.util.List;
import uni.entity.EnvioTo;

/**
 * SRP: única responsabilidad = validar los datos de un envío.
 * Antes esta lógica estaba mezclada dentro del formulario Swing.
 */
public class ValidadorEnvio {

    public List<String> validar(EnvioTo e) {
        List<String> errores = new ArrayList<>();
        if (e.getIdcliente() == null || e.getIdcliente().trim().isEmpty()) {
            errores.add("El cliente remitente es obligatorio.");
        }
        if (e.getServicio() == null) {
            errores.add("Debe seleccionar un tipo de servicio.");
        }
        if (e.getPesoKg() <= 0) {
            errores.add("El peso debe ser mayor que cero.");
        }
        if (e.getNombreDestinatario() == null || e.getNombreDestinatario().trim().isEmpty()) {
            errores.add("El nombre del destinatario es obligatorio.");
        }
        if (e.getTelefonoDestinatario() != null && !e.getTelefonoDestinatario().matches("9\\d{8}")) {
            errores.add("El celular del destinatario debe tener 9 dígitos y empezar con 9.");
        }
        return errores;
    }
}
