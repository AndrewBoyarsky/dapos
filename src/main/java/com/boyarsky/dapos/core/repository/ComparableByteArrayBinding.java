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
        int size = stream.read();
        byte[] bytes = new byte[size];
        if (size == 0) {
            return new ComparableByteArray(bytes);
        }
        stream.read(bytes);
        return new ComparableByteArray(bytes);
    }

    @Override
    public void writeObject(@NotNull LightOutputStream output, @NotNull Comparable object) {
        ComparableByteArray array = (ComparableByteArray) object;
        output.writeUnsignedInt(array.getData().length);
        output.write(array.getData());
    }
}
