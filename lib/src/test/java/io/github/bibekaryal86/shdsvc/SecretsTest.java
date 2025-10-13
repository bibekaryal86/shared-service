package io.github.bibekaryal86.shdsvc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

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

    assertThrows(SecurityException.class, () -> {
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
}
