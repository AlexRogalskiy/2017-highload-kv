package org.briarheart.storage.keyvalue.util;

/**
 * @author Roman Chigvintsev
 */
public class Exceptions {
    private Exceptions() {
        throw new AssertionError("No instance!");
    }

    public static RuntimeException createRuntimeException(Exception cause) {
        if (cause instanceof RuntimeException)
            return (RuntimeException) cause;
        return new RuntimeException(cause);
    }
}
