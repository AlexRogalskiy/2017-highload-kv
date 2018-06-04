package org.briarheart.storage.keyvalue;

import org.briarheart.storage.keyvalue.util.Strings;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Roman Chigvintsev
 */
public class StorageSettings {
    private static final Properties properties = new Properties();

    static {
        ClassLoader classLoader = StorageSettings.class.getClassLoader();
        try {
            properties.load(classLoader.getResourceAsStream("storage.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StorageSettings() {
        throw new AssertionError("No instance!");
    }

    public static long getSyncWaitTimeout() {
        return stringToLong(properties.getProperty("sync.wait.timeout"), 3000);
    }

    private static long stringToLong(String s, long defaultValue) {
        if (!Strings.isNullOrEmpty(s))
            return Long.valueOf(s);
        return defaultValue;
    }
}
