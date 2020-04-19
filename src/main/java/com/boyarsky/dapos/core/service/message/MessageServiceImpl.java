package com.boyarsky.dapos.core.service.message;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.repository.message.MessageRepository;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private Blockchain blockchain;
    private MessageRepository repo;

    @Autowired
    public MessageServiceImpl(Blockchain blockchain, MessageRepository repo) {
        this.blockchain = blockchain;
        this.repo = repo;
    }

    @Override
    public void handle(MessageAttachment attachment, Transaction tx) {
        MessageEntity entity = new MessageEntity(tx.getTxId(), attachment.getEncryptedData(), tx.getSender(), attachment.isToSelf() ? null : tx.getRecipient(), attachment.isCompressed());
        entity.setHeight(blockchain.getCurrentBlockHeight());
        repo.save(entity);
    }

    @Override
    public List<MessageEntity> getChat(AccountId acc1, AccountId acc2) {
        return repo.getWith(acc1, acc2);
    }

    @Override
    public List<MessageEntity> getToSelfNotes(AccountId id) {
        return repo.getToSelf(id);
    }
}
