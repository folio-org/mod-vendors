package org.folio.rest.utils;

import org.folio.rest.jaxrs.model.Contact;
import org.folio.rest.jaxrs.model.Vendor;

public enum TestEntities {
  CONTACT("/vendor-storage/contacts", Contact.class, "contact.sample",  "notes", "ABC123", 13),
  VENDOR("/vendor-storage/vendors", Vendor.class, "vendor.sample",  "code", "ABC123", 13);


  TestEntities(String endpoint, Class<?> clazz, String sampleFileName, String updatedFieldName, String updatedFieldValue, int initialQuantity) {
    this.endpoint = endpoint;
    this.clazz = clazz;
    this.sampleFileName = sampleFileName;
    this.updatedFieldName = updatedFieldName;
    this.updatedFieldValue = updatedFieldValue;
    this.initialQuantity = initialQuantity;
  }

  private int initialQuantity;
  private String endpoint;
  private String sampleFileName;
  private String updatedFieldName;
  private String updatedFieldValue;
  private Class<?> clazz;

  public String getEndpoint() {
    return endpoint;
  }

  public String getEndpointWithId() {
    return endpoint + "/{id}";
  }

  public String getSampleFileName() {
    return sampleFileName;
  }

  public String getUpdatedFieldName() {
    return updatedFieldName;
  }

  public String getUpdatedFieldValue() {
    return updatedFieldValue;
  }

  public int getInitialQuantity() {
    return initialQuantity;
  }

  public Class<?> getClazz() {
    return clazz;
  }
}
