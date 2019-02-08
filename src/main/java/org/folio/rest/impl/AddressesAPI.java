package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Address;
import org.folio.rest.jaxrs.model.AddressCollection;
import org.folio.rest.jaxrs.resource.VendorStorageAddresses;
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
import org.folio.rest.annotations.Validate;
import org.folio.rest.persist.PgUtil;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddressesAPI implements VendorStorageAddresses {
  private static final String ADDRESS_TABLE = "address";
  private static final String ADDRESS_LOCATION_PREFIX = "/vendor-storage/addresses/";

  private static final Logger log = LoggerFactory.getLogger(AddressesAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "id";

  private static void respond(Handler<AsyncResult<Response>> handler, Response response) {
    AsyncResult<Response> result = Future.succeededFuture(response);
    handler.handle(result);
  }

  private boolean isInvalidUUID (String errorMessage) {
    return (errorMessage != null && errorMessage.contains("invalid input syntax for uuid"));
  }

  public AddressesAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
  }


  @Override
  public void getVendorStorageAddresses(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String[] fieldList = {"*"};
        CQL2PgJSON cql2PgJSON = new CQL2PgJSON(String.format("%s.jsonb", ADDRESS_TABLE));
        CQLWrapper cql = new CQLWrapper(cql2PgJSON, query)
          .setLimit(new Limit(limit))
          .setOffset(new Offset(offset));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(ADDRESS_TABLE, Address.class, fieldList, cql,
          true, false, reply -> {
            try {
              if(reply.succeeded()){
                AddressCollection collection = new AddressCollection();
                @SuppressWarnings("unchecked")
                List<Address> results = (List<Address>)reply.result().getResults();
                collection.setAddresses(results);
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
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetVendorStorageAddressesResponse
                  .respond200WithApplicationJson(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetVendorStorageAddressesResponse
                  .respond400WithTextPlain(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetVendorStorageAddressesResponse
                .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(GetVendorStorageAddressesResponse
          .respond500WithTextPlain(message)));
      }
    });
  }

  @Override
  @Validate
  public void postVendorStorageAddresses(String lang, org.folio.rest.jaxrs.model.Address entity,
                                        Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.post(ADDRESS_TABLE, entity, okapiHeaders, vertxContext, PostVendorStorageAddressesResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void getVendorStorageAddressesById(String id, String lang, Map<String, String> okapiHeaders,
                                           Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.getById(ADDRESS_TABLE, Address.class, id, okapiHeaders,vertxContext, GetVendorStorageAddressesByIdResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void deleteVendorStorageAddressesById(String id, String lang, Map<String, String> okapiHeaders,
                                              Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.deleteById(ADDRESS_TABLE, id, okapiHeaders, vertxContext, DeleteVendorStorageAddressesByIdResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void putVendorStorageAddressesById(String id, String lang, org.folio.rest.jaxrs.model.Address entity,
                                           Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.put(ADDRESS_TABLE, entity, id, okapiHeaders, vertxContext, PutVendorStorageAddressesByIdResponse.class, asyncResultHandler);
  }
}
