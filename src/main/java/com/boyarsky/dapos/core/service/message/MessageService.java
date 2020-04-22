package com.boyarsky.dapos.core.service.message;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;

import java.util.List;

public interface MessageService {
    void handle(MessageAttachment attachment, Transaction tx);

    List<MessageEntity> getChat(AccountId acc1, AccountId acc2);

    List<MessageEntity> getChats(AccountId acc1);

    List<MessageEntity> getToSelfNotes(AccountId id);
}
