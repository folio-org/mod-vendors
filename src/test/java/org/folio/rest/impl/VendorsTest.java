package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

@RunWith(VertxUnitRunner.class)
public class VendorsTest {
  private Vertx vertx;
  private Async async;
  private final Logger logger = LoggerFactory.getLogger("okapi");
  private final int port = Integer.parseInt(System.getProperty("port", "8081"));

  @Before
  public void before(TestContext context) {
    logger.info("--- mod-vendors-test: START ");
    vertx = Vertx.vertx();

    try {
      PostgresClient.setIsEmbedded(true);
      PostgresClient.getInstance(vertx).startEmbeddedPostgres();
    } catch (Exception e) {
      e.printStackTrace();
      context.fail(e);
      return;
    }

    // Deploy a verticle
    JsonObject conf = new JsonObject()
      .put(HttpClientMock2.MOCK_MODE, "true")
      .put("http.port", port);
    DeploymentOptions opt = new DeploymentOptions()
      .setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(),
      opt, context.asyncAssertSuccess());

    // Set the default headers for the API calls to be tested
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
  }

  @After
  public void after(TestContext context) {
    async = context.async();
    vertx.close(res -> {   // This logs a stack trace, ignore it.
      logger.info("--- mod-vendors-test: END ");
      PostgresClient.stopEmbeddedPostgres();
      async.complete();
    });
  }

  // Validates that there are zero vendor records in the DB
  private void emptyCollection() {

    // Validate 200 response and that there are zero records
    getData("contact_category").then()
      .statusCode(200)
      .body("total_records", equalTo(0))
      .body("categories", empty());

    getData("vendor_category").then()
      .statusCode(200)
      .body("total_records", equalTo(0))
      .body("categories", empty());

    getData("vendor").then()
      .statusCode(200)
      .body("total_records", equalTo(0))
      .body("vendors", empty());
  }

  @Test
  public void testOrders(TestContext context) {
    async = context.async();
    try {
      logger.info("--- mod-vendors-test: Verifying empty database ... ");
      emptyCollection();

      logger.info("--- mod-vendors-test: Creating category ... ");
      String catSample = getFile("category.sample");
      Response response = postData("contact_category", catSample);
      response.then()
        .statusCode(201)
        .body("value", equalTo("Accounting"));
      String contact_category_id = response.then().extract().path("id");

      logger.info("--- mod-vendors-test: Verifying only 1 category was created ... ");
      getData("contact_category").then()
        .statusCode(200)
        .body("total_records", equalTo(1));

      logger.info("--- mod-vendors-test: Fetching category with ID:"+ contact_category_id);
      getDataById("contact_category", contact_category_id).then()
        .statusCode(200)
        .body("id", equalTo(contact_category_id));

      logger.info("--- mod-vendors-test: Editing category with ID:"+ contact_category_id);
      JSONObject catJSON = new JSONObject(catSample);
      catJSON.put("id", contact_category_id);
      catJSON.put("value", "Customer Service");
      response = putData("contact_category", contact_category_id, catJSON.toString());
      response.then()
        .statusCode(204);

      logger.info("--- mod-vendors-test: Fetching category with ID:"+ contact_category_id);
      getDataById("contact_category", contact_category_id).then()
        .statusCode(200)
        .body("value", equalTo("Customer Service"));

      logger.info("--- mod-vendors-test: Deleting contact-category with id ... ");
      deleteData("contact_category", contact_category_id).then()
        .statusCode(204);


      logger.info("--- mod-vendors-test: Creating vendor category ... ");
      String vendCatSample = getFile("category.sample");
      response = postData("vendor_category", vendCatSample);
      response.then()
        .statusCode(201)
        .body("value", equalTo("Accounting"));
      String vendor_category_id = response.then().extract().path("id");

      logger.info("--- mod-vendors-test: Verifying only 1 vendor category was created ... ");
      getData("vendor_category").then()
        .statusCode(200)
        .body("total_records", equalTo(1));

      logger.info("--- mod-vendors-test: Fetching vendor category with ID:"+ vendor_category_id);
      getDataById("vendor_category", vendor_category_id).then()
        .statusCode(200)
        .body("id", equalTo(vendor_category_id));

      logger.info("--- mod-vendors-test: Editing vendor category with ID:"+ vendor_category_id);
      JSONObject vendCatJSON = new JSONObject(vendCatSample);
      vendCatJSON.put("id", vendor_category_id);
      vendCatJSON.put("value", "Customer Service");
      response = putData("vendor_category", vendor_category_id, vendCatJSON.toString());
      response.then()
        .statusCode(204);

      logger.info("--- mod-vendors-test: Fetching category with ID:"+ vendor_category_id);
      getDataById("vendor_category", vendor_category_id).then()
        .statusCode(200)
        .body("value", equalTo("Customer Service"));

      logger.info("--- mod-vendors-test: Deleting vendor-category with id ... ");
      deleteData("vendor_category", vendor_category_id).then()
        .statusCode(204);


      logger.info("--- mod-vendors-test: Creating vendor ... ");
      String vendorSample = getFile("vendor.sample");
      response = postData("vendor", vendorSample);
      response.then()
        .statusCode(201)
        .body("name", equalTo("GOBI"));
      String vendor_id = response.then().extract().path("id");

      logger.info("--- mod-vendors-test: Verifying only 1 vendor was created ... ");
      getData("vendor").then()
        .statusCode(200)
        .body("total_records", equalTo(1));

      logger.info("--- mod-vendors-test: Fetching vendor with ID:"+ vendor_id);
      getDataById("vendor", vendor_id).then()
        .statusCode(200)
        .body("id", equalTo(vendor_id));

      logger.info("--- mod-vendors-test: Editing vendor with ID:"+ vendor_id);
      JSONObject vendorJSON = new JSONObject(vendorSample);
      vendorJSON.put("id", vendor_id);
      vendorJSON.put("name", "G.O.B.I.");
      response = putData("vendor", vendor_id, vendorJSON.toString());
      response.then()
        .statusCode(204);

      logger.info("--- mod-vendors-test: Fetching vendor with ID:"+ vendor_id);
      getDataById("vendor", vendor_id).then()
        .statusCode(200)
        .body("name", equalTo("G.O.B.I."));

      logger.info("--- mod-vendors-test: Deleting vendor with id ... ");
      deleteData("vendor", vendor_id).then()
        .statusCode(204);
    }
    catch (Exception e) {
      context.fail("--- mod-vendors-test: ERROR: " + e.getMessage());
    }
    async.complete();
  }

  private String getFile(String filename) {
    String value;
    try {
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
      value = IOUtils.toString(inputStream, "UTF-8");
    } catch (Exception e) {
      value = "";
    }
    return value;
  }

  private Response getData(String endpoint) {
    return given()
      .header("X-Okapi-Tenant","diku")
      .contentType(ContentType.JSON)
      .get(endpoint);
  }

  private Response getDataById(String endpoint, String id) {
    return given()
      .pathParam("id", id)
      .header("X-Okapi-Tenant","diku")
      .contentType(ContentType.JSON)
      .get(endpoint + "/{id}");
  }

  private Response postData(String endpoint, String input) {
    return given()
      .header("X-Okapi-Tenant", "diku")
      .accept(ContentType.JSON)
      .contentType(ContentType.JSON)
      .body(input)
      .post(endpoint);
  }

  private Response putData(String endpoint, String id, String input) {
    return given()
      .pathParam("id", id)
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .body(input)
      .put(endpoint + "/{id}");
  }

  private Response deleteData(String endpoint, String id) {
    return given()
      .pathParam("id", id)
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .delete(endpoint + "/{id}");
  }
}
