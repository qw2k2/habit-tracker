package ru.study.habits.repository.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
  private Database() {}

  public static Connections fromEnvironment() {
    String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/habits");
    String user = System.getenv().getOrDefault("DB_USER", "habits");
    String password = System.getenv("DB_PASSWORD");
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Задайте DB_PASSWORD перед запуском.");
    }
    return () -> DriverManager.getConnection(url, user, password);
  }

  public static void initialize(Connections connections) {
    try (Connection connection = connections.open()) {
      org.flywaydb.core.Flyway.configure()
          .dataSource(new ConnectionDataSource(connections))
          .defaultSchema(connection.getSchema())
          .locations("classpath:db/migration")
          .baselineOnMigrate(true)
          .baselineVersion("0")
          .cleanDisabled(true)
          .load()
          .migrate();
    } catch (SQLException | org.flywaydb.core.api.FlywayException exception) {
      throw new StorageException(exception);
    }
  }
}
