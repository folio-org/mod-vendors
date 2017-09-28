package org.folio.rest.impl.transactions;

public interface TransactionCompletionHandler<T> {
  void success(T result);
  void failed(Exception e);
}
