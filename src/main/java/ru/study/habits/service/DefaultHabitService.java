package ru.study.habits.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.repository.Repository;

public final class DefaultHabitService implements HabitService {
  private static final int DAYS_PER_WEEK = 7;
  private static final Comparator<Habit> DISPLAY_ORDER =
      Comparator.comparing(
              (Habit habit) -> habit.getDetails().name(), String.CASE_INSENSITIVE_ORDER)
          .thenComparing(habit -> habit.getDetails().category(), String.CASE_INSENSITIVE_ORDER)
          .thenComparing(Habit::getId);

  private final Repository<Habit> repository;
  private final Clock clock;

  public DefaultHabitService(Repository<Habit> repository, Clock clock) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public Habit create(HabitDetails details) {
    Habit habit = new Habit(UUID.randomUUID(), details, Set.of());
    ensureUnique(habit);
    repository.save(habit);
    return habit;
  }

  @Override
  public Habit update(UUID id, HabitDetails details) {
    Habit updated = requireHabit(id).withDetails(details);
    ensureUnique(updated);
    repository.save(updated);
    return updated;
  }

  @Override
  public void delete(UUID id) {
    if (!repository.deleteById(id)) {
      throw new IllegalArgumentException("Привычка не найдена.");
    }
  }

  @Override
  public List<Habit> findAll() {
    return repository.findAll().stream().sorted(DISPLAY_ORDER).toList();
  }

  @Override
  public List<Habit> searchByName(String query) {
    String normalized = normalizeQuery(query);
    return findAll().stream()
        .filter(habit -> habit.getDetails().name().toLowerCase(Locale.ROOT).contains(normalized))
        .toList();
  }

  @Override
  public List<Habit> searchByCategory(String category) {
    String normalized = normalizeQuery(category);
    return findAll().stream()
        .filter(habit -> habit.getDetails().category().toLowerCase(Locale.ROOT).equals(normalized))
        .toList();
  }

  @Override
  public Habit setCompleted(UUID id, LocalDate date, boolean completed) {
    Objects.requireNonNull(date, "date");
    if (date.isAfter(LocalDate.now(clock))) {
      throw new IllegalArgumentException("Нельзя отмечать будущую дату.");
    }
    Habit updated = requireHabit(id).withCompletion(date, completed);
    repository.save(updated);
    return updated;
  }

  @Override
  public long weeklyCompletions(UUID id, LocalDate date) {
    LocalDate monday =
        Objects.requireNonNull(date, "date")
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate nextMonday = monday.plusDays(DAYS_PER_WEEK);
    return requireHabit(id).getCompletions().stream()
        .filter(day -> !day.isBefore(monday) && day.isBefore(nextMonday))
        .count();
  }

  private Habit requireHabit(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Привычка не найдена."));
  }

  private void ensureUnique(Habit candidate) {
    boolean duplicate =
        repository.findAll().stream()
            .anyMatch(
                existing ->
                    !existing.getId().equals(candidate.getId())
                        && existing
                            .getDetails()
                            .name()
                            .equalsIgnoreCase(candidate.getDetails().name())
                        && existing
                            .getDetails()
                            .category()
                            .equalsIgnoreCase(candidate.getDetails().category()));
    if (duplicate) {
      throw new IllegalArgumentException("Такая привычка уже есть в этой категории.");
    }
  }

  private static String normalizeQuery(String query) {
    if (query == null || query.isBlank()) {
      throw new IllegalArgumentException("Поисковый запрос не должен быть пустым.");
    }
    return query.strip().toLowerCase(Locale.ROOT);
  }
}
