package org.briarheart.storage.keyvalue.event;

import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public interface StorageEventListener {
    void onUpsert(UpsertEvent event) throws StorageException;

    void onRemove(RemoveEvent event) throws StorageException;

    void onSynchronize(SynchronizeEvent event) throws StorageException;
}
