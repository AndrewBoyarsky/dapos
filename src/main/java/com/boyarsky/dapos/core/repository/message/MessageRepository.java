package com.boyarsky.dapos.core.repository.message;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;

import java.util.List;

public interface MessageRepository {
    MessageEntity get(long id);

    List<MessageEntity> getToSelf(AccountId sender);

    List<MessageEntity> getWith(AccountId sender, AccountId recipient);

    List<MessageEntity> getAll(AccountId sender);

    List<MessageEntity> getAllChats(AccountId sender);

    void save(MessageEntity entity);

}
