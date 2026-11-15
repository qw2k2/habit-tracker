package ru.study.habits.repository.jdbc;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public interface HabitSql {
  @Query(
      "INSERT INTO habits(id,name,category,weekly_target) VALUES (?,?,?,?) ON CONFLICT(id) DO UPDATE SET name=excluded.name, category=excluded.category, weekly_target=excluded.weekly_target, updated_at=CURRENT_TIMESTAMP")
  int save(UUID id, String name, String category, int target) throws SQLException;

  @Query("DELETE FROM habit_tags WHERE habit_id=?")
  int clearTags(UUID id) throws SQLException;

  @Query("DELETE FROM habit_completions WHERE habit_id=?")
  int clearCompletions(UUID id) throws SQLException;

  @Query("INSERT INTO habit_tags(habit_id,position,tag) VALUES (?,?,?)")
  int addTag(UUID id, int position, String tag) throws SQLException;

  @Query("INSERT INTO habit_completions(habit_id,completed_on) VALUES (?,?)")
  int addCompletion(UUID id, LocalDate date) throws SQLException;

  @Query("DELETE FROM habits WHERE id=?")
  int delete(UUID id) throws SQLException;
}
