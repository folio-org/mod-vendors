package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.Category_;
import org.jooq.DSLContext;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.ContactCategoryRecord;

import java.util.UUID;

public class GetContactCategoryByIdTransaction extends BaseTransaction<Category_> {
  private UUID category_id;
  private String tenantId;

  public static GetContactCategoryByIdTransaction newInstance (String uuid, String tenantId) {
    return new GetContactCategoryByIdTransaction(uuid, tenantId);
  }

  private GetContactCategoryByIdTransaction(String uuid, String tenantId) {
    this.category_id = UUID.fromString(uuid);
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<Category_> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        ContactCategoryRecord categoryRecord = ctx.selectFrom(CONTACT_CATEGORY).where(CONTACT_CATEGORY.ID.eq(category_id)).fetchOne();
        if (categoryRecord == null) {
          completionHandler.success(null);
        }

        Category_ category = new Category_();
        category.setId( categoryRecord.getId().toString() );
        category.setValue( categoryRecord.getValue() );
        completionHandler.success(category);
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }
}
