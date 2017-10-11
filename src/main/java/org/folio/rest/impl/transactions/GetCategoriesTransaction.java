package org.folio.rest.impl.transactions;

import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.CategoryCollection;
import org.folio.rest.jaxrs.resource.VendorCategoryResource;
import org.jooq.*;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.CategoryRecord;

import java.util.ArrayList;
import java.util.List;

public class GetCategoriesTransaction extends BaseTransaction<CategoryCollection> {
  private String tenantId;
  private String query;
  private String orderBy;
  private VendorCategoryResource.Order order;
  private int offset;
  private int limit;

  public static GetCategoriesTransaction newInstance(String query, String orderBy, VendorCategoryResource.Order order, int offset, int limit, String tenantId) {
    return new GetCategoriesTransaction(query, orderBy, order, offset, limit, tenantId);
  }

  private GetCategoriesTransaction(String query, String orderBy, VendorCategoryResource.Order order, int offset, int limit, String tenantId) {
    this.query = query;
    this.orderBy = orderBy;
    this.order = order;
    this.offset = offset;
    this.limit = limit;
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<CategoryCollection> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        // BUILD QUERY
        // 1. Select the table to get information from
        SelectWhereStep myQuery = ctx.selectFrom(CATEGORY);

        // 2. Add the where condition, if it was provided
        Condition whereCondition = dbClient.conditionFromParams(query);
        SelectConditionStep conditionStep = (SelectConditionStep)myQuery;
        if (whereCondition != null) {
          conditionStep = myQuery.where(whereCondition);
        }

        // 3. Specify the order, if it was provided
        SelectSeekStep1 seekStep = (SelectSeekStep1)conditionStep;
        SortField<Object> sortField = dbClient.sortFieldFromParam(orderBy, order == VendorCategoryResource.Order.asc);
        if (sortField != null) {
          seekStep = conditionStep.orderBy(sortField);
        }

        // 4. Add the limit and the offset if it was provided
        SelectForUpdateStep updateStep = seekStep.limit(limit).offset(offset);

        // 5. Fetch the results
        @SuppressWarnings("unchecked") Result<CategoryRecord> result = updateStep.fetch();

        // TOTAL RECORD COUNT
        // Calculate the total record count
        SelectWhereStep countQuery = ctx.selectCount().from(CATEGORY);
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
        for (CategoryRecord record: result) {
          Category category = new Category().withValue(record.getValue());
          category.setId(record.getId().toString());
          categories.add(category);
        }

        // Wrap the result
        CategoryCollection collection = new CategoryCollection();
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
