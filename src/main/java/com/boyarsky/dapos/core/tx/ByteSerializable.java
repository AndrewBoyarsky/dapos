package com.boyarsky.dapos.core.tx;

import java.nio.ByteBuffer;

public interface ByteSerializable {
    int size();

    void putBytes(ByteBuffer buffer);

    static int longSize(long l, byte defaultValue) {
        return l == defaultValue ? 1 : 9;
    }
    static long getDefaultLong(ByteBuffer buffer, byte defaultValue) {
        byte b = buffer.get();
        if (b == defaultValue) {
            return defaultValue;
        }
        return buffer.getLong();
    }

    static void putDefaultLong(ByteBuffer buffer, long l, byte defaultValue) {
        if (l == defaultValue) {
            buffer.put(defaultValue);
        } else {
            buffer.put(++defaultValue);
            buffer.putLong(l);
        }
    }

    static boolean getBoolean(ByteBuffer buffer) {
        byte b = buffer.get();
        return b == 0;
    }

    static void putBoolean(ByteBuffer buffer, boolean b) {
        if (b) {
            buffer.put((byte) 0);
        } else {
            buffer.put((byte) -1);
        }
    }

    static int intSize(int l, byte defaultValue) {
        return l == defaultValue ? 1 : 5;
    }

    static int getDefaultInt(ByteBuffer buffer, int defaultValue) {
        byte b = buffer.get();
        if (b == defaultValue) {
            return defaultValue;
        }
        return buffer.getInt();
    }
    static void putDefaultInt(ByteBuffer buffer, int l,byte defaultValue) {
        if (l == defaultValue) {
            buffer.put(defaultValue);
        } else {
            buffer.put(++defaultValue);
            buffer.putInt(l);
        }
    }
}
