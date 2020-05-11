package com.boyarsky.dapos.core.repository.message;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XodusMessageRepository implements MessageRepository {
    private static final String entityType = "message";
    private final XodusRepoContext context;

    public XodusMessageRepository(XodusRepoContext context) {
        this.context = context;
    }

    @Override
    @Transactional(readonly = true)
    public MessageEntity get(long id) {
        StoreTransaction tx = context.getTx();
        Entity ent = CollectionUtils.requireAtMostOne(tx.find(entityType, "id", id));
        if (ent == null) {
            return null;
        }
        return map(ent);
    }

    @Override
    @Transactional(readonly = true)
    public List<MessageEntity> getToSelf(AccountId sender) {
        EntityIterable all = context.getTx()
                .find(entityType, "sender", Convert.toHexString(sender.getAddressBytes()))
                .minus(context.getTx().findWithProp(entityType, "recipient"));
        EntityIterable sorted = context.getTx().sort(entityType, "height", all, false);
        return CollectionUtils.toList(sorted, this::map);
    }


    @Override
    @Transactional(readonly = true)
    public List<MessageEntity> getWith(AccountId sender, AccountId recipient) {
        EntityIterable wholeChat = context.getTx()
                .find(entityType, "sender", Convert.toHexString(sender.getAddressBytes()))
                .intersect(context.getTx().find(entityType, "recipient", Convert.toHexString(recipient.getAddressBytes())))
                .union(context.getTx().find(entityType, "sender", Convert.toHexString(recipient.getAddressBytes()))
                        .intersect(context.getTx().find(entityType, "recipient", Convert.toHexString(sender.getAddressBytes()))));
        EntityIterable sorted = context.getTx().sort(entityType, "height", wholeChat, false);
        return CollectionUtils.toList(sorted, this::map);
    }

    @Override
    @Transactional(readonly = true)
    public List<MessageEntity> getAll(AccountId sender) {
        EntityIterable all = context.getTx()
                .find(entityType, "sender", Convert.toHexString(sender.getAddressBytes()));
        return CollectionUtils.toList(all, this::map);
    }

    @Override
    @Transactional(readonly = true)
    public List<MessageEntity> getAllChats(AccountId sender) {
        StoreTransaction tx = context.getTx();

        EntityIterable all = tx.find(entityType, "sender", Convert.toHexString(sender.getAddressBytes())).union(
                tx.find(entityType, "recipient", Convert.toHexString(sender.getAddressBytes()))
        ).distinct();
        return CollectionUtils.toList(all, this::map);
    }

    MessageEntity map(Entity entity) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setDbId(entity.getId());
        messageEntity.setMid((Long) entity.getProperty("id"));
        byte[] data = Convert.parseHexString((String) entity.getProperty("data"));
        byte[] nonce = Convert.parseHexString((String) entity.getProperty("nonce"));
        messageEntity.setData(new EncryptedData(data, nonce));
        byte[] sender = Convert.parseHexString((String) entity.getProperty("sender"));
        AccountId senderId = AccountId.fromBytes(sender);
        messageEntity.setSender(senderId);
        Comparable recipient = entity.getProperty("recipient");
        if (recipient != null) {
            messageEntity.setRecipient(AccountId.fromBytes(Convert.parseHexString((String) recipient)));
        }
        messageEntity.setHeight((Long) entity.getProperty("height"));
        messageEntity.setCompressed((Boolean) entity.getProperty("compressed"));
        return messageEntity;
    }

    @Override
    @Transactional(requiredExisting = true)
    public void save(MessageEntity entity) {
        StoreTransaction storeTransaction = context.getTx();
        Entity e = storeTransaction.newEntity(entityType);
        e.setProperty("id", entity.getMid());
        e.setProperty("data", Convert.toHexString(entity.getData().getEncrypted()));
        e.setProperty("nonce", Convert.toHexString(entity.getData().getNonce()));
        e.setProperty("sender", Convert.toHexString(entity.getSender().getAddressBytes()));
        if (entity.getRecipient() != null) {
            e.setProperty("recipient", Convert.toHexString(entity.getRecipient().getAddressBytes()));
        }
        e.setProperty("height", entity.getHeight());
        e.setProperty("compressed", entity.isCompressed());
    }
}
