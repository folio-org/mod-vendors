package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.ContactCategoryCollection;
import org.folio.rest.jaxrs.resource.ContactCategoryResource;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;
import org.folio.rest.tools.utils.TenantTool;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class ContactCategoryAPI implements ContactCategoryResource {
  private static final String CONTACT_CATEGORY_TABLE = "contact_category";
  private static final String CONTACT_CATEGORY_LOCATION_PREFIX = "/contact_category/";

  private static final Logger log = LoggerFactory.getLogger(ContactCategoryAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "_id";

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

  @Override
  public void getContactCategory(String query, String orderBy, Order order, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        Criterion criterion = Criterion.json2Criterion(query);
//        Criteria criteria = new Criteria("/ramls/schemas/" +PURCHASE_ORDER_TABLE + ".json");
//        criterion.addCriterion(criteria);
        criterion.setLimit(new Limit(limit));
        criterion.setOffset(new Offset(offset));

        org.folio.rest.persist.Criteria.Order or = getOrder(order, orderBy);
        if (or != null) {
          criterion.setOrder(or);
        }

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(CONTACT_CATEGORY_TABLE, Category.class, criterion, true,
          reply -> {
            try {
              if(reply.succeeded()){
                ContactCategoryCollection collection = new ContactCategoryCollection();
                @SuppressWarnings("unchecked")
                List<Category> results = (List<Category>)reply.result().getResults();
                collection.setCategories(results);
                Integer totalRecords = reply.result().getResultInfo().getTotalRecords();
                collection.setTotalRecords(totalRecords);
                Integer first = 0, last = 0;
                if (results.size() > 0) {
                  first = offset + 1;
                  last = offset + results.size();
                }
                collection.setFirst(first);
                collection.setLast(last);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(ContactCategoryResource.GetContactCategoryResponse
                  .withJsonOK(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(ContactCategoryResource.GetContactCategoryResponse
                  .withPlainBadRequest(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(ContactCategoryResource.GetContactCategoryResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(ContactCategoryResource.GetContactCategoryResponse
          .withPlainInternalServerError(message)));
      }
    });
  }

  @Override
  public void postContactCategory(String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void getContactCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void deleteContactCategoryByCategoryId(String categoryId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }

  @Override
  public void putContactCategoryByCategoryId(String categoryId, String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {

  }
}
