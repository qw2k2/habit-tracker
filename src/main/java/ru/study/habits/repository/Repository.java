package ru.study.habits.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.study.habits.domain.AbstractEntity;

public interface Repository<T extends AbstractEntity> {
  void save(T entity);

  Optional<T> findById(UUID id);

  List<T> findAll();

  boolean deleteById(UUID id);
}
