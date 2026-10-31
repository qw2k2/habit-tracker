package ru.study.habits.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.repository.InMemoryHabitRepository;
import ru.study.habits.service.DefaultHabitService;
import ru.study.habits.service.HabitService;

class ConsoleApplicationTest {
  private final HabitService service =
      new DefaultHabitService(
          new InMemoryHabitRepository(),
          Clock.fixed(Instant.parse("2026-10-25T12:00:00Z"), ZoneOffset.UTC));

  @Test
  void completeUserScenario() {
    String output =
        run(
            "2\nЧтение\nУчёба\n3\nутро\n1\n5\nЧТЕН\n6\nУчёба\n"
                + "7\n1\n2026-10-19\n9\n1\n2026-10-20\n8\n1\n2026-10-19\n"
                + "3\n1\nКниги\nОтдых\n2\n\n4\n1\nнет\n4\n1\nда\n0\n");
    assertTrue(output.contains("Привычка добавлена."));
    assertTrue(output.contains("За неделю (пн–вс): 1 / 3"));
    assertTrue(output.contains("Привычка изменена."));
    assertTrue(output.contains("Удаление отменено."));
    assertTrue(output.contains("Привычка удалена."));
    assertTrue(service.findAll().isEmpty());
  }

  @Test
  void invalidMenuNumbersAndDatesDoNotCrashApplication() {
    service.create(new HabitDetails("Бег", "Спорт", 3, List.of()));
    String output = run("abc\n3\nx\n3\n99\n7\n1\n2026-02-30\n0\n");
    assertTrue(output.contains("Неизвестная команда"));
    assertTrue(output.contains("Введите целое число"));
    assertTrue(output.contains("Нет привычки с таким номером"));
    assertTrue(output.contains("Введите существующую дату"));
    assertTrue(output.contains("Работа завершена"));
    assertTrue(service.findAll().get(0).getCompletions().isEmpty());
  }

  @Test
  void endOfInputDuringCreateDoesNotSavePartialHabit() {
    String output = run("2\nЧтение\n");
    assertTrue(output.contains("Работа завершена"));
    assertTrue(service.findAll().isEmpty());
  }

  @Test
  void endOfInputDuringEditPreservesOriginal() {
    service.create(new HabitDetails("Бег", "Спорт", 3, List.of()));
    run("3\n1\nХодьба\n");
    assertEquals("Бег", service.findAll().get(0).getDetails().name());
  }

  @Test
  void emptyStorageAndInvalidTagsAreHandled() {
    String output = run("1\n4\n2\nЧтение\nУчёба\n3\nутро,\n0\n");
    assertTrue(output.contains("Сначала добавьте привычку"));
    assertTrue(output.contains("Тег: значение не должно быть пустым"));
    assertTrue(service.findAll().isEmpty());
  }

  private String run(String input) {
    StringWriter output = new StringWriter();
    try (Scanner scanner = new Scanner(input)) {
      new ConsoleApplication(service, new Console(scanner, new PrintWriter(output))).run();
    }
    return output.toString();
  }
}
