package uni.entity;

public class DireccionTo implements Cloneable {

    private String calle;
    private String distrito;
    private String provincia;
    private String departamento;
    private String referencia;
    private double latitud;
    private double longitud;

    public DireccionTo() {
    }

    public DireccionTo(String calle, String distrito, String provincia, String departamento) {
        this.calle = calle;
        this.distrito = distrito;
        this.provincia = provincia;
        this.departamento = departamento;
    }

    @Override
    public DireccionTo clone() {
        try {
            return (DireccionTo) super.clone(); // solo contiene String (inmutables) y primitivos
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    @Override
    public String toString() {
        return calle + ", " + distrito + " - " + provincia;
    }
}
