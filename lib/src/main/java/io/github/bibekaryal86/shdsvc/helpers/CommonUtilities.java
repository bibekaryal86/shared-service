package io.github.bibekaryal86.shdsvc.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CommonUtilities {

  private static final ObjectMapper OBJECT_MAPPER;
  private static Map<String, String> propertiesMap = new HashMap<>();

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public static String getSystemEnvProperty(final String key, final String defaultValue) {
    final String value = propertiesMap.getOrDefault(key, defaultValue);
    if (defaultValue.equals(value)) {
      final String actualValue = getSystemEnvPropertyActual(key);
      if (isEmpty(actualValue)) {
        return defaultValue;
      }
      return actualValue;
    }
    return value;
  }

  public static String getSystemEnvProperty(final String key) {
    final String value = propertiesMap.get(key);
    if (isEmpty(value)) {
      return getSystemEnvPropertyActual(key);
    }
    return propertiesMap.get(key);
  }

  public static Map<String, String> getSystemEnvProperties(final List<String> keys) {
    if (isEmpty(propertiesMap)) {
      setSystemEnvProperties(keys);
    }
    return propertiesMap;
  }

  private static void setSystemEnvProperties(final List<String> keys) {
    final Map<String, String> tempMap = new HashMap<>();

    final Properties systemProperties = System.getProperties();
    systemProperties.forEach(
        (key, value) -> {
          if (keys.contains(key.toString())) {
            tempMap.put((String) key, (String) value);
          }
        });

    final Map<String, String> envVariables = System.getenv();
    envVariables.forEach(
        (key, value) -> {
          if (keys.contains(key)) {
            tempMap.put(key, value);
          }
        });

    propertiesMap = Collections.unmodifiableMap(tempMap);
  }

  private static String getSystemEnvPropertyActual(String key) {
    final String value = System.getProperty(key);
    if (isEmpty(value)) {
      return System.getenv(key);
    }
    return value;
  }

  public static boolean isEmpty(final String s) {
    return s == null || s.trim().isEmpty();
  }

  public static boolean isEmpty(final Collection<?> c) {
    return c == null || c.isEmpty();
  }

  public static boolean isEmpty(final Map<?, ?> m) {
    return m == null || m.isEmpty();
  }

  public static String getBasicAuth(final String appUsername, final String appPassword) {
    if (isEmpty(appUsername) || isEmpty(appPassword)) {
      throw new IllegalArgumentException("UserName or Password is Missing...");
    }
    return "Basic "
        + Base64.getEncoder()
            .encodeToString((appUsername + ":" + appPassword).getBytes(StandardCharsets.UTF_8));
  }

  public static ObjectMapper objectMapperProvider() {
    return OBJECT_MAPPER;
  }

  public static String writeValueAsStringNoEx(final Object value) {
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (Exception ignored) {
      return value.toString();
    }
  }

  public static byte[] writeValueAsBytesNoEx(final Object object) {
    try {
      return CommonUtilities.objectMapperProvider().writeValueAsBytes(object);
    } catch (JsonProcessingException ex) {
      return new byte[0];
    }
  }

  // single object input param: new TypeReference<SomeClass>() {}
  // list of objects input param: new TypeReference<List<Person>>() {}
  public static <T> T readValueNoEx(final String content, final TypeReference<T> valueTypeRef) {
    try {
      return OBJECT_MAPPER.readValue(content, valueTypeRef);
    } catch (Exception ignored) {
      return null;
    }
  }

  public static <T> T readValueNoEx(final byte[] content, final TypeReference<T> valueTypeRef) {
    try {
      return OBJECT_MAPPER.readValue(content, valueTypeRef);
    } catch (Exception ignored) {
      return null;
    }
  }

  public static <T> T readValueNoEx(final byte[] content, final Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(content, clazz);
    } catch (Exception ignored) {
      return null;
    }
  }

  public static int parseIntNoEx(final String value) {
    try {
      return Integer.parseInt(value);
    } catch (Exception ignored) {
      return 0;
    }
  }

  public static void convert(Object source, Object destination, List<String> exclusions) {
    Field[] sourceFields = source.getClass().getDeclaredFields();
    Field[] destinationFields = destination.getClass().getDeclaredFields();

    for (Field sourceField : sourceFields) {
      for (Field destinationField : destinationFields) {
        if (sourceField.getName().equals(destinationField.getName())
            && !exclusions.contains(sourceField.getName())) {
          try {
            sourceField.setAccessible(true);
            destinationField.setAccessible(true);
            destinationField.set(destination, sourceField.get(source));
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }
}
