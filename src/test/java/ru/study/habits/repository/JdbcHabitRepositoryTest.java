package ru.study.habits.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.repository.jdbc.Connections;
import ru.study.habits.repository.jdbc.Database;
import ru.study.habits.repository.jdbc.JdbcHabitRepository;
import ru.study.habits.repository.jdbc.StorageException;

@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class JdbcHabitRepositoryTest {
  @Test
  void persistsUpdatesRollsBackAndCascades() throws Exception {
    String url = System.getenv("TEST_DB_URL");
    String user = System.getenv("DB_USER");
    String password = System.getenv("DB_PASSWORD");
    String schema = "test_" + UUID.randomUUID().toString().replace("-", "");
    try (Connection admin = DriverManager.getConnection(url, user, password);
        Statement ddl = admin.createStatement()) {
      ddl.execute("CREATE SCHEMA " + schema);
      try {
        Connections connections =
            () -> {
              Connection connection = DriverManager.getConnection(url, user, password);
              connection.setSchema(schema);
              return connection;
            };
        Database.initialize(connections);
        Database.initialize(connections);
        JdbcHabitRepository repository = new JdbcHabitRepository(connections);
        Habit habit =
            new Habit(
                UUID.randomUUID(),
                new HabitDetails("Чтение ' SQL", "Учёба", 3, List.of("утро", "книги")),
                Set.of(LocalDate.of(2026, 1, 1)));
        repository.save(habit);
        Habit loaded = new JdbcHabitRepository(connections).findById(habit.getId()).orElseThrow();
        assertEquals(habit.getDetails(), loaded.getDetails());
        assertEquals(habit.getCompletions(), loaded.getCompletions());
        assertEquals(1, repository.findAll().size());
        Habit updated = habit.withDetails(new HabitDetails("Новое", "Учёба", 7, List.of()));
        repository.save(updated);
        assertEquals(
            updated.getDetails(), repository.findById(habit.getId()).orElseThrow().getDetails());
        ddl.execute(
            "ALTER TABLE "
                + schema
                + ".habit_completions ADD CONSTRAINT reject_date CHECK (completed_on < DATE '2026-02-01')");
        Habit invalid =
            updated
                .withDetails(new HabitDetails("Не сохранять", "Учёба", 1, List.of()))
                .withCompletion(LocalDate.of(2026, 3, 1), true);
        assertThrows(StorageException.class, () -> repository.save(invalid));
        assertEquals(
            updated.getDetails(), repository.findById(habit.getId()).orElseThrow().getDetails());
        assertEquals(
            updated.getCompletions(),
            repository.findById(habit.getId()).orElseThrow().getCompletions());
        assertThrows(
            StorageException.class,
            () -> repository.save(new Habit(UUID.randomUUID(), updated.getDetails(), Set.of())));
        assertTrue(repository.deleteById(habit.getId()));
        assertFalse(repository.deleteById(habit.getId()));
        assertTrue(repository.findAll().isEmpty());
        try (var rows = ddl.executeQuery("SELECT count(*) FROM " + schema + ".habit_completions")) {
          rows.next();
          assertEquals(0, rows.getInt(1));
        }
      } finally {
        ddl.execute("DROP SCHEMA " + schema + " CASCADE");
      }
    }
  }
}
