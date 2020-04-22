package com.boyarsky.dapos.utils;

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.EntityIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CollectionUtils {
    private CollectionUtils() {
    }

    public static Entity requireAtMostOne(EntityIterable collection) {
        EntityIterator iterator = collection.iterator();
        Entity e = null;
        if (iterator.hasNext()) {
            e = iterator.next();
            if (iterator.hasNext()) {
                if (iterator.shouldBeDisposed()) {
                    iterator.dispose();
                }
                throw new RuntimeException("Exception");
            }
        }
        if (iterator.shouldBeDisposed()) {
            iterator.dispose();
        }
        return e;
    }

    public static <T> List<T> toList(EntityIterable e, Function<Entity, T> mapper) {
        ArrayList<T> ts = new ArrayList<>();
        for (Entity entity : e) {
            ts.add(mapper.apply(entity));
        }
        return ts;
    }
}
