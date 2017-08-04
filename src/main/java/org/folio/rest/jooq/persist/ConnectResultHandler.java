package org.folio.rest.jooq.persist;

import org.jooq.DSLContext;

public interface ConnectResultHandler {
  void success(DSLContext db);
  void failed(Exception exception);
}
