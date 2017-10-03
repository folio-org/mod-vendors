package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.impl.transactions.CreateVendorTransaction;
import org.folio.rest.impl.transactions.GetVendorsTransaction;
import org.folio.rest.impl.transactions.TransactionCompletionHandler;
import org.folio.rest.impl.transactions.UpdateVendorTransaction;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCollection;
import org.folio.rest.jaxrs.resource.VendorResource;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.util.Map;

public class VendorAPI implements VendorResource {
  private static final Logger log = LoggerFactory.getLogger(VendorAPI.class);

  private Response response = null;

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
                        String lang,
                        Map<String, String> okapiHeaders,
                        Handler<AsyncResult<Response>> asyncResultHandler,
                        Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      GetVendorsTransaction getVendorsTransaction = GetVendorsTransaction.newInstance(query, orderBy, order, offset, limit, tenantId);
      getVendorsTransaction.execute(new TransactionCompletionHandler<VendorCollection>() {
        @Override
        public void success(VendorCollection result) {
          response = GetVendorResponse.withJsonOK(result);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          response = GetVendorResponse.withPlainInternalServerError(e.getLocalizedMessage());
          respond(asyncResultHandler, response);
        }
      });
    });

  }

  /**
   * Create a new vendor item.
   *
   * @param lang               Requested language. Optional. [lang=en]
   * @param entity             See RAML API docs
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void postVendor(String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      CreateVendorTransaction createTransaction = CreateVendorTransaction.newInstance(entity, tenantId);
      createTransaction.execute(new TransactionCompletionHandler<Vendor>() {
        @Override
        public void success(Vendor result) {
          OutStream stream = new OutStream();
          stream.setData(result);

          response = PostVendorResponse.withJsonCreated("/vendor/" + result.getId(), stream);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = PostVendorResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Retrieve vendor item with given {vendorId}
   *
   * @param vendorId
   * @param lang
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void getVendorByVendorId(String vendorId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  /**
   * Delete vendor item with given {vendorId}
   *
   * @param vendorId
   * @param lang
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void deleteVendorByVendorId(String vendorId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  /**
   * Update vendor item with given {vendorId}
   *
   * @param vendorId
   * @param lang               Requested language. Optional. [lang=en]
   * @param entity             e.g. {
   *                           "id": "a02e1e9f-d4aa-4c97-bcfe-c6a3eef2282a",
   *                           "name": "GOBI",
   *                           "code": "AQ-GOBI",
   *                           "vendor_status": "Active",
   *                           "language": "English",
   *                           "erp_code": "AQ-GOBI-HIST",
   *                           "payment_method": "EFT",
   *                           "access_provider": true,
   *                           "governmental": true,
   *                           "licensor": true,
   *                           "material_supplier": true,
   *                           "claiming_interval": 30,
   *                           "discount_percent": 10,
   *                           "expected_activation_interval": 1,
   *                           "expected_invoice_interval": 5,
   *                           "renewal_activation_interval": 14,
   *                           "subscription_interval": 365,
   *                           "liable_for_vat": false,
   *                           "tax_id": "TX-GOBI-HIST",
   *                           "tax_percentage": 5,
   *                           "vendor_names": [
   *                           {
   *                           "id": "83a5e6e9-a09a-4129-9a0d-68829a4a56cb",
   *                           "description": "AKA",
   *                           "value": "YBP"
   *                           },
   *                           {
   *                           "id": "4bfe4c90-6ffe-4a3d-86bd-26e4fd0e2818",
   *                           "description": "Formerly known as",
   *                           "value": "Yankee Book Peddler"
   *                           },
   *                           ],
   *                           "vendor_currencies": [
   *                           {
   *                           "id": "42ac13bb-bf6b-41aa-9d4e-02aa01cd7c88",
   *                           "currency": "USD"
   *                           },
   *                           {
   *                           "id": "459433d0-7b11-4de5-86f3-c11b95a95399",
   *                           "currency": "CAD"
   *                           },
   *                           {
   *                           "id": "91212f53-3729-41b1-b630-ac013928e6a5",
   *                           "currency": "GBP"
   *                           },
   *                           {
   *                           "id": "7e1cc197-1ee7-4794-9b1a-70c7d6b55d8a",
   *                           "currency": "EUR"
   *                           }
   *                           ],
   *                           "vendor_interfaces": [
   *                           {
   *                           "id": "6389bfae-2612-41fe-8198-dc2727db64c4",
   *                           "name": "Sales Portal",
   *                           "uri": "https://www.gobi3.com/Pages/Login.aspx",
   *                           "username": "my_user",
   *                           "password": "my_password",
   *                           "notes": "This is the store-front for GOBI.",
   *                           "available": true,
   *                           "delivery_method": "Online",
   *                           "statistics_format": "PDF",
   *                           "locally_stored": "",
   *                           "online_location": "",
   *                           "statistics_notes": ""
   *                           }
   *                           ],
   *                           "agreements": [
   *                           {
   *                           "id": "13d245a6-1718-426d-90f2-5855343db6bc",
   *                           "discount": 7.5,
   *                           "name": "History Follower Incentive",
   *                           "notes": "",
   *                           "reference_url": "http://my_sample_agreement.com"
   *                           }
   *                           ],
   *                           "accounts": [
   *                           {
   *                           "id": "7fa0c4b4-05e8-4195-80ca-ef28e54d1182",
   *                           "name": "History Account",
   *                           "payment_method": "EFT",
   *                           "account_no": "GOBI-HIST-12",
   *                           "lib_ven_acct_status": "Active",
   *                           "description": "This is my account description.",
   *                           "contact_info": "Some basic contact information note.",
   *                           "appsystem_no": "FIN-GOBI-HIST-12",
   *                           "notes": "",
   *                           "library_code": "My Library",
   *                           "library_edi_code": "MY-LIB-1"
   *                           }
   *                           ],
   *                           "edi_info": {
   *                           "vendor_edi_code": "AQ-GOBI-HIST",
   *                           "vendor_edi_type": "014/EAN",
   *                           "lib_edi_code": "MY-LIB-1",
   *                           "lib_edi_type": "014/EAN",
   *                           "edi_naming_convention": "",
   *                           "prorate_tax": true,
   *                           "prorate_fees": true,
   *                           "send_acct_num": true,
   *                           "support_order": true,
   *                           "support_invoice": true,
   *                           "notes": "",
   *                           "ftp_format": "SFTP",
   *                           "ftp_mode": "ASCII",
   *                           "ftp_conn_mode": "Active",
   *                           "ftp_port": "22",
   *                           "server_address": "http://127.0.0.1",
   *                           "username": "edi_username",
   *                           "password": "edi_password",
   *                           "order_directory": "/path/to/order/directory",
   *                           "invoice_directory": "/path/to/invoice/directory",
   *                           "send_to_emails": "email1@site.com, email2@site.com",
   *                           "notify_all_edi": true,
   *                           "notify_invoice_only": true,
   *                           "notify_error_only": false
   *                           },
   *                           "job": {
   *                           "is_scheduled": false,
   *                           "start_date": "2008-09-15T15:53:00+05:00",
   *                           "time": "15:53:00+05:00",
   *                           "is_monday": false,
   *                           "is_tuesday": false,
   *                           "is_wednesday": false,
   *                           "is_thursday": false,
   *                           "is_friday": false,
   *                           "is_saturday": false,
   *                           "is_sunday": false,
   *                           "scheduling_notes": ""
   *                           },
   *                           "addresses": [
   *                           {
   *                           "id": "fe2946e7-7405-4220-b600-cf4362f3bbc8",
   *                           "address": {
   *                           "id": "5d9de3f1-eb47-4d09-8cc1-8040810cd924",
   *                           "address_line_1": "10 Estes Street",
   *                           "address_line_2": "",
   *                           "city": "Ipswich",
   *                           "region": "MA",
   *                           "postal_code": "01938",
   *                           "country": "USA"
   *                           },
   *                           "categories": [
   *                           {
   *                           "id":"b2e6d188-a226-11e7-abc4-cec278b6b50a",
   *                           "value": "Sales"
   *                           },
   *                           {
   *                           "id":"afd67ad8-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Accounting"
   *                           },
   *                           {
   *                           "id":"c3a9e7f2-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Serials"
   *                           }
   *                           ],
   *                           "language": "en",
   *                           "san_code": "1234567"
   *                           }
   *                           ],
   *                           "phone_numbers": [
   *                           {
   *                           "id": "93abb597-7be7-4397-9fb7-7097d18b7f27",
   *                           "phone_number": {
   *                           "id": "2bb94b02-fc26-467f-bc09-6175f8ef02c9",
   *                           "country_code": "US",
   *                           "area_code": "978",
   *                           "phone_number": "9999999"
   *                           },
   *                           "categories": [
   *                           {
   *                           "id":"b2e6d188-a226-11e7-abc4-cec278b6b50a",
   *                           "value": "Sales"
   *                           },
   *                           {
   *                           "id":"afd67ad8-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Accounting"
   *                           },
   *                           {
   *                           "id":"c3a9e7f2-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Serials"
   *                           }
   *                           ],
   *                           "language": "en-us"
   *                           }
   *                           ],
   *                           "emails": [
   *                           {
   *                           "id": "ecf6b712-d372-4afe-b4d8-6a8f60bca49e",
   *                           "email": {
   *                           "id": "93ca143e-ee72-4d6d-8ab2-5cbe44d58a6e",
   *                           "value": "noreply@folio.org"
   *                           },
   *                           "categories": [
   *                           {
   *                           "id":"b2e6d188-a226-11e7-abc4-cec278b6b50a",
   *                           "value": "Sales"
   *                           },
   *                           {
   *                           "id":"afd67ad8-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Accounting"
   *                           },
   *                           {
   *                           "id":"c3a9e7f2-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Serials"
   *                           }
   *                           ],
   *                           "language": "en-us"
   *                           }
   *                           ],
   *                           "contacts": [
   *                           {
   *                           "id": "83914f35-43c0-44a6-8642-6c8225d33077",
   *                           "language": "en-us",
   *                           "contact_person": {
   *                           "id": "e2e1a1d0-058f-46f2-8914-93374c534627",
   *                           "prefix": "Director",
   *                           "first_name": "Nick",
   *                           "last_name": "Fury",
   *                           "language": "en-us",
   *                           "notes": "SHIELD's Big Kahuna",
   *                           "phone_number": {
   *                           "id": "2c590145-a405-48d2-8e1d-9bc94e5fdc7f",
   *                           "country_code": "US",
   *                           "area_code": "978",
   *                           "phone_number": "9999999"
   *                           },
   *                           "email": {
   *                           "id": "4d3dade5-1314-4385-9c2a-a38e4f806c1b",
   *                           "value": "noreply@folio.org"
   *                           },
   *                           "address": {
   *                           "id": "55d3210d-f959-4826-a27e-739dd0b1b434",
   *                           "address_line_1": "10 Estes Street",
   *                           "address_line_2": "",
   *                           "city": "Ipswich",
   *                           "region": "MA",
   *                           "postal_code": "01938",
   *                           "country": "USA"
   *                           }
   *                           },
   *                           "categories": [
   *                           {
   *                           "id":"b2e6d188-a226-11e7-abc4-cec278b6b50a",
   *                           "value": "Sales"
   *                           },
   *                           {
   *                           "id":"afd67ad8-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Accounting"
   *                           },
   *                           {
   *                           "id":"c3a9e7f2-a227-11e7-abc4-cec278b6b50a",
   *                           "value": "Serials"
   *                           }
   *                           ]
   *                           }
   *                           ],
   *                           "notes": [
   *                           {
   *                           "id": "c3b9e7x2-a227-51e7-abc4-ced278b6b50a",
   *                           "description": "This is a sample note.",
   *                           "timestamp": "2008-09-15T15:53:00+05:00"
   *                           }
   *                           ]
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void putVendorByVendorId(String vendorId, String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      UpdateVendorTransaction updateVendorTransaction = UpdateVendorTransaction.newInstance(entity, tenantId);
      updateVendorTransaction.execute(new TransactionCompletionHandler<Vendor>() {
        @Override
        public void success(Vendor result) {
          response = PutVendorByVendorIdResponse.withNoContent();
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          response = PutVendorByVendorIdResponse.withPlainInternalServerError(e.getLocalizedMessage());
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
