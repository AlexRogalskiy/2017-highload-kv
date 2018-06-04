package org.briarheart.storage.keyvalue.synchronization;

import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public class SynchronizationException extends StorageException {
    public SynchronizationException(String message) {
        super(message);
    }
}
