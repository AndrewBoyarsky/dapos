package com.boyarsky.dapos.core.repository.message;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.repository.ComparableByteArray;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
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
                .find(entityType, "sender", new ComparableByteArray(sender.getAddressBytes()))
                .minus(context.getTx().findWithProp(entityType, "recipient"));
        return CollectionUtils.toList(all, this::map);
    }


    @Override
    @Transactional(readonly = true)
    public List<MessageEntity> getWith(AccountId sender, AccountId recipient) {
        EntityIterable all = context.getTx()
                .find(entityType, "sender", new ComparableByteArray(sender.getAddressBytes()))
                .intersect(context.getTx().find(entityType, "recipient", new ComparableByteArray(recipient.getAddressBytes())));

        return CollectionUtils.toList(all, this::map);
    }

    @Override
    @Transactional(readonly = true)
    public List<MessageEntity> getAll(AccountId sender) {
        EntityIterable all = context.getTx()
                .find(entityType, "sender", new ComparableByteArray(sender.getAddressBytes()));
        return CollectionUtils.toList(all, this::map);
    }

    MessageEntity map(Entity entity) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setDbId(entity.getId());
        messageEntity.setMid((Long) entity.getProperty("id"));
        byte[] data = ((ComparableByteArray) entity.getProperty("data")).getData();
        byte[] nonce = ((ComparableByteArray) entity.getProperty("nonce")).getData();
        messageEntity.setData(new EncryptedData(data, nonce));
        byte[] sender = ((ComparableByteArray) entity.getProperty("sender")).getData();
        AccountId senderId = AccountId.fromBytes(sender);
        messageEntity.setSender(senderId);
        Comparable recipient = entity.getProperty("recipient");
        if (recipient != null) {
            messageEntity.setRecipient(AccountId.fromBytes(((ComparableByteArray) recipient).getData()));
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
        e.setProperty("data", new ComparableByteArray(entity.getData().getEncrypted()));
        e.setProperty("nonce", new ComparableByteArray(entity.getData().getNonce()));
        e.setProperty("sender", new ComparableByteArray(entity.getSender().getAddressBytes()));
        if (entity.getRecipient() != null) {
            e.setProperty("recipient", new ComparableByteArray(entity.getRecipient().getAddressBytes()));
        }
        e.setProperty("height", entity.getHeight());
        e.setProperty("compressed", entity.isCompressed());
    }
}
