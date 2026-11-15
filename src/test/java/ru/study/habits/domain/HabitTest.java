package ru.study.habits.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HabitTest {
  @Test
  void normalizesTextAndDefensivelyCopiesTags() {
    List<String> tags = new ArrayList<>(List.of(" УТРО "));
    HabitDetails details = new HabitDetails(" Чтение ", " Развитие ", 3, tags);
    tags.clear();
    assertEquals("Чтение", details.name());
    assertEquals("Развитие", details.category());
    assertEquals(List.of("утро"), details.tags());
    assertThrows(UnsupportedOperationException.class, () -> details.tags().add("вечер"));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 8, Integer.MAX_VALUE})
  void rejectsInvalidTargets(int target) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new HabitDetails("Чтение", "Развитие", target, List.of()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "a\nb", "a\tb"})
  void rejectsInvalidNames(String name) {
    assertThrows(
        IllegalArgumentException.class, () -> new HabitDetails(name, "Развитие", 1, List.of()));
  }

  @Test
  void validatesTextLengthAndTagLimits() {
    assertThrows(
        IllegalArgumentException.class, () -> new HabitDetails("x".repeat(81), "a", 1, List.of()));
    assertThrows(
        IllegalArgumentException.class, () -> new HabitDetails("a", "x".repeat(41), 1, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HabitDetails("a", "b", 1, List.of("x".repeat(21))));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HabitDetails("a", "b", 1, List.of("a", "b", "c", "d")));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HabitDetails("a", "b", 1, List.of("Утро", " утро ")));
    assertThrows(IllegalArgumentException.class, () -> new HabitDetails("a", "b", 1, List.of("")));
    assertEquals(
        3,
        new HabitDetails("x".repeat(80), "x".repeat(40), 7, List.of("x".repeat(20), "a", "b"))
            .tags()
            .size());
  }

  @Test
  void rejectsNullAttributes() {
    assertThrows(IllegalArgumentException.class, () -> new HabitDetails(null, "b", 1, List.of()));
    assertThrows(IllegalArgumentException.class, () -> new HabitDetails("a", null, 1, List.of()));
    assertThrows(IllegalArgumentException.class, () -> new HabitDetails("a", "b", 1, null));
  }

  @Test
  void snapshotsAreImmutableAndIdentitySurvivesEdits() {
    LocalDate date = LocalDate.of(2026, 10, 19);
    Set<LocalDate> dates = new HashSet<>(Set.of(date));
    Habit habit = new Habit(UUID.randomUUID(), new HabitDetails("a", "b", 1, List.of()), dates);
    dates.clear();
    assertEquals(Set.of(date), habit.getCompletions());
    assertThrows(UnsupportedOperationException.class, () -> habit.getCompletions().clear());
    Habit edited = habit.withDetails(new HabitDetails("c", "b", 7, List.of()));
    assertEquals(habit, edited);
    assertEquals(habit.hashCode(), edited.hashCode());
    assertEquals("a", habit.getDetails().name());
    assertNotEquals(habit, new Habit(UUID.randomUUID(), habit.getDetails(), Set.of()));
    assertNotEquals(habit, null);
    assertNotEquals(habit, "a");
    assertTrue(habit.withCompletion(date, false).getCompletions().isEmpty());
    assertEquals(Set.of(date), habit.withCompletion(date, true).getCompletions());
  }
}
