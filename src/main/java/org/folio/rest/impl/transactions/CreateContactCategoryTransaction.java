package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category_;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.ContactCategoryRecord;

public class CreateContactCategoryTransaction extends BaseTransaction<Category_> {
  private Category_ entity = null;
  private String tenantId = null;

  public static CreateContactCategoryTransaction newInstance(Category_ entity, String tenantId) {
    return new CreateContactCategoryTransaction(entity, tenantId);
  }

  private CreateContactCategoryTransaction(Category_ entity, String tenantId) {
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
          persistCategory(db);
          completionHandler.success(entity);
        });
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }

  private ContactCategoryRecord persistCategory(DSLContext db) {
    ContactCategoryRecord categoryRecord = db.newRecord(CONTACT_CATEGORY);
    categoryRecord.setValue( entity.getValue() );
    categoryRecord.store();

    entity.setId(categoryRecord.getId().toString());
    return categoryRecord;
  }
}
