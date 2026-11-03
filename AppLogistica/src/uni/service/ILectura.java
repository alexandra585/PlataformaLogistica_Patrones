package uni.service;

import java.util.List;

/** ISP: operaciones de solo lectura. */
public interface ILectura<T> {

    T find(Object id) throws Exception;

    List<T> readAll() throws Exception;
}
