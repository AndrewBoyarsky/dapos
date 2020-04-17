package com.boyarsky.dapos.core.repository;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Data
public class ComparableByteArray implements Comparable<ComparableByteArray> {
    private final byte[] data;

    public ComparableByteArray(byte[] data) {
        this.data = data;
    }

    @Override
    public int compareTo(@NotNull ComparableByteArray o) {
        return Arrays.compare(this.data, o.data);
    }
}
