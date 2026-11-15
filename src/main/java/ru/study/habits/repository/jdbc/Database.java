package ru.study.habits.repository.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

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
    try (InputStream input =
            Objects.requireNonNull(Database.class.getResourceAsStream("/schema.sql"));
        Connection connection = connections.open();
        Statement statement = connection.createStatement()) {
      connection.setAutoCommit(false);
      for (String sql : new String(input.readAllBytes(), StandardCharsets.UTF_8).split(";")) {
        if (!sql.isBlank()) {
          statement.execute(sql);
        }
      }
      connection.commit();
    } catch (SQLException | IOException exception) {
      throw new StorageException(exception);
    }
  }
}
