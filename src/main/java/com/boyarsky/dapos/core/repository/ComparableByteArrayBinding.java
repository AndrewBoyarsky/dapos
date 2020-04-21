package com.boyarsky.dapos.core.repository;

import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.util.LightOutputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;

public class ComparableByteArrayBinding extends ComparableBinding {
    @SneakyThrows
    @Override
    public ComparableByteArray readObject(@NotNull ByteArrayInputStream stream) {
        byte[] intBytes = new byte[4];
        stream.read(intBytes);
        int size = intBytes[0] << 24 | intBytes[1] << 16 | intBytes[2] << 8 | intBytes[3];
        byte[] bytes = new byte[size];
        if (size == 0) {
            return new ComparableByteArray(bytes);
        }
        int read = stream.read(bytes);
        if (read != size) {
            throw new RuntimeException("Not enogh bytes read");
        }
        return new ComparableByteArray(bytes);
    }

    @Override
    public void writeObject(@NotNull LightOutputStream output, @NotNull Comparable object) {
        ComparableByteArray array = (ComparableByteArray) object;
        output.writeUnsignedInt(array.getData().length);
        output.write(array.getData());
    }
}
