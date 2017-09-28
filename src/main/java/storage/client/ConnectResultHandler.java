package storage.client;

import org.jooq.DSLContext;

public interface ConnectResultHandler {
  void success(DSLContext db);
  void failed(Exception exception);
}
