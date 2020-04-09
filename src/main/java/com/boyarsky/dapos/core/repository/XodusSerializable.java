package com.boyarsky.dapos.core.repository;

import java.nio.ByteBuffer;

public interface XodusSerializable {
    ByteBuffer toBuffer();
}
