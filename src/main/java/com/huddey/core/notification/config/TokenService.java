package com.huddey.core.notification.config;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.huddey.core.notification.data.DecodedTokenData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TokenService {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String ALPHANUMERIC_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int RANDOM_STRING_LENGTH = 64;
  private static final String SEPARATOR = ":";
  private static final Pattern SEPARATOR_PATTERN = Pattern.compile(Pattern.quote(SEPARATOR));

  public String generateEmailConfirmationLink(String email) {
    return generateAndEncodeData(email);
  }

  /*
   * Generates a random alphanumeric string of the specified length.
   *
   * @param length The length of the random string to generate.
   * @return A random alphanumeric string of the specified length.
   */
  private String generateRandomAlphanumericString(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
      builder.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
    }
    return builder.toString();
  }

  /**
   * Encodes the user email, UUID, and a random alphanumeric string into a Base64 encoded string.
   *
   * @param userEmail The user email to encode.
   * @return A Base64 encoded string containing the user email, UUID, and random string.
   */
  private String generateAndEncodeData(String userEmail) {
    if (userEmail == null) {
      log.error("User email cannot be null");
      throw new IllegalArgumentException("User email cannot be null");
    }

    UUID uuid = UUID.randomUUID();
    String randomString = generateRandomAlphanumericString(RANDOM_STRING_LENGTH);

    String combinedData = String.join(SEPARATOR, userEmail, uuid.toString(), randomString);

    return Base64.getEncoder().encodeToString(combinedData.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Decodes a Base64 encoded string into its original components.
   *
   * @param base64EncodedData The Base64 encoded string to decode.
   * @return A DecodedTokenData object containing the decoded components.
   * @throws IllegalArgumentException If the input string is not valid Base64 data or has an invalid
   *     format.
   */
  public DecodedTokenData decodeToken(String base64EncodedData) {

    byte[] decodedBytes;
    try {
      decodedBytes = Base64.getDecoder().decode(base64EncodedData);
    } catch (IllegalArgumentException e) {
      log.error("Input string is not valid Base64 data: {}", base64EncodedData);
      // FIXME: Add a more descriptive error message
      throw new IllegalArgumentException("Input string is not valid Base64 data", e);
    }

    String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
    String[] parts = SEPARATOR_PATTERN.split(decodedString);

    if (parts.length != 3) {
      log.error(
          "Invalid encoded data format: Expected 3 parts separated by '{}', but found: {}",
          SEPARATOR,
          parts.length);
      // FIXME: Add a more descriptive error message
      throw new IllegalArgumentException(
          "Invalid encoded data format: Expected 3 parts separated by '"
              + SEPARATOR
              + "', but found "
              + parts.length);
    }

    String email = parts[0];
    String uuidString = parts[1];

    UUID uuid;
    try {
      uuid = UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      log.error("Invalid encoded data format: Second part is not a valid UUID ('{}')", uuidString);
      // FIXME: Add a more descriptive error message
      throw new IllegalArgumentException(
          "Invalid encoded data format: Second part is not a valid UUID ('" + uuidString + "')", e);
    }

    return new DecodedTokenData(email, uuid);
  }
}
