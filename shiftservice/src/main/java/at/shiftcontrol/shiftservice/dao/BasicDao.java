package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

public interface BasicDao<T, K> {
    java.util.Optional<T> findById(K id);

    T save(T entity);

    Collection<T> saveAll(Collection<T> entities);

    void delete(T entity);
}
