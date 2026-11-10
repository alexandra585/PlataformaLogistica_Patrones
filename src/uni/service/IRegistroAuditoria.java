package uni.service;

/** ISP: bitácora de accesos usada por el Proxy de seguimiento. */
public interface IRegistroAuditoria {

    void registrar(String idusuario, String rol, String accion, String codigo, boolean permitido)
            throws Exception;
}
