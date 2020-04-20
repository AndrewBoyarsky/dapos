package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;

public interface FeeProviderService {
    void handle(FeeProviderAttachment attachment, Transaction tx);
}
