package uni.entity;

public class ClienteTo {

    private String idcliente;
    private String razonsocial;
    private String tipocliente;   // NATURAL | CORPORATIVO
    private String nrodocumento;  // DNI o RUC
    private String email;
    private String telefono;

    public ClienteTo() {
    }

    public ClienteTo(String idcliente, String razonsocial, String tipocliente,
                     String nrodocumento, String email, String telefono) {
        this.idcliente = idcliente;
        this.razonsocial = razonsocial;
        this.tipocliente = tipocliente;
        this.nrodocumento = nrodocumento;
        this.email = email;
        this.telefono = telefono;
    }

    public String getIdcliente() { return idcliente; }
    public void setIdcliente(String idcliente) { this.idcliente = idcliente; }
    public String getRazonsocial() { return razonsocial; }
    public void setRazonsocial(String razonsocial) { this.razonsocial = razonsocial; }
    public String getTipocliente() { return tipocliente; }
    public void setTipocliente(String tipocliente) { this.tipocliente = tipocliente; }
    public String getNrodocumento() { return nrodocumento; }
    public void setNrodocumento(String nrodocumento) { this.nrodocumento = nrodocumento; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
