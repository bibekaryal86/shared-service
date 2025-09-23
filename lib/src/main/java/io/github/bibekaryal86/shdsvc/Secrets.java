package io.github.bibekaryal86.shdsvc;

import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_ALGORITHM;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_NEW_LENGTH;
import static io.github.bibekaryal86.shdsvc.helpers.ConstantUtilities.ENV_SECRET_KEY;

import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Secrets {
  private static final Logger logger = LoggerFactory.getLogger(Secrets.class);

  public static String decryptSecret(final String encryptedData) {
    final String secretKey = CommonUtilities.getSystemEnvProperty(ENV_SECRET_KEY);
    final int newLength = Integer.parseInt(CommonUtilities.getSystemEnvProperty(ENV_NEW_LENGTH));
    final String algorithm = CommonUtilities.getSystemEnvProperty(ENV_ALGORITHM);
    final byte[] secretKeyBytes = Arrays.copyOf(secretKey.getBytes(), newLength);
    final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, algorithm);

    try {
      Cipher cipher = Cipher.getInstance(algorithm);
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
      byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
      return new String(decryptedBytes);
    } catch (Exception ex) {
      throw new RuntimeException("Secret Could Not Be Decoded!");
    }
  }

  public static String encryptSecret(String data) throws Exception {
    String secretKey = CommonUtilities.getSystemEnvProperty(ENV_SECRET_KEY);
    final int newLength = Integer.parseInt(CommonUtilities.getSystemEnvProperty(ENV_NEW_LENGTH));
    final String algorithm = CommonUtilities.getSystemEnvProperty(ENV_ALGORITHM);
    byte[] secretKeyBytes = Arrays.copyOf(secretKey.getBytes(), newLength);
    SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, algorithm);

    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    byte[] encryptedBytes = cipher.doFinal(data.getBytes());
    return Base64.getEncoder().encodeToString(encryptedBytes);
  }
}
