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
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.IOException;
import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;

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
    RestAssured.basePath = "/vendor";
  }

  @After
  public void after(TestContext context) {
    async = context.async();
    vertx.close(res -> {   // This logs a stack trace, ignore it.
      async.complete();
    });
  }

  @Test
  public void testGetVendors(TestContext context) {
    async = context.async();
    logger.info("--- getVendors() test starting ... ");
    Response response = getVendors();

    // Validate 200 response
    response.then().statusCode(200);

    logger.info("--- getVendors() test done. ");
    async.complete();
  }

  @Test
  public void testCreateVendor(TestContext context) {
    async = context.async();
    logger.info("--- createVendor() test starting ... ");
    logger.info("--- createVendor(): posting new vendor info ... ");
    Response response = postVendor();

    // Assert that we got a 201
    response.then().statusCode(201);

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
    Response response = postVendor();

    // Assert that we got a 201
    response.then().statusCode(201);

    String vendorID = response.then().extract().path("id");
    String responseBody = response.getBody().asString();
    logger.info("--- editVendor() RESPONSE BODY: "+ responseBody);
    try {
      JSONObject payload = new JSONObject(responseBody);
      payload.put("language", "French");

      logger.info("--- editVendor(): putting edited vendor info ... ");
      String editVendorInput = payload.toString();
      response = putVendor(vendorID, editVendorInput);

      // Assert a 204
      response.then().statusCode(204);

      // TODO: Run a search that the language actually changed

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

  private Response getVendors() {
    Response response = given()
      .header("X-Okapi-Tenant","diku")
      .contentType(ContentType.JSON)
      .get();

    return response;
  }

  private Response postVendor() {
    String postBody;
    try {
      postBody = getFile("vendor_post.sample");
    } catch (Exception e) {
      postBody = "";
    }

    return given()
      .header("X-Okapi-Tenant", "diku")
      .accept(ContentType.JSON)
      .contentType(ContentType.JSON)
      .body(postBody)
      .post();
  }

  private Response putVendor(String vendorId, String input) {
    return given()
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .body(input)
      .put(vendorId);
  }

  private Response deleteVendor(String vendorID) {
    return given()
      .header("X-Okapi-Tenant", "diku")
      .contentType(ContentType.JSON)
      .delete(vendorID);
  }

  private String getFile(String filename) throws IOException {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
    return IOUtils.toString(inputStream, "UTF-8");
  }


}
