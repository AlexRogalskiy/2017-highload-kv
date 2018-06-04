package org.briarheart.storage.keyvalue.util;

/**
 * @author Roman Chigvintsev
 */
public class Arrays {
    private Arrays() {
        throw new AssertionError("No instance!");
    }

    public static boolean isNullOrEmpty(byte[] array) {
        return array == null || array.length == 0;
    }
}
