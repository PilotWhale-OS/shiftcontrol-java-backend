package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;

public interface BasicDao<T, K> {
    java.util.Optional<T> findById(K id);

    default T getById(K id) throws NotFoundException {
        return findById(id).orElseThrow(NotFoundException::new);
    }

    T save(T entity);

    Collection<T> saveAll(Collection<T> entities);

    void delete(T entity);
}
