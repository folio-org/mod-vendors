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
import org.folio.rest.jooq.persist.PostgresClient;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCollection;
import org.folio.rest.jaxrs.resource.VendorResource;
import org.folio.rest.jaxrs.resource.support.ResponseWrapper;
import org.folio.rest.jooq.persist.ResultHandler;
import org.folio.rest.tools.utils.TenantTool;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendorAPI implements VendorResource {
  private String userName = "jbenito";
  private String password = "password";
  private String url = "jdbc:postgresql://localhost:5432/jbenito";

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
      String tenantId = TenantTool.tenantId(okapiHeaders);
      PostgresClient dbClient = PostgresClient.getInstance(tenantId);

      dbClient.execute(new ResultHandler<DSLContext, SQLException>() {
        @Override
        public void success(DSLContext db) {
          Result<VendorRecord> result = db.selectFrom(jooq.models.tables.Vendor.VENDOR).fetch();
          String sql = db.selectFrom(jooq.models.tables.Vendor.VENDOR).getSQL();

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

          ResponseWrapper response = GetVendorResponse.withJsonOK(collection);
          AsyncResult<Response> output = Future.succeededFuture(response);
          asyncResultHandler.handle(output);
        }

        @Override
        public void failed(SQLException exception) {
          ResponseWrapper response = GetVendorResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);

          // TODO: 403 - Forbidden
//        response = GetVendorResponse.withPlainForbidden(ErrorMessage.FORBIDDEN);
          AsyncResult<Response> result = Future.succeededFuture(response);
          asyncResultHandler.handle(result);
        }
      });

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
    vertxContext.runOnContext(v -> {
      ResponseWrapper response;

      try (Connection conn = DriverManager.getConnection(url, userName, password)) {
        // ...
        DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
        Integer vID = new Integer(vendorId);

        // Get the DB record that maps to the vendorId
        VendorRecord record = db.fetchOne(jooq.models.tables.Vendor.VENDOR, jooq.models.tables.Vendor.VENDOR.ID.eq(vID));
        if (record == null) {
          response = GetVendorByVendorIdResponse.withPlainNotFound(ErrorMessage.NOT_FOUND);
        }
        else {
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

          response = GetVendorByVendorIdResponse.withJsonOK(vendor);
        }
      }
      catch (Exception e) {
        response = GetVendorByVendorIdResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);

        // TODO: 403 - Forbidden
//        response = GetVendorByVendorIdResponse.withPlainForbidden(ErrorMessage.FORBIDDEN);
      }

      AsyncResult<Response> result = Future.succeededFuture(response);
      asyncResultHandler.handle(result);
    });
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
    vertxContext.runOnContext(v -> {
      ResponseWrapper response;

      try (Connection conn = DriverManager.getConnection(url, userName, password)) {
        // ...
        DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
        Integer vID = new Integer(vendorId);

        VendorRecord vendorRecord = db.fetchOne(jooq.models.tables.Vendor.VENDOR, jooq.models.tables.Vendor.VENDOR.ID.eq(vID));
        if (vendorRecord == null) {
          response = DeleteVendorByVendorIdResponse.withPlainNotFound(ErrorMessage.NOT_FOUND);
        }
        else {
          vendorRecord.delete();
          response = DeleteVendorByVendorIdResponse.withNoContent();
        }
      }
      catch (Exception e) {
        response = DeleteVendorByVendorIdResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);

        // TODO: 403 - Forbidden
//        response = DeleteVendorByVendorIdResponse.withPlainForbidden(ErrorMessage.FORBIDDEN);
      }

      AsyncResult<Response> result = Future.succeededFuture(response);
      asyncResultHandler.handle(result);
    });
  }


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
   *                           "language_id": 1,
   *                           "vendor_status_id": 1
   *                           }
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void postVendor(Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
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
        response = PostVendorResponse.withJsonOK(vendorDetails);
      }
      catch (Exception e) {
        response = PostVendorResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);

        // TODO: 403 - Forbidden
//        response = PostVendorResponse.withPlainForbidden(ErrorMessage.FORBIDDEN);
      }

      AsyncResult<Response> result = Future.succeededFuture(response);
      asyncResultHandler.handle(result);
    });
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
  public void putVendorByVendorId(String vendorId, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      ResponseWrapper response;

      try (Connection conn = DriverManager.getConnection(url, userName, password)) {
        // ...
        DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
        Integer vID = new Integer(vendorId);

        if ( !vID.equals(entity.getId()) ) {
          response = PutVendorByVendorIdResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);
        }
        else {
          // Get the DB record that maps to the vendorId
          VendorRecord vendorRecord = db.fetchOne(jooq.models.tables.Vendor.VENDOR, jooq.models.tables.Vendor.VENDOR.ID.eq(vID));
          if (vendorRecord == null) {
            response = PutVendorByVendorIdResponse.withPlainNotFound(ErrorMessage.NOT_FOUND);
          }
          else {
            vendorRecord.setAccessProvider(entity.getAccessProvider());
            vendorRecord.setClaimingInterval(entity.getClaimingInterval());
            vendorRecord.setCode(entity.getCode());

            BigDecimal discount = new BigDecimal(entity.getDiscountPercent());
            vendorRecord.setDiscountPercent(discount);
            vendorRecord.setExpectedActivationInterval(entity.getExpectedActivationInterval());
            vendorRecord.setExpectedInvoiceInterval(entity.getExpectedInvoiceInterval());
            vendorRecord.setFinancialSysCode(entity.getFinancialSysCode());
            vendorRecord.setGovernmental(entity.getGovernmental());
            vendorRecord.setLiableForVat(entity.getLiableForVat());
            vendorRecord.setLicensor(entity.getLicensor());
            vendorRecord.setMaterialSupplier(entity.getMaterialSupplier());
            vendorRecord.setName(entity.getName());
            vendorRecord.setNationalTaxId(entity.getNationalTaxId());
            vendorRecord.setRenewalActivationInterval(entity.getRenewalActivationInterval());
            vendorRecord.setSubscriptionInterval(entity.getSubscriptionInterval());

            BigDecimal tax = new BigDecimal(entity.getTaxPercentage());
            vendorRecord.setTaxPercentage(tax);

            // Relationships
            vendorRecord.setContactInfoId(entity.getContactInfoId());
            vendorRecord.setCurrencyId(entity.getCurrencyId());
            vendorRecord.setInterfaceId(entity.getInterfaceId());
            vendorRecord.setLanguageId(entity.getLanguageId());
            vendorRecord.setVendorStatusId(entity.getVendorStatusId());
            vendorRecord.store();

            response = PutVendorByVendorIdResponse.withNoContent();
          }
        }
      }
      catch (Exception e) {
        response = PutVendorByVendorIdResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);

        // TODO: 403 - Forbidden
//        response = PutVendorByVendorIdResponse.withPlainForbidden(ErrorMessage.FORBIDDEN);
      }

      AsyncResult<Response> result = Future.succeededFuture(response);
      asyncResultHandler.handle(result);
    });
  }

  private void testJOOQ() {
    System.out.println("Test JOOQ");

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

  private void testVendorLanguageRel() {
    System.out.println("Test Vendor-Language Relationship");
    String userName = "jbenito";
    String password = "password";
    String url = "jdbc:postgresql://localhost:5432/jbenito";

    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      // ...
      DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);

      Record record = db.select()
        .from(jooq.models.tables.Vendor.VENDOR)
        .join(Language.LANGUAGE).on(jooq.models.tables.Vendor.VENDOR.LANGUAGE_ID.eq(Language.LANGUAGE.ID))
        .fetchOne();

      VendorRecord vendorRecord = record.into(jooq.models.tables.Vendor.VENDOR);
      LanguageRecord langRecord = record.into(Language.LANGUAGE);
      System.out.println("LANG - Code: " + langRecord.getCode() + " Description: " + langRecord.getDescription());
    }

    // For the sake of this tutorial, let's keep exception handling simple
    catch (Exception e) {
      System.out.println("Error opening DB: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
