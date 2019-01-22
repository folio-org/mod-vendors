//package org.folio.rest.impl;
//
//import io.vertx.core.*;
//import io.vertx.core.logging.Logger;
//import io.vertx.core.logging.LoggerFactory;
//import org.folio.rest.RestVerticle;
//import org.folio.rest.annotations.Validate;
//import org.folio.rest.jaxrs.model.AccountCollection;
//import org.folio.rest.jaxrs.resource.VendorsAccounts;
//import org.folio.rest.persist.Criteria.Criteria;
//import org.folio.rest.persist.Criteria.Criterion;
//import org.folio.rest.persist.Criteria.Limit;
//import org.folio.rest.persist.Criteria.Offset;
//import org.folio.rest.persist.PostgresClient;
//import org.folio.rest.persist.cql.CQLWrapper;
//import org.folio.rest.tools.messages.MessageConsts;
//import org.folio.rest.tools.messages.Messages;
//import org.folio.rest.tools.utils.OutStream;
//import org.folio.rest.tools.utils.TenantTool;
//import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;
//
//import javax.ws.rs.core.Response;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class AccountsAPI implements VendorsAccounts {
//  private static final String ACCOUNT_TABLE = "account";
//  private static final String ACCOUNT_LOCATION_PREFIX = "/vendors/accounts/";
//
//  private static final Logger log = LoggerFactory.getLogger(AccountsAPI.class);
//  private final Messages messages = Messages.getInstance();
//  private String idFieldName = "id";
//
//  private static void respond(Handler<AsyncResult<Response>> handler, Response response) {
//    AsyncResult<Response> result = Future.succeededFuture(response);
//    handler.handle(result);
//  }
//
//  private boolean isInvalidUUID (String errorMessage) {
//    return (errorMessage != null && errorMessage.contains("invalid input syntax for uuid"));
//  }
//
//  public AccountsAPI(Vertx vertx, String tenantId) {
//    PostgresClient.getInstance(vertx, tenantId).setIdField(idFieldName);
//  }
//
//
//  @Override
//  @Validate
//  public void getVendorsAccounts(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
//    vertxContext.runOnContext((Void v) -> {
//      try {
//        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
//
//        String[] fieldList = {"*"};
//        CQL2PgJSON cql2PgJSON = new CQL2PgJSON(String.format("%s.jsonb", ACCOUNT_TABLE));
//        CQLWrapper cql = new CQLWrapper(cql2PgJSON, query)
//          .setLimit(new Limit(limit))
//          .setOffset(new Offset(offset));
//
//        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(ACCOUNT_TABLE, org.folio.rest.jaxrs.model.Account.class, fieldList, cql,
//          true, false, reply -> {
//            try {
//              if(reply.succeeded()){
//                AccountCollection collection = new AccountCollection();
//                @SuppressWarnings("unchecked")
//                List<org.folio.rest.jaxrs.model.Account> results = reply.result().getResults();
//                collection.setAccounts(results);
//                Integer totalRecords = reply.result().getResultInfo().getTotalRecords();
//                collection.setTotalRecords(totalRecords);
//                Integer first = 0;
//                Integer last = 0;
//                if (!results.isEmpty()) {
//                  first = offset + 1;
//                  last = offset + results.size();
//                }
//                collection.setFirst(first);
//                collection.setLast(last);
//                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorsAccounts.GetVendorsAccountsResponse
//                  .respond200WithApplicationJson(collection)));
//              }
//              else{
//                log.error(reply.cause().getMessage(), reply.cause());
//                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorsAccounts.GetVendorsAccountsResponse
//                  .respond400WithTextPlain(reply.cause().getMessage())));
//              }
//            } catch (Exception e) {
//              log.error(e.getMessage(), e);
//              asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorsAccounts.GetVendorsAccountsResponse
//                .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//            }
//          });
//      } catch (Exception e) {
//        log.error(e.getMessage(), e);
//        String message = messages.getMessage(lang, MessageConsts.InternalServerError);
//        if(e.getCause() != null && e.getCause().getClass().getSimpleName().endsWith("CQLParseException")){
//          message = " CQL parse error " + e.getLocalizedMessage();
//        }
//        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(VendorsAccounts.GetVendorsAccountsResponse
//          .respond500WithTextPlain(message)));
//      }
//    });
//  }
//
//  @Override
//  @Validate
//  public void postVendorsAccounts(String lang, org.folio.rest.jaxrs.model.Account entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
//    vertxContext.runOnContext(v -> {
//
//      try {
//        String id = UUID.randomUUID().toString();
//        if(entity.getId() == null){
//          entity.setId(id);
//        }
//        else{
//          id = entity.getId();
//        }
//
//        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
//        PostgresClient.getInstance(vertxContext.owner(), tenantId).save(
//          ACCOUNT_TABLE, id, entity,
//          reply -> {
//            try {
//              if (reply.succeeded()) {
//                String persistenceId = reply.result();
//                entity.setId(persistenceId);
//                OutStream stream = new OutStream();
//                stream.setData(entity);
//
//                Response response = VendorsAccounts.PostVendorsAccountsResponse.
//                  respond201WithApplicationJson(stream, VendorsAccounts.PostVendorsAccountsResponse.headersFor201().withLocation( ACCOUNT_LOCATION_PREFIX + persistenceId));
//                respond(asyncResultHandler, response);
//              }
//              else {
//                log.error(reply.cause().getMessage(), reply.cause());
//                Response response = VendorsAccounts.PostVendorsAccountsResponse.respond500WithTextPlain(reply.cause().getMessage());
//                respond(asyncResultHandler, response);
//              }
//            }
//            catch (Exception e) {
//              log.error(e.getMessage(), e);
//
//              Response response = VendorsAccounts.PostVendorsAccountsResponse.respond500WithTextPlain(e.getMessage());
//              respond(asyncResultHandler, response);
//            }
//
//          }
//        );
//      }
//      catch (Exception e) {
//        log.error(e.getMessage(), e);
//
//        String errMsg = messages.getMessage(lang, MessageConsts.InternalServerError);
//        Response response = VendorsAccounts.PostVendorsAccountsResponse.respond500WithTextPlain(errMsg);
//        respond(asyncResultHandler, response);
//      }
//
//    });
//  }
//
//  @Override
//  @Validate
//  public void getVendorsAccountsById(String accountId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
//    vertxContext.runOnContext(v -> {
//      try {
//        String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
//
//        String idArgument = String.format("'%s'", accountId);
//        Criterion c = new Criterion(
//          new Criteria().addField(idFieldName).setJSONB(false).setOperation("=").setValue(idArgument));
//
//        PostgresClient.getInstance(vertxContext.owner(), tenantId).get(ACCOUNT_TABLE, org.folio.rest.jaxrs.model.Account.class, c, true,
//          reply -> {
//            try {
//              if (reply.succeeded()) {
//                List<org.folio.rest.jaxrs.model.Account> results = reply.result().getResults();
//                if (results.isEmpty()) {
//                  asyncResultHandler.handle(Future.succeededFuture(GetVendorsAccountsByIdResponse
//                    .respond404WithTextPlain(accountId)));
//                }
//                else{
//                  asyncResultHandler.handle(Future.succeededFuture(GetVendorsAccountsByIdResponse
//                    .respond200WithApplicationJson(results.get(0))));
//                }
//              }
//              else{
//                log.error(reply.cause().getMessage(), reply.cause());
//                if (isInvalidUUID(reply.cause().getMessage())) {
//                  asyncResultHandler.handle(Future.succeededFuture(GetVendorsAccountsByIdResponse
//                    .respond404WithTextPlain(accountId)));
//                }
//                else{
//                  asyncResultHandler.handle(Future.succeededFuture(GetVendorsAccountsByIdResponse.respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//                }
//              }
//            } catch (Exception e) {
//              log.error(e.getMessage(), e);
//              asyncResultHandler.handle(Future.succeededFuture(GetVendorsAccountsByIdResponse
//                .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//            }
//          });
//      } catch (Exception e) {
//        log.error(e.getMessage(), e);
//        asyncResultHandler.handle(Future.succeededFuture(GetVendorsAccountsByIdResponse
//          .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//      }
//    });
//  }
//
//  @Override
//  @Validate
//  public void deleteVendorsAccountsById(String accountId, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
//    String tenantId = TenantTool.tenantId(okapiHeaders);
//
//    try {
//      vertxContext.runOnContext(v -> {
//        PostgresClient postgresClient = PostgresClient.getInstance(
//          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));
//
//        try {
//          postgresClient.delete(ACCOUNT_TABLE, accountId, reply -> {
//            if (reply.succeeded()) {
//              asyncResultHandler.handle(Future.succeededFuture(
//                VendorsAccounts.DeleteVendorsAccountsByIdResponse.noContent()
//                  .build()));
//            } else {
//              asyncResultHandler.handle(Future.succeededFuture(
//                VendorsAccounts.DeleteVendorsAccountsByIdResponse.
//                  respond500WithTextPlain(reply.cause().getMessage())));
//            }
//          });
//        } catch (Exception e) {
//          asyncResultHandler.handle(Future.succeededFuture(
//            VendorsAccounts.DeleteVendorsAccountsByIdResponse.
//              respond500WithTextPlain(e.getMessage())));
//        }
//      });
//    }
//    catch(Exception e) {
//      asyncResultHandler.handle(Future.succeededFuture(
//        VendorsAccounts.DeleteVendorsAccountsByIdResponse.
//          respond500WithTextPlain(e.getMessage())));
//    }
//  }
//
//  @Override
//  @Validate
//  public void putVendorsAccountsById(String accountId, String lang, org.folio.rest.jaxrs.model.Account entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
//    vertxContext.runOnContext(v -> {
//      String tenantId = TenantTool.calculateTenantId( okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT) );
//      try {
//        if(entity.getId() == null){
//          entity.setId(accountId);
//        }
//        PostgresClient.getInstance(vertxContext.owner(), tenantId).update(
//          ACCOUNT_TABLE, entity, accountId,
//          reply -> {
//            try {
//              if(reply.succeeded()){
//                if (reply.result().getUpdated() == 0) {
//                  asyncResultHandler.handle(Future.succeededFuture(PutVendorsAccountsByIdResponse
//                    .respond404WithTextPlain(messages.getMessage(lang, MessageConsts.NoRecordsUpdated))));
//                }
//                else{
//                  asyncResultHandler.handle(Future.succeededFuture(PutVendorsAccountsByIdResponse
//                    .respond204()));
//                }
//              }
//              else{
//                log.error(reply.cause().getMessage());
//                asyncResultHandler.handle(Future.succeededFuture(PutVendorsAccountsByIdResponse
//                  .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//              }
//            } catch (Exception e) {
//              log.error(e.getMessage(), e);
//              asyncResultHandler.handle(Future.succeededFuture(PutVendorsAccountsByIdResponse
//                .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//            }
//          });
//      } catch (Exception e) {
//        log.error(e.getMessage(), e);
//        asyncResultHandler.handle(Future.succeededFuture(PutVendorsAccountsByIdResponse
//          .respond500WithTextPlain(messages.getMessage(lang, MessageConsts.InternalServerError))));
//      }
//    });
//  }
//}
