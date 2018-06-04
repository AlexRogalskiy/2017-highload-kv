package org.briarheart.storage.keyvalue.util;

import java.util.Iterator;

/**
 * @author Roman Chigvintsev
 */
public class Iterables {
    private Iterables() {
        throw new AssertionError("No instance!");
    }

    public static <T> T firstItem(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
}
