package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
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
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(VertxUnitRunner.class)
public class VendorsTest {
  private Vertx vertx;
  private Async async;
  private final Logger logger = LoggerFactory.getLogger("okapi");
  private final int port = Integer.parseInt(System.getProperty("port", "8081"));

  private final String TENANT_NAME = "diku";
  private final Header TENANT_HEADER = new Header("X-Okapi-Tenant", TENANT_NAME);

  private String moduleName;      // "mod_vendors";
  private String moduleVersion;   // "1.0.0"
  private String moduleId;        // "mod-vendors-1.0.0"


  @Before
  public void before(TestContext context) {
    logger.info("--- mod-vendors-test: START ");
    vertx = Vertx.vertx();

    moduleName = PomReader.INSTANCE.getModuleName();
    moduleVersion = PomReader.INSTANCE.getVersion();

    moduleId = String.format("%s-%s", moduleName, moduleVersion);

    // RMB returns a 'normalized' name, with underscores
    moduleId = moduleId.replaceAll("_", "-");

    try {
      // Run this test in embedded postgres mode
      // IMPORTANT: Later we will initialize the schema by calling the tenant interface.
      PostgresClient.setIsEmbedded(true);
      PostgresClient.getInstance(vertx).startEmbeddedPostgres();
      PostgresClient.getInstance(vertx).dropCreateDatabase(TENANT_NAME + "_" + PomReader.INSTANCE.getModuleName());

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
      PostgresClient.stopEmbeddedPostgres();
      async.complete();
      logger.info("--- mod-vendors-test: END ");
    });
  }

  // Validates that there are zero vendor records in the DB
  private void verifyCollection() {

    // Validate that contact_category is prepopulated with 12 records
    // and this particular call returns a default limit of 10
    getData("contact_category").then()
      .log().ifValidationFails()
      .statusCode(200)
      .body("total_records", equalTo(12))
      .body("categories.size()", is(10));

    // Validate that vendor_category is prepopulated with 4 records
    // and this particular call returns all 4
    getData("vendor_category").then()
      .log().ifValidationFails()
      .statusCode(200)
      .body("total_records", equalTo(4))
      .body("categories.size()", is(4));

    // Verify that there are no existing vendor records
    getData("vendor").then()
      .log().ifValidationFails()
      .statusCode(200)
      .body("total_records", equalTo(0))
      .body("vendors", empty());
  }

  @Test
  public void tests(TestContext context) {
    async = context.async();
    try {

      // IMPORTANT: Call the tenant interface to initialize the tenant-schema
      logger.info("--- mod-vendors-test: Preparing test tenant");
      prepareTenant();

      logger.info("--- mod-vendors-test: Verifying database's initial state ... ");
      verifyCollection();

      logger.info("--- mod-vendors-test: Creating category ... ");
      String catSample = getFile("category.sample");
      Response response = postData("contact_category", catSample);
      response.then().log().ifValidationFails()
        .statusCode(201)
        .body("value", equalTo("Accounting"));
      String contact_category_id = response.then().extract().path("id");

      logger.info("--- mod-vendors-test: Verifying only 1 category was created ... ");
      getData("contact_category").then().log().ifValidationFails()
        .statusCode(200)
        .body("total_records", equalTo(13));

      logger.info("--- mod-vendors-test: Fetching category with ID:"+ contact_category_id);
      getDataById("contact_category", contact_category_id).then().log().ifValidationFails()
        .statusCode(200)
        .body("id", equalTo(contact_category_id));

      logger.info("--- mod-vendors-test: Editing category with ID:"+ contact_category_id);
      JSONObject catJSON = new JSONObject(catSample);
      catJSON.put("id", contact_category_id);
      catJSON.put("value", "Customer Service");
      response = putData("contact_category", contact_category_id, catJSON.toString());
      response.then().log().ifValidationFails()
        .statusCode(204);

      logger.info("--- mod-vendors-test: Fetching category with ID:"+ contact_category_id);
      getDataById("contact_category", contact_category_id).then()
        .statusCode(200).log().ifValidationFails()
        .body("value", equalTo("Customer Service"));

      logger.info("--- mod-vendors-test: Deleting contact-category with id ... ");
      deleteData("contact_category", contact_category_id).then().log().ifValidationFails()
        .statusCode(204);


      logger.info("--- mod-vendors-test: Creating vendor category ... ");
      String vendCatSample = getFile("category.sample");
      response = postData("vendor_category", vendCatSample);
      response.then().log().ifValidationFails()
        .statusCode(201)
        .body("value", equalTo("Accounting"));
      String vendor_category_id = response.then().extract().path("id");

      logger.info("--- mod-vendors-test: Verifying only 1 vendor category was created ... ");
      getData("vendor_category").then().log().ifValidationFails()
        .statusCode(200)
        .body("total_records", equalTo(5));

      logger.info("--- mod-vendors-test: Fetching vendor category with ID:"+ vendor_category_id);
      getDataById("vendor_category", vendor_category_id).then()
        .statusCode(200).log().ifValidationFails()
        .body("id", equalTo(vendor_category_id));

      logger.info("--- mod-vendors-test: Editing vendor category with ID:"+ vendor_category_id);
      JSONObject vendCatJSON = new JSONObject(vendCatSample);
      vendCatJSON.put("id", vendor_category_id);
      vendCatJSON.put("value", "Customer Service");
      response = putData("vendor_category", vendor_category_id, vendCatJSON.toString());
      response.then().log().ifValidationFails()
        .statusCode(204);

      logger.info("--- mod-vendors-test: Fetching category with ID:"+ vendor_category_id);
      getDataById("vendor_category", vendor_category_id).then().log().ifValidationFails()
        .statusCode(200)
        .body("value", equalTo("Customer Service"));

      logger.info("--- mod-vendors-test: Deleting vendor-category with id ... ");
      deleteData("vendor_category", vendor_category_id).then().log().ifValidationFails()
        .statusCode(204);


      logger.info("--- mod-vendors-test: Creating vendor ... ");
      String vendorSample = getFile("vendor.sample");
      response = postData("vendor", vendorSample);
      response.then().log().ifValidationFails()
        .statusCode(201)
        .body("name", equalTo("GOBI"));
      String vendor_id = response.then().extract().path("id");

      logger.info("--- mod-vendors-test: Verifying only 1 vendor was created ... ");
      getData("vendor").then().log().ifValidationFails()
        .statusCode(200)
        .body("total_records", equalTo(1));

      logger.info("--- mod-vendors-test: Fetching vendor with ID:"+ vendor_id);
      getDataById("vendor", vendor_id).then().log().ifValidationFails()
        .statusCode(200)
        .body("id", equalTo(vendor_id));

      logger.info("--- mod-vendors-test: Editing vendor with ID:"+ vendor_id);
      JSONObject vendorJSON = new JSONObject(vendorSample);
      vendorJSON.put("id", vendor_id);
      vendorJSON.put("name", "G.O.B.I.");
      response = putData("vendor", vendor_id, vendorJSON.toString());
      response.then().log().ifValidationFails()
        .statusCode(204);

      logger.info("--- mod-vendors-test: Fetching vendor with ID:"+ vendor_id);
      getDataById("vendor", vendor_id).then().log().ifValidationFails()
        .statusCode(200)
        .body("name", equalTo("G.O.B.I."));

      logger.info("--- mod-vendors-test: Deleting vendor with id ... ");
      deleteData("vendor", vendor_id).then().log().ifValidationFails()
        .statusCode(204);
    }
    catch (Exception e) {
      context.fail("--- mod-vendors-test: ERROR: " + e.getMessage());
    }
    async.complete();
  }

  private void prepareTenant() {
    String tenants = "{\"module_to\":\"" + moduleId + "\"}";
    given()
      .header(TENANT_HEADER)
      .contentType(ContentType.JSON)
      .body(tenants)
      .post("/_/tenant")
      .then().log().ifValidationFails()
      .statusCode(201);
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
      .header("X-Okapi-Tenant", TENANT_NAME)
      .contentType(ContentType.JSON)
      .get(endpoint);
  }

  private Response getDataById(String endpoint, String id) {
    return given()
      .pathParam("id", id)
      .header("X-Okapi-Tenant", TENANT_NAME)
      .contentType(ContentType.JSON)
      .get(endpoint + "/{id}");
  }

  private Response postData(String endpoint, String input) {
    return given()
      .header("X-Okapi-Tenant", TENANT_NAME)
      .accept(ContentType.JSON)
      .contentType(ContentType.JSON)
      .body(input)
      .post(endpoint);
  }

  private Response putData(String endpoint, String id, String input) {
    return given()
      .pathParam("id", id)
      .header("X-Okapi-Tenant", TENANT_NAME)
      .contentType(ContentType.JSON)
      .body(input)
      .put(endpoint + "/{id}");
  }

  private Response deleteData(String endpoint, String id) {
    return given()
      .pathParam("id", id)
      .header("X-Okapi-Tenant", TENANT_NAME)
      .contentType(ContentType.JSON)
      .delete(endpoint + "/{id}");
  }
}
