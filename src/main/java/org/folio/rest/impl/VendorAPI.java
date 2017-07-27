package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import jooq.models.tables.Department;
import jooq.models.tables.Language;
import jooq.models.tables.Person;
import jooq.models.tables.records.DepartmentRecord;
import jooq.models.tables.records.LanguageRecord;
import jooq.models.tables.records.PersonRecord;
import jooq.models.tables.records.VendorRecord;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCollection;
import org.folio.rest.jaxrs.resource.VendorResource;
import org.folio.rest.jaxrs.resource.support.ResponseWrapper;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendorAPI implements VendorResource {
  private String userName = "jbenito";
  private String password = "password";
  private String url = "jdbc:postgresql://localhost:5432/jbenito";


  /**
   * Create a new vendor
   *
   * @param entity             e.g. {
   *                           "id": null,
   *                           "access_provider": false,
   *                           "claiming_interval": 1,
   *                           "code": "ABC-XYZ",
   *                           "discount_percent": 12.5,
   *                           "expected_activation_interval": 1,
   *                           "expected_invoice_interval": 1,
   *                           "financial_sys_code": "FIN-CODE-A12",
   *                           "governmental": false,
   *                           "liable_for_vat": true,
   *                           "licensor": false,
   *                           "material_supplier": true,
   *                           "name": "ABC Global Inc.",
   *                           "national_tax_id": "AB-12-FIN-ID-25",
   *                           "renewal_activation_interval": "",
   *                           "subscription_interval": "MONTHLY",
   *                           "tax_percentage": 0.15,
   *                           "contact_info_id": 1,
   *                           "currency_id": 1,
   *                           "interface_id": 1,
   *                           "language": 1,
   *                           "vendor_status": 1
   *                           }
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void putVendor(Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      ResponseWrapper response;

      try (Connection conn = DriverManager.getConnection(url, userName, password)) {
        // ...
        DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);

        VendorRecord vendor = db.newRecord(jooq.models.tables.Vendor.VENDOR);
        vendor.setAccessProvider(entity.getAccessProvider());
        vendor.setClaimingInterval(entity.getClaimingInterval());
        vendor.setCode(entity.getCode());

        BigDecimal discount = new BigDecimal(entity.getDiscountPercent());
        vendor.setDiscountPercent(discount);
        vendor.setExpectedActivationInterval(entity.getExpectedActivationInterval());
        vendor.setExpectedInvoiceInterval(entity.getExpectedInvoiceInterval());
        vendor.setFinancialSysCode(entity.getFinancialSysCode());
        vendor.setGovernmental(entity.getGovernmental());
        vendor.setLiableForVat(entity.getLiableForVat());
        vendor.setLicensor(entity.getLicensor());
        vendor.setMaterialSupplier(entity.getMaterialSupplier());
        vendor.setName(entity.getName());
        vendor.setNationalTaxId(entity.getNationalTaxId());
        vendor.setRenewalActivationInterval(entity.getRenewalActivationInterval());
        vendor.setSubscriptionInterval(entity.getSubscriptionInterval());

        BigDecimal tax = new BigDecimal(entity.getTaxPercentage());
        vendor.setTaxPercentage(tax);

        // Relationships
        vendor.setContactInfoId(entity.getContactInfoId());
        vendor.setCurrencyId(entity.getCurrencyId());
        vendor.setInterfaceId(entity.getInterfaceId());
        vendor.setLanguageId(entity.getLanguageId());
        vendor.setVendorStatusId(entity.getVendorStatusId());
        vendor.store();

        Vendor vendorDetails = new Vendor();
        vendorDetails.setId(vendor.getId());
        response = PutVendorResponse.withJsonOK(vendorDetails);
      }

      // For the sake of this tutorial, let's keep exception handling simple
      catch (Exception e) {
        response = PutVendorResponse.withBadRequest();

        // TODO: 403 - Forbidden
        // response = PutVendorResponse.withForbidden();
      }

      AsyncResult<Response> result = Future.succeededFuture(response);
      asyncResultHandler.handle(result);
    });
  }

  /**
   * Get list of vendors
   *
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void getVendor(Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      ResponseWrapper response;

      try (Connection conn = DriverManager.getConnection(url, userName, password)) {
        // ...
        DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
        Result<VendorRecord> result = db.selectFrom(jooq.models.tables.Vendor.VENDOR).fetch();

        List<Vendor> vendors = new ArrayList<>();
        for (VendorRecord record: result) {
          Vendor vendor = new Vendor();

          vendor.setId(record.getId());
          vendor.setAccessProvider(record.getAccessProvider());
          vendor.setClaimingInterval(record.getClaimingInterval());
          vendor.setCode(record.getCode());
          vendor.setDiscountPercent(record.getDiscountPercent().doubleValue());
          vendor.setExpectedActivationInterval(record.getExpectedActivationInterval());
          vendor.setExpectedInvoiceInterval(record.getExpectedInvoiceInterval());
          vendor.setFinancialSysCode(record.getFinancialSysCode());
          vendor.setGovernmental(record.getGovernmental());
          vendor.setLiableForVat(record.getLiableForVat());
          vendor.setLicensor(record.getLicensor());
          vendor.setMaterialSupplier(record.getMaterialSupplier());
          vendor.setName(record.getName());
          vendor.setNationalTaxId(record.getNationalTaxId());
          vendor.setRenewalActivationInterval(record.getRenewalActivationInterval());
          vendor.setSubscriptionInterval(record.getSubscriptionInterval());
          vendor.setTaxPercentage(record.getTaxPercentage().doubleValue());

          // Relationships
          vendor.setContactInfoId(record.getContactInfoId());
          vendor.setCurrencyId(record.getCurrencyId());
          vendor.setInterfaceId(record.getInterfaceId());
          vendor.setLanguageId(record.getLanguageId());
          vendor.setVendorStatusId(record.getVendorStatusId());

          vendors.add(vendor);
        }

        VendorCollection collection = new VendorCollection();
        collection.setVendors(vendors);
        collection.setTotalRecords(result.size());

        response = GetVendorResponse.withJsonOK(collection);
      }

      // For the sake of this tutorial, let's keep exception handling simple
      catch (Exception e) {
        response = GetVendorResponse.withBadRequest();

        // TODO: 403 - Forbidden
        // response = PutVendorResponse.withForbidden();
      }

      AsyncResult<Response> result = Future.succeededFuture(response);
      asyncResultHandler.handle(result);
    });
  }

  /**
   * Fetch vendor with vendor ID
   *
   * @param vendorId
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void getVendorByVendorId(String vendorId, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    System.out.println("Getting Vendor by Vendor Id: " + vendorId);

    Vendor sample = new Vendor();
    sample.setId(1);
    sample.setAccessProvider(true);
    sample.setCode("ABC-123");
    sample.setFinancialSysCode("ABC123");
    sample.setGovernmental(false);
    sample.setLiableForVat(true);
    sample.setName("My Test Vendor");
    sample.setTaxPercentage(15.0);

    testJOOQ();
    testRelationships();

    ResponseWrapper response = GetVendorByVendorIdResponse.withJsonOK(sample);
    AsyncResult<Response> result = Future.succeededFuture(response);

    asyncResultHandler.handle(result);
  }

  /**
   * Update vendor with ID
   *
   * @param vendorId
   * @param entity
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void postVendorByVendorId(String vendorId, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  /**
   * Delete vendor with ID
   *
   * @param vendorId
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void deleteVendorByVendorId(String vendorId, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  private void testJOOQ() {
    System.out.println("Test JOOQ");
    String userName = "jbenito";
    String password = "password";
    String url = "jdbc:postgresql://localhost:5432/jbenito";

    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      // ...
      DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
      Result<LanguageRecord> result = db.selectFrom(Language.LANGUAGE).fetch();
      for (LanguageRecord r: result) {
        System.out.println("ID: " + r.getId() + " Code: " + r.getId() + " Description: " + r.getDescription());
      }
    }

    // For the sake of this tutorial, let's keep exception handling simple
    catch (Exception e) {
      System.out.println("Error opening DB: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void testRelationships() {
    System.out.println("Test Relationships");
    String userName = "jbenito";
    String password = "password";
    String url = "jdbc:postgresql://localhost:5432/jbenito";

    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      // ...
      DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);

      DepartmentRecord dept = db.newRecord(Department.DEPARTMENT);
      dept.setName("HR");
      dept.store();

      PersonRecord myself = db.newRecord(Person.PERSON);
      myself.setFirstName("JD");
      myself.setLastName("Benito");
      myself.setDepartmentId(dept.getId());
      myself.store();

//      Result<LanguageRecord> result = db.selectFrom(Language.LANGUAGE).fetch();
//      for (LanguageRecord r: result) {
//        System.out.println("ID: " + r.getId() + " Code: " + r.getId() + " Description: " + r.getDescription());
//      }
    }

    // For the sake of this tutorial, let's keep exception handling simple
    catch (Exception e) {
      System.out.println("Error opening DB: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
