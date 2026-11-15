package ru.study.habits.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.study.habits.domain.Habit;
import ru.study.habits.domain.HabitDetails;
import ru.study.habits.repository.Repository;

public final class JdbcHabitRepository implements Repository<Habit> {
  private final Connections connections;

  public JdbcHabitRepository(Connections connections) {
    this.connections = connections;
  }

  @Override
  public void save(Habit habit) {
    try (Connection connection = connections.open()) {
      connection.setAutoCommit(false);
      try {
        HabitSql sql = SqlCommands.create(HabitSql.class, connection);
        HabitDetails details = habit.getDetails();
        sql.save(habit.getId(), details.name(), details.category(), details.weeklyTarget());
        sql.clearTags(habit.getId());
        sql.clearCompletions(habit.getId());
        for (int i = 0; i < details.tags().size(); i++) {
          sql.addTag(habit.getId(), i, details.tags().get(i));
        }
        for (LocalDate date : habit.getCompletions()) {
          sql.addCompletion(habit.getId(), date);
        }
        connection.commit();
      } catch (SQLException | RuntimeException exception) {
        try {
          connection.rollback();
        } catch (SQLException rollback) {
          exception.addSuppressed(rollback);
        }
        throw exception;
      }
    } catch (SQLException exception) {
      throw new StorageException(exception);
    }
  }

  @Override
  public boolean deleteById(UUID id) {
    try (Connection connection = connections.open()) {
      return SqlCommands.create(HabitSql.class, connection).delete(id) > 0;
    } catch (SQLException exception) {
      throw new StorageException(exception);
    }
  }

  @Override
  public List<Habit> findAll() {
    return read(null);
  }

  @Override
  public Optional<Habit> findById(UUID id) {
    return read(java.util.Objects.requireNonNull(id)).stream().findFirst();
  }

  private List<Habit> read(UUID id) {
    try (Connection connection = connections.open()) {
      connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
      connection.setAutoCommit(false);
      List<Habit> habits = new ArrayList<>();
      String sql =
          "SELECT id,name,category,weekly_target FROM habits" + (id == null ? "" : " WHERE id=?");
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        if (id != null) {
          statement.setObject(1, id);
        }
        try (ResultSet rows = statement.executeQuery()) {
          while (rows.next()) {
            UUID habitId = rows.getObject("id", UUID.class);
            List<String> tags = new ArrayList<>();
            HashSet<LocalDate> dates = new HashSet<>();
            try (PreparedStatement children =
                connection.prepareStatement(
                    "SELECT tag FROM habit_tags WHERE habit_id=? ORDER BY position")) {
              children.setObject(1, habitId);
              try (ResultSet values = children.executeQuery()) {
                while (values.next()) {
                  tags.add(values.getString(1));
                }
              }
            }
            try (PreparedStatement children =
                connection.prepareStatement(
                    "SELECT completed_on FROM habit_completions WHERE habit_id=?")) {
              children.setObject(1, habitId);
              try (ResultSet values = children.executeQuery()) {
                while (values.next()) {
                  dates.add(values.getObject(1, LocalDate.class));
                }
              }
            }
            habits.add(
                new Habit(
                    habitId,
                    new HabitDetails(
                        rows.getString("name"),
                        rows.getString("category"),
                        rows.getInt("weekly_target"),
                        tags),
                    dates));
          }
        }
      }
      connection.commit();
      return List.copyOf(habits);
    } catch (SQLException exception) {
      throw new StorageException(exception);
    }
  }
}
