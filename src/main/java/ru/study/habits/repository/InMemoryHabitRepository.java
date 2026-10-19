package ru.study.habits.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import ru.study.habits.domain.Habit;

public final class InMemoryHabitRepository implements Repository<Habit> {
  private final Map<UUID, Habit> habits = new LinkedHashMap<>();

  @Override
  public void save(Habit habit) {
    Objects.requireNonNull(habit, "habit");
    habits.put(habit.getId(), habit);
  }

  @Override
  public Optional<Habit> findById(UUID id) {
    return Optional.ofNullable(habits.get(Objects.requireNonNull(id, "id")));
  }

  @Override
  public List<Habit> findAll() {
    return List.copyOf(habits.values());
  }

  @Override
  public boolean deleteById(UUID id) {
    return habits.remove(Objects.requireNonNull(id, "id")) != null;
  }
}
