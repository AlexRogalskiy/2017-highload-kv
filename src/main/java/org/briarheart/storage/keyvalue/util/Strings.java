package org.briarheart.storage.keyvalue.util;

/**
 * @author Roman Chigvintsev
 */
public class Strings {
    private Strings() {
        throw new AssertionError("No instance!");
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
