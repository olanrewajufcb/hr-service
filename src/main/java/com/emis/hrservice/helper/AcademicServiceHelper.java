package com.emis.hrservice.helper;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
public class AcademicServiceHelper {

    /**
     * Generates a deterministic UUID based on an array of string components
     * to ensure the same combination of values always produces the same event ID
     */
    public static UUID generateDeterministicEventId(String[] components, String correlationId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < components.length; i++) {
                sb.append(components[i] != null ? components[i] : "");
                if (i < components.length - 1) {
                    sb.append("|");
                }
            }

            // Add correlation ID to the string
            sb.append("|").append(correlationId != null ? correlationId : "");

            byte[] hashBytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            // Create a UUID from the first 16 bytes of the hash
            long mostSigBits = ((long) (hashBytes[0] & 0xff) << 56) |
                    ((long) (hashBytes[1] & 0xff) << 48) |
                    ((long) (hashBytes[2] & 0xff) << 40) |
                    ((long) (hashBytes[3] & 0xff) << 32) |
                    ((long) (hashBytes[4] & 0xff) << 24) |
                    ((long) (hashBytes[5] & 0xff) << 16) |
                    ((long) (hashBytes[6] & 0xff) << 8) |
                    (hashBytes[7] & 0xff);

            long leastSigBits = ((long) (hashBytes[8] & 0xff) << 56) |
                    ((long) (hashBytes[9] & 0xff) << 48) |
                    ((long) (hashBytes[10] & 0xff) << 40) |
                    ((long) (hashBytes[11] & 0xff) << 32) |
                    ((long) (hashBytes[12] & 0xff) << 24) |
                    ((long) (hashBytes[13] & 0xff) << 16) |
                    ((long) (hashBytes[14] & 0xff) << 8) |
                    (hashBytes[15] & 0xff);

            return new UUID(mostSigBits, leastSigBits);
        } catch (NoSuchAlgorithmException e) {
            // Fall back to random UUID if SHA-256 algorithm is not available
            log.error("Failed to generate deterministic event ID, falling back to random UUID", e);
            return UUID.randomUUID();
        }
    }

    /**
     * Generates a deterministic UUID based on individual string components
     */
    public static UUID generateDeterministicEventId(String correlationId, String... components) {
        return generateDeterministicEventId(components, correlationId);
    }

}
