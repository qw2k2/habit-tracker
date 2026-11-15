package ru.study.habits.repository.jdbc;

public final class StorageException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public StorageException(Throwable cause) {
    super("Ошибка базы данных. Проверьте подключение и повторите операцию.", cause);
  }
}
