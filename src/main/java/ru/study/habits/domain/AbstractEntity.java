package ru.study.habits.domain;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractEntity {
  private final UUID id;

  protected AbstractEntity(UUID id) {
    this.id = Objects.requireNonNull(id, "id");
  }

  public final UUID getId() {
    return id;
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other != null
        && getClass() == other.getClass()
        && id.equals(((AbstractEntity) other).id);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), id);
  }
}
