package uni.service;

/** ISP: operaciones de modificación. */
public interface IEscritura<T> {

    void create(T o) throws Exception;

    void update(T o) throws Exception;

    void delete(T o) throws Exception;
}
