package com.boyarsky.dapos.core.service.message;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.repository.message.MessageRepository;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<MessageEntity> getChats(AccountId acc1) {
        List<MessageEntity> allChats = repo.getAllChats(acc1);
        Map<AccountId, MessageEntity> chats = new HashMap<>();
        for (MessageEntity allChat : allChats) {
            AccountId checkKey;
            if (allChat.isToSelf()) {
                checkKey = allChat.getSender();
            } else {
                if (allChat.getSender().equals(acc1)) {
                    checkKey = allChat.getRecipient();
                } else {
                    checkKey = allChat.getSender();
                }
            }
            MessageEntity currentValue = chats.get(checkKey);
            if (currentValue == null) {
                chats.put(checkKey, allChat);
            } else if (currentValue.getHeight() < allChat.getHeight()) {
                chats.put(checkKey, allChat);
            }
        }
        List<MessageEntity> resultChats = chats.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparing(MessageEntity::getHeight))).map(Map.Entry::getValue).collect(Collectors.toList());

        return resultChats;
    }

    @Override
    public List<MessageEntity> getToSelfNotes(AccountId id) {
        return repo.getToSelf(id);
    }
}
