package org.briarheart.storage.keyvalue.event;

import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public class SynchronizeEvent implements StorageEvent {
    @Override
    public void dispatch(StorageEventListener listener) throws StorageException {
        listener.onSynchronize(this);
    }
}
