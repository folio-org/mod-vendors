package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.impl.transactions.*;
import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.Category_;
import org.folio.rest.jaxrs.model.ContactCategoryCollection;
import org.folio.rest.jaxrs.resource.VendorContactCategoryResource;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.util.Map;

public class ContactCategoriesAPI implements VendorContactCategoryResource {
  private static final Logger log = LoggerFactory.getLogger(VendorAPI.class);

  private Response response = null;

  @Override
  public void getVendorContactCategory(String query, String orderBy, Order order, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      GetContactCategoriesTransaction getCategoriesTransaction = GetContactCategoriesTransaction.newInstance(query, orderBy, order, offset, limit, tenantId);
      getCategoriesTransaction.execute(new TransactionCompletionHandler<ContactCategoryCollection>() {
        @Override
        public void success(ContactCategoryCollection result) {
          response = GetVendorContactCategoryResponse.withJsonOK(result);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          response = GetVendorContactCategoryResponse.withPlainInternalServerError(e.getLocalizedMessage());
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  @Override
  public void postVendorContactCategory(String lang, Category_ entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      if (entity.getId() != null) {
        response = PostVendorContactCategoryResponse.withPlainBadRequest("categoryId in payload must be null");
        respond(asyncResultHandler, response);
        return;
      }

      String tenantId = TenantTool.tenantId(okapiHeaders);
      CreateContactCategoryTransaction createTransaction = CreateContactCategoryTransaction.newInstance(entity, tenantId);
      createTransaction.execute(new TransactionCompletionHandler<Category_>() {
        @Override
        public void success(Category_ result) {
          OutStream stream = new OutStream();
          stream.setData(result);

          response = PostVendorContactCategoryResponse.withJsonCreated("/vendorContactCategory/" + result.getId(), stream);
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = PostVendorContactCategoryResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  @Override
  public void getVendorContactCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      GetContactCategoryByIdTransaction getCategoryByIdTransaction = GetContactCategoryByIdTransaction.newInstance(categoryId, tenantId);
      getCategoryByIdTransaction.execute(new TransactionCompletionHandler<Category_>() {
        @Override
        public void success(Category_ result) {
          if (result == null) {
            response = GetVendorContactCategoryByCategoryIdResponse.withPlainNotFound("id not found");
          } else {
            response = GetVendorContactCategoryByCategoryIdResponse.withJsonOK(result);
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = GetVendorContactCategoryByCategoryIdResponse.withPlainInternalServerError(ErrorMessage.INTERNAL_SERVER_ERROR);
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  @Override
  public void deleteVendorContactCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.tenantId(okapiHeaders);

      DeleteContactCategoryByIdTransaction deleteVendorByIdTransaction = DeleteContactCategoryByIdTransaction.newInstance(categoryId, tenantId);
      deleteVendorByIdTransaction.execute(new TransactionCompletionHandler<Category_>() {
        @Override
        public void success(Category_ result) {
          if (result == null) {
            response = DeleteVendorContactCategoryByCategoryIdResponse.withPlainNotFound("id not found");
          } else {
            response = DeleteVendorContactCategoryByCategoryIdResponse.withNoContent();
          }
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          log.error(e.getMessage());
          response = DeleteVendorContactCategoryByCategoryIdResponse.withPlainInternalServerError(e.getLocalizedMessage());
          respond(asyncResultHandler, response);
        }
      });
    });
  }

  @Override
  public void putVendorContactCategoryByCategoryId(String categoryId, String lang, Category_ entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      if ( !categoryId.equals(entity.getId()) ) {
        response = PutVendorContactCategoryByCategoryIdResponse.withPlainBadRequest("categoryId does not match the payload's UUID");
        respond(asyncResultHandler, response);
        return;
      }

      String tenantId = TenantTool.tenantId(okapiHeaders);
      UpdateContactCategoryTransaction updateCategoryTransaction = UpdateContactCategoryTransaction.newInstance(categoryId, entity, tenantId);
      updateCategoryTransaction.execute(new TransactionCompletionHandler<Category_>() {
        @Override
        public void success(Category_ result) {
          if (result == null) {
            response = PutVendorContactCategoryByCategoryIdResponse.withPlainNotFound(categoryId);
            respond(asyncResultHandler, response);
            return;
          }

          response = PutVendorContactCategoryByCategoryIdResponse.withNoContent();
          respond(asyncResultHandler, response);
        }

        @Override
        public void failed(Exception e) {
          response = PutVendorContactCategoryByCategoryIdResponse.withPlainInternalServerError(e.getLocalizedMessage());
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
