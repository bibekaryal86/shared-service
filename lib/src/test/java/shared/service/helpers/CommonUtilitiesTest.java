package shared.service.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommonUtilitiesTest {

  private static final String TEST_KEY = "TEST_KEY";
  private static final String TEST_VALUE = "TEST_VALUE";
  private static final String DEFAULT_VALUE = "DEFAULT_VALUE";

  @BeforeEach
  void setup() {
    System.setProperty(TEST_KEY, TEST_VALUE);
  }

  @AfterEach
  void cleanup() {
    System.clearProperty(TEST_KEY);
  }

  @Test
  void getSystemEnvProperty_checksSystemPropertyFirst() {
    String value = CommonUtilities.getSystemEnvProperty(TEST_KEY, DEFAULT_VALUE);
    assertEquals(TEST_VALUE, value, "Expected system property value to be returned.");
  }

  @Test
  void getSystemEnvProperty_defaultsWhenNotFound() {
    System.clearProperty(TEST_KEY);
    String value = CommonUtilities.getSystemEnvProperty(TEST_KEY, DEFAULT_VALUE);
    assertEquals(DEFAULT_VALUE, value, "Expected default value when property is not found.");
  }

  @Test
  void isEmpty_stringIsNull_returnsTrue() {
    assertTrue(
        CommonUtilities.isEmpty((String) null), "Expected isEmpty to return true for null string.");
  }

  @Test
  void isEmpty_stringIsEmpty_returnsTrue() {
    assertTrue(CommonUtilities.isEmpty(""), "Expected isEmpty to return true for empty string.");
  }

  @Test
  void isEmpty_stringIsWhitespace_returnsTrue() {
    assertTrue(
        CommonUtilities.isEmpty("   "), "Expected isEmpty to return true for whitespace string.");
  }

  @Test
  void isEmpty_stringIsNotEmpty_returnsFalse() {
    assertFalse(
        CommonUtilities.isEmpty("Hello"), "Expected isEmpty to return false for non-empty string.");
  }

  @Test
  void isEmpty_collectionIsNull_returnsTrue() {
    assertTrue(
        CommonUtilities.isEmpty((Collection<?>) null),
        "Expected isEmpty to return true for null collection.");
  }

  @Test
  void isEmpty_collectionIsEmpty_returnsTrue() {
    assertTrue(
        CommonUtilities.isEmpty(List.of()),
        "Expected isEmpty to return true for empty collection.");
  }

  @Test
  void isEmpty_collectionIsNotEmpty_returnsFalse() {
    assertFalse(
        CommonUtilities.isEmpty(List.of(1, 2, 3)),
        "Expected isEmpty to return false for non-empty collection.");
  }

  @Test
  void isEmpty_mapIsNull_returnsTrue() {
    assertTrue(
        CommonUtilities.isEmpty((Map<?, ?>) null), "Expected isEmpty to return true for null map.");
  }

  @Test
  void isEmpty_mapIsEmpty_returnsTrue() {
    assertTrue(CommonUtilities.isEmpty(Map.of()), "Expected isEmpty to return true for empty map.");
  }

  @Test
  void isEmpty_mapIsNotEmpty_returnsFalse() {
    assertFalse(
        CommonUtilities.isEmpty(Map.of("key", "value")),
        "Expected isEmpty to return false for non-empty map.");
  }

  @Test
  void getBasicAuth_validCredentials_returnsEncodedAuthString() {
    String username = "testUser";
    String password = "testPass";
    String expectedAuth =
        "Basic "
            + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    String actualAuth = CommonUtilities.getBasicAuth(username, password);

    assertEquals(
        expectedAuth,
        actualAuth,
        "Expected getBasicAuth to return correctly encoded Basic Auth string.");
  }

  @Test
  void getBasicAuth_nullUsernameOrPassword_throwsNullPointerException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> CommonUtilities.getBasicAuth(null, "password"),
        "Expected getBasicAuth to throw NullPointerException for null username.");
    assertThrows(
        IllegalArgumentException.class,
        () -> CommonUtilities.getBasicAuth("username", ""),
        "Expected getBasicAuth to throw NullPointerException for null password.");
  }

  @Test
  void objectMapperProvider_returnsSameInstance() {
    ObjectMapper mapper1 = CommonUtilities.objectMapperProvider();
    ObjectMapper mapper2 = CommonUtilities.objectMapperProvider();

    assertSame(
        mapper1,
        mapper2,
        "Expected objectMapperProvider to return the same ObjectMapper instance.");
  }

  @Test
  void writeValueAsStringNoEx_validObject_returnsJsonString() {
    Map<String, String> testMap = Map.of("key", "value");
    String json = CommonUtilities.writeValueAsStringNoEx(testMap);

    assertEquals(
        "{\"key\":\"value\"}",
        json,
        "Expected writeValueAsStringNoEx to return a valid JSON string.");
  }

  @Test
  void writeValueAsStringNoEx_invalidObject_returnsToStringValue() {
    Object invalidObject =
        new Object() {
          @Override
          public String toString() {
            return "InvalidObject";
          }
        };
    String result = CommonUtilities.writeValueAsStringNoEx(invalidObject);

    assertEquals(
        "InvalidObject",
        result,
        "Expected writeValueAsStringNoEx to return the object's toString value.");
  }

  @Test
  void readValueNoEx_validJson_returnsDeserializedObject() {
    String json = "{\"key\":\"value\"}";
    TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
    Map<String, String> result = CommonUtilities.readValueNoEx(json, typeRef);

    assertNotNull(result, "Expected readValueNoEx to return a non-null object for valid JSON.");
    assertEquals(
        "value", result.get("key"), "Expected deserialized object to have correct key-value pair.");
  }

  @Test
  void readValueNoEx_invalidJson_returnsNull() {
    String invalidJson = "{\"key\":\"value\""; // Missing closing brace
    TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
    Map<String, String> result = CommonUtilities.readValueNoEx(invalidJson, typeRef);

    assertNull(result, "Expected readValueNoEx to return null for invalid JSON.");
  }

  @Test
  void readValueNoEx_nullJson_returnsNull() {
    TypeReference<Map<String, String>> typeRef = new TypeReference<>() {};
    Map<String, String> result = CommonUtilities.readValueNoEx(null, typeRef);

    assertNull(result, "Expected readValueNoEx to return null for null JSON input.");
  }

  @Test
  void parseIntNoEx_validInteger_returnsParsedValue() {
    String validInt = "123";
    int result = CommonUtilities.parseIntNoEx(validInt);

    assertEquals(123, result, "Expected parseIntNoEx to parse a valid integer string.");
  }

  @Test
  void parseIntNoEx_nonInteger_returnsZero() {
    String invalidInt = "abc";
    int result = CommonUtilities.parseIntNoEx(invalidInt);

    assertEquals(0, result, "Expected parseIntNoEx to return 0 for non-integer input.");
  }

  @Test
  void parseIntNoEx_emptyString_returnsZero() {
    String empty = "";
    int result = CommonUtilities.parseIntNoEx(empty);

    assertEquals(0, result, "Expected parseIntNoEx to return 0 for an empty string.");
  }

  @Test
  void parseIntNoEx_nullInput_returnsZero() {
    String nullInput = null;
    int result = CommonUtilities.parseIntNoEx(nullInput);

    assertEquals(0, result, "Expected parseIntNoEx to return 0 for null input.");
  }

  @Test
  void parseIntNoEx_largeInteger_returnsParsedValue() {
    String largeInt = String.valueOf(Integer.MAX_VALUE);
    int result = CommonUtilities.parseIntNoEx(largeInt);

    assertEquals(
        Integer.MAX_VALUE, result, "Expected parseIntNoEx to parse large integers correctly.");
  }

  @Test
  void parseIntNoEx_negativeInteger_returnsParsedValue() {
    String negativeInt = "-456";
    int result = CommonUtilities.parseIntNoEx(negativeInt);

    assertEquals(-456, result, "Expected parseIntNoEx to parse negative integers correctly.");
  }
}
