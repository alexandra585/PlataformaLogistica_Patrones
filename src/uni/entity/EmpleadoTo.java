package uni.entity;

public class EmpleadoTo {

    private String idempleado;
    private String nombre;
    private String apellidos;
    private String usuario;
    private String clave;
    private RolUsuario rol;
    private String idsucursal;
    private String email;
    private boolean activo = true;
    private String estadoLaboral = "ACTIVO";   // ACTIVO | VACACIONES | INACTIVO (patrón State)
    private double sueldo = 1500.0;            // Command/Memento (Sesión 13)
    private String area = "OPERACIONES";       // Command/Memento (Sesión 13)
    private String regimen = "AFP";            // AFP | SNP (Polimorfismo GRASP, Sesión 17)

    public EmpleadoTo() {
    }

    public EmpleadoTo(String idempleado, String nombre, String apellidos, RolUsuario rol) {
        this.idempleado = idempleado;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
    }

    public String getIdempleado() { return idempleado; }
    public void setIdempleado(String idempleado) { this.idempleado = idempleado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getEstadoLaboral() { return estadoLaboral; }
    public void setEstadoLaboral(String estadoLaboral) { this.estadoLaboral = estadoLaboral; }
    public String getNombreCompleto() { return nombre + " " + apellidos; }
    public String getIdsucursal() { return idsucursal; }
    public void setIdsucursal(String idsucursal) { this.idsucursal = idsucursal; }
    public double getSueldo() { return sueldo; }
    public void setSueldo(double sueldo) { this.sueldo = sueldo; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getRegimen() { return regimen; }
    public void setRegimen(String regimen) { this.regimen = regimen; }
}
