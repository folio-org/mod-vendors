package org.folio.rest.impl.transactions;


import org.folio.rest.impl.mapper.VendorMapper;
import org.folio.rest.jaxrs.model.Vendor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.VendorRecord;

import java.util.List;
import java.util.UUID;

public class DeleteVendorByIdTransaction extends BaseTransaction<Vendor> {
  private UUID vendor_id;
  private String tenantId;

  public static DeleteVendorByIdTransaction newInstance (String uuid, String tenantId) {
    return new DeleteVendorByIdTransaction(uuid, tenantId);
  }

  private DeleteVendorByIdTransaction(String uuid, String tenantId) {
    this.vendor_id = UUID.fromString(uuid);
    this.tenantId = tenantId;
  }

  public void execute(TransactionCompletionHandler<Vendor> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        ctx.transaction(configuration -> {
          DSLContext db = DSL.using(configuration);

          VendorRecord dbRecord = db.selectFrom(VENDOR).where(VENDOR.ID.eq(vendor_id)).fetchOne();
          if (dbRecord == null) {
            Exception e = new Exception("no vendor found");
            completionHandler.failed(e);
            return;
          }

          VendorMapper mapper = VendorMapper.newInstance(db);
          Vendor vendorDataToBeDeleted = mapper.mapDBRecordToEntity(dbRecord);

          // DELETE ALL ASSOCIATED OBJECTS
          db.deleteFrom(VENDOR_NAME).where(VENDOR_NAME.VENDOR_ID.eq(vendor_id)).execute();
          db.deleteFrom(VENDOR_CURRENCY).where(VENDOR_CURRENCY.VENDOR_ID.eq(vendor_id)).execute();
          db.deleteFrom(VENDOR_INTERFACE).where(VENDOR_INTERFACE.VENDOR_ID.eq(vendor_id)).execute();
          db.deleteFrom(AGREEMENT).where(AGREEMENT.VENDOR_ID.eq(vendor_id)).execute();
          db.deleteFrom(LIBRARY_VENDOR_ACCT).where(LIBRARY_VENDOR_ACCT.VENDOR_ID.eq(vendor_id)).execute();
          db.deleteFrom(NOTE).where(NOTE.VENDOR_ID.eq(vendor_id)).execute();
          db.deleteFrom(JOB).where(JOB.VENDOR_ID.eq(vendor_id)).execute();

          List<UUID> vendor_address_ids = db.select()
            .from(VENDOR_ADDRESS)
            .join(VENDOR).on(VENDOR_ADDRESS.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch(VENDOR_ADDRESS.ID);
          db.deleteFrom(VENDOR_ADDRESS_CATEGORY).where(VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID.in(vendor_address_ids)).execute();
          db.deleteFrom(VENDOR_ADDRESS).where(VENDOR_ADDRESS.VENDOR_ID.eq(vendor_id)).execute();

          List<UUID> vendor_phone_ids = db.select()
            .from(VENDOR_PHONE)
            .join(VENDOR).on(VENDOR_PHONE.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch(VENDOR_PHONE.ID);
          db.deleteFrom(VENDOR_PHONE_CATEGORY).where(VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID.in(vendor_phone_ids)).execute();
          db.deleteFrom(VENDOR_PHONE).where(VENDOR_PHONE.VENDOR_ID.eq(vendor_id)).execute();

          List<UUID> vendor_email_ids = db.select()
            .from(VENDOR_EMAIL)
            .join(VENDOR).on(VENDOR_EMAIL.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch(VENDOR_EMAIL.ID);
          db.deleteFrom(VENDOR_EMAIL_CATEGORY).where(VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID.in(vendor_email_ids )).execute();
          db.deleteFrom(VENDOR_EMAIL).where(VENDOR_EMAIL.VENDOR_ID.eq(vendor_id)).execute();

          List<UUID> vendor_contact_ids = db.select()
            .from(VENDOR_CONTACT)
            .join(VENDOR).on(VENDOR_CONTACT.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch(VENDOR_CONTACT.ID);
          db.deleteFrom(VENDOR_CONTACT_CATEGORY).where(VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID.in(vendor_contact_ids )).execute();
          db.deleteFrom(VENDOR_CONTACT).where(VENDOR_CONTACT.VENDOR_ID.eq(vendor_id)).execute();

          db.deleteFrom(EDI_INFO).where(EDI_INFO.ID.eq(dbRecord.getId())).execute();
          dbRecord.delete();

          completionHandler.success(vendorDataToBeDeleted);
        });
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }
}
