package org.folio.rest.impl;

import org.folio.rest.jaxrs.model.Error;

public class ErrorFactory {
  public static Error badRequest() {
    Error err = new Error();
//    err.setError("Bad request");
    return err;
  }

  public static Error forbidden() {
    Error err = new Error();
//    err.setError("Forbidden");
    return err;
  }

  public static Error notFound() {
    Error err = new Error();
//    err.setError("Resource not found");
    return err;
  }
}
