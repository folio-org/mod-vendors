package org.folio.rest.impl.transactions;


import org.folio.rest.impl.mapper.VendorMapper;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCollection;
import org.folio.rest.jaxrs.resource.VendorResource.Order;
import org.jooq.*;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.VendorRecord;

import java.util.ArrayList;
import java.util.List;

public class GetVendorsTransaction extends BaseTransaction<VendorCollection> {
  private String query;
  private String orderBy;
  private Order order;
  private int offset;
  private int limit;
  private String tenantId;

  public static GetVendorsTransaction newInstance (String query, String orderBy, Order order, int offset, int limit, String tenantId) {
    return new GetVendorsTransaction(query, orderBy, order, offset, limit, tenantId);
  }

  private GetVendorsTransaction(String query, String orderBy, Order order, int offset, int limit, String tenantId) {
    this.query = query;
    this.orderBy = orderBy;
    this.order = order;
    this.offset = offset;
    this.limit = limit;
    this.tenantId = tenantId;
  }

  public void execute(TransactionCompletionHandler<VendorCollection> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        // BUILD QUERY
        // 1. Select the table to get information from
        SelectWhereStep myQuery = ctx.selectFrom(VENDOR);

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
        @SuppressWarnings("unchecked") Result<VendorRecord> result = updateStep.fetch();

        // TOTAL RECORD COUNT
        // Calculate the total record count
        SelectWhereStep countQuery = ctx.selectCount().from(VENDOR);
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

        VendorMapper mapper = VendorMapper.newInstance(ctx);
        List<Vendor> vendors = new ArrayList<>();
        for (VendorRecord record: result) {
          Vendor vendor = mapper.mapDBRecordToEntity(record);
          vendors.add(vendor);
        }

        // Wrap the result inside a VendorCollection
        VendorCollection collection = new VendorCollection();
        collection.setVendors(vendors);
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
