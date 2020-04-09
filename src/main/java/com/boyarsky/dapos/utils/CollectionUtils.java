package com.boyarsky.dapos.utils;

import java.util.Iterator;

public class CollectionUtils {
    private CollectionUtils() {}

    public static <T> T requireAtMostOne(Iterable<T> collection) {

        Iterator<T> iterator = collection.iterator();
        if (iterator.hasNext()) {
            T t = iterator.next();
            if (iterator.hasNext()) {
                throw new RuntimeException("Expected at most one element inside " + collection);
            } else {
                return t;
            }
        } else {
            return null;
        }
    }
}
