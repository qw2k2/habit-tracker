package ru.study.habits.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public record HabitDetails(String name, String category, int weeklyTarget, List<String> tags) {
  private static final int MAX_NAME_LENGTH = 80;
  private static final int MAX_CATEGORY_LENGTH = 40;
  private static final int MAX_TAG_LENGTH = 20;
  private static final int MAX_TAGS = 3;
  private static final int DAYS_PER_WEEK = 7;

  public HabitDetails {
    name = validateText(name, "Название", MAX_NAME_LENGTH);
    category = validateText(category, "Категория", MAX_CATEGORY_LENGTH);
    if (weeklyTarget < 1 || weeklyTarget > DAYS_PER_WEEK) {
      throw new IllegalArgumentException("Цель должна быть от 1 до 7 дней в неделю.");
    }
    if (tags == null || tags.size() > MAX_TAGS) {
      throw new IllegalArgumentException("Допускается от 0 до 3 тегов.");
    }
    tags =
        tags.stream()
            .map(tag -> validateText(tag, "Тег", MAX_TAG_LENGTH).toLowerCase(Locale.ROOT))
            .toList();
    if (new HashSet<>(tags).size() != tags.size()) {
      throw new IllegalArgumentException("Теги не должны повторяться.");
    }
  }

  private static String validateText(String value, String field, int maxLength) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + ": значение не должно быть пустым.");
    }
    String normalized = value.strip();
    if (normalized.length() > maxLength || normalized.chars().anyMatch(Character::isISOControl)) {
      throw new IllegalArgumentException(
          field + ": максимум " + maxLength + " символов, без управляющих символов.");
    }
    return normalized;
  }
}
