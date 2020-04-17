package com.boyarsky.dapos.utils;

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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

    public static <T> List<T> toList(EntityIterable e, Function<Entity, T> mapper) {
        ArrayList<T> ts = new ArrayList<>();
        for (Entity entity : e) {
            ts.add(mapper.apply(entity));
        }
        return ts;
    }
}
