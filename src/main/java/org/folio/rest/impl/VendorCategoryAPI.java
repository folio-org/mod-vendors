package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.CategoryCollection;
import org.folio.rest.jaxrs.resource.VendorCategoryResource;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VendorCategoryAPI implements VendorCategoryResource {
  private static final String CATEGORY_TABLE = "category";
  private static final String CATEGORY_LOCATION_PREFIX = "/category/";

  private static final Logger log = LoggerFactory.getLogger(VendorCategoryAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "id";

  private org.folio.rest.persist.Criteria.Order getOrder(Order order, String field) {
    if (field == null) {
      return null;
    }

    String sortOrder = org.folio.rest.persist.Criteria.Order.ASC;
    if (order.name().equals("desc")) {
      sortOrder = org.folio.rest.persist.Criteria.Order.DESC;
    }

    String fieldTemplate = String.format("jsonb->'%s'", field);
    return new org.folio.rest.persist.Criteria.Order(fieldTemplate, org.folio.rest.persist.Criteria.Order.ORDER.valueOf(sortOrder.toUpperCase()));
  }

  private static void respond(Handler<AsyncResult<Response>> handler, Response response) {
    AsyncResult<Response> result = Future.succeededFuture(response);
    handler.handle(result);
  }

  private boolean isInvalidUUID (String errorMessage) {
    return (errorMessage != null && errorMessage.contains("invalid input syntax for uuid"));
  }

  public VendorCategoryAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
  }

  @Override
  public void getVendorCategory(String query, String orderBy, Order order, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        Criterion criterion = Criterion.json2Criterion(query);
        criterion.setLimit(new Limit(limit));
        criterion.setOffset(new Offset(offset));

        org.folio.rest.persist.Criteria.Order or = getOrder(order, orderBy);
        if (or != null) {
          criterion.setOrder(or);
        }

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(CATEGORY_TABLE, Category.class, criterion, true,
          reply -> {
            try {
              if(reply.succeeded()){
                CategoryCollection collection = new CategoryCollection();
                @SuppressWarnings("unchecked")
                List<Category> results = (List<Category>)reply.result().getResults();
                collection.setCategories(results);
                Integer totalRecords = reply.result().getResultInfo().getTotalRecords();
                collection.setTotalRecords(totalRecords);
                Integer first = 0;
                Integer last = 0;
                if (!results.isEmpty()) {
                  first = offset + 1;
                  last = offset + results.size();
                }
                collection.setFirst(first);
                collection.setLast(last);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryResponse
                  .withJsonOK(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryResponse
                  .withPlainBadRequest(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryResponse
          .withPlainInternalServerError(message)));
      }
    });
  }

  @Override
  public void postVendorCategory(String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {

      try {
        String id = UUID.randomUUID().toString();
        if(entity.getId() == null){
          entity.setId(id);
        }
        else{
          id = entity.getId();
        }

        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
        PostgresClient.getInstance(vertxContext.owner(), tenantId).save(
          CATEGORY_TABLE, id, entity,
          reply -> {
            try {
              if (reply.succeeded()) {
                String persistenceId = reply.result();
                entity.setId(persistenceId);
                OutStream stream = new OutStream();
                stream.setData(entity);

                Response response = VendorCategoryResource.PostVendorCategoryResponse.
                  withJsonCreated(CATEGORY_LOCATION_PREFIX + persistenceId, stream);
                respond(asyncResultHandler, response);
              }
              else {
                log.error(reply.cause().getMessage(), reply.cause());
                Response response = VendorCategoryResource.PostVendorCategoryResponse.withPlainInternalServerError(reply.cause().getMessage());
                respond(asyncResultHandler, response);
              }
            }
            catch (Exception e) {
              log.error(e.getMessage(), e);

              Response response = VendorCategoryResource.PostVendorCategoryResponse.withPlainInternalServerError(e.getMessage());
              respond(asyncResultHandler, response);
            }

          }
        );
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);

        String errMsg = messages.getMessage(lang, MessageConsts.InternalServerError);
        Response response = VendorCategoryResource.PostVendorCategoryResponse.withPlainInternalServerError(errMsg);
        respond(asyncResultHandler, response);
      }

    });
  }

  @Override
  public void getVendorCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String idArgument = String.format("'%s'", categoryId);
        Criterion c = new Criterion(
          new Criteria().addField(idFieldName).setJSONB(false).setOperation("=").setValue(idArgument));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(CATEGORY_TABLE, Category.class, c, true,
          reply -> {
            try {
              if (reply.succeeded()) {
                @SuppressWarnings("unchecked")
                List<Category> results = (List<Category>) reply.result().getResults();
                if (results.isEmpty()) {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryByCategoryIdResponse
                    .withPlainNotFound(categoryId)));
                }
                else{
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryByCategoryIdResponse
                    .withJsonOK(results.get(0))));
                }
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                if (isInvalidUUID(reply.cause().getMessage())) {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryByCategoryIdResponse
                    .withPlainNotFound(categoryId)));
                }
                else{
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryByCategoryIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
                }
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryByCategoryIdResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.GetVendorCategoryByCategoryIdResponse
          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
      }
    });
  }

  @Override
  public void deleteVendorCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    String tenantId = TenantTool.tenantId(okapiHeaders);

    try {
      vertxContext.runOnContext(v -> {
        PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

        try {
          postgresClient.delete(CATEGORY_TABLE, categoryId, reply -> {
            if (reply.succeeded()) {
              asyncResultHandler.handle(Future.succeededFuture(
                VendorCategoryResource.DeleteVendorCategoryByCategoryIdResponse.noContent()
                  .build()));
            } else {
              asyncResultHandler.handle(Future.succeededFuture(
                VendorCategoryResource.DeleteVendorCategoryByCategoryIdResponse.
                  withPlainInternalServerError(reply.cause().getMessage())));
            }
          });
        } catch (Exception e) {
          asyncResultHandler.handle(Future.succeededFuture(
            VendorCategoryResource.DeleteVendorCategoryByCategoryIdResponse.
              withPlainInternalServerError(e.getMessage())));
        }
      });
    }
    catch(Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
        VendorCategoryResource.DeleteVendorCategoryByCategoryIdResponse.
          withPlainInternalServerError(e.getMessage())));
    }
  }

  @Override
  public void putVendorCategoryByCategoryId(String categoryId, String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
      try {
        if(entity.getId() == null){
          entity.setId(categoryId);
        }
        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
          CATEGORY_TABLE, entity, categoryId,
          reply -> {
            try {
              if(reply.succeeded()){
                if (reply.result().getUpdated() == 0) {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.PutVendorCategoryByCategoryIdResponse
                    .withPlainNotFound(messages.getMessage(lang, MessageConsts.NoRecordsUpdated))));
                }
                else{
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.PutVendorCategoryByCategoryIdResponse
                    .withNoContent()));
                }
              }
              else{
                log.error(reply.cause().getMessage());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.PutVendorCategoryByCategoryIdResponse
                  .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.PutVendorCategoryByCategoryIdResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorCategoryResource.PutVendorCategoryByCategoryIdResponse
          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
      }
    });
  }
}
