package org.folio.rest.impl.transactions;

import org.folio.rest.impl.utils.StringUtils;
import org.folio.rest.jaxrs.model.*;
import org.folio.rest.jaxrs.model.Address;
import org.folio.rest.jaxrs.model.Agreement;
import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.EdiInfo;
import org.folio.rest.jaxrs.model.Email;
import org.folio.rest.jaxrs.model.Job;
import org.folio.rest.jaxrs.model.Note;
import org.folio.rest.jaxrs.model.PhoneNumber;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCurrency;
import org.folio.rest.jaxrs.model.VendorInterface;
import org.folio.rest.jaxrs.model.VendorName;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import storage.client.ConnectResultHandler;
import storage.client.PostgresClient;
import storage.model.tables.*;
import storage.model.tables.records.*;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UpdateVendorTransaction extends BaseTransaction<Vendor> {
  private Vendor entity = null;
  private String tenantId = null;
  private UUID db_uuid = null;

  public static UpdateVendorTransaction newInstance (String uuid, Vendor entity, String tenantId) {
    return new UpdateVendorTransaction(uuid, entity, tenantId);
  }

  private UpdateVendorTransaction(String uuid, Vendor entity, String tenantId) {
    this.db_uuid = UUID.fromString(uuid);
    this.entity = entity;
    this.tenantId = tenantId;
  }

  @Override
  public void execute(TransactionCompletionHandler<Vendor> completionHandler) {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext ctx) {
        ctx.transaction(configuration -> {
          DSLContext db = DSL.using(configuration);

          // Persist the COMPLETE vendor record
          VendorRecord vendorRecord = updateVendor(db);
          if (vendorRecord == null) {
            completionHandler.success(null);
            return;
          }

          updateEdiInfo(db, vendorRecord);
          updateVendorNames(db, vendorRecord);
          updateVendorCurrencies(db, vendorRecord);
          updateVendorInterfaces(db, vendorRecord);
          updateVendorAgreements(db, vendorRecord);
          updateVendorAccounts(db, vendorRecord);
          updateVendorJob(db, vendorRecord);
          updateVendorAddresses(db, vendorRecord);
          updateVendorPhoneNumbers(db, vendorRecord);
          updateVendorEmails(db, vendorRecord);
          updateVendorContacts(db, vendorRecord);
          updateVendorNotes(db, vendorRecord);

          completionHandler.success(entity);
        });
      }

      @Override
      public void failed(Exception exception) {
        completionHandler.failed(exception);
      }
    });
  }

  private VendorRecord updateVendor(DSLContext db) {
    VendorRecord vendorRecord = db.selectFrom(VENDOR).where(VENDOR.ID.eq(db_uuid)).fetchOne();
    if (vendorRecord == null) {
      return null;
    }

    vendorRecord.setName(entity.getName());
    vendorRecord.setCode(entity.getCode());
    vendorRecord.setVendorStatus(entity.getVendorStatus());
    vendorRecord.setLanguage(entity.getLanguage());
    vendorRecord.setErpCode(entity.getErpCode());
    vendorRecord.setPaymentMethod(entity.getPaymentMethod());
    vendorRecord.setAccessProvider(entity.getAccessProvider());
    vendorRecord.setGovernmental(entity.getGovernmental());
    vendorRecord.setLicensor(entity.getLicensor());
    vendorRecord.setMaterialSupplier(entity.getMaterialSupplier());
    vendorRecord.setClaimingInterval(entity.getClaimingInterval());
    BigDecimal discount = new BigDecimal(entity.getDiscountPercent());
    vendorRecord.setDiscountPercent( discount );
    vendorRecord.setExpectedActivationInterval(entity.getExpectedActivationInterval());
    vendorRecord.setExpectedInvoiceInterval(entity.getExpectedInvoiceInterval());
    vendorRecord.setRenewalActivationInterval(entity.getRenewalActivationInterval());
    vendorRecord.setSubscriptionInterval(entity.getSubscriptionInterval());
    vendorRecord.setLiableForVat(entity.getLiableForVat());
    vendorRecord.setTaxId(entity.getTaxId());
    BigDecimal taxPercentage = new BigDecimal(entity.getTaxPercentage());
    vendorRecord.setTaxPercentage(taxPercentage);
//    vendorRecord.setEdiInfoId(ediInfoRecord.getId());
    vendorRecord.update();

    return vendorRecord;
  }

  private void updateEdiInfo(DSLContext db, VendorRecord vendorRecord) {
    EdiInfo source = entity.getEdiInfo();

    EdiInfoRecord ediInfoRecord = db.selectFrom(EDI_INFO).where(EDI_INFO.ID.eq(vendorRecord.getEdiInfoId())).fetchOne();
    ediInfoRecord.setVendorEdiCode(source.getVendorEdiCode());
    ediInfoRecord.setVendorEdiType(source.getVendorEdiType());
    ediInfoRecord.setLibEdiCode(source.getLibEdiCode());
    ediInfoRecord.setLibEdiType(source.getLibEdiType());
    ediInfoRecord.setEdiNamingConvention(source.getEdiNamingConvention());
    ediInfoRecord.setProrateTax(source.getProrateTax());
    ediInfoRecord.setProrateFees(source.getProrateFees());
    ediInfoRecord.setSendAcctNum(source.getSendAcctNum());
    ediInfoRecord.setSupportOrder(source.getSupportOrder());
    ediInfoRecord.setSupportInvoice(source.getSupportInvoice());
    ediInfoRecord.setNotes(source.getNotes());
    ediInfoRecord.setFtpFormat(source.getFtpFormat());
    ediInfoRecord.setFtpMode(source.getFtpMode());
    ediInfoRecord.setFtpConnMode(source.getFtpConnMode());
    ediInfoRecord.setFtpPort(source.getFtpPort());
    ediInfoRecord.setServerAddress(source.getServerAddress());
    ediInfoRecord.setUsername(source.getUsername());
    ediInfoRecord.setPassword(source.getPassword());
    ediInfoRecord.setOrderDirectory(source.getOrderDirectory());
    ediInfoRecord.setInvoiceDirectory(source.getInvoiceDirectory());
    ediInfoRecord.setSendToEmails(source.getSendToEmails());
    ediInfoRecord.setNotifyAllEdi(source.getNotifyAllEdi());
    ediInfoRecord.setNotifyInvoiceOnly(source.getNotifyInvoiceOnly());
    ediInfoRecord.setNotifyErrorOnly(source.getNotifyErrorOnly());
    ediInfoRecord.update();
  }

  private void updateVendorNames(DSLContext db, VendorRecord vendorRecord) {
    List<VendorName> sources = entity.getVendorNames();
    VendorNameRecord[] dbRecords = db.selectFrom(VENDOR_NAME).where(VENDOR_NAME.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorNameRecord u_record = dbRecords[idx];
      VendorName each = sources.get(idx);
      u_record.setDescription(each.getDescription());
      u_record.setValue(each.getValue());
      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorNameRecord d_record = dbRecords[rem_idx];
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        VendorName each = sources.get(rem_idx);
        VendorNameRecord n_record = db.newRecord(VENDOR_NAME);
        n_record.setValue(each.getValue());
        n_record.setDescription(each.getDescription());
        n_record.setVendorId(vendorRecord.getId());
        n_record.store();

        each.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorCurrencies(DSLContext db, VendorRecord vendorRecord) {
    List<VendorCurrency> sources = entity.getVendorCurrencies();
    VendorCurrencyRecord[] dbRecords = db.selectFrom(VENDOR_CURRENCY).where(VENDOR_CURRENCY.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;

    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorCurrencyRecord u_record = dbRecords[idx];
      VendorCurrency input = sources.get(idx);
      u_record.setCurrency(input.getCurrency());
      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorCurrencyRecord d_record = dbRecords[rem_idx];
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        VendorCurrency each = sources.get(rem_idx);
        VendorCurrencyRecord n_record = db.newRecord(VENDOR_CURRENCY);
        n_record.setCurrency(each.getCurrency());
        n_record.setVendorId(vendorRecord.getId());
        n_record.store();

        each.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorInterfaces(DSLContext db, VendorRecord vendorRecord) {
    List<VendorInterface> sources = entity.getVendorInterfaces();
    VendorInterfaceRecord[] dbRecords = db.selectFrom(VENDOR_INTERFACE).where(VENDOR_INTERFACE.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;

    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorInterfaceRecord u_record = dbRecords[idx];
      VendorInterface input = sources.get(idx);
      u_record.setName(input.getName());
      u_record.setUri(input.getUri());
      u_record.setUsername(input.getUsername());
      u_record.setPassword(input.getPassword());
      u_record.setNotes(input.getNotes());
      u_record.setAvailable(input.getAvailable());
      u_record.setDeliveryMethod(input.getDeliveryMethod());
      u_record.setStatisticsFormat(input.getStatisticsFormat());
      u_record.setLocallyStored(input.getLocallyStored());
      u_record.setOnlineLocation(input.getOnlineLocation());
      u_record.setStatisticsNotes(input.getStatisticsNotes());
      u_record.setVendorId(vendorRecord.getId());
      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorInterfaceRecord d_record = dbRecords[rem_idx];
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        VendorInterface input = sources.get(rem_idx);
        VendorInterfaceRecord n_record = db.newRecord(VENDOR_INTERFACE);
        n_record.setName(input.getName());
        n_record.setUri(input.getUri());
        n_record.setUsername(input.getUsername());
        n_record.setPassword(input.getPassword());
        n_record.setNotes(input.getNotes());
        n_record.setAvailable(input.getAvailable());
        n_record.setDeliveryMethod(input.getDeliveryMethod());
        n_record.setStatisticsFormat(input.getStatisticsFormat());
        n_record.setLocallyStored(input.getLocallyStored());
        n_record.setOnlineLocation(input.getOnlineLocation());
        n_record.setStatisticsNotes(input.getStatisticsNotes());
        n_record.setVendorId(vendorRecord.getId());
        n_record.store();

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorAgreements(DSLContext db, VendorRecord vendorRecord) {
    List<Agreement> sources = entity.getAgreements();
    AgreementRecord[] dbRecords = db.selectFrom(AGREEMENT).where(AGREEMENT.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;

    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      AgreementRecord u_record = dbRecords[idx];
      Agreement input = sources.get(idx);
      BigDecimal agreementDiscount = new BigDecimal(entity.getDiscountPercent());
      u_record.setDiscount(agreementDiscount);
      u_record.setName(input.getName());
      u_record.setNotes(input.getNotes());
      u_record.setReferenceUrl(input.getReferenceUrl());
      u_record.setVendorId(vendorRecord.getId());
      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        AgreementRecord d_record = dbRecords[rem_idx];
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        Agreement input = sources.get(rem_idx);
        AgreementRecord n_record = db.newRecord(AGREEMENT);
        BigDecimal agreementDiscount = new BigDecimal(entity.getDiscountPercent());
        n_record.setDiscount(agreementDiscount);
        n_record.setName(input.getName());
        n_record.setNotes(input.getNotes());
        n_record.setReferenceUrl(input.getReferenceUrl());
        n_record.setVendorId(vendorRecord.getId());
        n_record.store();

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorAccounts(DSLContext db, VendorRecord vendorRecord) {
    List<Account> sources = entity.getAccounts();
    LibraryVendorAcctRecord[] dbRecords = db.selectFrom(LIBRARY_VENDOR_ACCT).where(LIBRARY_VENDOR_ACCT.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      LibraryVendorAcctRecord u_record = dbRecords[idx];
      Account input = sources.get(idx);
      u_record.setName(input.getName());
      u_record.setPaymentMethod(input.getPaymentMethod());
      u_record.setAccountNo(input.getAccountNo());
      u_record.setLibVenAcctStatus(input.getLibVenAcctStatus());
      u_record.setDescription(input.getDescription());
      u_record.setContactInfo(input.getContactInfo());
      u_record.setApsystemNo(input.getAppsystemNo());
      u_record.setNotes(input.getNotes());
      u_record.setLibraryCode(input.getLibraryCode());
      u_record.setLibraryEdiCode(input.getLibraryEdiCode());
      u_record.setVendorId(vendorRecord.getId());
      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        LibraryVendorAcctRecord d_record = dbRecords[rem_idx];
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        Account input = sources.get(rem_idx);
        LibraryVendorAcctRecord n_record = db.newRecord(LIBRARY_VENDOR_ACCT);
        n_record.setName(input.getName());
        n_record.setPaymentMethod(input.getPaymentMethod());
        n_record.setAccountNo(input.getAccountNo());
        n_record.setLibVenAcctStatus(input.getLibVenAcctStatus());
        n_record.setDescription(input.getDescription());
        n_record.setContactInfo(input.getContactInfo());
        n_record.setApsystemNo(input.getAppsystemNo());
        n_record.setNotes(input.getNotes());
        n_record.setLibraryCode(input.getLibraryCode());
        n_record.setLibraryEdiCode(input.getLibraryEdiCode());
        n_record.setVendorId(vendorRecord.getId());
        n_record.store();

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorJob(DSLContext db, VendorRecord vendorRecord) {
    Job job = entity.getJob();

    JobRecord jobRecord = db.selectFrom(JOB).where(JOB.VENDOR_ID.eq(vendorRecord.getId())).fetchOne();
    jobRecord.setIsScheduled(job.getIsScheduled());
    jobRecord.setIsMonday(job.getIsMonday());
    jobRecord.setIsTuesday(job.getIsTuesday());
    jobRecord.setIsWednesday(job.getIsWednesday());
    jobRecord.setIsThursday(job.getIsThursday());
    jobRecord.setIsFriday(job.getIsFriday());
    jobRecord.setIsSaturday(job.getIsSaturday());
    jobRecord.setIsSunday(job.getIsSunday());
    jobRecord.setSchedulingNotes(job.getSchedulingNotes());
    jobRecord.setVendorId(vendorRecord.getId());

    Timestamp startDate = StringUtils.timestampFromString(job.getStartDate().toString(), "yyyy'-'mm'-'dd'T'hhmmss");
    jobRecord.setStartDate(startDate);

    Time time = StringUtils.timeFromString(job.getTime().toString(), "hhmmss");
    jobRecord.setTime(time);
    jobRecord.update();
  }

  private void updateVendorAddresses(DSLContext db, VendorRecord vendorRecord) {
    List<Address> sources = entity.getAddresses();

    List<UUID> sourceIDs = new ArrayList<>();
    for (Address each: sources) {
      UUID myAddressID = UUID.fromString(each.getId());
      sourceIDs.add(myAddressID);
    }
    VendorAddressRecord[] dbRecords = db.selectFrom(VENDOR_ADDRESS).where(VENDOR_ADDRESS.VENDOR_ID.in(sourceIDs)).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorAddressRecord u_record = dbRecords[idx];
      Address input = sources.get(idx);
      u_record.setLanguage(input.getLanguage());
      u_record.setSanCode(input.getSanCode());
      u_record.setVendorId(vendorRecord.getId());

      // TODO: Update the AddressRecord -- this will likely be decoupled out later
      Address_ input_address = input.getAddress();
      UUID address_uuid = UUID.fromString(input_address.getId());
      AddressRecord addressRecord = db.selectFrom(ADDRESS).where(ADDRESS.ID.eq(address_uuid)).fetchOne();
      addressRecord.setAddressLine_1(input_address.getAddressLine1());
      addressRecord.setAddressLine_2(input_address.getAddressLine2());
      addressRecord.setCity(input_address.getCity());
      addressRecord.setRegion(input_address.getRegion());
      addressRecord.setPostalCode(input_address.getPostalCode());
      addressRecord.setCountry(input_address.getCountry());
      addressRecord.update();

      // Update categories
      db.deleteFrom(VENDOR_ADDRESS_CATEGORY).where(VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID.eq(u_record.getId())).execute();
      List<Category> categories = input.getCategories();
      for (Category eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        db.insertInto(VENDOR_ADDRESS_CATEGORY, VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID, VENDOR_ADDRESS_CATEGORY.CATEGORY_ID)
          .values(u_record.getId(), categoryID)
          .execute();
      }

      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorAddressRecord d_record = dbRecords[rem_idx];

        // Clear all the associated VendorAddressCategory
        db.deleteFrom(VENDOR_ADDRESS_CATEGORY).where(VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID.eq(d_record.getId())).execute();
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        Address input = sources.get(rem_idx);
        AddressRecord addressRecord = db.newRecord(ADDRESS);
        addressRecord.setAddressLine_1(input.getAddress().getAddressLine1());
        addressRecord.setAddressLine_2(input.getAddress().getAddressLine2());
        addressRecord.setCity(input.getAddress().getCity());
        addressRecord.setRegion(input.getAddress().getRegion());
        addressRecord.setPostalCode(input.getAddress().getPostalCode());
        addressRecord.setCountry(input.getAddress().getCountry());
        addressRecord.store();

        // Update 'entity'
        input.setId(addressRecord.getId().toString());
        // ----

        VendorAddressRecord n_record = db.newRecord(VendorAddress.VENDOR_ADDRESS);
        n_record.setLanguage(input.getLanguage());
        n_record.setSanCode(input.getSanCode());
        n_record.setVendorId(vendorRecord.getId());
        n_record.setAddress(addressRecord.getId().toString());
        n_record.store();

        List<Category> categories = input.getCategories();
        for (Category eachCategory: categories) {
          UUID categoryID = UUID.fromString(eachCategory.getId());
          db.insertInto(VENDOR_ADDRESS_CATEGORY, VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID, VENDOR_ADDRESS_CATEGORY.CATEGORY_ID)
            .values(n_record.getId(), categoryID)
            .execute();
        }

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorPhoneNumbers(DSLContext db, VendorRecord vendorRecord) {
    List<PhoneNumber> sources = entity.getPhoneNumbers();

    List<UUID> sourceIDs = new ArrayList<>();
    for (PhoneNumber each: sources) {
      UUID myAddressID = UUID.fromString(each.getId());
      sourceIDs.add(myAddressID);
    }
    VendorPhoneRecord[] dbRecords = db.selectFrom(VENDOR_PHONE).where(VENDOR_PHONE.VENDOR_ID.in(sourceIDs)).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorPhoneRecord u_record = dbRecords[idx];
      PhoneNumber input = sources.get(idx);
      u_record.setVendorId(vendorRecord.getId());
      u_record.setLanguage(input.getLanguage());

      // TODO: Update the PhoneNumberRecord -- this will likely be decoupled out later
      PhoneNumber_ input_phone = input.getPhoneNumber();
      UUID address_uuid = UUID.fromString(input_phone.getId());
      PhoneNumberRecord phoneNumberRecord = db.selectFrom(PHONE_NUMBER).where(PHONE_NUMBER.ID.eq(address_uuid)).fetchOne();
      phoneNumberRecord.setCountryCode(input_phone.getCountryCode());
      phoneNumberRecord.setAreaCode(input_phone.getAreaCode());
      phoneNumberRecord.setPhoneNumber(input_phone.getPhoneNumber());
      phoneNumberRecord.update();

      // Update categories
      db.deleteFrom(VENDOR_PHONE_CATEGORY).where(VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID.eq(u_record.getId())).execute();
      List<Category> categories = input.getCategories();
      for (Category eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        db.insertInto(VENDOR_PHONE_CATEGORY, VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID, VENDOR_PHONE_CATEGORY.CATEGORY_ID)
          .values(u_record.getId(), categoryID)
          .execute();
      }

      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorPhoneRecord d_record = dbRecords[rem_idx];
        // Clear all the associated VendorAddressCategory
        db.deleteFrom(VENDOR_PHONE_CATEGORY).where(VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID.eq(d_record.getId())).execute();
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        PhoneNumber input = sources.get(rem_idx);
        PhoneNumberRecord phoneNumberRecord = db.newRecord(PHONE_NUMBER);
        phoneNumberRecord.setCountryCode(input.getPhoneNumber().getCountryCode());
        phoneNumberRecord.setAreaCode(input.getPhoneNumber().getAreaCode());
        phoneNumberRecord.setPhoneNumber(input.getPhoneNumber().getPhoneNumber());
        phoneNumberRecord.store();

        // Update 'entity'
        input.setId(phoneNumberRecord.getId().toString());
        // ----

        VendorPhoneRecord n_record = db.newRecord(VendorPhone.VENDOR_PHONE);
        n_record.setVendorId(vendorRecord.getId());
        n_record.setLanguage(input.getLanguage());
        n_record.setPhoneNumber(phoneNumberRecord.getId().toString());
        n_record.store();

        List<Category> categories = input.getCategories();
        for (Category eachCategory: categories) {
          UUID categoryID = UUID.fromString(eachCategory.getId());
          db.insertInto(VENDOR_PHONE_CATEGORY, VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID, VENDOR_PHONE_CATEGORY.CATEGORY_ID)
            .values(n_record.getId(), categoryID)
            .execute();
        }

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorEmails(DSLContext db, VendorRecord vendorRecord) {
    List<Email> sources = entity.getEmails();

    List<UUID> sourceIDs = new ArrayList<>();
    for (Email each: sources) {
      UUID myAddressID = UUID.fromString(each.getId());
      sourceIDs.add(myAddressID);
    }
    VendorEmailRecord[] dbRecords = db.selectFrom(VENDOR_EMAIL).where(VENDOR_EMAIL.VENDOR_ID.in(sourceIDs)).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorEmailRecord u_record = dbRecords[idx];
      Email input = sources.get(idx);
      u_record.setVendorId(vendorRecord.getId());
      u_record.setLanguage(input.getLanguage());

      // TODO: Update the EmailRecord -- this will likely be decoupled out later
      Email_ input_email = input.getEmail();
      UUID address_uuid = UUID.fromString(input_email.getId());
      EmailRecord emailRecord = db.selectFrom(EMAIL).where(EMAIL.ID.eq(address_uuid)).fetchOne();
      emailRecord.setValue(input_email.getValue());
      emailRecord.update();

      // TODO: Update categories
      // Update categories
      db.deleteFrom(VENDOR_EMAIL_CATEGORY).where(VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID.eq(u_record.getId())).execute();
      List<Category> categories = input.getCategories();
      for (Category eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        db.insertInto(VENDOR_EMAIL_CATEGORY, VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID, VENDOR_EMAIL_CATEGORY.CATEGORY_ID)
          .values(u_record.getId(), categoryID)
          .execute();
      }

      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorEmailRecord d_record = dbRecords[rem_idx];
        // Clear all the associated VendorAddressCategory
        db.deleteFrom(VENDOR_EMAIL_CATEGORY).where(VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID.eq(d_record.getId())).execute();
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        Email input = sources.get(rem_idx);
        EmailRecord emailRecord = db.newRecord(EMAIL);
        emailRecord.setValue(input.getEmail().getValue());
        emailRecord.store();

        // Update 'entity'
        input.setId(emailRecord.getId().toString());
        // ----

        VendorEmailRecord n_record = db.newRecord(VendorEmail.VENDOR_EMAIL);
        n_record.setVendorId(vendorRecord.getId());
        n_record.setLanguage(input.getLanguage());
        n_record.setEmail(emailRecord.getId().toString());
        n_record.store();

        List<Category> categories = input.getCategories();
        for (Category eachCategory: categories) {
          UUID categoryID = UUID.fromString(eachCategory.getId());
          db.insertInto(VENDOR_EMAIL_CATEGORY, VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID, VENDOR_EMAIL_CATEGORY.CATEGORY_ID)
            .values(n_record.getId(), categoryID)
            .execute();
        }

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorContacts(DSLContext db, VendorRecord vendorRecord) {
    List<Contact> sources = entity.getContacts();
    VendorContactRecord[] dbRecords = db.selectFrom(VENDOR_CONTACT).where(VENDOR_CONTACT.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      VendorContactRecord u_record = dbRecords[idx];
      Contact input = sources.get(idx);
      u_record.setLanguage(input.getLanguage());
      u_record.setVendorId(vendorRecord.getId());

      // TODO: Update the PersonRecord -- this will likely be decoupled out later
      ContactPerson input_person = input.getContactPerson();
      PhoneNumber_ input_phone = input_person.getPhoneNumber();
      Email_ input_email = input_person.getEmail();
      Address_ input_address = input_person.getAddress();

      UUID phone_uuid = UUID.fromString(input_phone.getId());
      PhoneNumberRecord phoneNumberRecord = db.selectFrom(PHONE_NUMBER).where(PHONE_NUMBER.ID.eq(phone_uuid)).fetchOne();
      phoneNumberRecord.setCountryCode(input_phone.getCountryCode());
      phoneNumberRecord.setAreaCode(input_phone.getAreaCode());
      phoneNumberRecord.setPhoneNumber(input_phone.getPhoneNumber());
      phoneNumberRecord.update();

      UUID email_uuid = UUID.fromString(input_email.getId());
      EmailRecord emailRecord = db.selectFrom(EMAIL).where(EMAIL.ID.eq(email_uuid)).fetchOne();
      emailRecord.setValue(input_email.getValue());
      emailRecord.update();

      UUID address_uuid = UUID.fromString(input_address.getId());
      AddressRecord addressRecord = db.selectFrom(ADDRESS).where(ADDRESS.ID.eq(address_uuid)).fetchOne();
      addressRecord.setAddressLine_1(input_address.getAddressLine1());
      addressRecord.setAddressLine_2(input_address.getAddressLine2());
      addressRecord.setCity(input_address.getCity());
      addressRecord.setRegion(input_address.getRegion());
      addressRecord.setPostalCode(input_address.getPostalCode());
      addressRecord.setCountry(input_address.getCountry());
      addressRecord.update();

      UUID person_uuid = UUID.fromString(input_person.getId());
      PersonRecord personRecord = db.selectFrom(PERSON).where(PERSON.ID.eq(person_uuid)).fetchOne();
      personRecord.setPrefix(input_person.getPrefix());
      personRecord.setFirstName(input_person.getFirstName());
      personRecord.setLastName(input_person.getLastName());
      personRecord.setLanguage(input_person.getLanguage());
      personRecord.setNotes(input_person.getNotes());
//      personRecord.setPhoneNumberId(phoneNumberRecord.getId());
//      personRecord.setEmailId(emailRecord.getId());
//      personRecord.setAddressId(addressRecord.getId());
      personRecord.update();

      // TODO: Update the associated categories
      // Update categories
      db.deleteFrom(VENDOR_CONTACT_CATEGORY).where(VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID.eq(u_record.getId())).execute();
      List<Category_> categories = input.getCategories();
      for (Category_ eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        db.insertInto(VENDOR_CONTACT_CATEGORY, VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID, VENDOR_CONTACT_CATEGORY.CATEGORY_ID)
          .values(u_record.getId(), categoryID)
          .execute();
      }

      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        VendorContactRecord d_record = dbRecords[rem_idx];

        // Clear all the associated VendorAddressCategory
        db.deleteFrom(VENDOR_CONTACT_CATEGORY).where(VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID.eq(d_record.getId())).execute();
        d_record.delete();
        // TODO: WHAT DO WE DO WITH ORPHANED RECORDS? #YIKES
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        Contact input = sources.get(rem_idx);

        // Get references to the original data passed into the API
        ContactPerson contact = input.getContactPerson();
        PhoneNumber_ phoneNumber = contact.getPhoneNumber();
        Email_ email = contact.getEmail();
        Address_ address = contact.getAddress();

        PhoneNumberRecord phoneNumberRecord = db.newRecord(PHONE_NUMBER);
        phoneNumberRecord.setCountryCode(phoneNumber.getCountryCode());
        phoneNumberRecord.setAreaCode(phoneNumber.getAreaCode());
        phoneNumberRecord.setPhoneNumber(phoneNumber.getPhoneNumber());
        phoneNumberRecord.store();

        EmailRecord emailRecord = db.newRecord(EMAIL);
        emailRecord.setValue(email.getValue());
        emailRecord.store();

        AddressRecord addressRecord = db.newRecord(ADDRESS);
        addressRecord.setAddressLine_1(address.getAddressLine1());
        addressRecord.setAddressLine_2(address.getAddressLine2());
        addressRecord.setCity(address.getCity());
        addressRecord.setRegion(address.getRegion());
        addressRecord.setPostalCode(address.getPostalCode());
        addressRecord.setCountry(address.getCountry());
        addressRecord.store();

        PersonRecord personRecord = db.newRecord(Person.PERSON);
        personRecord.setPrefix(contact.getPrefix());
        personRecord.setFirstName(contact.getFirstName());
        personRecord.setLastName(contact.getLastName());
        personRecord.setLanguage(contact.getLanguage());
        personRecord.setNotes(contact.getNotes());
        personRecord.setPhoneNumberId(phoneNumberRecord.getId());
        personRecord.setEmailId(emailRecord.getId());
        personRecord.setAddressId(addressRecord.getId());
        personRecord.store();

        VendorContactRecord n_record = db.newRecord(VENDOR_CONTACT);
        n_record.setLanguage(contact.getLanguage());
        n_record.setContactPersonId(personRecord.getId().toString());
        n_record.setVendorId(vendorRecord.getId());
        n_record.store();

        // Update 'entity'
        phoneNumber.setId(phoneNumberRecord.getId().toString());
        email.setId(emailRecord.getId().toString());
        address.setId(addressRecord.getId().toString());
        contact.setId(n_record.getId().toString());
        // -----

        List<Category_> categories = input.getCategories();
        for (Category_ eachCategory: categories) {
          UUID categoryID = UUID.fromString(eachCategory.getId());
          db.insertInto(VENDOR_CONTACT_CATEGORY, VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID, VENDOR_CONTACT_CATEGORY.CATEGORY_ID)
            .values(n_record.getId(), categoryID)
            .execute();
        }

        input.setId(n_record.getId().toString());
      }
    }
  }

  private void updateVendorNotes(DSLContext db, VendorRecord vendorRecord) {
    List<Note> sources = entity.getNotes();
    NoteRecord[] dbRecords = db.selectFrom(NOTE).where(NOTE.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

    int newValueCount = sources.size();
    int oldValueCount = dbRecords.length;
    int minCount = Math.min(newValueCount, oldValueCount);

    for (int idx = 0; idx < minCount; idx++) {
      NoteRecord u_record = dbRecords[idx];
      Note input = sources.get(idx);
      u_record.setDescription(input.getDescription());
      u_record.setVendorId(vendorRecord.getId());
      Timestamp timestamp = StringUtils.timestampFromString(input.getTimestamp().toString(), "yyyy'-'mm'-'dd'T'hhmmss");
      u_record.setTimestamp(timestamp);
      u_record.update();
    }

    if (newValueCount < oldValueCount) {
      for (int rem_idx = minCount; rem_idx < oldValueCount; rem_idx++) {
        NoteRecord d_record = dbRecords[rem_idx];
        d_record.delete();
      }
    } else if (newValueCount > oldValueCount) {
      for (int rem_idx = minCount; rem_idx < newValueCount; rem_idx++) {
        Note input = sources.get(rem_idx);
        NoteRecord n_record = db.newRecord(NOTE);
        n_record.setDescription(input.getDescription());
        n_record.setVendorId(vendorRecord.getId());

        Timestamp timestamp = StringUtils.timestampFromString(input.getTimestamp().toString(), "yyyy'-'mm'-'dd'T'hhmmss");
        n_record.setTimestamp(timestamp);
        n_record.store();

        input.setId(n_record.getId().toString());
      }
    }
  }
}
