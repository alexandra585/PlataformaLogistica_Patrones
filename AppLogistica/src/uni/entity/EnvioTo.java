package uni.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transfer Object del envío. Se construye mediante uni.util.EnvioBuilder
 * y puede clonarse (Prototype) para registros masivos.
 */
public class EnvioTo implements Cloneable {

    private String codigo;
    private String idcliente;
    private TipoServicio servicio;
    private String nombreDestinatario;
    private String telefonoDestinatario;
    private String emailDestinatario;
    private DireccionTo origen;
    private DireccionTo destino;
    private double pesoKg;
    private double valorDeclarado;
    private boolean fragil;
    private boolean conSeguro;
    private boolean requiereFirma;
    private LocalDate fechaProgramada;   // solo mensajería programada
    private String idtiendaRecojo;       // solo recojo en tienda
    private EstadoEnvio estado = EstadoEnvio.REGISTRADO;
    private double costo;
    private Integer idzonaDestino;       // zona de reparto (reportes / manifiestos)
    private int pasoFlujo;               // posición en el flujo del servicio (State)
    private int intentos;                // intentos de entrega fallidos (State)
    private LocalDate fechaEstimada;
    private LocalDateTime fechaRegistro;
    private String codigoFirma;          // código devuelto por IFirmaDigital

    public EnvioTo() {
    }

    /** Copia profunda: las direcciones también se clonan. */
    @Override
    public EnvioTo clone() {
        try {
            EnvioTo copia = (EnvioTo) super.clone();
            copia.origen = (origen == null) ? null : origen.clone();
            copia.destino = (destino == null) ? null : destino.clone();
            copia.codigo = null;                 // la copia aún no está registrada
            copia.estado = EstadoEnvio.REGISTRADO;
            copia.pasoFlujo = 0;
            copia.intentos = 0;
            copia.codigoFirma = null;
            return copia;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getIdcliente() { return idcliente; }
    public void setIdcliente(String idcliente) { this.idcliente = idcliente; }
    public TipoServicio getServicio() { return servicio; }
    public void setServicio(TipoServicio servicio) { this.servicio = servicio; }
    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String n) { this.nombreDestinatario = n; }
    public String getTelefonoDestinatario() { return telefonoDestinatario; }
    public void setTelefonoDestinatario(String t) { this.telefonoDestinatario = t; }
    public String getEmailDestinatario() { return emailDestinatario; }
    public void setEmailDestinatario(String e) { this.emailDestinatario = e; }
    public DireccionTo getOrigen() { return origen; }
    public void setOrigen(DireccionTo origen) { this.origen = origen; }
    public DireccionTo getDestino() { return destino; }
    public void setDestino(DireccionTo destino) { this.destino = destino; }
    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
    public double getValorDeclarado() { return valorDeclarado; }
    public void setValorDeclarado(double v) { this.valorDeclarado = v; }
    public boolean isFragil() { return fragil; }
    public void setFragil(boolean fragil) { this.fragil = fragil; }
    public boolean isConSeguro() { return conSeguro; }
    public void setConSeguro(boolean conSeguro) { this.conSeguro = conSeguro; }
    public boolean isRequiereFirma() { return requiereFirma; }
    public void setRequiereFirma(boolean r) { this.requiereFirma = r; }
    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDate f) { this.fechaProgramada = f; }
    public String getIdtiendaRecojo() { return idtiendaRecojo; }
    public void setIdtiendaRecojo(String t) { this.idtiendaRecojo = t; }
    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public Integer getIdzonaDestino() { return idzonaDestino; }
    public void setIdzonaDestino(Integer z) { this.idzonaDestino = z; }
    public int getPasoFlujo() { return pasoFlujo; }
    public void setPasoFlujo(int pasoFlujo) { this.pasoFlujo = pasoFlujo; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
    public LocalDate getFechaEstimada() { return fechaEstimada; }
    public void setFechaEstimada(LocalDate f) { this.fechaEstimada = f; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }
    public String getCodigoFirma() { return codigoFirma; }
    public void setCodigoFirma(String c) { this.codigoFirma = c; }

    @Override
    public String toString() {
        return String.format("Envio[%s | %s | %s -> %s | %.2f kg | %s]",
                codigo, servicio, origen == null ? "-" : origen.getDistrito(),
                destino == null ? "-" : destino.getDistrito(), pesoKg, estado);
    }
}
