package org.folio.rest.impl.transactions;

import storage.model.tables.*;

public abstract class BaseTransaction<T> {
  storage.model.tables.EdiInfo EDI_INFO = storage.model.tables.EdiInfo.EDI_INFO;
  storage.model.tables.Vendor VENDOR = storage.model.tables.Vendor.VENDOR;
  storage.model.tables.VendorName VENDOR_NAME = storage.model.tables.VendorName.VENDOR_NAME;
  storage.model.tables.VendorCurrency VENDOR_CURRENCY = storage.model.tables.VendorCurrency.VENDOR_CURRENCY;
  storage.model.tables.VendorInterface VENDOR_INTERFACE = storage.model.tables.VendorInterface.VENDOR_INTERFACE;
  storage.model.tables.Agreement AGREEMENT = storage.model.tables.Agreement.AGREEMENT;
  storage.model.tables.LibraryVendorAcct LIBRARY_VENDOR_ACCT = LibraryVendorAcct.LIBRARY_VENDOR_ACCT;
  storage.model.tables.Job JOB = storage.model.tables.Job.JOB;
  storage.model.tables.VendorAddress VENDOR_ADDRESS = VendorAddress.VENDOR_ADDRESS;
  storage.model.tables.VendorAddressCategory VENDOR_ADDRESS_CATEGORY = VendorAddressCategory.VENDOR_ADDRESS_CATEGORY;
  storage.model.tables.Address ADDRESS = storage.model.tables.Address.ADDRESS;
  storage.model.tables.VendorPhone VENDOR_PHONE = VendorPhone.VENDOR_PHONE;
  storage.model.tables.VendorPhoneCategory VENDOR_PHONE_CATEGORY = VendorPhoneCategory.VENDOR_PHONE_CATEGORY;
  storage.model.tables.PhoneNumber PHONE_NUMBER = storage.model.tables.PhoneNumber.PHONE_NUMBER;
  storage.model.tables.VendorEmail VENDOR_EMAIL = VendorEmail.VENDOR_EMAIL;
  storage.model.tables.VendorEmailCategory VENDOR_EMAIL_CATEGORY = VendorEmailCategory.VENDOR_EMAIL_CATEGORY;
  storage.model.tables.Email EMAIL = storage.model.tables.Email.EMAIL;
  storage.model.tables.VendorContact VENDOR_CONTACT = VendorContact.VENDOR_CONTACT;
  storage.model.tables.VendorContactCategory VENDOR_CONTACT_CATEGORY = VendorContactCategory.VENDOR_CONTACT_CATEGORY;
  storage.model.tables.Note NOTE = storage.model.tables.Note.NOTE;
  storage.model.tables.Category CATEGORY = Category.CATEGORY;
  storage.model.tables.ContactCategory CONTACT_CATEGORY = ContactCategory.CONTACT_CATEGORY;
  storage.model.tables.Person PERSON = Person.PERSON;

  public abstract void execute(TransactionCompletionHandler<T> completionHandler);

}
