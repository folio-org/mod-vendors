package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.impl.transactions.*;
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
   */
  @Override
  public void postVendor(String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

    vertxContext.runOnContext(v -> {
      if (entity.getId() != null) {
        response = PostVendorResponse.withPlainBadRequest("vendorID in payload must be null");
        respond(asyncResultHandler, response);
        return;
      }

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
   */
  @Override
  public void getVendorByVendorId(String vendorId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      GetVendorByIdTransaction getVendorByIdTransaction = GetVendorByIdTransaction.newInstance(vendorId, tenantId);
      getVendorByIdTransaction.execute(new TransactionCompletionHandler<Vendor>() {
        @Override
        public void success(Vendor result) {
          if (result == null) {
            response = GetVendorByVendorIdResponse.withPlainNotFound("vendor not found");
          } else {
            response = GetVendorByVendorIdResponse.withJsonOK(result);
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = GetVendorByVendorIdResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Delete vendor item with given {vendorId}
   */
  @Override
  public void deleteVendorByVendorId(String vendorId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      DeleteVendorByIdTransaction deleteVendorByIdTransaction = DeleteVendorByIdTransaction.newInstance(vendorId, tenantId);
      deleteVendorByIdTransaction.execute(new TransactionCompletionHandler<Vendor>() {
        @Override
        public void success(Vendor result) {
          if (result == null) {
            response = DeleteVendorByVendorIdResponse.withPlainNotFound("vendor not found");
          } else {
            response = DeleteVendorByVendorIdResponse.withNoContent();
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = DeleteVendorByVendorIdResponse.withPlainInternalServerError(e.getLocalizedMessage());
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Update vendor item with given {vendorId}
   */
  @Override
  public void putVendorByVendorId(String vendorId, String lang, Vendor entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      if ( !vendorId.equals(entity.getId()) ) {
        response = PutVendorByVendorIdResponse.withPlainBadRequest("vendorID does not match the payload's UUID");
        respond(asyncResultHandler, response);
        return;
      }

      String tenantId = TenantTool.tenantId(okapiHeaders);
      UpdateVendorTransaction updateVendorTransaction = UpdateVendorTransaction.newInstance(vendorId, entity, tenantId);
      updateVendorTransaction.execute(new TransactionCompletionHandler<Vendor>() {
        @Override
        public void success(Vendor result) {
          if (result == null) {
            response = PutVendorByVendorIdResponse.withPlainNotFound(vendorId);
            respond(asyncResultHandler, response);
            return;
          }

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
