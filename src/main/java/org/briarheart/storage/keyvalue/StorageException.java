package org.briarheart.storage.keyvalue;

/**
 * @author Roman Chigvintsev
 */
public abstract class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
}
