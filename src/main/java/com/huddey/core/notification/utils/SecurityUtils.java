package com.huddey.core.notification.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityUtils {

  private SecurityUtils() {}

  public static boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) {
      return false;
    }
    byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
    byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(aBytes, bBytes);
  }
}
