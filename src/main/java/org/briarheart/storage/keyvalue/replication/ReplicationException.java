package org.briarheart.storage.keyvalue.replication;

import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public class ReplicationException extends StorageException {
    public ReplicationException(String message) {
        super(message);
    }
}
