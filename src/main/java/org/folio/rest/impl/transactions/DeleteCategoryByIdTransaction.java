package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category;
import org.jooq.DSLContext;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.CategoryRecord;

import java.util.UUID;

public class DeleteCategoryByIdTransaction extends BaseTransaction<Category> {
  private UUID category_id;
  private String tenantId;

  public static DeleteCategoryByIdTransaction newInstance (String uuid, String tenantId) {
    return new DeleteCategoryByIdTransaction(uuid, tenantId);
  }

  private DeleteCategoryByIdTransaction(String uuid, String tenantId) {
    this.category_id = UUID.fromString(uuid);
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<Category> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        CategoryRecord categoryRecord = ctx.selectFrom(CATEGORY).where(CATEGORY.ID.eq(category_id)).fetchOne();
        if (categoryRecord == null) {
          completionHandler.success(null);
        }

        Category category = new Category();
        category.setId( categoryRecord.getId().toString() );
        category.setValue( categoryRecord.getValue() );

        categoryRecord.delete();

        completionHandler.success(category);
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }
}
