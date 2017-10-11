package org.folio.rest.impl.transactions;

import org.folio.rest.impl.utils.StringUtils;
import org.folio.rest.jaxrs.model.*;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.Person;
import storage.model.tables.VendorAddress;
import storage.model.tables.VendorEmail;
import storage.model.tables.VendorPhone;
import storage.model.tables.records.*;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateCategoryTransaction extends BaseTransaction<Category> {
  private Category entity = null;
  private String tenantId = null;
  private UUID db_uuid = null;

  public static UpdateCategoryTransaction newInstance (String uuid, Category entity, String tenantId) {
    return new UpdateCategoryTransaction(uuid, entity, tenantId);
  }

  private UpdateCategoryTransaction(String uuid, Category entity, String tenantId) {
    this.db_uuid = UUID.fromString(uuid);
    this.entity = entity;
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<Category> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        ctx.transaction(configuration -> {
          DSLContext db = DSL.using(configuration);

          CategoryRecord categoryRecord = ctx.selectFrom(CATEGORY).where(CATEGORY.ID.eq(db_uuid)).fetchOne();
          if (categoryRecord == null) {
            completionHandler.success(null);
            return;
          }

          categoryRecord.setValue( entity.getValue() );
          categoryRecord.update();

          completionHandler.success(entity);
        });
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }
}
