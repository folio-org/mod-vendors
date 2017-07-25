package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import jooq.models.tables.Language;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.resource.VendorResource;
import org.folio.rest.jaxrs.resource.support.ResponseWrapper;

import static jooq.models.Tables.LANGUAGE;
import static org.jooq.impl.DSL.*;
import jooq.models.tables.records.*;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.ws.rs.core.Response;
import java.io.Reader;
import java.sql.*;
import java.util.Map;

public class VendorAPI implements VendorResource {
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
    sample.setTaxPercentage("15");

    testJOOQ();

    ResponseWrapper response = GetVendorByVendorIdResponse.withJsonOK(sample);
    AsyncResult<Response> result = Future.succeededFuture(response);

    asyncResultHandler.handle(result);
  }

  /**
   * Unimplemented action
   *
   * @param vendorId
   * @param entity
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void postVendorByVendorId(String vendorId, Reader entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  private void testJOOQ() {
    System.out.println("Test JOOQ");
    String userName = "jbenito";
    String password = "password";
    String url = "jdbc:postgresql://localhost:5432/jbenito";

    try (Connection conn = DriverManager.getConnection(url, userName, password)) {
      // ...
      DSLContext db = DSL.using(conn, SQLDialect.POSTGRES);
      Result<LanguageRecord> result = db.selectFrom(LANGUAGE).fetch();
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
}
