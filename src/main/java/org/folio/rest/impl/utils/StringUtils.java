package org.folio.rest.impl.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
  public static Timestamp timestampFromString(String input, String pattern) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
      Date parsedDate = dateFormat.parse(input);
      return new Timestamp(parsedDate.getTime());
    } catch (ParseException e) {
      return null;
    }
  }

  public static Time timeFromString(String input, String pattern) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
      Date parsedDate = dateFormat.parse(input);
      return new Time(parsedDate.getTime());
    } catch (ParseException e) {
      return null;
    }
  }
}
