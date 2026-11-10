package uni.entity;

import java.time.LocalDateTime;

/** Alerta para supervisores generada por AlertaSupervisorObserver. */
public class AlertaTo {

    private long idalerta;
    private String codigo;
    private String nivel;       // INFO | ALTA | CRITICA
    private String mensaje;
    private boolean atendida;
    private LocalDateTime fecha;

    public AlertaTo() {
    }

    public AlertaTo(String codigo, String nivel, String mensaje) {
        this.codigo = codigo;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
    }

    public long getIdalerta() { return idalerta; }
    public void setIdalerta(long idalerta) { this.idalerta = idalerta; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public boolean isAtendida() { return atendida; }
    public void setAtendida(boolean atendida) { this.atendida = atendida; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return "[" + nivel + "] " + codigo + " - " + mensaje;
    }
}
