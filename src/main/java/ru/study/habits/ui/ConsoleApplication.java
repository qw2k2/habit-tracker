package ru.study.habits.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import ru.study.habits.service.HabitService;
import ru.study.habits.ui.command.HabitCommands;
import ru.study.habits.ui.command.MenuItem;

public final class ConsoleApplication {
  private static final String SEPARATOR = "-".repeat(56);
  private final Console console;
  private final Map<String, MenuItem> menu = new LinkedHashMap<>();
  private boolean running;

  public ConsoleApplication(HabitService service, Console console) {
    this.console = console;
    HabitCommands commands = new HabitCommands(service, console);
    menu.put("1", new MenuItem("Список привычек", commands::showAll));
    menu.put("2", new MenuItem("Добавить привычку", commands::create));
    menu.put("3", new MenuItem("Изменить привычку", commands::edit));
    menu.put("4", new MenuItem("Удалить привычку", commands::delete));
    menu.put("5", new MenuItem("Поиск по названию", commands::searchByName));
    menu.put("6", new MenuItem("Поиск по категории", commands::searchByCategory));
    menu.put("7", new MenuItem("Отметить выполнение", commands::markCompleted));
    menu.put("8", new MenuItem("Снять отметку", commands::unmarkCompleted));
    menu.put("9", new MenuItem("Статистика недели и история", commands::showWeeklyProgress));
    menu.put("0", new MenuItem("Выход", () -> running = false));
  }

  public void run() {
    running = true;
    console.println("ТРЕКЕР ПРИВЫЧЕК — часть 1. Данные хранятся до выхода.");
    while (running) {
      try {
        console.println(SEPARATOR);
        menu.forEach((key, item) -> console.println(key + ". " + item.label()));
        MenuItem selected = menu.get(console.readLine("Выберите действие: "));
        if (selected == null) {
          console.println("Неизвестная команда. Выберите пункт от 0 до 9.");
        } else {
          selected.command().execute();
        }
      } catch (EndOfInputException exception) {
        running = false;
      } catch (IllegalArgumentException exception) {
        console.println("Ошибка: " + exception.getMessage());
      }
    }
    console.println("Работа завершена. Данные из памяти удалены.");
  }
}
