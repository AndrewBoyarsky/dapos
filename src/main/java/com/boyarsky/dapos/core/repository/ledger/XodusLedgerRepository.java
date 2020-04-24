package com.boyarsky.dapos.core.repository.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XodusLedgerRepository implements LedgerRepository {
    private static final String entityType = "ledger";
    private XodusRepoContext context;

    public XodusLedgerRepository(XodusRepoContext context) {
        this.context = context;
    }

    @Override
    @Transactional(readonly = true)
    public List<LedgerRecord> getRecords(AccountId id, TxType type) {
        StoreTransaction tx = context.getTx();
        String account = Convert.toHexString(id.getAddressBytes());
        EntityIterable all = tx.find(entityType, "sender", account).union(
                tx.find(entityType, "recipient", account)
        ).intersect(tx.find(entityType, "type", type.getCode()));
        return CollectionUtils.toList(all, this::map);
    }

    LedgerRecord map(Entity record) {
        LedgerRecord ledgerRecord = new LedgerRecord();
        ledgerRecord.setDbId(record.getId());
        ledgerRecord.setAmount((Long) record.getProperty("amount"));
        ledgerRecord.setFee((Long) record.getProperty("fee"));
        ledgerRecord.setSender(AccountId.fromBytes(Convert.parseHexString((String) record.getProperty("sender"))));
        ledgerRecord.setId(((Long) record.getProperty("id")));
        Comparable recipient = record.getProperty("recipient");
        if (recipient != null) {
            ledgerRecord.setRecipient(AccountId.fromBytes(Convert.parseHexString((String) recipient)));
        }
        ledgerRecord.setHeight((Long) record.getProperty("height"));
        ledgerRecord.setType(TxType.ofCode((Byte) record.getProperty("type")));
        return ledgerRecord;
    }

    @Override
    @Transactional(readonly = true)
    public List<LedgerRecord> getRecords(AccountId id) {
        StoreTransaction tx = context.getTx();
        String account = Convert.toHexString(id.getAddressBytes());
        EntityIterable all = tx.find(entityType, "sender", account).union(
                tx.find(entityType, "recipient", account));
        return CollectionUtils.toList(all, this::map);
    }

    @Override
    @Transactional(requiredExisting = true)
    public void save(LedgerRecord record) {
        StoreTransaction tx = context.getTx();
        Entity entity = tx.newEntity(entityType);
        entity.setProperty("id", record.getId());
        entity.setProperty("type", record.getType().getCode());
        entity.setProperty("sender", Convert.toHexString(record.getSender().getAddressBytes()));
        if (record.getRecipient() != null) {
            entity.setProperty("recipient", Convert.toHexString(record.getRecipient().getAddressBytes()));
        }
        entity.setProperty("amount", record.getAmount());
        entity.setProperty("fee", record.getFee());
        entity.setProperty("height", record.getHeight());
    }
}
