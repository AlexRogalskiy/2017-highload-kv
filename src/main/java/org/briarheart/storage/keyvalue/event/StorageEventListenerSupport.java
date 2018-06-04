package org.briarheart.storage.keyvalue.event;

import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public abstract class StorageEventListenerSupport implements StorageEventListener {
    @Override
    public void onUpsert(UpsertEvent event) throws StorageException {
        // Override in subclass if needed
    }

    @Override
    public void onRemove(RemoveEvent event) throws StorageException {
        // Override in subclass if needed
    }

    @Override
    public void onSynchronize(SynchronizeEvent event) throws StorageException {
        // Override in subclass if needed
    }
}
