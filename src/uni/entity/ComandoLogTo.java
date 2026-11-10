package uni.entity;

import java.time.LocalDateTime;

/** Registro de bitácora de un comando ejecutado (patrón Command). */
public class ComandoLogTo {

    private long idcomando;
    private String codigo;
    private String comando;
    private String descripcion;
    private String estadoPrevio;
    private String estadoNuevo;
    private String idempleado;
    private boolean deshecho;
    private LocalDateTime fecha;

    public ComandoLogTo() {
    }

    public ComandoLogTo(String codigo, String comando, String descripcion,
                        String estadoPrevio, String estadoNuevo, String idempleado) {
        this.codigo = codigo;
        this.comando = comando;
        this.descripcion = descripcion;
        this.estadoPrevio = estadoPrevio;
        this.estadoNuevo = estadoNuevo;
        this.idempleado = idempleado;
        this.fecha = LocalDateTime.now();
    }

    public long getIdcomando() { return idcomando; }
    public void setIdcomando(long idcomando) { this.idcomando = idcomando; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getComando() { return comando; }
    public void setComando(String comando) { this.comando = comando; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstadoPrevio() { return estadoPrevio; }
    public void setEstadoPrevio(String estadoPrevio) { this.estadoPrevio = estadoPrevio; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public String getIdempleado() { return idempleado; }
    public void setIdempleado(String idempleado) { this.idempleado = idempleado; }
    public boolean isDeshecho() { return deshecho; }
    public void setDeshecho(boolean deshecho) { this.deshecho = deshecho; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
