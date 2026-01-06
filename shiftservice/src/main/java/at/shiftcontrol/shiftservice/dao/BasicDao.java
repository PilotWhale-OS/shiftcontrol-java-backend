package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import org.slf4j.LoggerFactory;

import at.shiftcontrol.lib.exception.NotFoundException;

public interface BasicDao<T, K> {
    String getName();

    java.util.Optional<T> findById(K id);

    default T getById(K id) {
        LoggerFactory.getLogger("BasicDaoLogger").error("{} not found with id:{}", getName(), id);
        return findById(id).orElseThrow(() -> new NotFoundException(getName() + " not found."));
    }

    T save(T entity);

    Collection<T> saveAll(Collection<T> entities);

    void delete(T entity);
}
