package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import at.shiftcontrol.lib.exception.NotFoundException;

public interface BasicDao<T, K> {
    String getName();

    Optional<T> findById(K id);

    default T getById(K id) {
        Optional<T> obj = findById(id);
        if (obj.isEmpty()) {
            LoggerFactory.getLogger("BasicDaoLogger").error("{} not found with id:{}", getName(), id);
        }
        return obj.orElseThrow(() -> new NotFoundException(getName() + " not found."));
    }

    T save(T entity);

    Collection<T> saveAll(Collection<T> entities);

    void delete(T entity);
}
