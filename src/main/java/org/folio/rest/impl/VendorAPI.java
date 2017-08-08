package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import jooq.models.tables.records.VendorRecord;
import org.folio.rest.impl.mapper.VendorMapper;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCollection;
import org.folio.rest.jaxrs.resource.VendorResource;
import org.folio.rest.jaxrs.resource.support.ResponseWrapper;
import org.folio.rest.jooq.persist.ConnectResultHandler;
import org.folio.rest.jooq.persist.PostgresClient;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;
import org.jooq.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendorAPI implements VendorResource {
  private VendorMapper mapper = new VendorMapper();

  /**
   * Get list of vendors
   *
   * @param query              JSON array [{"field1","value1","operator1"},{"field2","value2","operator2"},...,{"fieldN","valueN","operatorN"}] with valid searchable fields: for example code
   *                           e.g. ["code", "MEDGRANT", "="]
   * @param orderBy            Order by field: field A, field B
   * @param order              Order: asc OR desc
   * @param offset             Skip over a number of elements by specifying an offset value for the query e.g. 0
   * @param limit              Limit the number of elements returned in the response e.g. 10
   * @param lang               Requested language. Optional. [lang=en]
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void getVendor(String query,
                        String orderBy,
                        Order order,
                        int offset,
                        int limit,
                        String lang, Map<String, String> okapiHeaders,
                        Handler<AsyncResult<Response>> asyncResultHandler,
                        Context vertxContext) throws Exception {


    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);
      PostgresClient dbClient = PostgresClient.getInstance(tenantId);

      dbClient.connect(new ConnectResultHandler() {
        @Override
        public void success(DSLContext db) {

          // BUILD QUERY
          // 1. Select the table to get information from
          SelectWhereStep myQuery = db.selectFrom(jooq.models.tables.Vendor.VENDOR);

          // 2. Add the where condition, if it was provided
          Condition whereCondition = dbClient.conditionFromParams(query);
          SelectConditionStep conditionStep = (SelectConditionStep)myQuery;
          if (whereCondition != null) {
            conditionStep = myQuery.where(whereCondition);
          }

          // 3. Specify the order, if it was provided
          SelectSeekStep1 seekStep = (SelectSeekStep1)conditionStep;
          SortField<Object> sortField = dbClient.sortFieldFromParam(orderBy, order == Order.asc);
          if (sortField != null) {
            seekStep = conditionStep.orderBy(sortField);
          }

          // 4. Add the limit and the offset if it was provided
          SelectForUpdateStep updateStep = seekStep.limit(limit).offset(offset);

          // 5. Fetch the results
          @SuppressWarnings("unchecked") Result<VendorRecord> result = updateStep.fetch();

          // TOTAL RECORD COUNT
          // Calculate the total record count
          SelectWhereStep countQuery = db.selectCount().from(jooq.models.tables.Vendor.VENDOR);
          SelectConditionStep countConditionStep = (SelectConditionStep)countQuery;
          if (whereCondition != null) {
            countConditionStep = countQuery.where(whereCondition);
          }
          int totalCount = (int)countConditionStep.fetchOne(0,int.class);
          int first = 0, last = 0;

          // INDEXES
          // Calculate the start and end index of the result set
          if (result.size() > 0) {
            first = limit * offset + 1;
            last = first + result.size() - 1;
          }

          List<Vendor> vendors = new ArrayList<>();
          for (VendorRecord record: result) {
            Vendor vendor = mapper.mapDBRecordToEntity(record);
            vendors.add(vendor);
          }

          VendorCollection collection = new VendorCollection();
          collection.setVendors(vendors);
          collection.setTotalRecords(totalCount);
          collection.setFirst(first);
          collection.setLast(last);

          Response response = GetVendorResponse.withJsonOK(collection);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception exception) {
          Response response = GetVendorResponse.withPlainBadRequest(ErrorMessage.BAD_REQUEST);
          respond(asyncResultHandler, response);
        }
      });
    });

  }

  /**
   * Create a new vendor item.
   *
   * @param lang               Requested language. Optional. [lang=en]
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
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void postVendor(String lang,
                         Vendor entity,
                         Map<String, String> okapiHeaders,
                         Handler<AsyncResult<Response>> asyncResultHandler,
                         Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);
      PostgresClient dbClient = PostgresClient.getInstance(tenantId);

      dbClient.connect(new ConnectResultHandler() {
        Response response = null;

        @Override
        public void success(DSLContext db) {
          VendorRecord vendorRecord = db.newRecord(jooq.models.tables.Vendor.VENDOR);
          mapper.mapEntityToDBRecord(entity, vendorRecord);

          // TODO: Ignore the ID since it's auto-incrementing
          vendorRecord.store();

          Vendor vendorDetails = new Vendor();
          vendorDetails.setId(vendorRecord.getId());

          OutStream stream = new OutStream();
          stream.setData(vendorDetails);

          response = PostVendorResponse.withJsonCreated("/vendor/" + vendorRecord.getId(), stream);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception exception) {
          response = PostVendorResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Retrieve vendor item with given {vendorId}
   *
   * @param vendorId           ID of the vendor resource
   * @param lang
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void getVendorByVendorId(String vendorId,
                                  String lang,
                                  Map<String, String> okapiHeaders,
                                  Handler<AsyncResult<Response>> asyncResultHandler,
                                  Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);
      PostgresClient dbClient = PostgresClient.getInstance(tenantId);
      Integer vID = new Integer(vendorId);

      dbClient.connect(new ConnectResultHandler() {
        Response response;
        @Override
        public void success(DSLContext db) {
          // Get the DB record that maps to the vendorId
          VendorRecord record = db.fetchOne(jooq.models.tables.Vendor.VENDOR, jooq.models.tables.Vendor.VENDOR.ID.eq(vID));
          if (record == null) {
            response = GetVendorByVendorIdResponse.withPlainNotFound(ErrorMessage.NOT_FOUND);
          }
          else {
            Vendor vendor = mapper.mapDBRecordToEntity(record);
            response = GetVendorByVendorIdResponse.withJsonOK(vendor);
          }

          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception exception) {
          ResponseWrapper response = GetVendorByVendorIdResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });

    });
  }

  /**
   * Delete vendor item with given {vendorId}
   *
   * @param vendorId           ID of the vendor resource
   * @param lang
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void deleteVendorByVendorId(String vendorId,
                                     String lang,
                                     Map<String, String> okapiHeaders,
                                     Handler<AsyncResult<Response>> asyncResultHandler,
                                     Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);
      PostgresClient dbClient = PostgresClient.getInstance(tenantId);
      Integer vID = new Integer(vendorId);

      dbClient.connect(new ConnectResultHandler() {
        ResponseWrapper response = null;

        @Override
        public void success(DSLContext db) {
          VendorRecord vendorRecord = db.fetchOne(jooq.models.tables.Vendor.VENDOR, jooq.models.tables.Vendor.VENDOR.ID.eq(vID));
          if (vendorRecord == null) {
            response = DeleteVendorByVendorIdResponse.withPlainNotFound(ErrorMessage.NOT_FOUND);
          }
          else {
            vendorRecord.delete();
            response = DeleteVendorByVendorIdResponse.withNoContent();
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception exception) {
          response = DeleteVendorByVendorIdResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Update vendor item with given {vendorId}
   *
   * @param vendorId           ID of the vendor resource
   * @param lang               Requested language. Optional. [lang=en]
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
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void putVendorByVendorId(String vendorId, String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);
      PostgresClient dbClient = PostgresClient.getInstance(tenantId);
      final Integer vID = new Integer(vendorId);

      dbClient.connect(new ConnectResultHandler() {
        Response response;
        @Override
        public void success(DSLContext db) {
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
              mapper.mapEntityToDBRecord(entity, vendorRecord);
              vendorRecord.store();

              response = PutVendorByVendorIdResponse.withNoContent();
            }
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception exception) {
          response = PutVendorByVendorIdResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  private static void respond(Handler<AsyncResult<Response>> handler, Response response) {
    AsyncResult<Response> result = Future.succeededFuture(response);
    handler.handle(result);
  }
}
