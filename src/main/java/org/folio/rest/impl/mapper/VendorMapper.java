package org.folio.rest.impl.mapper;

import org.folio.rest.jaxrs.model.*;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import storage.model.tables.records.*;

import java.util.ArrayList;
import java.util.List;

public class VendorMapper extends Mapping<Vendor, VendorRecord> {

  public static VendorMapper newInstance(DSLContext ctx) {
    return new VendorMapper(ctx);
  }

  VendorMapper(DSLContext ctx) {
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

    return result;
  }

  private Vendor mapVendorRecordForOutput(VendorRecord source) {
    Vendor result = new Vendor();

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
//      String timestamp = StringUtils.stringFromTimestamp(each.getTimestamp(), "yyyy'-'mm'-'dd'T'hhmmss");
        element.setTimestamp(each.getTimestamp());

        results.add(element);
      }
    } catch (DataAccessException e) {
      // DO NOTHING
    }
    return results;
  }
}
