package ru.study.habits.ui;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;

public final class Console {
  private final Scanner input;
  private final PrintWriter output;

  public Console(Scanner input, PrintWriter output) {
    this.input = input;
    this.output = output;
  }

  public void println(String message) {
    output.println(message);
    output.flush();
  }

  public String readLine(String prompt) {
    output.print(prompt);
    output.flush();
    if (!input.hasNextLine()) {
      throw new EndOfInputException();
    }
    return input.nextLine().strip();
  }

  public int readInt(String prompt) {
    try {
      return Integer.parseInt(readLine(prompt));
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Введите целое число.", exception);
    }
  }

  public LocalDate readDate() {
    try {
      return LocalDate.parse(readLine("Дата (ГГГГ-ММ-ДД): "));
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException(
          "Введите существующую дату в формате ГГГГ-ММ-ДД.", exception);
    }
  }

  public HabitDetails readDetails() {
    String name = readLine("Название: ");
    String category = readLine("Категория: ");
    int target = readInt("Цель (1–7 дней в неделю): ");
    String rawTags = readLine("Теги через запятую (до 3, можно оставить пустым): ");
    List<String> tags = rawTags.isEmpty() ? List.of() : Arrays.asList(rawTags.split(",", -1));
    return new HabitDetails(name, category, target, tags);
  }

  public UUID selectHabit(List<Habit> habits) {
    printHabits(habits);
    if (habits.isEmpty()) {
      throw new IllegalArgumentException("Сначала добавьте привычку.");
    }
    int index = readInt("Номер привычки: ") - 1;
    if (index < 0 || index >= habits.size()) {
      throw new IllegalArgumentException("Нет привычки с таким номером.");
    }
    return habits.get(index).getId();
  }

  public void printHabits(List<Habit> habits) {
    if (habits.isEmpty()) {
      println("Ничего не найдено.");
    }
    for (int index = 0; index < habits.size(); index++) {
      HabitDetails details = habits.get(index).getDetails();
      println(
          (index + 1)
              + ". "
              + details.name()
              + " | "
              + details.category()
              + " | цель: "
              + details.weeklyTarget()
              + " дн./нед. | теги: "
              + String.join(", ", details.tags()));
    }
  }
}
