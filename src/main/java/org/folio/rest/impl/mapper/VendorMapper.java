package org.folio.rest.impl.mapper;

import org.folio.rest.impl.utils.StringUtils;
import org.folio.rest.jaxrs.model.*;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import storage.model.tables.records.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VendorMapper extends Mapping<Vendor, VendorRecord> {

  public static VendorMapper newInstance(DSLContext ctx) {
    return new VendorMapper(ctx);
  }

  private VendorMapper(DSLContext ctx) {
    this.db = ctx;
  }

  /**
   * @param apiModel model object that represents an API's JSON schema
   * @param dbRecord model object that represents a table in the DB
   */
  @Override
  public void mapEntityToDBRecord(Vendor apiModel, VendorRecord dbRecord) {

  }

  /**
   * @param dbRecord model object that represents a table in the DB
   * @return model object that represents an API's JSON schema
   */
  @Override
  public Vendor mapDBRecordToEntity(VendorRecord dbRecord) {
    Vendor result = mapVendorRecordForOutput(dbRecord);

    EdiInfo ediInfo = mapEdiInfoRecordForOutput(dbRecord);
    result.setEdiInfo(ediInfo);

    List<VendorName> vendorNames = mapVendorNameRecordsForOutput(dbRecord);
    result.setVendorNames(vendorNames);

    List<VendorCurrency> vendorCurrencies = mapVendorCurrenciesForOutput(dbRecord);
    result.setVendorCurrencies(vendorCurrencies);

    List<VendorInterface> vendorInterfaces = mapVendorInterfacesForOutput(dbRecord);
    result.setVendorInterfaces(vendorInterfaces);

    List<Agreement> agreements = mapAgreementsForOutput(dbRecord);
    result.setAgreements(agreements);

    List<Account> accounts = mapAccountsForOutput(dbRecord);
    result.setAccounts(accounts);

    Job job = mapJobForOutput(dbRecord);
    result.setJob(job);

    List<Note> notes = mapNotesForOutput(dbRecord);
    result.setNotes(notes);

    List<Address> addresses = mapAddressForOutput(dbRecord);
    result.setAddresses(addresses);

    List<PhoneNumber> phoneNumbers = mapPhoneNumbersForOutput(dbRecord);
    result.setPhoneNumbers(phoneNumbers);

    List<Email> emails = mapEmailsForOutput(dbRecord);
    result.setEmails(emails);

    List<Contact> contacts = mapContactsForOutput(dbRecord);
    result.setContacts(contacts);

    return result;
  }

  private Vendor mapVendorRecordForOutput(VendorRecord source) {
    Vendor result = new Vendor();

    result.setId(source.getId().toString());
    result.setName(source.getName());
    result.setDescription(source.getDescription());
    result.setCode(source.getCode());
    result.setVendorStatus(source.getVendorStatus());
    result.setLanguage(source.getLanguage());
    result.setErpCode(source.getErpCode());
    result.setPaymentMethod(source.getPaymentMethod());
    result.setAccessProvider(source.getAccessProvider());
    result.setGovernmental(source.getGovernmental());
    result.setLicensor(source.getLicensor());
    result.setMaterialSupplier(source.getMaterialSupplier());
    result.setClaimingInterval(source.getClaimingInterval());
    result.setDiscountPercent(source.getDiscountPercent().doubleValue());
    result.setExpectedActivationInterval(source.getExpectedActivationInterval());
    result.setExpectedInvoiceInterval(source.getExpectedInvoiceInterval());
    result.setRenewalActivationInterval(source.getExpectedActivationInterval());
    result.setSubscriptionInterval(source.getSubscriptionInterval());
    result.setLiableForVat(source.getLiableForVat());
    result.setTaxId(source.getTaxId());
    result.setTaxPercentage(source.getTaxPercentage().doubleValue());

    return result;
  }

  private EdiInfo mapEdiInfoRecordForOutput(VendorRecord vendorRecord) {
    try {
      EdiInfoRecord source = db.selectFrom(EDI_INFO).where(EDI_INFO.ID.equal(vendorRecord.getEdiInfoId())).fetchOne();
      if (source == null) {
        return null;
      }

      EdiInfo result = new EdiInfo();
      result.setVendorEdiCode(source.getVendorEdiCode());
      result.setVendorEdiType(source.getVendorEdiType());
      result.setLibEdiCode(source.getLibEdiCode());
      result.setLibEdiType(source.getLibEdiType());
      result.setEdiNamingConvention(source.getEdiNamingConvention());
      result.setProrateTax(source.getProrateTax());
      result.setProrateFees(source.getProrateFees());
      result.setSendAcctNum(source.getSendAcctNum());
      result.setSupportOrder(source.getSupportOrder());
      result.setSupportInvoice(source.getSupportInvoice());
      result.setNotes(source.getNotes());
      result.setFtpFormat(source.getFtpFormat());
      result.setFtpMode(source.getFtpMode());
      result.setFtpConnMode(source.getFtpConnMode());
      result.setFtpPort(source.getFtpPort());
      result.setServerAddress(source.getServerAddress());
      result.setUsername(source.getUsername());
      result.setPassword(source.getPassword());
      result.setOrderDirectory(source.getOrderDirectory());
      result.setInvoiceDirectory(source.getInvoiceDirectory());
      result.setSendToEmails(source.getSendToEmails());
      result.setNotifyAllEdi(source.getNotifyAllEdi());
      result.setNotifyInvoiceOnly(source.getNotifyInvoiceOnly());
      result.setNotifyErrorOnly(source.getNotifyErrorOnly());
      return result;
    }
    catch (DataAccessException e) {
      return null;
    }
  }

  private List<VendorName> mapVendorNameRecordsForOutput(VendorRecord vendorRecord) {
    List<VendorName> results = new ArrayList<>();
    try {
      VendorNameRecord[] source = db.selectFrom(VENDOR_NAME).where(VENDOR_NAME.VENDOR_ID.eq(vendorRecord.getId())).fetchArray();

      for (VendorNameRecord each: source) {
        VendorName element = new VendorName();
        element.setId(each.getId().toString());
        element.setValue(each.getValue());
        element.setDescription(each.getDescription());
        results.add(element);
      }
    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<VendorCurrency> mapVendorCurrenciesForOutput(VendorRecord dbRecord) {
    List<VendorCurrency> results = new ArrayList<>();
    try {
      VendorCurrencyRecord[] source = db.selectFrom(VENDOR_CURRENCY).where(VENDOR_CURRENCY.VENDOR_ID.eq(dbRecord.getId())).fetchArray();

      for (VendorCurrencyRecord each: source) {
        VendorCurrency element = new VendorCurrency();
        element.setId(each.getId().toString());
        element.setCurrency(each.getCurrency());
        results.add(element);
      }
    }
    catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<VendorInterface> mapVendorInterfacesForOutput(VendorRecord dbRecord) {
    List<VendorInterface> results = new ArrayList<>();
    try {
      VendorInterfaceRecord[] source = db.selectFrom(VENDOR_INTERFACE).where(VENDOR_INTERFACE.VENDOR_ID.eq(dbRecord.getId())).fetchArray();
      for (VendorInterfaceRecord each: source) {
        VendorInterface element = new VendorInterface();
        element.setId(each.getId().toString());
        element.setName(each.getName());
        element.setUri(each.getUri());
        element.setUsername(each.getUsername());
        element.setPassword(each.getPassword());
        element.setNotes(each.getNotes());
        element.setAvailable(each.getAvailable());
        element.setDeliveryMethod(each.getDeliveryMethod());
        element.setStatisticsFormat(each.getStatisticsFormat());
        element.setLocallyStored(each.getLocallyStored());
        element.setOnlineLocation(each.getOnlineLocation());
        element.setStatisticsNotes(each.getStatisticsNotes());
        results.add(element);
      }
    }
    catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<Agreement> mapAgreementsForOutput(VendorRecord dbRecord) {
    AgreementRecord[] source = db.selectFrom(AGREEMENT).where(AGREEMENT.VENDOR_ID.eq(dbRecord.getId())).fetchArray();
    List<Agreement> results = new ArrayList<>();
    try {
      for (AgreementRecord each: source) {
        Agreement element = new Agreement();
        element.setId(each.getId().toString());
        element.setDiscount(each.getDiscount().doubleValue());
        element.setName(each.getName());
        element.setNotes(each.getNotes());
        element.setReferenceUrl(each.getReferenceUrl());
        results.add(element);
      }
    }
    catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<Account> mapAccountsForOutput(VendorRecord dbRecord) {
    List<Account> results = new ArrayList<>();
    try {
      LibraryVendorAcctRecord[] source = db.selectFrom(LIBRARY_VENDOR_ACCT).where(LIBRARY_VENDOR_ACCT.VENDOR_ID.eq(dbRecord.getId())).fetchArray();
      for (LibraryVendorAcctRecord each: source) {
        Account element = new Account();
        element.setId(each.getId().toString());
        element.setName(each.getName());
        element.setPaymentMethod(each.getPaymentMethod());
        element.setAccountNo(each.getAccountNo());
        element.setLibVenAcctStatus(each.getLibVenAcctStatus());
        element.setDescription(each.getDescription());
        element.setContactInfo(each.getContactInfo());
        element.setAppsystemNo(each.getApsystemNo());
        element.setNotes(each.getNotes());
        element.setLibraryCode(each.getLibraryCode());
        element.setLibraryEdiCode(each.getLibraryEdiCode());
        results.add(element);
      }
    }
    catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private Job mapJobForOutput(VendorRecord dbRecord) {
    try {
      JobRecord source = db.selectFrom(JOB).where(JOB.VENDOR_ID.eq(dbRecord.getId())).fetchOne();
      if (source == null) {
        return null;
      }
      Job result = new Job();
      result.setIsScheduled(source.getIsScheduled());
      result.setIsMonday(source.getIsMonday());
      result.setIsTuesday(source.getIsTuesday());
      result.setIsWednesday(source.getIsWednesday());
      result.setIsThursday(source.getIsThursday());
      result.setIsFriday(source.getIsFriday());
      result.setIsSaturday(source.getIsSaturday());
      result.setIsSunday(source.getIsSunday());
      result.setSchedulingNotes(source.getSchedulingNotes());
      String startDate = StringUtils.stringFromTimestamp(source.getStartDate(), "yyyy'-'mm'-'dd'T'hh:mm:ssX");
      result.setStartDate(startDate);
      String time = StringUtils.stringFromTime(source.getTime(), "hh:mm:ssX");
      result.setTime(time);
      return result;
    }
    catch (DataAccessException e) {
      return null;
    }
  }

  private List<Note> mapNotesForOutput(VendorRecord dbRecord) {

    List<Note> results = new ArrayList<>();
    try {
      NoteRecord[] source = db.selectFrom(NOTE).where(NOTE.VENDOR_ID.eq(dbRecord.getId())).fetchArray();
      for (NoteRecord each: source) {
        Note element = new Note();
        element.setId(each.getId().toString());
        element.setDescription(each.getDescription());
        String timestamp = StringUtils.stringFromTimestamp(each.getTimestamp(), "yyyy'-'mm'-'dd'T'hh:mm:ssX");
        element.setTimestamp(timestamp);

        results.add(element);
      }
    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<Address> mapAddressForOutput(VendorRecord dbRecord) {
    List<Address> results = new ArrayList<>();
    try {
      VendorAddressRecord[] source = db.selectFrom(VENDOR_ADDRESS)
        .where(VENDOR_ADDRESS.VENDOR_ID.eq(dbRecord.getId()))
        .fetchArray();

      if (source.length == 0) {
        return results;
      }

      // Loop through all the associated VendorAddressRecords
      for (VendorAddressRecord each: source) {
        Address element = new Address();
        element.setId(each.getId().toString());

        // TODO: THIS SECTION WILL CHANGE WHEN THE CONTACTS MODULE IS DECOUPLED
        // Retrieve the actual address information as it lives in another table
        UUID add_uuid = UUID.fromString(each.getAddress());
        AddressRecord add_source = db.selectFrom(ADDRESS).where(ADDRESS.ID.eq(add_uuid)).fetchOne();
        Address_ sub_address = new Address_();
        sub_address.setId(add_source.getId().toString());
        sub_address.setAddressLine1(add_source.getAddressLine_1());
        sub_address.setAddressLine2(add_source.getAddressLine_2());
        sub_address.setCity(add_source.getCity());
        sub_address.setRegion(add_source.getRegion());
        sub_address.setPostalCode(add_source.getPostalCode());
        sub_address.setCountry(add_source.getCountry());
        element.setAddress(sub_address);
        // ------

        // Retrieve all the category records associated with the VendorAddressRecord
        Record[] category_source = db.select()
          .from(VENDOR_ADDRESS)
          .join(VENDOR_ADDRESS_CATEGORY).on(VENDOR_ADDRESS_CATEGORY.VENDOR_ADDRESS_ID.eq(each.getId()))
          .join(CATEGORY).on(VENDOR_ADDRESS_CATEGORY.CATEGORY_ID.eq(CATEGORY.ID))
          .where(VENDOR_ADDRESS.ID.eq(each.getId()))
          .fetchArray();

        List<Category> categoriesForVendor = new ArrayList<>();
        for (Record each_cat_source: category_source) {
          CategoryRecord cat_source = each_cat_source.into(CATEGORY);

          Category associatedCategory = new Category();
          associatedCategory.setId(cat_source.getId().toString());
          associatedCategory.setValue(cat_source.getValue());
          categoriesForVendor.add(associatedCategory);
        }
        element.setCategories(categoriesForVendor);
        element.setLanguage(each.getLanguage());
        element.setSanCode(each.getSanCode());

        results.add(element);
      }

    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<PhoneNumber> mapPhoneNumbersForOutput(VendorRecord dbRecord) {
    List<PhoneNumber> results = new ArrayList<>();
    try {
      VendorPhoneRecord[] source = db.selectFrom(VENDOR_PHONE)
        .where(VENDOR_PHONE.VENDOR_ID.eq(dbRecord.getId()))
        .fetchArray();

      if (source.length == 0) {
        return results;
      }

      // Loop through all the associated VendorAddressRecords
      for (VendorPhoneRecord each: source) {
        PhoneNumber element = new PhoneNumber();
        element.setId(each.getId().toString());

        // TODO: THIS SECTION WILL CHANGE WHEN THE CONTACTS MODULE IS DECOUPLED
        // Retrieve the actual phone number information as it lives in another table
        UUID add_uuid = UUID.fromString(each.getPhoneNumber());
        PhoneNumberRecord add_source = db.selectFrom(PHONE_NUMBER).where(PHONE_NUMBER.ID.eq(add_uuid)).fetchOne();
        PhoneNumber_ sub_content = new PhoneNumber_();
        sub_content.setId(add_source.getId().toString());
        sub_content.setAreaCode(add_source.getAreaCode());
        sub_content.setCountryCode(add_source.getCountryCode());
        sub_content.setPhoneNumber(add_source.getPhoneNumber());
        element.setPhoneNumber(sub_content);
        // ------

        // Retrieve all the category records associated with the VendorAddressRecord
        Record[] category_source = db.select()
          .from(VENDOR_PHONE)
          .join(VENDOR_PHONE_CATEGORY).on(VENDOR_PHONE_CATEGORY.VENDOR_PHONE_ID.eq(each.getId()))
          .join(CATEGORY).on(VENDOR_PHONE_CATEGORY.CATEGORY_ID.eq(CATEGORY.ID))
          .where(VENDOR_PHONE.ID.eq(each.getId()))
          .fetchArray();

        List<Category> categoriesForVendor = new ArrayList<>();
        for (Record each_cat_source: category_source) {
          CategoryRecord cat_source = each_cat_source.into(CATEGORY);

          Category associatedCategory = new Category();
          associatedCategory.setId(cat_source.getId().toString());
          associatedCategory.setValue(cat_source.getValue());
          categoriesForVendor.add(associatedCategory);
        }
        element.setCategories(categoriesForVendor);
        element.setLanguage(each.getLanguage());

        results.add(element);
      }

    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }

  private List<Email> mapEmailsForOutput(VendorRecord dbRecord) {
    List<Email> results = new ArrayList<>();
    try {
      VendorEmailRecord[] source = db.selectFrom(VENDOR_EMAIL)
        .where(VENDOR_EMAIL.VENDOR_ID.eq(dbRecord.getId()))
        .fetchArray();

      if (source.length == 0) {
        return results;
      }

      // Loop through all the associated VendorAddressRecords
      for (VendorEmailRecord each: source) {
        Email element = new Email();
        element.setId(each.getId().toString());

        // TODO: THIS SECTION WILL CHANGE WHEN THE CONTACTS MODULE IS DECOUPLED
        // Retrieve the actual email information as it lives in another table
        UUID add_uuid = UUID.fromString(each.getEmail());
        EmailRecord add_source = db.selectFrom(EMAIL).where(EMAIL.ID.eq(add_uuid)).fetchOne();
        Email_ sub_content = new Email_();
        sub_content.setId(add_source.getId().toString());
        sub_content.setValue(add_source.getValue());
        element.setEmail(sub_content);
        // ------

        // Retrieve all the category records associated with the VendorAddressRecord
        Record[] category_source = db.select()
          .from(VENDOR_EMAIL)
          .join(VENDOR_EMAIL_CATEGORY).on(VENDOR_EMAIL_CATEGORY.VENDOR_EMAIL_ID.eq(each.getId()))
          .join(CATEGORY).on(VENDOR_EMAIL_CATEGORY.CATEGORY_ID.eq(CATEGORY.ID))
          .where(VENDOR_EMAIL.ID.eq(each.getId()))
          .fetchArray();

        List<Category> categoriesForVendor = new ArrayList<>();
        for (Record each_cat_source: category_source) {
          CategoryRecord cat_source = each_cat_source.into(CATEGORY);

          Category associatedCategory = new Category();
          associatedCategory.setId(cat_source.getId().toString());
          associatedCategory.setValue(cat_source.getValue());
          categoriesForVendor.add(associatedCategory);
        }
        element.setCategories(categoriesForVendor);
        element.setLanguage(each.getLanguage());

        results.add(element);
      }

    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }


  private List<Contact> mapContactsForOutput(VendorRecord dbRecord) {
    List<Contact> results = new ArrayList<>();
    try {
      VendorContactRecord[] source = db.selectFrom(VENDOR_CONTACT)
        .where(VENDOR_CONTACT.VENDOR_ID.eq(dbRecord.getId()))
        .fetchArray();

      if (source.length == 0) {
        return results;
      }

      // Loop through all the associated VendorAddressRecords
      for (VendorContactRecord each: source) {
        Contact element = new Contact();
        element.setId(each.getId().toString());

        // TODO: THIS SECTION WILL CHANGE WHEN THE CONTACTS MODULE IS DECOUPLED
        // Retrieve the actual address information as it lives in another table
        UUID person_uuid = UUID.fromString(each.getContactPersonId());

        Record record_source = db.select()
          .from(VENDOR_CONTACT)
          .join(PERSON).on(PERSON.ID.eq(person_uuid))
          .join(ADDRESS).on(PERSON.ADDRESS_ID.eq(ADDRESS.ID))
          .join(EMAIL).on(PERSON.EMAIL_ID.eq(EMAIL.ID))
          .join(PHONE_NUMBER).on(PHONE_NUMBER.ID.eq(PHONE_NUMBER.ID))
          .where(VENDOR_CONTACT.ID.eq(each.getId()))
          .fetchAny();

        PersonRecord person_source = record_source.into(PERSON);
        AddressRecord address_source = record_source.into(ADDRESS);
        PhoneNumberRecord phone_source = record_source.into(PHONE_NUMBER);
        EmailRecord email_source = record_source.into(EMAIL);

        Address_ ser_address = new Address_();
        ser_address.setId(address_source.getId().toString());
        ser_address.setAddressLine1(address_source.getAddressLine_1());
        ser_address.setAddressLine2(address_source.getAddressLine_2());
        ser_address.setCity(address_source.getCity());
        ser_address.setRegion(address_source.getRegion());
        ser_address.setPostalCode(address_source.getPostalCode());
        ser_address.setCountry(address_source.getCountry());

        PhoneNumber_ ser_phone_number = new PhoneNumber_();
        ser_phone_number.setId(phone_source.getId().toString());
        ser_phone_number.setAreaCode(phone_source.getAreaCode());
        ser_phone_number.setCountryCode(phone_source.getCountryCode());
        ser_phone_number.setPhoneNumber(phone_source.getPhoneNumber());

        Email_ ser_email = new Email_();
        ser_email.setId(email_source.getId().toString());
        ser_email.setValue(email_source.getValue());

        ContactPerson ser_contact = new ContactPerson();
        ser_contact.setId(person_source.getId().toString());
        ser_contact.setPrefix(person_source.getPrefix());
        ser_contact.setFirstName(person_source.getFirstName());
        ser_contact.setLastName(person_source.getLastName());
        ser_contact.setLanguage(person_source.getLanguage());
        ser_contact.setNotes(person_source.getNotes());
        ser_contact.setAddress(ser_address);
        ser_contact.setEmail(ser_email);
        ser_contact.setPhoneNumber(ser_phone_number);

        element.setContactPerson(ser_contact);
        // ------

        // Retrieve all the category records associated with the VendorAddressRecord
        Record[] category_source = db.select()
          .from(VENDOR_CONTACT)
          .join(VENDOR_CONTACT_CATEGORY).on(VENDOR_CONTACT_CATEGORY.VENDOR_CONTACT_ID.eq(each.getId()))
          .join(CONTACT_CATEGORY).on(VENDOR_CONTACT_CATEGORY.CATEGORY_ID.eq(CONTACT_CATEGORY.ID))
          .where(VENDOR_CONTACT.ID.eq(each.getId()))
          .fetchArray();

        List<Category_> categoriesForVendor = new ArrayList<>();
        for (Record each_cat_source: category_source) {
          ContactCategoryRecord cat_source = each_cat_source.into(CONTACT_CATEGORY);

          Category_ associatedCategory = new Category_();
          associatedCategory.setId(cat_source.getId().toString());
          associatedCategory.setValue(cat_source.getValue());
          categoriesForVendor.add(associatedCategory);
        }
        element.setCategories(categoriesForVendor);
        element.setLanguage(each.getLanguage());

        results.add(element);
      }

    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }
}
