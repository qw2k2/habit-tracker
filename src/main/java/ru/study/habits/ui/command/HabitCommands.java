package ru.study.habits.ui.command;

import java.time.LocalDate;
import java.util.UUID;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.service.HabitService;
import ru.study.habits.ui.Console;

public final class HabitCommands {
  private final HabitService service;
  private final Console console;

  public HabitCommands(HabitService service, Console console) {
    this.service = service;
    this.console = console;
  }

  public void showAll() {
    console.printHabits(service.findAll());
  }

  public void create() {
    service.create(console.readDetails());
    console.println("Привычка добавлена.");
  }

  public void edit() {
    UUID id = console.selectHabit(service.findAll());
    console.println("Введите новые значения всех полей. История выполнения сохранится.");
    HabitDetails details = console.readDetails();
    service.update(id, details);
    console.println("Привычка изменена.");
  }

  public void delete() {
    UUID id = console.selectHabit(service.findAll());
    if (console.readLine("Удалить привычку и историю? (да/нет): ").equalsIgnoreCase("да")) {
      service.delete(id);
      console.println("Привычка удалена.");
    } else {
      console.println("Удаление отменено.");
    }
  }

  public void searchByName() {
    console.printHabits(service.searchByName(console.readLine("Часть названия: ")));
  }

  public void searchByCategory() {
    console.printHabits(service.searchByCategory(console.readLine("Категория целиком: ")));
  }

  public void markCompleted() {
    setCompleted(true);
  }

  public void unmarkCompleted() {
    setCompleted(false);
  }

  public void showWeeklyProgress() {
    UUID id = console.selectHabit(service.findAll());
    LocalDate date = console.readDate();
    long count = service.weeklyCompletions(id, date);
    Habit habit =
        service.findAll().stream()
            .filter(item -> item.getId().equals(id))
            .findFirst()
            .orElseThrow();
    int target = habit.getDetails().weeklyTarget();
    console.println(
        "За неделю (пн–вс): "
            + count
            + " / "
            + target
            + " дней. "
            + (count >= target ? "Цель достигнута!" : "Цель пока не достигнута."));
    console.println("История: " + habit.getCompletions().stream().sorted().toList());
  }

  private void setCompleted(boolean completed) {
    UUID id = console.selectHabit(service.findAll());
    service.setCompleted(id, console.readDate(), completed);
    console.println(completed ? "Выполнение отмечено." : "Отметка снята.");
  }
}
