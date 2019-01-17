package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.VendorPhone;
import org.folio.rest.jaxrs.model.VendorPhoneCollection;
import org.folio.rest.jaxrs.resource.VendorPhoneResource;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VendorPhoneAPI implements VendorPhoneResource {
  private static final String VENDOR_PHONE_TABLE = "vendor_phone";
  private static final String VENDOR_PHONE_LOCATION_PREFIX = "/vendors/vendor_phones/";

  private static final Logger log = LoggerFactory.getLogger(VendorPhoneAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "id";

  private static void respond(Handler<AsyncResult<Response>> handler, Response response) {
    AsyncResult<Response> result = Future.succeededFuture(response);
    handler.handle(result);
  }

  private boolean isInvalidUUID (String errorMessage) {
    return (errorMessage != null && errorMessage.contains("invalid input syntax for uuid"));
  }

  public VendorPhoneAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
  }


  @Override
  public void getVendorPhone(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String[] fieldList = {"*"};
        CQL2PgJSON cql2PgJSON = new CQL2PgJSON(String.format("%s.jsonb", VENDOR_PHONE_TABLE));
        CQLWrapper cql = new CQLWrapper(cql2PgJSON, query)
          .setLimit(new Limit(limit))
          .setOffset(new Offset(offset));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(VENDOR_PHONE_TABLE, VendorPhone.class, fieldList, cql,
          true, false, reply -> {
            try {
              if(reply.succeeded()){
                VendorPhoneCollection collection = new VendorPhoneCollection();
                @SuppressWarnings("unchecked")
                List<VendorPhone> results = (List<VendorPhone>)reply.result().getResults();
                collection.setVendorPhones(results);
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
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorPhoneResource.GetVendorPhoneResponse
                  .withJsonOK(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorPhoneResource.GetVendorPhoneResponse
                  .withPlainBadRequest(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorPhoneResource.GetVendorPhoneResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorPhoneResource.GetVendorPhoneResponse
          .withPlainInternalServerError(message)));
      }
    });
  }

  @Override
  public void postVendorPhone(String lang, VendorPhone entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
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
          VENDOR_PHONE_TABLE, id, entity,
          reply -> {
            try {
              if (reply.succeeded()) {
                String persistenceId = reply.result();
                entity.setId(persistenceId);
                OutStream stream = new OutStream();
                stream.setData(entity);

                Response response = VendorPhoneResource.PostVendorPhoneResponse.
                  withJsonCreated(VENDOR_PHONE_LOCATION_PREFIX + persistenceId, stream);
                respond(asyncResultHandler, response);
              }
              else {
                log.error(reply.cause().getMessage(), reply.cause());
                Response response = VendorPhoneResource.PostVendorPhoneResponse.withPlainInternalServerError(reply.cause().getMessage());
                respond(asyncResultHandler, response);
              }
            }
            catch (Exception e) {
              log.error(e.getMessage(), e);

              Response response = VendorPhoneResource.PostVendorPhoneResponse.withPlainInternalServerError(e.getMessage());
              respond(asyncResultHandler, response);
            }

          }
        );
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);

        String errMsg = messages.getMessage(lang, MessageConsts.InternalServerError);
        Response response = VendorPhoneResource.PostVendorPhoneResponse.withPlainInternalServerError(errMsg);
        respond(asyncResultHandler, response);
      }

    });
  }

  @Override
  public void getVendorPhoneById(String vendorPhoneId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(v -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String idArgument = String.format("'%s'", vendorPhoneId);
        Criterion c = new Criterion(
          new Criteria().addField(idFieldName).setJSONB(false).setOperation("=").setValue(idArgument));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(VENDOR_PHONE_TABLE, VendorPhone.class, c, true,
          reply -> {
            try {
              if (reply.succeeded()) {
                @SuppressWarnings("unchecked")
                List<VendorPhone> results = (List<VendorPhone>) reply.result().getResults();
                if (results.isEmpty()) {
                  asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.GetVendorPhoneByIdResponse
                    .withPlainNotFound(vendorPhoneId)));
                }
                else{
                  asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.GetVendorPhoneByIdResponse
                    .withJsonOK(results.get(0))));
                }
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                if (isInvalidUUID(reply.cause().getMessage())) {
                  asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.GetVendorPhoneByIdResponse
                    .withPlainNotFound(vendorPhoneId)));
                }
                else{
                  asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.GetVendorPhoneByIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
                }
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.GetVendorPhoneByIdResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.GetVendorPhoneByIdResponse
          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
      }
    });
  }

  @Override
  public void deleteVendorPhoneById(String vendorPhoneId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);

    try {
      vertxContext.runOnContext(v -> {
        PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

        try {
          postgresClient.delete(VENDOR_PHONE_TABLE, vendorPhoneId, reply -> {
            if (reply.succeeded()) {
              asyncResultHandler.handle(Future.succeededFuture(
                VendorPhoneAPI.DeleteVendorPhoneByIdResponse.noContent()
                  .build()));
            } else {
              asyncResultHandler.handle(Future.succeededFuture(
                VendorPhoneAPI.DeleteVendorPhoneByIdResponse.
                  withPlainInternalServerError(reply.cause().getMessage())));
            }
          });
        } catch (Exception e) {
          asyncResultHandler.handle(Future.succeededFuture(
            VendorPhoneAPI.DeleteVendorPhoneByIdResponse.
              withPlainInternalServerError(e.getMessage())));
        }
      });
    }
    catch(Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
        VendorPhoneAPI.DeleteVendorPhoneByIdResponse.
          withPlainInternalServerError(e.getMessage())));
    }
  }

  @Override
  public void putVendorPhoneById(String vendorPhoneId, String lang, VendorPhone entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
      try {
        if(entity.getId() == null){
          entity.setId(vendorPhoneId);
        }
        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
          VENDOR_PHONE_TABLE, entity, vendorPhoneId,
          reply -> {
            try {
              if(reply.succeeded()){
                if (reply.result().getUpdated() == 0) {
                  asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.PutVendorPhoneByIdResponse
                    .withPlainNotFound(messages.getMessage(lang, MessageConsts.NoRecordsUpdated))));
                }
                else{
                  asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.PutVendorPhoneByIdResponse
                    .withNoContent()));
                }
              }
              else{
                log.error(reply.cause().getMessage());
                asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.PutVendorPhoneByIdResponse
                  .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.PutVendorPhoneByIdResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(Future.succeededFuture(VendorPhoneAPI.PutVendorPhoneByIdResponse
          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
      }
    });
  }
}
