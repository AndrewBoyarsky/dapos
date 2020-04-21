package com.boyarsky.dapos.utils;

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CollectionUtils {
    private CollectionUtils() {
    }

    public static Entity requireAtMostOne(EntityIterable collection) {
        if (collection.size() == 0) {
            return null;
        }
        if (collection.size() > 1) {
            throw new RuntimeException("Expected at most one element inside " + collection);
        }
        return collection.getFirst();
    }

    public static <T> List<T> toList(EntityIterable e, Function<Entity, T> mapper) {
        ArrayList<T> ts = new ArrayList<>();
        for (Entity entity : e) {
            ts.add(mapper.apply(entity));
        }
        return ts;
    }
}
