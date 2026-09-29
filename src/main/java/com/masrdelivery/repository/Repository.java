package com.masrdelivery.repository;

import java.util.Collection;
import java.util.Optional;

public interface Repository<ID, T> {
    void save(T entity);
    void update(T entity);
    Optional<T> findById(ID id);
    Collection<T> findAll();
    void deleteById(ID id);
}
