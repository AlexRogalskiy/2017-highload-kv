package org.briarheart.storage.keyvalue.event;

import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public interface StorageEvent {
    void dispatch(StorageEventListener listener) throws StorageException;
}
