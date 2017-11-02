package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category_;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.CategoryRecord;
import storage.model.tables.records.ContactCategoryRecord;

import java.util.UUID;

public class UpdateContactCategoryTransaction extends BaseTransaction<Category_> {
  private Category_ entity = null;
  private String tenantId = null;
  private UUID db_uuid = null;

  public static UpdateContactCategoryTransaction newInstance (String uuid, Category_ entity, String tenantId) {
    return new UpdateContactCategoryTransaction(uuid, entity, tenantId);
  }

  private UpdateContactCategoryTransaction(String uuid, Category_ entity, String tenantId) {
    this.db_uuid = UUID.fromString(uuid);
    this.entity = entity;
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<Category_> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        ctx.transaction(configuration -> {
          DSLContext db = DSL.using(configuration);

          ContactCategoryRecord categoryRecord = ctx.selectFrom(CONTACT_CATEGORY).where(CONTACT_CATEGORY.ID.eq(db_uuid)).fetchOne();
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
