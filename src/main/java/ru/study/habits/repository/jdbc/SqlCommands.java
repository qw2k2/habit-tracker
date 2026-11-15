package ru.study.habits.repository.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;

public final class SqlCommands {
  private SqlCommands() {}

  public static <T> T create(Class<T> contract, Connection connection) {
    return contract.cast(
        Proxy.newProxyInstance(
            contract.getClassLoader(),
            new Class<?>[] {contract},
            (proxy, method, args) -> {
              if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                  case "equals" -> proxy == args[0];
                  case "hashCode" -> System.identityHashCode(proxy);
                  case "toString" -> "SQL commands: " + contract.getName();
                  default -> throw new UnsupportedOperationException(method.getName());
                };
              }
              Query query = method.getAnnotation(Query.class);
              if (query == null || method.getReturnType() != int.class) {
                throw new IllegalArgumentException("SQL command must have @Query and return int");
              }
              try (PreparedStatement statement = connection.prepareStatement(query.value())) {
                if (args != null) {
                  for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                  }
                }
                return statement.executeUpdate();
              }
            }));
  }
}
