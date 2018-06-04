package org.briarheart.storage.keyvalue.event;

import org.briarheart.storage.keyvalue.Quorum;
import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public class RemoveEvent implements StorageEvent {
    private final Quorum quorum;

    public RemoveEvent(Quorum quorum) {
        this.quorum = quorum;
    }

    @Override
    public void dispatch(StorageEventListener listener) throws StorageException {
        listener.onRemove(this);
    }

    public Quorum getQuorum() {
        return quorum;
    }
}
