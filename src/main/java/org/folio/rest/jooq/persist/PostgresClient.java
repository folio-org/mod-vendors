package org.folio.rest.jooq.persist;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresClient {

  private static String KEY_HOST = "db.host";
  private static String KEY_PORT = "db.port";
  private static String KEY_NAME = "db.name";
  private static String KEY_USERNAME = "db.username";
  private static String KEY_PASSWORD = "db.password";

  private String DB_HOST;
  private String DB_PORT;
  private String DB_NAME;
  private String DB_USERNAME;
  private String DB_PASSWORD;

  private String DB_TENANT;

  public static PostgresClient getInstance(String tenantId)
  {
    PostgresClient client = new PostgresClient();
    Properties properties = PropertyLoader.loadPropertiesFromResource("/db.properties");
    client.DB_HOST = properties.getProperty(KEY_HOST);
    client.DB_PORT = properties.getProperty(KEY_PORT);
    client.DB_NAME = properties.getProperty(KEY_NAME);
    client.DB_USERNAME = properties.getProperty(KEY_USERNAME);
    client.DB_PASSWORD = properties.getProperty(KEY_PASSWORD);
    return client;
  }

  public void execute(ResultHandler<DSLContext, SQLException> handler) {
    String jdbc_url = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    try (Connection conn = DriverManager.getConnection(jdbc_url, DB_USERNAME, DB_PASSWORD)) {
      DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
      handler.success(db);
    }
    catch (SQLException e) {
      handler.failed(e);
    }
  }
}
