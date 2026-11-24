package uni.entity;

import java.time.LocalDateTime;

/** Estado de seguridad de un usuario: intentos fallidos seguidos y fin del bloqueo. */
public class BloqueoLoginTo {

    private String usuario;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime ultimoFallo;

    public BloqueoLoginTo() {
    }

    public BloqueoLoginTo(String usuario, int intentosFallidos, LocalDateTime bloqueadoHasta,
            LocalDateTime ultimoFallo) {
        this.usuario = usuario;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.ultimoFallo = ultimoFallo;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
    public LocalDateTime getUltimoFallo() { return ultimoFallo; }
    public void setUltimoFallo(LocalDateTime ultimoFallo) { this.ultimoFallo = ultimoFallo; }

    /** Experto en información: el propio registro sabe si sigue bloqueado en un instante dado. */
    public boolean estaBloqueado(LocalDateTime ahora) {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
    }
}
