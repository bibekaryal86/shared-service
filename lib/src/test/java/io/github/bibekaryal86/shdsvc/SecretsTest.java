package io.github.bibekaryal86.shdsvc;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bibekaryal86.shdsvc.dtos.AuthToken;
import io.github.bibekaryal86.shdsvc.exception.CheckPermissionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SecretsTest {

  public static class SampleObject {
    public String userId;
    public List<String> roles;

    public SampleObject() {}

    public SampleObject(String userId, List<String> roles) {
      this.userId = userId;
      this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SampleObject that)) return false;
      return userId.equals(that.userId) && roles.equals(that.roles);
    }

    @Override
    public int hashCode() {
      return userId.hashCode() + roles.hashCode();
    }
  }

  @Test
  public void testEncodeAndDecode() throws Exception {
    final SampleObject original = new SampleObject("super_user_id", List.of("admin", "user"));
    final String encoded = Secrets.encodeBase64(original);
    final SampleObject decoded = Secrets.decodeBase64(encoded, SampleObject.class);
    assertEquals(original, decoded);
  }

  @Test
  public void testEncodeAndSign_thenDecodeAndVerify() throws Exception {
    final SampleObject original = new SampleObject("super_user_id", List.of("admin"));
    final String signed = Secrets.encodeAndSign(original);
    final SampleObject verified = Secrets.decodeAndVerify(signed, SampleObject.class);
    assertEquals(original, verified);
  }

  @Test
  public void testTamperedTokenFailsVerification() throws Exception {
    final SampleObject original = new SampleObject("super_user_id", List.of("admin"));
    final String signed = Secrets.encodeAndSign(original);

    final SampleObject tamperedObj = new SampleObject("super_user_id", List.of("hacker"));
    final String tamperedBase64 = Secrets.encodeBase64(tamperedObj);

    final String tampered = tamperedBase64 + "." + signed.split("\\.")[1];

    assertThrows(
        SecurityException.class,
        () -> {
          Secrets.decodeAndVerify(tampered, SampleObject.class);
        });
  }

  @Test
  public void testInvalidFormatFailsVerification() {
    final String malformed = "not.a.valid.token";
    assertThrows(
        SecurityException.class,
        () -> {
          Secrets.decodeAndVerify(malformed, SampleObject.class);
        });
  }

  @Test
  void returnsFalseWhenBothParamsAreNull() {
    assertFalse(Secrets.checkPermissionsMap(null, null));
  }

  @Test
  void returnsFalseWhenParam1IsNull() {
    List<String> keys = List.of("admin", "user");
    assertFalse(Secrets.checkPermissionsMap(null, keys));
  }

  @Test
  void returnsFalseWhenParam2IsNull() {
    Map<String, Boolean> map = Map.of("admin", true);
    assertFalse(Secrets.checkPermissionsMap(map, null));
  }

  @Test
  void returnsFalseWhenBothParamsAreEmpty() {
    assertFalse(Secrets.checkPermissionsMap(Collections.emptyMap(), Collections.emptyList()));
  }

  @Test
  void returnsTrueWhenSuperuserIsTrue() {
    Map<String, Boolean> map = Map.of("SUPERUSER", true, "admin", false);
    List<String> keys = List.of("admin");
    assertTrue(Secrets.checkPermissionsMap(map, keys));
  }

  @Test
  void returnsFalseWhenSuperuserIsFalseAndNoMatchingTrueKeys() {
    Map<String, Boolean> map = Map.of("SUPERUSER", false, "admin", false);
    List<String> keys = List.of("admin");
    assertFalse(Secrets.checkPermissionsMap(map, keys));
  }

  @Test
  void returnsTrueWhenAnyKeyInParam2IsTrue() {
    Map<String, Boolean> map = Map.of("admin", false, "user", true);
    List<String> keys = List.of("admin", "user");
    assertTrue(Secrets.checkPermissionsMap(map, keys));
  }

  @Test
  void returnsFalseWhenNoKeysInParam2AreTrue() {
    Map<String, Boolean> map = Map.of("admin", false, "user", false);
    List<String> keys = List.of("admin", "user");
    assertFalse(Secrets.checkPermissionsMap(map, keys));
  }

  @Test
  void returnsFalseWhenKeysInParam2AreNotInMap() {
    Map<String, Boolean> map = Map.of("admin", false);
    List<String> keys = List.of("unknown", "ghost");
    assertFalse(Secrets.checkPermissionsMap(map, keys));
  }

  @Test
  void handlesNullValuesInMapSafely() {
    Map<String, Boolean> map = new HashMap<>();
    map.put("admin", null);
    map.put("user", false);
    List<String> keys = List.of("admin", "user");
    assertFalse(Secrets.checkPermissionsMap(map, keys));
  }

  @Test
  void testSuperUserHasAccess() {
    AuthToken authToken = Mockito.mock(AuthToken.class);
    Mockito.when(authToken.getIsSuperUser()).thenReturn(true);

    assertDoesNotThrow(
        () -> Secrets.checkPermissionsToken(authToken, List.of("READ_USER", "WRITE_USER")));
  }

  @Test
  void testHasRequiredPermission() {
    AuthToken.AuthTokenPermission permission = Mockito.mock(AuthToken.AuthTokenPermission.class);
    Mockito.when(permission.getPermissionName()).thenReturn("READ_USER");

    AuthToken authToken = Mockito.mock(AuthToken.class);
    Mockito.when(authToken.getIsSuperUser()).thenReturn(false);
    Mockito.when(authToken.getPermissions()).thenReturn(List.of(permission));

    assertDoesNotThrow(
        () -> Secrets.checkPermissionsToken(authToken, List.of("READ_USER", "DELETE_USER")));
  }

  @Test
  void testMissingRequiredPermission() {
    AuthToken.AuthTokenPermission permission = Mockito.mock(AuthToken.AuthTokenPermission.class);
    Mockito.when(permission.getPermissionName()).thenReturn("READ_USER");

    AuthToken authToken = Mockito.mock(AuthToken.class);
    Mockito.when(authToken.getIsSuperUser()).thenReturn(false);
    Mockito.when(authToken.getPermissions()).thenReturn(List.of(permission));

    CheckPermissionException ex =
        assertThrows(
            CheckPermissionException.class,
            () -> Secrets.checkPermissionsToken(authToken, List.of("WRITE_USER")));

    assertEquals(
        "Permission Denied: Profile does not have required permissions...", ex.getMessage());
  }

  @Test
  void testUnexpectedExceptionWrapped() {
    AuthToken authToken = Mockito.mock(AuthToken.class);
    Mockito.when(authToken.getIsSuperUser()).thenReturn(false);
    Mockito.when(authToken.getPermissions()).thenThrow(new RuntimeException("DB error"));

    CheckPermissionException ex =
        assertThrows(
            CheckPermissionException.class,
            () -> Secrets.checkPermissionsToken(authToken, List.of("READ_USER")));

    assertEquals("Permission Denied: DB error", ex.getMessage());
  }

  @Test
  void testRethrowCheckPermissionException() {
    AuthToken authToken = Mockito.mock(AuthToken.class);
    Mockito.when(authToken.getIsSuperUser()).thenReturn(false);
    Mockito.when(authToken.getPermissions())
        .thenThrow(new CheckPermissionException("Explicit throw"));

    CheckPermissionException ex =
        assertThrows(
            CheckPermissionException.class,
            () -> Secrets.checkPermissionsToken(authToken, List.of("ANY_PERMISSION")));

    assertEquals("Permission Denied: Explicit throw", ex.getMessage());
  }
}
