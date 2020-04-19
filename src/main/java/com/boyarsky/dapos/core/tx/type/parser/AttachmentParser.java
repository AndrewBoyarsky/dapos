package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public interface AttachmentParser<T extends AbstractAttachment> {
    T parseAttachment(ByteBuffer buffer);
}
