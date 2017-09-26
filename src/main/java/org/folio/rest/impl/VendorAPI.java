package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.resource.VendorResource;

import javax.ws.rs.core.Response;
import java.util.Map;

public class VendorAPI implements VendorResource {
  @Override
  public void getVendor(String query, String orderBy, Order order, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void postVendor(String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void getVendorByVendorId(String vendorId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void deleteVendorByVendorId(String vendorId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void putVendorByVendorId(String vendorId, String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }
}
