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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.repository.jdbc.Connections;
import ru.study.habits.repository.jdbc.Database;
import ru.study.habits.repository.jdbc.JdbcHabitRepository;
import ru.study.habits.repository.jdbc.StorageException;

@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class JdbcHabitRepositoryTest {
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void persistsUpdatesRollsBackAndCascades(boolean legacy) throws Exception {
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
        if (legacy) {
          try (Connection previous = connections.open();
              Statement oldDdl = previous.createStatement();
              var input =
                  getClass().getResourceAsStream("/db/migration/V1__create_habit_tables.sql")) {
            oldDdl.execute(
                new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            oldDdl.execute(
                "INSERT INTO habits(id,name,category,weekly_target) VALUES ('00000000-0000-0000-0000-000000000001','Existing','Legacy',1)");
          }
        }
        Database.initialize(connections);
        Database.initialize(connections);
        JdbcHabitRepository repository = new JdbcHabitRepository(connections);
        if (legacy) {
          assertEquals("Existing", repository.findAll().get(0).getDetails().name());
          repository.deleteById(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        }
        try (var history =
            ddl.executeQuery(
                "SELECT count(*) FROM "
                    + schema
                    + ".flyway_schema_history WHERE type='SQL' AND success")) {
          history.next();
          assertEquals(2, history.getInt(1));
        }
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
        ddl.execute("UPDATE " + schema + ".flyway_schema_history SET checksum=0 WHERE version='1'");
        assertThrows(StorageException.class, () -> Database.initialize(connections));
      } finally {
        ddl.execute("DROP SCHEMA " + schema + " CASCADE");
      }
    }
  }
}
