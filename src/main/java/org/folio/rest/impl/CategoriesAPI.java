package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.impl.transactions.*;
import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.CategoryCollection;
import org.folio.rest.jaxrs.resource.VendorCategoryResource;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.util.Map;

public class CategoriesAPI implements VendorCategoryResource {
  private static final Logger log = LoggerFactory.getLogger(VendorAPI.class);

  private Response response = null;

  /**
   * Get list of categories
   */
  @Override
  public void getVendorCategory(String query, String orderBy, Order order, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      GetCategoriesTransaction getCategoriesTransaction = GetCategoriesTransaction.newInstance(query, orderBy, order, offset, limit, tenantId);
      getCategoriesTransaction.execute(new TransactionCompletionHandler<CategoryCollection>() {
        @Override
        public void success(CategoryCollection result) {
          response = GetVendorCategoryResponse.withJsonOK(result);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          response = GetVendorCategoryResponse.withPlainInternalServerError(e.getLocalizedMessage());
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Create a new vendorCategory item.
   */
  @Override
  public void postVendorCategory(String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      if (entity.getId() != null) {
        response = PostVendorCategoryResponse.withPlainBadRequest("categoryId in payload must be null");
        respond(asyncResultHandler, response);
        return;
      }

      String tenantId = TenantTool.tenantId(okapiHeaders);
      CreateCategoryTransaction createTransaction = CreateCategoryTransaction.newInstance(entity, tenantId);
      createTransaction.execute(new TransactionCompletionHandler<Category>() {
        @Override
        public void success(Category result) {
          OutStream stream = new OutStream();
          stream.setData(result);

          response = PostVendorCategoryResponse.withJsonCreated("/vendorCategory/" + result.getId(), stream);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = PostVendorCategoryResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Retrieve vendorCategory item with given {vendorCategoryId}
   */
  @Override
  public void getVendorCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      GetCategoryByIdTransaction getCategoryByIdTransaction = GetCategoryByIdTransaction.newInstance(categoryId, tenantId);
      getCategoryByIdTransaction.execute(new TransactionCompletionHandler<Category>() {
        @Override
        public void success(Category result) {
          if (result == null) {
            response = GetVendorCategoryByCategoryIdResponse.withPlainNotFound("vendor not found");
          } else {
            response = GetVendorCategoryByCategoryIdResponse.withJsonOK(result);
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = GetVendorCategoryByCategoryIdResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Delete vendorCategory item with given {vendorCategoryId}
   *
   * @param categoryId
   * @param lang               Requested language. Optional. [lang=en]
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void deleteVendorCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      DeleteCategoryByIdTransaction deleteVendorByIdTransaction = DeleteCategoryByIdTransaction.newInstance(categoryId, tenantId);
      deleteVendorByIdTransaction.execute(new TransactionCompletionHandler<Category>() {
        @Override
        public void success(Category result) {
          if (result == null) {
            response = DeleteVendorCategoryByCategoryIdResponse.withPlainNotFound("vendor not found");
          } else {
            response = DeleteVendorCategoryByCategoryIdResponse.withNoContent();
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = DeleteVendorCategoryByCategoryIdResponse.withPlainInternalServerError(e.getLocalizedMessage());
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  /**
   * Update vendorCategory item with given {vendorCategoryId}
   *
   * @param categoryId
   * @param lang               Requested language. Optional. [lang=en]
   * @param entity             e.g. {
   *                           "id": "08e0eb27-b57f-4638-a703-9a2c57bd8708",
   *                           "value": "Accounting"
   * @param okapiHeaders
   * @param asyncResultHandler A <code>Handler<AsyncResult<Response>>></code> handler {@link Handler} which must be called as follows - Note the 'GetPatronsResponse' should be replaced with '[nameOfYourFunction]Response': (example only) <code>asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetPatronsResponse.withJsonOK( new ObjectMapper().readValue(reply.result().body().toString(), Patron.class))));</code> in the final callback (most internal callback) of the function.
   * @param vertxContext       The Vertx Context Object <code>io.vertx.core.Context</code>
   */
  @Override
  public void putVendorCategoryByCategoryId(String categoryId, String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      if ( !categoryId.equals(entity.getId()) ) {
        response = PutVendorCategoryByCategoryIdResponse.withPlainBadRequest("categoryId does not match the payload's UUID");
        respond(asyncResultHandler, response);
        return;
      }

      String tenantId = TenantTool.tenantId(okapiHeaders);
      UpdateCategoryTransaction updateCategoryTransaction = UpdateCategoryTransaction.newInstance(categoryId, entity, tenantId);
      updateCategoryTransaction.execute(new TransactionCompletionHandler<Category>() {
        @Override
        public void success(Category result) {
          if (result == null) {
            response = PutVendorCategoryByCategoryIdResponse.withPlainNotFound(categoryId);
            respond(asyncResultHandler, response);
            return;
          }

          response = PutVendorCategoryByCategoryIdResponse.withNoContent();
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          response = PutVendorCategoryByCategoryIdResponse.withPlainInternalServerError(e.getLocalizedMessage());
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
