package org.folio.rest.impl;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
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
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

@RunWith(VertxUnitRunner.class)
public class VendorAPITest {
  private Vertx vertx;
  private Async async;
  private final Logger logger = LoggerFactory.getLogger("okapi");
  private final int port = Integer.parseInt(System.getProperty("port", "8081"));

  @Before
  public void before(TestContext context) {
    vertx = Vertx.vertx();

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
      async.complete();
    });
  }

  @Test
  public void testGetCategories(TestContext context) {
    async = context.async();
    logger.info("-- getCategories() test starting ... ");

    given()
      .header("X-Okapi-Tenant","diku")
      .contentType(ContentType.JSON)
      .get("vendorCategory")
      .then().statusCode(200);

    logger.info("-- getCategories() test done. ");
    async.complete();
  }

  @Test
  public void testGetVendors(TestContext context) {
    async = context.async();
    logger.info("--- getVendors() test starting ... ");

    emptyCollection();

    logger.info("--- getVendors() test done. ");
    async.complete();
  }

  // Validates that there are zero vendor records in the DB
  private void emptyCollection() {
    Response response = getVendors();

    // Validate 200 response and that there are zero records
    response.then()
      .statusCode(200)
      .body("total_records", equalTo(0))
      .body("vendors", empty());
  }

  @Test
  public void testCreateVendor(TestContext context) {
    async = context.async();
    logger.info("--- createVendor() test starting ... ");
    logger.info("--- createVendor(): posting new vendor info ... ");

    emptyCollection();

    Response response = createVendorFromSampleFile();
    response.then()
      .statusCode(201)
      .body("tax_id", equalTo("TX-GOBI-HIST"));

    // If we get to this point, we've validated that the POST was successful
    // Grab the ID then delete the newly added Vendor
    String vendorID = response.then().extract().path("id");
    logger.info("--- createVendor(): deleting ID '" + vendorID + "'");
    response = deleteVendor(vendorID);

    // Assert that the data was successfully deleted
    response.then().statusCode(204);

    logger.info("--- createVendor() test done. ");
    async.complete();
  }

  @Test
  public void testEditVendor(TestContext context) {
    async = context.async();
    logger.info("--- editVendor() test starting ... ");

    emptyCollection();

    Response response = createVendorFromSampleFile();
    response.then()
      .statusCode(201)
      .body("tax_id", equalTo("TX-GOBI-HIST"));

    String vendorID = response.then().extract().path("id");
    String responseBody = response.getBody().asString();
    try {
      JSONObject payload = new JSONObject(responseBody);
      payload.put("language", "French");

      logger.info("--- editVendor(): putting edited vendor info ... ");
      // Edit the vendor info with a new language
      String editVendorInput = payload.toString();
      response = putVendor(vendorID, editVendorInput);
      response.then().statusCode(204);

      // Query for this edited entry
      logger.info("--- editVendor(): querying to validate edited record ... ");
      response = getVendorById(vendorID);
      response.then()
        .statusCode(200)
        .body("language", equalTo("French"))
        .body("id", equalTo(vendorID));
      logger.info("--- editVendor(): verified that the record has been edited");

      logger.info("--- editVendor(): deleting ID '" + vendorID + "'");
      response = deleteVendor(vendorID);

      // Assert that the data was successfully deleted
      response.then().statusCode(204);
    } catch (JSONException e) {
      context.fail("--- editVendor() Error: " + e.getMessage());
    }

    logger.info("--- editVendor() test done. ");
    async.complete();
  }

  private Response createVendorFromSampleFile() {
    // Populate the DB with a sample vendor
    String postBody;
    try {
      postBody = getFile("vendor_post.sample");
    } catch (Exception e) {
      postBody = "";
    }
    return postVendor(postBody);
  }

  private Response getVendors() {
    return given()
      .header("X-Okapi-Tenant","diku")
      .contentType(ContentType.JSON)
      .get("vendor");
  }

  private Response getVendorById(String vendorId) {
    return given()
      .pathParam("vendor_id", vendorId)
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .get("vendor/{vendor_id}");
  }

  private Response postVendor(String postBody) {
    return given()
      .header("X-Okapi-Tenant", "diku")
      .accept(ContentType.JSON)
      .contentType(ContentType.JSON)
      .body(postBody)
      .post("vendor");
  }

  private Response putVendor(String vendorId, String input) {
    return given()
      .pathParam("vendor_id", vendorId)
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .body(input)
      .put("vendor/{vendor_id}");
  }

  private Response deleteVendor(String vendorId) {
    return given()
      .pathParam("vendor_id", vendorId)
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .delete("vendor/{vendor_id}");
  }

  private String getFile(String filename) throws IOException {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
    return IOUtils.toString(inputStream, "UTF-8");
  }

}
