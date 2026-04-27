package com.anomalydetection.domain.base;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Pure domain repository interface — no Spring dependencies.
 *
 * <p>The infrastructure layer provides a Spring Data JPA implementation.
 *
 * @param <T>  the aggregate root entity type
 * @param <ID> the identifier type
 */
public interface BaseRepository<T extends Entity<ID>, ID extends Serializable> {

  T save(T entity);

  Optional<T> findById(ID id);

  List<T> findAll();

  void delete(T entity);

  void deleteById(ID id);

  boolean existsById(ID id);

  long count();
}
