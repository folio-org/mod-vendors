package org.folio.rest.impl.transactions;


import org.folio.rest.impl.mapper.VendorMapper;
import org.folio.rest.jaxrs.model.Vendor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.records.*;

import java.util.ArrayList;
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

          Result<Record> vendor_addresses = db.select()
            .from(VENDOR_ADDRESS)
            .join(VENDOR).on(VENDOR_ADDRESS.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch();
          List<UUID> vendor_address_ids = new ArrayList<>();
          List<UUID> address_ids = new ArrayList<>();
          for (Record record: vendor_addresses) {
            VendorAddressRecord each = record.into(VENDOR_ADDRESS);
            vendor_address_ids.add(each.getId());

            UUID uuid = UUID.fromString(each.getAddress());
            address_ids.add(uuid);
          }
          db.deleteFrom(ADDRESS).where(ADDRESS.ID.in(address_ids)).execute();
          db.deleteFrom(VENDOR_ADDRESS_CATEGORY).where(VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID.in(vendor_address_ids)).execute();
          db.deleteFrom(VENDOR_ADDRESS).where(VENDOR_ADDRESS.VENDOR_ID.eq(vendor_id)).execute();

          Result<Record> vendor_phones = db.select()
            .from(VENDOR_PHONE)
            .join(VENDOR).on(VENDOR_PHONE.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch();
          List<UUID> vendor_phone_ids = new ArrayList<>();
          List<UUID> phone_ids = new ArrayList<>();
          for (Record record: vendor_phones) {
            VendorPhoneRecord each = record.into(VENDOR_PHONE);
            vendor_phone_ids.add(each.getId());

            UUID uuid = UUID.fromString(each.getPhoneNumber());
            phone_ids.add(uuid);
          }
          db.deleteFrom(PHONE_NUMBER).where(PHONE_NUMBER.ID.in(phone_ids)).execute();
          db.deleteFrom(VENDOR_PHONE_CATEGORY).where(VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID.in(vendor_phone_ids)).execute();
          db.deleteFrom(VENDOR_PHONE).where(VENDOR_PHONE.VENDOR_ID.eq(vendor_id)).execute();


          Result<Record> vendor_emails = db.select()
            .from(VENDOR_EMAIL)
            .join(VENDOR).on(VENDOR_EMAIL.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch();
          List<UUID> vendor_email_ids = new ArrayList<>();
          List<UUID> email_ids = new ArrayList<>();
          for (Record record: vendor_emails) {
            VendorEmailRecord each = record.into(VENDOR_EMAIL);
            vendor_email_ids.add(each.getId());

            UUID uuid = UUID.fromString(each.getEmail());
            email_ids.add(uuid);
          }
          db.deleteFrom(EMAIL).where(EMAIL.ID.in(email_ids)).execute();
          db.deleteFrom(VENDOR_EMAIL_CATEGORY).where(VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID.in(vendor_email_ids)).execute();
          db.deleteFrom(VENDOR_EMAIL).where(VENDOR_EMAIL.VENDOR_ID.eq(vendor_id)).execute();

          Result<Record> vendor_contacts = db.select()
            .from(VENDOR_CONTACT)
            .join(VENDOR).on(VENDOR_CONTACT.VENDOR_ID.eq(VENDOR.ID))
            .where(VENDOR.ID.eq(vendor_id))
            .fetch();
          List<UUID> vendor_contact_ids = new ArrayList<>();
          List<UUID> contact_ids = new ArrayList<>();
          for (Record record: vendor_contacts) {
            VendorContactRecord each = record.into(VENDOR_CONTACT);
            vendor_contact_ids.add(each.getId());

            UUID uuid = UUID.fromString(each.getContactPersonId());
            contact_ids.add(uuid);
          }
          Result<Record> people = db.select()
            .from(PERSON)
            .join(ADDRESS).on(ADDRESS.ID.eq(PERSON.ADDRESS_ID))
            .join(EMAIL).on(EMAIL.ID.eq(PERSON.EMAIL_ID))
            .join(PHONE_NUMBER).on(PHONE_NUMBER.ID.eq(PERSON.PHONE_NUMBER_ID))
            .where(PERSON.ID.in(contact_ids))
            .fetch();
          for (Record record: people) {
            PersonRecord personRecord = record.into(PERSON);
            AddressRecord addressRecord = record.into(ADDRESS);
            EmailRecord emailRecord = record.into(EMAIL);
            PhoneNumberRecord phoneNumberRecord = record.into(PHONE_NUMBER);
            personRecord.delete();
            addressRecord.delete();
            emailRecord.delete();
            phoneNumberRecord.delete();
          }
          db.deleteFrom(VENDOR_CONTACT_CATEGORY).where(VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID.in(vendor_contact_ids)).execute();
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
