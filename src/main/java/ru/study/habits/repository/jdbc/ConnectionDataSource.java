package ru.study.habits.repository.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

final class ConnectionDataSource implements DataSource {
  private final Connections connections;

  ConnectionDataSource(Connections connections) {
    this.connections = connections;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return connections.open();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public PrintWriter getLogWriter() {
    return null;
  }

  @Override
  public void setLogWriter(PrintWriter writer) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public int getLoginTimeout() {
    return 0;
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public boolean isWrapperFor(Class<?> type) {
    return type.isInstance(this);
  }

  @Override
  public <T> T unwrap(Class<T> type) throws SQLException {
    if (isWrapperFor(type)) {
      return type.cast(this);
    }
    throw new SQLException("Unsupported wrapper type");
  }
}
