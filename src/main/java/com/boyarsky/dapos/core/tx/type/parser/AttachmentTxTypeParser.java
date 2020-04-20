package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.TxTypedComponent;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;

public interface AttachmentTxTypeParser<T extends Attachment> extends TxTypedComponent, AttachmentParser<T> {
}
