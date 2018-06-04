package org.briarheart.storage.keyvalue;

import java.io.IOException;
import java.util.Set;

/**
 * @author Roman Chigvintsev
 */
public interface KeyValueStorage {
    byte[] get(String key) throws IOException;

    void upsert(String key, byte[] value) throws IOException;

    void remove(String key) throws IOException;

    void clear() throws IOException;

    void open() throws IOException;

    void close() throws IOException;

    boolean isOpen();

    Set<String> keySet() throws IOException;
}
