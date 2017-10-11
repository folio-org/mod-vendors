package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.CategoryRecord;

public class CreateCategoryTransaction extends BaseTransaction<Category> {
  private Category entity = null;
  private String tenantId = null;

  public static CreateCategoryTransaction newInstance(Category entity, String tenantId) {
    return new CreateCategoryTransaction(entity, tenantId);
  }

  private CreateCategoryTransaction(Category entity, String tenantId) {
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

  private CategoryRecord persistCategory(DSLContext db) {
    CategoryRecord categoryRecord = db.newRecord(CATEGORY);
    categoryRecord.setValue( entity.getValue() );
    categoryRecord.store();

    entity.setId(categoryRecord.getId().toString());
    return categoryRecord;
  }
}
