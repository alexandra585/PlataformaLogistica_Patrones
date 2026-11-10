package uni.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import uni.entity.EstadoEnvio;

/**
 * PATRÓN MEMENTO - Memento.
 * Instantánea inmutable del estado interno de un envío. Los datos solo
 * pueden leerse desde el paquete uni.service (el Originator EnvioContexto);
 * el Caretaker (comandos, HistorialMementos) solo ve la etiqueta y la hora.
 */
public final class EnvioMemento {

    private final String etiqueta;
    private final String codigoEnvio;
    private final EstadoEnvio estado;
    private final int pasoFlujo;
    private final int intentos;
    private final LocalDate fechaProgramada;
    private final String codigoFirma;
    private final LocalDateTime creado = LocalDateTime.now();

    EnvioMemento(String etiqueta, String codigoEnvio, EstadoEnvio estado, int pasoFlujo,
                 int intentos, LocalDate fechaProgramada, String codigoFirma) {
        this.etiqueta = etiqueta;
        this.codigoEnvio = codigoEnvio;
        this.estado = estado;
        this.pasoFlujo = pasoFlujo;
        this.intentos = intentos;
        this.fechaProgramada = fechaProgramada;
        this.codigoFirma = codigoFirma;
    }

    // --- Interfaz "amplia": solo para el Originator (visibilidad de paquete)
    EstadoEnvio estado() { return estado; }
    int pasoFlujo() { return pasoFlujo; }
    int intentos() { return intentos; }
    LocalDate fechaProgramada() { return fechaProgramada; }
    String codigoFirma() { return codigoFirma; }

    // --- Interfaz "estrecha": pública, para el Caretaker
    public String getEtiqueta() { return etiqueta; }
    public String getCodigoEnvio() { return codigoEnvio; }
    public LocalDateTime getCreado() { return creado; }

    /** Texto para listas de la vista (no revela más que el estado guardado). */
    public String getResumen() {
        return creado.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | " + etiqueta
                + " | volver a " + estado;
    }

    @Override
    public String toString() {
        return getResumen();
    }
}
