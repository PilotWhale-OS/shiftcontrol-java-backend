package at.shiftcontrol.shiftservice.dao;

public interface BasicDao<T, K> {
    java.util.Optional<T> findById(K id);

    T save(T entity);

    void delete(T entity);
}
