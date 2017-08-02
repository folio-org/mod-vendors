package org.folio.rest.jooq.persist;

public interface ResultHandler<S, E> {
  void success(S response);
  void failed(E exception);
}
