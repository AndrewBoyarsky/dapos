package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.TypedComponent;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

import java.nio.ByteBuffer;

public interface AttachmentParser<T extends AbstractAttachment> extends TypedComponent {
    T parseAttachment(ByteBuffer buffer);
}
