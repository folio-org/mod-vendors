package org.folio.rest.jooq.persist;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.folio.rest.persist.LoadConfs;
import org.jooq.*;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgresClient {

  /**
   * Convenience consts - can use a regular string to represent the operator
   */
  private static final String   OP_IS_NOT_NULL     = "IS NOT NULL"; //[{"field":"'ebook_url'","value":null,"op":"IS NOT NULL"}]
  private static final String   OP_IS_NULL         = "IS NULL"; //[{"field":"'ebook_url'","value":null,"op":"IS NULL"}]
  private static final String   OP_IS_TRUE         = "IS TRUE";
  private static final String   OP_IS_NOT_TRUE     = "IS NOT TRUE";
  private static final String   OP_IS_FALSE        = "IS FALSE";
  private static final String   OP_IS_NOT_FALSE    = "IS NOT FALSE";
  private static final String   OP_SIMILAR_TO      = "SIMILAR TO"; // [{"field":"'rush'","value":"ru(s|t)h","op":"SIMILAR TO"}]
  private static final String   OP_NOT_SIMILAR_TO  = "NOT SIMILAR TO";
  private static final String   OP_NOT_EQUAL       = "!=";
  private static final String   OP_EQUAL           = "="; //[{"field":"'rush'","value":"false","op":"="}]
  private static final String   OP_LIKE            = "LIKE"; //[{"field":"'po_line_status'->>'value'","value":"SENT%","op":"like"}]
  private static final String   OP_GREATER_THAN    = ">"; //non-array values only --> [{"field":"'fund_distributions'->'amount'->>'sum'","value":120,"op":">"}]
  private static final String   OP_GREATER_THAN_EQ = ">=";
  private static final String   OP_LESS_THAN       = "<";
  private static final String   OP_LESS_THAN_EQ    = "<=";
  private static final String   OP_NOT             = "NOT"; //[{"field":"'po_line_status'->>'value'","value":"fa(l|t)se","op":"SIMILAR TO"}, {"op":"NOT"}]
  private static final String   OP_OR              = "OR";
  private static final String   OP_AND             = "AND";

  private static String KEY_HOST = "host";
  private static String KEY_PORT = "port";
  private static String KEY_NAME = "database";
  private static String KEY_USERNAME = "username";
  private static String KEY_PASSWORD = "password";

  private String DB_HOST;
  private String DB_PORT;
  private String DB_NAME;
  private String DB_USERNAME;
  private String DB_PASSWORD;
  private String DB_TENANT;

  public static PostgresClient getInstance(String tenantId)
  {
    PostgresClient client = new PostgresClient();
    JsonObject props = LoadConfs.loadConfig("/postgres-conf.json");
    client.DB_HOST = props.getString(KEY_HOST);
    client.DB_PORT = Integer.toString(props.getInteger(KEY_PORT));
    client.DB_NAME = props.getString(KEY_NAME);
    client.DB_USERNAME = props.getString(KEY_USERNAME);
    client.DB_PASSWORD = props.getString(KEY_PASSWORD);
    client.DB_TENANT = tenantId;
    return client;
  }

  public void connect(ConnectResultHandler handler) {
    String jdbc_url = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

    try (Connection conn = DriverManager.getConnection(jdbc_url, DB_USERNAME, DB_PASSWORD)) {
      RenderMapping tenantMapping = new RenderMapping().withSchemata(
        new MappedSchema().withInput("TENANT").withOutput(DB_TENANT)
      );
      Settings settings = new Settings().withRenderMapping(tenantMapping);

      DSLContext db = DSL.using(conn, SQLDialect.POSTGRES, settings);
      handler.success(db);
    }
    catch (Exception e) {
      handler.failed(e);
    }
  }

  /**
   * Creates a Condition object that can be passed into a JOOQ-query where clause
   *
   * @param jsonString a JSON array string of the format [ { "field":"field-name", "value":some-value, "operator":"operator" }, ...]
   * @return Condition | null
   */
  public Condition conditionFromParams (String jsonString) {
    Condition condition = null;
    try {
      JsonArray params = new JsonArray(jsonString);
      for (int i = 0; i < params.size(); i++) {
        JsonObject param = params.getJsonObject(i);
        String fieldNameValue = param.getString("field");
        Object fieldValue = param.getValue("value");
        String operator = param.getString("operator");

        Field<Object> TABLE_FIELD = fieldFromName(fieldNameValue);

        Condition pCondition = null;
        switch (operator) {
          case OP_EQUAL:
            pCondition = TABLE_FIELD.equal(fieldValue);
            break;
          case OP_NOT_EQUAL:
            pCondition = TABLE_FIELD.notEqual(fieldValue);
            break;
          case OP_LIKE:
            pCondition = TABLE_FIELD.like((String)fieldValue);
            break;
          case OP_GREATER_THAN:
            pCondition = TABLE_FIELD.gt(fieldValue);
            break;
          case OP_GREATER_THAN_EQ:
            pCondition = TABLE_FIELD.ge(fieldValue);
            break;
          case OP_LESS_THAN:
            pCondition = TABLE_FIELD.lt(fieldValue);
            break;
          case OP_LESS_THAN_EQ:
            pCondition = TABLE_FIELD.le(fieldValue);
            break;
          case OP_IS_TRUE:
            pCondition = TABLE_FIELD.isTrue();
            break;
          case OP_IS_NOT_TRUE:
            pCondition = TABLE_FIELD.isFalse();
            break;
          case OP_IS_FALSE:
            pCondition = TABLE_FIELD.isFalse();
            break;
          case OP_IS_NOT_FALSE:
            pCondition = TABLE_FIELD.isTrue();
            break;
          case OP_IS_NULL:
            pCondition = TABLE_FIELD.isNull();
            break;
          case OP_IS_NOT_NULL:
            pCondition = TABLE_FIELD.isNotNull();
            break;
          default:
            break;
        }
        if (condition == null) {
          condition = pCondition;
        } else {
          condition = condition.and(pCondition);
        }
      }
    } catch (Exception e) {
      condition = null;
    }
    return condition;
  }

  /**
   * Returns a Field object, when possible
   *
   * @param name
   * @return Field | null
   */
  public Field<Object> fieldFromName(String name) {
    if (name == null) {
      return null;
    }
    String trimmedName = name.trim();
    if (trimmedName.isEmpty()) {
      return null;
    }

    Name fieldName = DSL.name(trimmedName);
    Field<Object> fieldObject = DSL.field(fieldName);
    return fieldObject;
  }

  /**
   * Returns a SortField object, when possible
   *
   * @param fieldname
   * @param isAscending
   * @return SortField | null
   */
  public SortField<Object> sortFieldFromParam(String fieldname, boolean isAscending) {
    if (fieldname == null) {
      return null;
    }
    String trimmedName = fieldname.trim();
    Field<Object> orderField = fieldFromName(trimmedName);
    if (isAscending) {
      return orderField.asc();
    }
    return orderField.desc();
  }
}
