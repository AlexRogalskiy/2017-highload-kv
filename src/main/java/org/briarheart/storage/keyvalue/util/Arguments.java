package org.briarheart.storage.keyvalue.util;

import java.util.Collection;

/**
 * @author Roman Chigvintsev
 */
public class Arguments {
    private Arguments() {
        throw new AssertionError("No instance!");
    }

    public static void assertNonNegative(int value, String message) {
        if (value < 0)
            throw new IllegalArgumentException(message);
    }

    public static void assertNotNull(Object value, String message) {
        if (value == null)
            throw new IllegalArgumentException(message);
    }

    public static void assertNotNullOrEmpty(String value, String message) {
        if (Strings.isNullOrEmpty(value))
            throw new IllegalArgumentException(message);
    }

    public static void assertNotNullOrEmpty(byte[] value, String message) {
        if (Arrays.isNullOrEmpty(value))
            throw new IllegalArgumentException(message);
    }

    public static void assertNotNullOrEmpty(Collection<?> value, String message) {
        if (value == null || value.isEmpty())
            throw new IllegalArgumentException(message);
    }
}
