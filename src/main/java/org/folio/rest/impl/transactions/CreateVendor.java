package org.folio.rest.impl.transactions;

import jooq.models.tables.*;
import jooq.models.tables.records.*;
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
import org.folio.rest.jooq.persist.ConnectResultHandler;
import org.folio.rest.jooq.persist.PostgresClient;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class CreateVendor {

  private Vendor entity = null;
  private String tenantId = null;

  public static CreateVendor newInstance (Vendor entity, String tenantId) {
    return new CreateVendor(entity, tenantId);
  }

  private CreateVendor(Vendor entity, String tenantId) {
    this.entity = entity;
    this.tenantId = tenantId;
  }

  public void execute() {
    PostgresClient dbClient = PostgresClient.getInstance(tenantId);
    dbClient.connect(new ConnectResultHandler() {
      @Override
      public void success(DSLContext db) {
        db.transaction(() -> {

          // To build the COMPLETE vendor record, we do the following steps:
          EdiInfoRecord ediInfoRecord = persistEdiInfo(db);
          VendorRecord vendorRecord = persistVendor(db, ediInfoRecord);
          persistVendorNames(db, vendorRecord);
          persistVendorCurrencies(db, vendorRecord);
          persistVendorInterfaces(db, vendorRecord);
          persistVendorAgreements(db, vendorRecord);
          persistVendorJob(db, vendorRecord);
          persistVendorAddresses(db, vendorRecord);
          persistVendorPhoneNumbers(db, vendorRecord);
          persistVendorEmails(db, vendorRecord);
          persistVendorContacts(db, vendorRecord);
          persistNotes(db, vendorRecord);
        });
      }

      @Override
      public void failed(Exception exception) {

      }
    });
  }

  private EdiInfoRecord persistEdiInfo(DSLContext db) {
    // Persist the EDI Info
    EdiInfo ediInfo = entity.getEdiInfo();
    EdiInfoRecord ediInfoRecord = db.newRecord(jooq.models.tables.EdiInfo.EDI_INFO);
    ediInfoRecord.setVendorEdiCode(ediInfo.getVendorEdiCode());
    ediInfoRecord.setVendorEdiType(ediInfo.getVendorEdiType());
    ediInfoRecord.setLibEdiCode(ediInfo.getLibEdiCode());
    ediInfoRecord.setLibEdiType(ediInfo.getLibEdiType());
    ediInfoRecord.setEdiNamingConvention(ediInfo.getEdiNamingConvention());
    ediInfoRecord.setProrateTax(ediInfo.getProrateTax());
    ediInfoRecord.setProrateFees(ediInfo.getProrateFees());
    ediInfoRecord.setSendAcctNum(ediInfo.getSendAcctNum());
    ediInfoRecord.setSupportOrder(ediInfo.getSupportOrder());
    ediInfoRecord.setSupportInvoice(ediInfo.getSupportInvoice());
    ediInfoRecord.setNotes(ediInfo.getNotes());
    ediInfoRecord.setFtpFormat(ediInfo.getFtpFormat());
    ediInfoRecord.setFtpMode(ediInfo.getFtpMode());
    ediInfoRecord.setFtpConnMode(ediInfo.getFtpConnMode());
    ediInfoRecord.setFtpPort(ediInfo.getFtpPort());
    ediInfoRecord.setServerAddress(ediInfo.getServerAddress());
    ediInfoRecord.setUsername(ediInfo.getUsername());
    ediInfoRecord.setPassword(ediInfo.getPassword());
    ediInfoRecord.setOrderDirectory(ediInfo.getOrderDirectory());
    ediInfoRecord.setInvoiceDirectory(ediInfo.getInvoiceDirectory());
    ediInfoRecord.setSendToEmails(ediInfo.getSendToEmails());
    ediInfoRecord.setNotifyAllEdi(ediInfo.getNotifyAllEdi());
    ediInfoRecord.setNotifyInvoiceOnly(ediInfo.getNotifyInvoiceOnly());
    ediInfoRecord.setNotifyErrorOnly(ediInfo.getNotifyErrorOnly());
    ediInfoRecord.store();
    return ediInfoRecord;
  }

  private VendorRecord persistVendor(DSLContext db, EdiInfoRecord ediInfoRecord) {
    // Persist the vendor metadata
    VendorRecord vendorRecord = db.newRecord(jooq.models.tables.Vendor.VENDOR);
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
    vendorRecord.setEdiInfoId(ediInfoRecord.getId());
    vendorRecord.store();
    return vendorRecord;
  }

  private void persistVendorNames(DSLContext db, VendorRecord vendorRecord) {
    // Persist the vendor names
    List<VendorName> vendorNames = entity.getVendorNames();
    for (VendorName each: vendorNames) {
      VendorNameRecord vendorNameRecord = db.newRecord(jooq.models.tables.VendorName.VENDOR_NAME);
      vendorNameRecord.setValue(each.getValue());
      vendorNameRecord.setDescription(each.getDescription());
      vendorNameRecord.setVendorId(vendorRecord.getId());
      vendorNameRecord.store();
    }
  }

  private void persistVendorCurrencies(DSLContext db, VendorRecord vendorRecord) {
    List<VendorCurrency> vendorCurrencies = entity.getVendorCurrencies();
    for (VendorCurrency each: vendorCurrencies) {
      VendorCurrencyRecord vendorCurrencyRecord = db.newRecord(jooq.models.tables.VendorCurrency.VENDOR_CURRENCY);
      vendorCurrencyRecord.setCurrency(each.getCurrency());
      vendorCurrencyRecord.setVendorId(vendorRecord.getId());
      vendorCurrencyRecord.store();
    }
  }

  private void persistVendorInterfaces(DSLContext db, VendorRecord vendorRecord) {
    List<VendorInterface> vendorInterface = entity.getVendorInterfaces();
    for (VendorInterface each: vendorInterface) {
      VendorInterfaceRecord vendorInterfaceRecord = db.newRecord(jooq.models.tables.VendorInterface.VENDOR_INTERFACE);
      vendorInterfaceRecord.setName(each.getName());
      vendorInterfaceRecord.setUri(each.getUri());
      vendorInterfaceRecord.setUsername(each.getUsername());
      vendorInterfaceRecord.setPassword(each.getPassword());
      vendorInterfaceRecord.setNotes(each.getNotes());
      vendorInterfaceRecord.setAvailable(each.getAvailable());
      vendorInterfaceRecord.setDeliveryMethod(each.getDeliveryMethod());
      vendorInterfaceRecord.setStatisticsFormat(each.getStatisticsFormat());
      vendorInterfaceRecord.setLocallyStored(each.getLocallyStored());
      vendorInterfaceRecord.setOnlineLocation(each.getOnlineLocation());
      vendorInterfaceRecord.setStatisticsNotes(each.getStatisticsNotes());
      vendorInterfaceRecord.store();
    }
  }

  private void persistVendorAgreements(DSLContext db, VendorRecord vendorRecord) {
    List<Agreement> agreements = entity.getAgreements();
    for (Agreement each: agreements) {
      AgreementRecord agreementRecord = db.newRecord(jooq.models.tables.Agreement.AGREEMENT);
      BigDecimal agreementDiscount = new BigDecimal(entity.getDiscountPercent());
      agreementRecord.setDiscount(agreementDiscount);
      agreementRecord.setName(each.getName());
      agreementRecord.setNotes(each.getNotes());
      agreementRecord.setReferenceUrl(each.getReferenceUrl());
      agreementRecord.store();
    }
  }

  private void persistVendorAccounts(DSLContext db, VendorRecord vendorRecord) {
    List<Account> accounts = entity.getAccounts();
    for (Account each: accounts) {
      LibraryVendorAcctRecord libraryVendorAcctRecord = db.newRecord(LibraryVendorAcct.LIBRARY_VENDOR_ACCT);
      libraryVendorAcctRecord.setName(each.getName());
      libraryVendorAcctRecord.setPaymentMethod(each.getPaymentMethod());
      libraryVendorAcctRecord.setAccountNo(each.getAccountNo());
      libraryVendorAcctRecord.setLibVenAcctStatus(each.getLibVenAcctStatus());
      libraryVendorAcctRecord.setDescription(each.getDescription());
      libraryVendorAcctRecord.setContactInfo(each.getContactInfo());
      libraryVendorAcctRecord.setApsystemNo(each.getAppsystemNo());
      libraryVendorAcctRecord.setNotes(each.getNotes());
      libraryVendorAcctRecord.setLibraryCode(each.getLibraryCode());
      libraryVendorAcctRecord.setLibraryEdiCode(each.getLibraryEdiCode());
      libraryVendorAcctRecord.setVendorId(vendorRecord.getId());
      libraryVendorAcctRecord.store();
    }
  }

  private void persistVendorJob(DSLContext db, VendorRecord vendorRecord) {
    Job job = entity.getJob();
    JobRecord jobRecord = db.newRecord(jooq.models.tables.Job.JOB);
    jobRecord.setIsScheduled(job.getIsScheduled());

    // TODO: Process the date data properly
//          jobRecord.setStartDate(job.getStartDate());
    jobRecord.setIsMonday(job.getIsMonday());
    jobRecord.setIsTuesday(job.getIsTuesday());
    jobRecord.setIsWednesday(job.getIsWednesday());
    jobRecord.setIsThursday(job.getIsThursday());
    jobRecord.setIsFriday(job.getIsFriday());
    jobRecord.setIsSaturday(job.getIsSaturday());
    jobRecord.setIsSunday(job.getIsSunday());
    jobRecord.setSchedulingNotes(job.getSchedulingNotes());
    jobRecord.store();
  }

  private void persistVendorAddresses(DSLContext db, VendorRecord vendorRecord) {
    List<Address> addresses = entity.getAddresses();
    for (Address each: addresses) {
      /**
       * TODO: Move this over to the Contacts module
       */
      AddressRecord addressRecord = db.newRecord(jooq.models.tables.Address.ADDRESS);
      addressRecord.setAddressLine_1(each.getAddress().getAddressLine1());
      addressRecord.setAddressLine_2(each.getAddress().getAddressLine2());
      addressRecord.setCity(each.getAddress().getCity());
      addressRecord.setRegion(each.getAddress().getRegion());
      addressRecord.setPostalCode(each.getAddress().getPostalCode());
      addressRecord.setCountry(each.getAddress().getCountry());
      addressRecord.store();
      // ----

      VendorAddressRecord vendorAddressRecord = db.newRecord(VendorAddress.VENDOR_ADDRESS);
      vendorAddressRecord.setLanguage(each.getLanguage());
      vendorAddressRecord.setSanCode(each.getSanCode());
      vendorAddressRecord.setVendorId(vendorRecord.getId());
      vendorAddressRecord.setAddress(addressRecord.getId().toString());
      vendorAddressRecord.store();

      List<Category> categories = each.getCategories();
      for (Category eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        VendorAddressCategory VENDOR_ADDRESS_CATEGORY = VendorAddressCategory.VENDOR_ADDRESS_CATEGORY;
        DSL.insertInto(VENDOR_ADDRESS_CATEGORY, VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID, VENDOR_ADDRESS_CATEGORY.CATEGORY_ID)
          .values(vendorAddressRecord.getId(), categoryID)
          .execute();
      }
    }
  }

  private void persistVendorPhoneNumbers(DSLContext db, VendorRecord vendorRecord) {
    List<PhoneNumber> phoneNumbers = entity.getPhoneNumbers();
    for (PhoneNumber each: phoneNumbers) {
      PhoneNumberRecord phoneNumberRecord = db.newRecord(jooq.models.tables.PhoneNumber.PHONE_NUMBER);
      phoneNumberRecord.setCountryCode(each.getPhoneNumber().getCountryCode());
      phoneNumberRecord.setAreaCode(each.getPhoneNumber().getAreaCode());
      phoneNumberRecord.setPhoneNumber(each.getPhoneNumber().getPhoneNumber());
      phoneNumberRecord.store();

      /**
       * TODO: Move this over to the Contacts module
       */
      VendorPhoneRecord vendorPhoneRecord = db.newRecord(VendorPhone.VENDOR_PHONE);
      vendorPhoneRecord.setVendorId(vendorRecord.getId());
      vendorPhoneRecord.setLanguage(each.getLanguage());
      vendorPhoneRecord.setPhoneNumber(phoneNumberRecord.getId().toString());
      vendorPhoneRecord.store();
      // -----

      List<Category> categories = each.getCategories();
      for (Category eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        VendorPhoneCategory VENDOR_PHONE_CATEGORY = VendorPhoneCategory.VENDOR_PHONE_CATEGORY;
        DSL.insertInto(VENDOR_PHONE_CATEGORY, VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID, VENDOR_PHONE_CATEGORY.CATEGORY_ID)
          .values(vendorPhoneRecord.getId(), categoryID)
          .execute();
      }
    }
  }

  private void persistVendorEmails(DSLContext db, VendorRecord vendorRecord) {
    List<Email> emails = entity.getEmails();
    for (Email each: emails) {
      EmailRecord emailRecord = db.newRecord(jooq.models.tables.Email.EMAIL);
      emailRecord.setValue(each.getEmail().getValue());
      emailRecord.store();

      /**
       * TODO: Move this over to the Contacts module
       */
      VendorEmailRecord vendorEmailRecord = db.newRecord(VendorEmail.VENDOR_EMAIL);
      vendorEmailRecord.setVendorId(vendorRecord.getId());
      vendorEmailRecord.setLanguage(each.getLanguage());
      vendorEmailRecord.setEmail(emailRecord.getId().toString());
      vendorEmailRecord.store();
      // -----

      List<Category> categories = each.getCategories();
      for (Category eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        VendorEmailCategory VENDOR_EMAIL_CATEGORY = VendorEmailCategory.VENDOR_EMAIL_CATEGORY;
        DSL.insertInto(VENDOR_EMAIL_CATEGORY, VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID, VENDOR_EMAIL_CATEGORY.CATEGORY_ID)
          .values(vendorEmailRecord.getId(), categoryID)
          .execute();
      }
    }
  }

  private void persistVendorContacts(DSLContext db, VendorRecord vendorRecord) {
    List<Contact> contacts = entity.getContacts();
    for (Contact each: contacts) {
      /**
       * TODO: Move this over to the Contacts module
       */
      PhoneNumberRecord phoneNumberRecord = db.newRecord(jooq.models.tables.PhoneNumber.PHONE_NUMBER);
      phoneNumberRecord.setCountryCode(each.getContactPerson().getPhoneNumber().getCountryCode());
      phoneNumberRecord.setAreaCode(each.getContactPerson().getPhoneNumber().getAreaCode());
      phoneNumberRecord.setPhoneNumber(each.getContactPerson().getPhoneNumber().getPhoneNumber());
      phoneNumberRecord.store();

      EmailRecord emailRecord = db.newRecord(jooq.models.tables.Email.EMAIL);
      emailRecord.setValue(each.getContactPerson().getEmail().getValue());
      emailRecord.store();

      AddressRecord addressRecord = db.newRecord(jooq.models.tables.Address.ADDRESS);
      addressRecord.setAddressLine_1(each.getContactPerson().getAddress().getAddressLine1());
      addressRecord.setAddressLine_2(each.getContactPerson().getAddress().getAddressLine2());
      addressRecord.setCity(each.getContactPerson().getAddress().getCity());
      addressRecord.setRegion(each.getContactPerson().getAddress().getRegion());
      addressRecord.setPostalCode(each.getContactPerson().getAddress().getPostalCode());
      addressRecord.setCountry(each.getContactPerson().getAddress().getCountry());
      addressRecord.store();

      PersonRecord personRecord = db.newRecord(Person.PERSON);
      personRecord.setPrefix(each.getContactPerson().getPrefix());
      personRecord.setFirstName(each.getContactPerson().getFirstName());
      personRecord.setLastName(each.getContactPerson().getLastName());
      personRecord.setLanguage(each.getContactPerson().getLanguage());
      personRecord.setNotes(each.getContactPerson().getNotes());
      personRecord.setPhoneNumberId(phoneNumberRecord.getId());
      personRecord.setEmailId(emailRecord.getId());
      personRecord.setAddressId(addressRecord.getId());
      personRecord.store();
      // -----

      VendorContactRecord vendorContactRecord = db.newRecord(VendorContact.VENDOR_CONTACT);
      vendorContactRecord.setLanguage(each.getLanguage());
      vendorContactRecord.setContactPersonId(personRecord.getId().toString());
      vendorContactRecord.setVendorId(vendorRecord.getId());
      vendorContactRecord.store();

      List<Category_> categories = each.getCategories();
      for (Category_ eachCategory: categories) {
        UUID categoryID = UUID.fromString(eachCategory.getId());

        VendorContactCategory VENDOR_CONTACT_CATEGORY = VendorContactCategory.VENDOR_CONTACT_CATEGORY;
        DSL.insertInto(VENDOR_CONTACT_CATEGORY, VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID, VENDOR_CONTACT_CATEGORY.CATEGORY_ID)
          .values(vendorContactRecord.getId(), categoryID)
          .execute();
      }
    }
  }

  private void persistNotes(DSLContext db, VendorRecord vendorRecord) {
    List<Note> notes = entity.getNotes();
    for (Note each: notes) {
      NoteRecord noteRecord = db.newRecord(jooq.models.tables.Note.NOTE);
      noteRecord.setDescription(each.getDescription());
      noteRecord.setVendorId(vendorRecord.getId());

      // TODO: Need to set User ID
//            noteRecord.setUserId();

      long unixTime = System.currentTimeMillis() / 1000L;
      Timestamp currentTime = new Timestamp(unixTime);
      noteRecord.setTimestamp(currentTime);
      noteRecord.store();
    }
  }
}
