package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

import java.nio.ByteBuffer;

public interface AttachmentParser<T extends Attachment> {
    T parseAttachment(ByteBuffer buffer);
}
