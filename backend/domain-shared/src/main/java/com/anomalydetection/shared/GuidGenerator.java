package com.anomalydetection.shared;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUID v7 generator — time-ordered, k-sortable UUIDs.
 *
 * <p>Implements the draft UUID v7 spec: a 48-bit Unix epoch millis timestamp followed by 74 bits
 * of random data. The result sorts chronologically, which improves B-tree index performance
 * compared to random UUID v4.
 *
 * <p>Matching ABP's {@code IGuidGenerator} interface.
 */
public final class GuidGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();

  private GuidGenerator() {}

  /** Creates a new time-ordered UUID (v7-like). */
  public static UUID newId() {
    long timestamp = System.currentTimeMillis();
    long mostSigBits = (timestamp << 16) | (RANDOM.nextLong() & 0xFFFFL);
    long leastSigBits = RANDOM.nextLong();
    // Mask and set version 7
    mostSigBits &= 0xFFFFFFFFFFFF0FFFL; // Clear version nibble
    mostSigBits |= 0x0000000000007000L; // Set version = 7
    // Mask and set variant
    leastSigBits &= 0x3FFFFFFFFFFFFFFFL; // Clear variant bits
    leastSigBits |= 0x8000000000000000L; // Set RFC 4122 variant
    return new UUID(mostSigBits, leastSigBits);
  }

  /** Creates a new ID and returns its string representation. */
  public static String newIdString() {
    return newId().toString();
  }
}