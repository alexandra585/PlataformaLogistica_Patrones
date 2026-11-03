package uni.service;

/**
 * Se conserva el contrato de AppEmpresa, pero ahora es la unión de dos
 * interfaces pequeñas (ISP). Solo lo implementan los DAO que realmente
 * necesitan las cinco operaciones (clientes, empleados, envíos...).
 */
public interface ICrudDao<T> extends ILectura<T>, IEscritura<T> {
}
