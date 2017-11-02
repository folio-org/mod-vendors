package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.ContactCategoryCollection;
import org.folio.rest.jaxrs.resource.VendorContactCategoryResource.Order;
import org.jooq.*;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.ContactCategory;
import storage.model.tables.records.CategoryRecord;
import storage.model.tables.records.ContactCategoryRecord;

import java.util.ArrayList;
import java.util.List;

public class GetContactCategoriesTransaction extends BaseTransaction<ContactCategoryCollection> {
  private String tenantId;
  private String query;
  private String orderBy;
  private Order order;
  private int offset;
  private int limit;

  public static GetContactCategoriesTransaction newInstance(String query, String orderBy, Order order, int offset, int limit, String tenantId) {
    return new GetContactCategoriesTransaction(query, orderBy, order, offset, limit, tenantId);
  }

  private GetContactCategoriesTransaction(String query, String orderBy, Order order, int offset, int limit, String tenantId) {
    this.query = query;
    this.orderBy = orderBy;
    this.order = order;
    this.offset = offset;
    this.limit = limit;
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<ContactCategoryCollection> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        // BUILD QUERY
        // 1. Select the table to get information from
        SelectWhereStep myQuery = ctx.selectFrom(CONTACT_CATEGORY);

        // 2. Add the where condition, if it was provided
        Condition whereCondition = dbClient.conditionFromParams(query);
        SelectConditionStep conditionStep = (SelectConditionStep)myQuery;
        if (whereCondition != null) {
          conditionStep = myQuery.where(whereCondition);
        }

        // 3. Specify the order, if it was provided
        SelectSeekStep1 seekStep = (SelectSeekStep1)conditionStep;
        SortField<Object> sortField = dbClient.sortFieldFromParam(orderBy, order == Order.asc);
        if (sortField != null) {
          seekStep = conditionStep.orderBy(sortField);
        }

        // 4. Add the limit and the offset if it was provided
        SelectForUpdateStep updateStep = seekStep.limit(limit).offset(offset);

        // 5. Fetch the results
        @SuppressWarnings("unchecked") Result<ContactCategoryRecord> result = updateStep.fetch();

        // TOTAL RECORD COUNT
        // Calculate the total record count
        SelectWhereStep countQuery = ctx.selectCount().from(CONTACT_CATEGORY);
        SelectConditionStep countConditionStep = (SelectConditionStep)countQuery;
        if (whereCondition != null) {
          countConditionStep = countQuery.where(whereCondition);
        }
        int totalCount = (int)countConditionStep.fetchOne(0,int.class);
        int first = 0, last = 0;

        // INDEXES
        // Calculate the start and end index of the result set
        if (result.size() > 0) {
          first = limit * offset + 1;
          last = first + result.size() - 1;
        }

        List<Category> categories = new ArrayList<>();
        for (ContactCategoryRecord record: result) {
          Category category = new Category().withValue(record.getValue());
          category.setId(record.getId().toString());
          categories.add(category);
        }

        // Wrap the result
        ContactCategoryCollection collection = new ContactCategoryCollection();
        collection.setCategories(categories);
        collection.setTotalRecords(totalCount);
        collection.setFirst(first);
        collection.setLast(last);

        completionHandler.success(collection);
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }
}
