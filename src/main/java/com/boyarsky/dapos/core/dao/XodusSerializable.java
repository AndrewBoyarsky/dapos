package com.boyarsky.dapos.core.dao;

import java.nio.ByteBuffer;

public interface XodusSerializable {
    ByteBuffer toBuffer();
}
