package ru.study.habits;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Scanner;
import ru.study.habits.repository.jdbc.Connections;
import ru.study.habits.repository.jdbc.Database;
import ru.study.habits.repository.jdbc.JdbcHabitRepository;
import ru.study.habits.service.DefaultHabitService;
import ru.study.habits.service.HabitService;
import ru.study.habits.ui.Console;
import ru.study.habits.ui.ConsoleApplication;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    Connections connections = Database.fromEnvironment();
    Database.initialize(connections);
    HabitService service =
        new DefaultHabitService(new JdbcHabitRepository(connections), Clock.systemDefaultZone());
    try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
      Console console =
          new Console(input, new PrintWriter(System.out, true, StandardCharsets.UTF_8));
      new ConsoleApplication(service, console).run();
    }
  }
}
