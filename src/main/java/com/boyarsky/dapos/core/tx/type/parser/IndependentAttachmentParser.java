package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.AttachmentTypedComponent;

public interface IndependentAttachmentParser<T extends AbstractAttachment> extends AttachmentTypedComponent, AttachmentParser<T> {
}
