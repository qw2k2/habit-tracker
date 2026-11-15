package ru.study.habits.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.repository.InMemoryHabitRepository;

class DefaultHabitServiceTest {
  private final HabitService service =
      new DefaultHabitService(
          new InMemoryHabitRepository(),
          Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void rejectsDuplicateNamesWithinCategoryIgnoringCaseAndWhitespace() {
    service.create(details("Чтение", "Учёба"));
    assertThrows(
        IllegalArgumentException.class, () -> service.create(details(" чтение ", " УЧЁБА ")));
    service.create(details("Чтение", "Отдых"));
    assertEquals(2, service.findAll().size());
  }

  @Test
  void sortsByNameThenCategoryAndSearchesRussianText() {
    Habit last = service.create(details("Чтение", "Учёба"));
    Habit second = service.create(details("Бег", "Спорт"));
    Habit first = service.create(details("Бег", "Отдых"));
    assertEquals(List.of(first, second, last), service.findAll());
    assertEquals(List.of(last), service.searchByName(" ЧТЕН "));
    assertEquals(List.of(second), service.searchByCategory(" СПОРТ "));
    assertTrue(service.searchByCategory("Спор").isEmpty());
    assertTrue(service.searchByName("сон").isEmpty());
    assertThrows(IllegalArgumentException.class, () -> service.searchByName(" "));
    assertThrows(IllegalArgumentException.class, () -> service.searchByCategory(null));
  }

  @Test
  void updatePreservesIdentityAndHistoryAndFailedUpdateIsAtomic() {
    Habit first = service.create(details("Чтение", "Учёба"));
    service.create(details("Письмо", "Учёба"));
    LocalDate date = LocalDate.of(2026, 10, 19);
    service.setCompleted(first.getId(), date, true);
    assertThrows(
        IllegalArgumentException.class,
        () -> service.update(first.getId(), details("Письмо", "Учёба")));
    assertEquals(1, service.searchByName("Чтение").size());
    Habit updated = service.update(first.getId(), details("Книги", "Отдых"));
    assertEquals(first.getId(), updated.getId());
    assertEquals(Set.of(date), updated.getCompletions());
    service.update(first.getId(), updated.getDetails());
  }

  @Test
  void completionsAreIdempotentAndFutureDatesAreRejected() {
    Habit habit = service.create(details("Бег", "Спорт"));
    LocalDate today = LocalDate.of(2026, 10, 25);
    service.setCompleted(habit.getId(), today, true);
    service.setCompleted(habit.getId(), today, true);
    assertEquals(1, service.weeklyCompletions(habit.getId(), today));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.setCompleted(habit.getId(), today.plusDays(1), true));
    service.setCompleted(habit.getId(), today, false);
    service.setCompleted(habit.getId(), today, false);
    assertEquals(0, service.weeklyCompletions(habit.getId(), today));
  }

  @Test
  void weekIncludesMondayAndSundayButExcludesAdjacentWeeks() {
    Habit habit = service.create(details("Бег", "Спорт"));
    for (int day : List.of(18, 19, 25)) {
      service.setCompleted(habit.getId(), LocalDate.of(2026, 10, day), true);
    }
    assertEquals(2, service.weeklyCompletions(habit.getId(), LocalDate.of(2026, 10, 21)));
    assertEquals(2, service.weeklyCompletions(habit.getId(), LocalDate.of(2026, 10, 25)));
    assertEquals(0, service.weeklyCompletions(habit.getId(), LocalDate.of(2026, 10, 26)));
  }

  @Test
  void weekCanCrossYearBoundary() {
    Habit habit = service.create(details("Бег", "Спорт"));
    service.setCompleted(habit.getId(), LocalDate.of(2025, 12, 29), true);
    service.setCompleted(habit.getId(), LocalDate.of(2026, 1, 4), true);
    assertEquals(2, service.weeklyCompletions(habit.getId(), LocalDate.of(2026, 1, 1)));
  }

  @Test
  void unknownIdentityIsReportedAndDeletedHabitsDisappear() {
    UUID missing = UUID.randomUUID();
    assertThrows(IllegalArgumentException.class, () -> service.delete(missing));
    assertThrows(IllegalArgumentException.class, () -> service.update(missing, details("a", "b")));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.setCompleted(missing, LocalDate.of(2026, 1, 1), true));
    Habit habit = service.create(details("a", "b"));
    service.delete(habit.getId());
    assertTrue(service.findAll().isEmpty());
  }

  private static HabitDetails details(String name, String category) {
    return new HabitDetails(name, category, 3, List.of());
  }
}
