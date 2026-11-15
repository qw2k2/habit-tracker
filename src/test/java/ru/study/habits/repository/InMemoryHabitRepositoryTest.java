package ru.study.habits.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;

class InMemoryHabitRepositoryTest {
  @Test
  void savesReplacesFindsAndDeletesByIdentity() {
    Repository<Habit> repository = new InMemoryHabitRepository();
    Habit habit = new Habit(UUID.randomUUID(), new HabitDetails("a", "b", 1, List.of()), Set.of());
    assertTrue(repository.findById(habit.getId()).isEmpty());
    repository.save(habit);
    List<Habit> snapshot = repository.findAll();
    Habit updated = habit.withDetails(new HabitDetails("c", "b", 2, List.of()));
    repository.save(updated);
    assertEquals(1, repository.findAll().size());
    assertEquals("c", repository.findById(habit.getId()).orElseThrow().getDetails().name());
    assertEquals("a", snapshot.get(0).getDetails().name());
    assertThrows(UnsupportedOperationException.class, snapshot::clear);
    assertTrue(repository.deleteById(habit.getId()));
    assertFalse(repository.deleteById(habit.getId()));
    assertTrue(repository.findAll().isEmpty());
    assertEquals(1, snapshot.size());
  }
}
