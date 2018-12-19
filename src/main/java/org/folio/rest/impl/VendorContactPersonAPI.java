package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.VendorContactPerson;
import org.folio.rest.jaxrs.model.VendorContactPersonCollection;
import org.folio.rest.jaxrs.resource.VendorContactPersonResource;
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

public class VendorContactPersonAPI implements VendorContactPersonResource {
  private static final String VENDOR_CONTACT_PERSON_TABLE = "vendor_contact_person";
  private static final String VENDOR_CONTACT_PERSON_LOCATION_PREFIX = "/vendor_contact_person/";

  private static final Logger log = LoggerFactory.getLogger(VendorContactPersonAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "id";

  private static void respond(Handler<AsyncResult<Response>> handler, Response response) {
    AsyncResult<Response> result = Future.succeededFuture(response);
    handler.handle(result);
  }

  private boolean isInvalidUUID (String errorMessage) {
    return (errorMessage != null && errorMessage.contains("invalid input syntax for uuid"));
  }

  public VendorContactPersonAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
  }


  @Override
  public void getVendorContactPerson(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String[] fieldList = {"*"};
        CQL2PgJSON cql2PgJSON = new CQL2PgJSON(String.format("%s.jsonb", VENDOR_CONTACT_PERSON_TABLE));
        CQLWrapper cql = new CQLWrapper(cql2PgJSON, query)
          .setLimit(new Limit(limit))
          .setOffset(new Offset(offset));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(VENDOR_CONTACT_PERSON_TABLE, VendorContactPerson.class, fieldList, cql,
          true, false, reply -> {
            try {
              if(reply.succeeded()){
                VendorContactPersonCollection collection = new VendorContactPersonCollection();
                @SuppressWarnings("unchecked")
                List<VendorContactPerson> results = (List<VendorContactPerson>)reply.result().getResults();
                collection.setVendorContactPersons(results);
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
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorContactPersonResource.GetVendorContactPersonResponse
                  .withJsonOK(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorContactPersonResource.GetVendorContactPersonResponse
                  .withPlainBadRequest(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorContactPersonResource.GetVendorContactPersonResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorContactPersonResource.GetVendorContactPersonResponse
          .withPlainInternalServerError(message)));
      }
    });
  }

  @Override
  public void postVendorContactPerson(String lang, VendorContactPerson entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
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
          VENDOR_CONTACT_PERSON_TABLE, id, entity,
          reply -> {
            try {
              if (reply.succeeded()) {
                String persistenceId = reply.result();
                entity.setId(persistenceId);
                OutStream stream = new OutStream();
                stream.setData(entity);

                Response response = VendorContactPersonResource.PostVendorContactPersonResponse.
                  withJsonCreated(VENDOR_CONTACT_PERSON_LOCATION_PREFIX + persistenceId, stream);
                respond(asyncResultHandler, response);
              }
              else {
                log.error(reply.cause().getMessage(), reply.cause());
                Response response = VendorContactPersonResource.PostVendorContactPersonResponse.withPlainInternalServerError(reply.cause().getMessage());
                respond(asyncResultHandler, response);
              }
            }
            catch (Exception e) {
              log.error(e.getMessage(), e);

              Response response = VendorContactPersonResource.PostVendorContactPersonResponse.withPlainInternalServerError(e.getMessage());
              respond(asyncResultHandler, response);
            }

          }
        );
      }
      catch (Exception e) {
        log.error(e.getMessage(), e);

        String errMsg = messages.getMessage(lang, MessageConsts.InternalServerError);
        Response response = VendorContactPersonResource.PostVendorContactPersonResponse.withPlainInternalServerError(errMsg);
        respond(asyncResultHandler, response);
      }

    });
  }

  @Override
  public void getVendorContactPersonById(String vendorAccountPersonId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String idArgument = String.format("'%s'", vendorAccountPersonId);
        Criterion c = new Criterion(
          new Criteria().addField(idFieldName).setJSONB(false).setOperation("=").setValue(idArgument));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(VENDOR_CONTACT_PERSON_TABLE, VendorContactPerson.class, c, true,
          reply -> {
            try {
              if (reply.succeeded()) {
                @SuppressWarnings("unchecked")
                List<VendorContactPerson> results = (List<VendorContactPerson>) reply.result().getResults();
                if (results.isEmpty()) {
                  asyncResultHandler.handle(Future.succeededFuture(AccountAPI.GetAccountByIdResponse
                    .withPlainNotFound(vendorAccountPersonId)));
                }
                else{
                  asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.GetVendorContactPersonByIdResponse
                    .withJsonOK(results.get(0))));
                }
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                if (isInvalidUUID(reply.cause().getMessage())) {
                  asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.GetVendorContactPersonByIdResponse
                    .withPlainNotFound(vendorAccountPersonId)));
                }
                else{
                  asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.GetVendorContactPersonByIdResponse
                    .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
                }
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.GetVendorContactPersonByIdResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.GetVendorContactPersonByIdResponse
          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
      }
    });
  }

  @Override
  public void deleteVendorContactPersonById(String vendorContactPersonId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    String tenantId = TenantTool.tenantId(okapiHeaders);

    try {
      vertxContext.runOnContext(v -> {
        PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

        try {
          postgresClient.delete(VENDOR_CONTACT_PERSON_TABLE, vendorContactPersonId, reply -> {
            if (reply.succeeded()) {
              asyncResultHandler.handle(Future.succeededFuture(
                VendorContactPersonAPI.DeleteVendorContactPersonByIdResponse.noContent()
                  .build()));
            } else {
              asyncResultHandler.handle(Future.succeededFuture(
                VendorContactPersonAPI.DeleteVendorContactPersonByIdResponse.
                  withPlainInternalServerError(reply.cause().getMessage())));
            }
          });
        } catch (Exception e) {
          asyncResultHandler.handle(Future.succeededFuture(
            VendorContactPersonAPI.DeleteVendorContactPersonByIdResponse.
              withPlainInternalServerError(e.getMessage())));
        }
      });
    }
    catch(Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
        VendorContactPersonAPI.DeleteVendorContactPersonByIdResponse.
          withPlainInternalServerError(e.getMessage())));
    }
  }

  @Override
  public void putVendorContactPersonById(String vendorContactPersonId, String lang, VendorContactPerson entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    vertxContext.runOnContext(v -> {
      String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
      try {
        if(entity.getId() == null){
          entity.setId(vendorContactPersonId);
        }
        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
          VENDOR_CONTACT_PERSON_TABLE, entity, vendorContactPersonId,
          reply -> {
            try {
              if(reply.succeeded()){
                if (reply.result().getUpdated() == 0) {
                  asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.PutVendorContactPersonByIdResponse
                    .withPlainNotFound(messages.getMessage(lang, MessageConsts.NoRecordsUpdated))));
                }
                else{
                  asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.PutVendorContactPersonByIdResponse
                    .withNoContent()));
                }
              }
              else{
                log.error(reply.cause().getMessage());
                asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.PutVendorContactPersonByIdResponse
                  .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.PutVendorContactPersonByIdResponse
                .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        asyncResultHandler.handle(Future.succeededFuture(VendorContactPersonAPI.PutVendorContactPersonByIdResponse
          .withPlainInternalServerError(messages.getMessage(lang, MessageConsts.InternalServerError))));
      }
    });
  }
}
