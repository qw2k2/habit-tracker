package ru.study.habits.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Habit extends AbstractEntity {
  private final HabitDetails details;
  private final Set<LocalDate> completions;

  public Habit(UUID id, HabitDetails details, Set<LocalDate> completions) {
    super(id);
    this.details = Objects.requireNonNull(details, "details");
    this.completions = Set.copyOf(completions);
  }

  public HabitDetails getDetails() {
    return details;
  }

  public Set<LocalDate> getCompletions() {
    return completions;
  }

  public Habit withDetails(HabitDetails newDetails) {
    return new Habit(getId(), newDetails, completions);
  }

  public Habit withCompletion(LocalDate date, boolean completed) {
    Objects.requireNonNull(date, "date");
    Set<LocalDate> updated = new HashSet<>(completions);
    if (completed) {
      updated.add(date);
    } else {
      updated.remove(date);
    }
    return new Habit(getId(), details, updated);
  }
}
