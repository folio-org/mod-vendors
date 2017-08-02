package org.folio.rest.jooq.persist;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author jbenito
 *
 * Convenience class that returns a Properties object given a properties file
 * that resides in src/main/resources
 */
public class PropertyLoader {
  public static Properties loadPropertiesFromResource(String relativePath) {
    InputStream stream = PropertyLoader.class.getResourceAsStream(relativePath);
    if (stream != null) {
      Properties props = new Properties();
      try {
        props.load(stream);
        stream.close();
        return props;
      } catch (Exception e) {

      }
    }
    return new Properties();
  }
}
