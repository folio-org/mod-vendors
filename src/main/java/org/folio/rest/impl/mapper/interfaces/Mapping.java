package org.folio.rest.impl.mapper.interfaces;

/**
 *
 * @param <S> JSON schema class defined in the RAML file
 * @param <T> The data model represented in the DB
 */
public abstract class Mapping<S, T> {
  /**
   *
   * @param apiModel  model object that represents an API's JSON schema
   * @param dbRecord  model object that represents a table in the DB
   */
  public abstract void mapEntityToDBRecord(S apiModel, T dbRecord);

  /**
   *
   * @param dbRecord  model object that represents a table in the DB
   * @return model object that represents an API's JSON schema
   */
  public abstract S mapDBRecordToEntity(T dbRecord);
}
