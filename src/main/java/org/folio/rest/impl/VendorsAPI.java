package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.rest.RestVerticle;
import org.folio.rest.jaxrs.model.Vendor;
import org.folio.rest.jaxrs.model.VendorCollection;
import org.folio.rest.jaxrs.resource.VendorStorageVendors;
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

public class VendorsAPI implements VendorStorageVendors {
  private static final String VENDOR_TABLE = "vendor";

  private static final Logger log = LoggerFactory.getLogger(VendorsAPI.class);
  private final Messages messages = Messages.getInstance();
  private String idFieldName = "id";

  public VendorsAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
  }

  @Override
  public void getVendorStorageVendors(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    vertxContext.runOnContext((Void v) -> {
      try {
        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );

        String[] fieldList = {"*"};
        CQL2PgJSON cql2PgJSON = new CQL2PgJSON(String.format("%s.jsonb", VENDOR_TABLE));
        CQLWrapper cql = new CQLWrapper(cql2PgJSON, query)
          .setLimit(new Limit(limit))
          .setOffset(new Offset(offset));

        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(VENDOR_TABLE, Vendor.class, fieldList, cql,
          true, false, reply -> {
            try {
              if(reply.succeeded()){
                VendorCollection collection = new VendorCollection();
                @SuppressWarnings("unchecked")
                List<Vendor> results = reply.result().getResults();
                collection.setVendors(results);
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
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendors.GetVendorStorageVendorsResponse
                  .respond200WithApplicationJson(collection)));
              }
              else{
                log.error(reply.cause().getMessage(), reply.cause());
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendors.GetVendorStorageVendorsResponse
                  .respond400WithTextPlain(reply.cause().getMessage())));
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendors.GetVendorStorageVendorsResponse
                .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
            }
          });
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
          message = " CQL parse error " + e.getLocalizedMessage();
        }
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorStorageVendors.GetVendorStorageVendorsResponse
          .respond500WithTextPlain(message)));
      }
    });
  }

  @Override
  @Validate
  public void postVendorStorageVendors(String lang, org.folio.rest.jaxrs.model.Vendor entity,
                                        Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.post(VENDOR_TABLE, entity, okapiHeaders, vertxContext, PostVendorStorageVendorsResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void getVendorStorageVendorsById(String id, String lang, Map<String, String> okapiHeaders,
                                           Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.getById(VENDOR_TABLE, Vendor.class, id, okapiHeaders,vertxContext, GetVendorStorageVendorsByIdResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void deleteVendorStorageVendorsById(String id, String lang, Map<String, String> okapiHeaders,
                                              Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.deleteById(VENDOR_TABLE, id, okapiHeaders, vertxContext, DeleteVendorStorageVendorsByIdResponse.class, asyncResultHandler);
  }

  @Override
  @Validate
  public void putVendorStorageVendorsById(String id, String lang, org.folio.rest.jaxrs.model.Vendor entity,
                                           Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    PgUtil.put(VENDOR_TABLE, entity, id, okapiHeaders, vertxContext, PutVendorStorageVendorsByIdResponse.class, asyncResultHandler);
  }
}
