package org.briarheart.storage.keyvalue.event;

import org.briarheart.storage.keyvalue.Quorum;
import org.briarheart.storage.keyvalue.StorageException;

/**
 * @author Roman Chigvintsev
 */
public class UpsertEvent implements StorageEvent {
    private final Quorum quorum;
    private final String id;
    private final byte[] value;

    public UpsertEvent(String id, byte[] value, Quorum quorum) {
        this.quorum = quorum;
        this.id = id;
        this.value = value;
    }

    @Override
    public void dispatch(StorageEventListener listener) throws StorageException {
        listener.onUpsert(this);
    }

    public Quorum getQuorum() {
        return quorum;
    }

    public String getId() {
        return id;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "upsert [id: " + id + "]";
    }
}
