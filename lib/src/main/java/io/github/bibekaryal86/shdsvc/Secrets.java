package io.github.bibekaryal86.shdsvc;

import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_ALGORITHM1;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_ALGORITHM2;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_NEW_LENGTH;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_SECRET_KEY;

import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Secrets {

  private static final String SECRET_KEY;
  private static final Integer NEW_LENGTH;
  private static final String ALGORITHM1;
  private static final String ALGORITHM2;

  static {
    SECRET_KEY = CommonUtilities.getSystemEnvProperty(ENV_SECRET_KEY);
    NEW_LENGTH = Integer.parseInt(CommonUtilities.getSystemEnvProperty(ENV_NEW_LENGTH));
    ALGORITHM1 = CommonUtilities.getSystemEnvProperty(ENV_ALGORITHM1);
    ALGORITHM2 = CommonUtilities.getSystemEnvProperty(ENV_ALGORITHM2);
  }

  public static String encryptSecret(String data) throws Exception {
    byte[] secretKeyBytes = Arrays.copyOf(SECRET_KEY.getBytes(), NEW_LENGTH);
    SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, ALGORITHM1);

    Cipher cipher = Cipher.getInstance(ALGORITHM1);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    byte[] encryptedBytes = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(encryptedBytes);
  }

  public static String decryptSecret(final String encryptedData) {
    final byte[] secretKeyBytes = Arrays.copyOf(SECRET_KEY.getBytes(), NEW_LENGTH);
    final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, ALGORITHM1);

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM1);
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
      byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
      return new String(decryptedBytes);
    } catch (Exception ex) {
      throw new RuntimeException("Secret Could Not Be Decoded!");
    }
  }

  public static <T> String encodeAndSign(final T object) throws Exception {
    String base64 = encodeBase64(object);
    String signature = sign(base64);
    return base64 + "." + signature;
  }

  public static <T> T decodeAndVerify(final String signedValue, final Class<T> clazz)
      throws Exception {
    String[] parts = signedValue.split("\\.");
    if (parts.length != 2 || !verify(parts[0], parts[1])) {
      throw new SecurityException("Invalid signature");
    }
    return decodeBase64(parts[0], clazz);
  }

  public static <T> String encodeBase64(final T object) {
    byte[] jsonBytes = CommonUtilities.writeValueAsBytesNoEx(object);
    return Base64.getEncoder().encodeToString(jsonBytes);
  }

  public static <T> T decodeBase64(final String base64, final Class<T> clazz) {
    byte[] jsonBytes = Base64.getDecoder().decode(base64);
    return CommonUtilities.readValueNoEx(jsonBytes, clazz);
  }

  public static boolean checkPermissions(
      final Map<String, Boolean> permissionsMap, final List<String> permissionsList) {
    if (permissionsMap == null || permissionsList == null) {
      return false;
    }

    if (Boolean.TRUE.equals(permissionsMap.get("SUPERUSER"))) {
      return true;
    }

    for (String key : permissionsList) {
      if (Boolean.TRUE.equals(permissionsMap.get(key))) {
        return true;
      }
    }

    return false;
  }

  private static String sign(final String base64) throws Exception {
    Mac mac = Mac.getInstance(ALGORITHM2);
    SecretKeySpec keySpec =
        new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM2);
    mac.init(keySpec);
    byte[] signature = mac.doFinal(base64.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(signature);
  }

  private static boolean verify(final String base64, String signature) throws Exception {
    String expected = sign(base64);
    return MessageDigest.isEqual(expected.getBytes(), signature.getBytes());
  }
}
