package org.folio.rest.impl.transactions;


import org.folio.rest.impl.mapper.VendorMapper;
import org.folio.rest.jaxrs.model.Vendor;
import org.jooq.DSLContext;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.VendorRecord;

import java.util.UUID;

public class GetVendorByIdTransaction extends BaseTransaction<Vendor> {
  private UUID vendor_id;
  private String tenantId;

  public static GetVendorByIdTransaction newInstance (String uuid, String tenantId) {
    return new GetVendorByIdTransaction(uuid, tenantId);
  }

  private GetVendorByIdTransaction(String uuid, String tenantId) {
    this.vendor_id = UUID.fromString(uuid);
    this.tenantId = tenantId;
  }

  public void execute(TransactionCompletionHandler<Vendor> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        // BUILD QUERY
        VendorRecord dbRecord = ctx.selectFrom(VENDOR).where(VENDOR.ID.eq(vendor_id)).fetchOne();
        if (dbRecord == null) {
          completionHandler.success(null);
          return;
        }

        VendorMapper mapper = VendorMapper.newInstance(ctx);
        Vendor vendor = mapper.mapDBRecordToEntity(dbRecord);
        completionHandler.success(vendor);
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }
}
