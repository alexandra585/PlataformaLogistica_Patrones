package uni.entity;

public class IncidenciaTo {

    private int idincidencia;
    private String codigoEnvio;
    private String motivo;      // DIRECCION_INCORRECTA, AUSENCIA_DESTINATARIO, CLIMA, INCIDENTE_OPERATIVO, OTRO
    private String zona;
    private String descripcion;
    private String idempleado;
    private java.time.LocalDateTime fecha;

    public IncidenciaTo() {
    }

    public IncidenciaTo(String codigoEnvio, String motivo, String zona, String descripcion) {
        this.codigoEnvio = codigoEnvio;
        this.motivo = motivo;
        this.zona = zona;
        this.descripcion = descripcion;
    }

    public int getIdincidencia() { return idincidencia; }
    public void setIdincidencia(int idincidencia) { this.idincidencia = idincidencia; }
    public String getCodigoEnvio() { return codigoEnvio; }
    public void setCodigoEnvio(String codigoEnvio) { this.codigoEnvio = codigoEnvio; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getIdempleado() { return idempleado; }
    public void setIdempleado(String idempleado) { this.idempleado = idempleado; }
    public java.time.LocalDateTime getFecha() { return fecha; }
    public void setFecha(java.time.LocalDateTime fecha) { this.fecha = fecha; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
