package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.VendorType;
import org.folio.rest.jaxrs.model.VendorTypeCollection;
import org.folio.rest.jaxrs.resource.VendorStorageVendorTypes;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;
import org.folio.rest.tools.utils.TenantTool;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;
import org.folio.rest.annotations.Validate;
import org.folio.rest.persist.PgUtil;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class VendorTypesAPI implements VendorStorageVendorTypes {
  private static final String VENDOR_TYPE_TABLE = "vendor_type";

  private static final Logger log = LoggerFactory.getLogger(VendorTypesAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "id";

  public VendorTypesAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
  }


  @Override
  public void getVendorStorageVendorTypes(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String[] fieldList = {"*"};
        CQL2PgJSON cql2PgJSON = new CQL2PgJSON(String.format("%s.jsonb", VENDOR_TYPE_TABLE));
        CQLWrapper cql = new CQLWrapper(cql2PgJSON, query)
          .setLimit(new Limit(limit))
          .setOffset(new Offset(offset));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(VENDOR_TYPE_TABLE, VendorType.class, fieldList, cql,
          true, false, reply -> {
            try {
              if(reply.succeeded()){
                VendorTypeCollection collection = new VendorTypeCollection();
                List<VendorType> results = reply.result().getResults();
                collection.setVendorTypes(results);
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
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendorTypes.GetVendorStorageVendorTypesResponse
                  .respond200WithApplicationJson(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendorTypes.GetVendorStorageVendorTypesResponse
                  .respond400WithTextPlain(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendorTypes.GetVendorStorageVendorTypesResponse
                .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendorTypes.GetVendorStorageVendorTypesResponse
          .respond500WithTextPlain(message)));
      }
    });
  }

  @Override
  @Validate
  public void postVendorStorageVendorTypes(String lang, org.folio.rest.jaxrs.model.VendorType entity,
                                        Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.post(VENDOR_TYPE_TABLE, entity, okapiHeaders, vertxContext, PostVendorStorageVendorTypesResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void getVendorStorageVendorTypesById(String id, String lang, Map<String, String> okapiHeaders,
                                           Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.getById(VENDOR_TYPE_TABLE, VendorType.class, id, okapiHeaders,vertxContext, GetVendorStorageVendorTypesByIdResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void deleteVendorStorageVendorTypesById(String id, String lang, Map<String, String> okapiHeaders,
                                              Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.deleteById(VENDOR_TYPE_TABLE, id, okapiHeaders, vertxContext, DeleteVendorStorageVendorTypesByIdResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void putVendorStorageVendorTypesById(String id, String lang, org.folio.rest.jaxrs.model.VendorType entity,
                                           Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.put(VENDOR_TYPE_TABLE, entity, id, okapiHeaders, vertxContext, PutVendorStorageVendorTypesByIdResponse.class, asyncResultHandler);
  }
}
