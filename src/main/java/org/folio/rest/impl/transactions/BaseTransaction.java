package org.folio.rest.impl.transactions;

import storage.model.tables.LibraryVendorAcct;
import storage.model.tables.VendorContact;

public abstract class BaseTransaction<T> {
  storage.model.tables.EdiInfo EDI_INFO = storage.model.tables.EdiInfo.EDI_INFO;
  storage.model.tables.Vendor VENDOR = storage.model.tables.Vendor.VENDOR;
  storage.model.tables.VendorName VENDOR_NAME = storage.model.tables.VendorName.VENDOR_NAME;
  storage.model.tables.VendorCurrency VENDOR_CURRENCY = storage.model.tables.VendorCurrency.VENDOR_CURRENCY;
  storage.model.tables.VendorInterface VENDOR_INTERFACE = storage.model.tables.VendorInterface.VENDOR_INTERFACE;
  storage.model.tables.Agreement AGREEMENT = storage.model.tables.Agreement.AGREEMENT;
  storage.model.tables.LibraryVendorAcct LIBRARY_VENDOR_ACCT = LibraryVendorAcct.LIBRARY_VENDOR_ACCT;
  storage.model.tables.Job JOB = storage.model.tables.Job.JOB;
  storage.model.tables.Address ADDRESS = storage.model.tables.Address.ADDRESS;
  storage.model.tables.PhoneNumber PHONE_NUMBER = storage.model.tables.PhoneNumber.PHONE_NUMBER;
  storage.model.tables.Email EMAIL = storage.model.tables.Email.EMAIL;
  storage.model.tables.VendorContact VENDOR_CONTACT = VendorContact.VENDOR_CONTACT;
  storage.model.tables.Note NOTE= storage.model.tables.Note.NOTE;

  public abstract void execute(TransactionCompletionHandler<T> completionHandler);

}
