package org.briarheart.storage.keyvalue;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.briarheart.storage.keyvalue.util.Arguments.assertNotNull;
import static org.briarheart.storage.keyvalue.util.Arguments.assertNotNullOrEmpty;

/**
 * @author Roman Chigvintsev
 */
public class FileKeyValueStorage implements KeyValueStorage {
    private final File data;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final ReadWriteLock storageLock = new ReentrantReadWriteLock();

    public FileKeyValueStorage(@NotNull File root) throws IOException {
        assertNotNull(root, "Root directory cannot be null or empty");
        this.data = new File(root, "data");
        if (!this.data.exists())
            Files.createDirectory(this.data.toPath());
    }

    @Override
    public byte[] get(@NotNull String key) throws IOException {
        assertNotNullOrEmpty(key, "Key cannot be null or empty");
        File valueFile = new File(data, key);
        storageLock.readLock().lock();
        try {
            if (!valueFile.exists())
                return null;
            return Files.readAllBytes(valueFile.toPath());
        } finally {
            storageLock.readLock().unlock();
        }
    }

    @Override
    public void upsert(@NotNull String key, @NotNull byte[] value) throws IOException {
        assertNotNullOrEmpty(key, "Key cannot be null or empty");
        assertNotNull(value, "Value cannot be null");
        File valueFile = new File(data, key);
        storageLock.readLock().lock();
        try {
            Files.write(valueFile.toPath(), value);
        } finally {
            storageLock.readLock().unlock();
        }

    }

    @Override
    public void remove(@NotNull String key) throws IOException {
        assertNotNullOrEmpty(key, "Key cannot be null or empty");
        File valueFile = new File(data, key);
        storageLock.readLock().lock();
        try {
            if (valueFile.exists() && !valueFile.delete())
                throw new IOException("Failed to deleted file " + valueFile);
        } finally {
            storageLock.readLock().unlock();
        }
    }

    @Override
    public void clear() throws IOException {
        Path dataPath = data.toPath();
        storageLock.writeLock().lock();
        try {
            List<File> files = Files.walk(dataPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : files)
                if (!file.delete())
                    throw new IOException("Failed to clear storage: failed to delete file " + file);
            Files.createDirectory(dataPath);
        } finally {
            storageLock.writeLock().unlock();
        }

    }

    @Override
    public void open() throws IOException {
        if (state.compareAndSet(State.CLOSED, State.OPENING))
            state.set(State.OPEN);
    }

    @Override
    public void close() throws IOException {
        if (state.compareAndSet(State.OPEN, State.CLOSING))
            state.set(State.CLOSED);
    }

    @Override
    public boolean isOpen() {
        return state.get() == State.OPEN;
    }

    @Override
    public Set<String> keySet() throws IOException {
        storageLock.readLock().lock();
        try {
            try (Stream<Path> files = Files.list(data.toPath())) {
                return files.map(file -> file.getFileName().toString()).collect(Collectors.toSet());
            }
        } finally {
            storageLock.readLock().unlock();
        }
    }

    private enum State {OPENING, OPEN, CLOSING, CLOSED}
}
