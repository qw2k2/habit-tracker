package ru.study.habits.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;

public interface HabitService {
  Habit create(HabitDetails details);

  Habit update(UUID id, HabitDetails details);

  void delete(UUID id);

  List<Habit> findAll();

  List<Habit> searchByName(String query);

  List<Habit> searchByCategory(String category);

  Habit setCompleted(UUID id, LocalDate date, boolean completed);

  long weeklyCompletions(UUID id, LocalDate date);
}
