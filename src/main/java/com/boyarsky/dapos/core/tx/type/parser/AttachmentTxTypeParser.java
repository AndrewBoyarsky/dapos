package com.boyarsky.dapos.core.tx.type.parser;

import com.boyarsky.dapos.core.tx.type.TxTypedComponent;
import com.boyarsky.dapos.core.tx.type.attachment.AbstractAttachment;

public interface AttachmentTxTypeParser<T extends AbstractAttachment> extends TxTypedComponent, AttachmentParser<T> {
}
