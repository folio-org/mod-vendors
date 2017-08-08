package org.folio.rest.impl.mapper;

import jooq.models.tables.records.VendorRecord;
import org.folio.rest.impl.mapper.interfaces.Mapping;
import org.folio.rest.jaxrs.model.Vendor;

import java.math.BigDecimal;


public class VendorMapper extends Mapping<Vendor, VendorRecord> {
  @Override
  public void mapEntityToDBRecord(Vendor apiModel, VendorRecord dbRecord) {
    // Do not map the ID field since it's an auto-incrementing key
    // dbRecord.setId(apiModel.getId());

    dbRecord.setAccessProvider(apiModel.getAccessProvider());
    dbRecord.setClaimingInterval(apiModel.getClaimingInterval());
    dbRecord.setCode(apiModel.getCode());

    BigDecimal discount = new BigDecimal(apiModel.getDiscountPercent());
    dbRecord.setDiscountPercent(discount);
    dbRecord.setExpectedActivationInterval(apiModel.getExpectedActivationInterval());
    dbRecord.setExpectedInvoiceInterval(apiModel.getExpectedInvoiceInterval());
    dbRecord.setFinancialSysCode(apiModel.getFinancialSysCode());
    dbRecord.setGovernmental(apiModel.getGovernmental());
    dbRecord.setLiableForVat(apiModel.getLiableForVat());
    dbRecord.setLicensor(apiModel.getLicensor());
    dbRecord.setMaterialSupplier(apiModel.getMaterialSupplier());
    dbRecord.setName(apiModel.getName());
    dbRecord.setNationalTaxId(apiModel.getNationalTaxId());
    dbRecord.setRenewalActivationInterval(apiModel.getRenewalActivationInterval());
    dbRecord.setSubscriptionInterval(apiModel.getSubscriptionInterval());

    BigDecimal tax = new BigDecimal(apiModel.getTaxPercentage());
    dbRecord.setTaxPercentage(tax);

    // Relationships
    dbRecord.setContactInfoId(apiModel.getContactInfoId());
    dbRecord.setCurrencyId(apiModel.getCurrencyId());
    dbRecord.setInterfaceId(apiModel.getInterfaceId());
    dbRecord.setLanguageId(apiModel.getLanguageId());
    dbRecord.setVendorStatusId(apiModel.getVendorStatusId());
  }

  @Override
  public Vendor mapDBRecordToEntity(VendorRecord dbRecord) {
    Vendor vendor = new Vendor();

    vendor.setId(dbRecord.getId());
    vendor.setAccessProvider(dbRecord.getAccessProvider());
    vendor.setClaimingInterval(dbRecord.getClaimingInterval());
    vendor.setCode(dbRecord.getCode());
    vendor.setDiscountPercent(dbRecord.getDiscountPercent().doubleValue());
    vendor.setExpectedActivationInterval(dbRecord.getExpectedActivationInterval());
    vendor.setExpectedInvoiceInterval(dbRecord.getExpectedInvoiceInterval());
    vendor.setFinancialSysCode(dbRecord.getFinancialSysCode());
    vendor.setGovernmental(dbRecord.getGovernmental());
    vendor.setLiableForVat(dbRecord.getLiableForVat());
    vendor.setLicensor(dbRecord.getLicensor());
    vendor.setMaterialSupplier(dbRecord.getMaterialSupplier());
    vendor.setName(dbRecord.getName());
    vendor.setNationalTaxId(dbRecord.getNationalTaxId());
    vendor.setRenewalActivationInterval(dbRecord.getRenewalActivationInterval());
    vendor.setSubscriptionInterval(dbRecord.getSubscriptionInterval());
    vendor.setTaxPercentage(dbRecord.getTaxPercentage().doubleValue());

    // Relationships
    vendor.setContactInfoId(dbRecord.getContactInfoId());
    vendor.setCurrencyId(dbRecord.getCurrencyId());
    vendor.setInterfaceId(dbRecord.getInterfaceId());
    vendor.setLanguageId(dbRecord.getLanguageId());
    vendor.setVendorStatusId(dbRecord.getVendorStatusId());

    return vendor;
  }
}
