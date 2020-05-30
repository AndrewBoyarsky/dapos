package com.boyarsky.dapos.core.repository.ledger;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.repository.XodusAbstractRepository;
import com.boyarsky.dapos.core.repository.XodusRepoContext;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.utils.CollectionUtils;
import com.boyarsky.dapos.utils.Convert;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XodusLedgerRepository extends XodusAbstractRepository<LedgerRecord> implements LedgerRepository {
    private static final String entityType = "ledger";

    public XodusLedgerRepository(XodusRepoContext context) {
        super(entityType, false, context);
    }

    @Override
    @Transactional(readonly = true)
    public List<LedgerRecord> getRecords(AccountId id, String type, Pagination pagination) {
        String account = Convert.toHexString(id.getAddressBytes());

        StoreTransaction tx = getTx();
        EntityIterable all = paginate(sort(tx.find(entityType, "sender", account).union(
                tx.find(entityType, "recipient", account)
        ).intersect(tx.find(entityType, "type", type))), pagination);
        return CollectionUtils.toList(all, this::map);
    }

    @Override
    protected LedgerRecord doMap(Entity e) {
        LedgerRecord ledgerRecord = new LedgerRecord();
        ledgerRecord.setAmount((Long) e.getProperty("amount"));
        Comparable senderProperty = e.getProperty("sender");
        if (senderProperty != null) {
            ledgerRecord.setSender(AccountId.fromBytes(Convert.parseHexString((String) senderProperty)));
        }
        ledgerRecord.setId(((Long) e.getProperty("id")));
        Comparable recipient = e.getProperty("recipient");
        if (recipient != null) {
            ledgerRecord.setRecipient(AccountId.fromBytes(Convert.parseHexString((String) recipient)));
        }
        ledgerRecord.setHeight((Long) e.getProperty("height"));
        ledgerRecord.setType(((String) e.getProperty("type")));
        return ledgerRecord;
    }

    @Override
    @Transactional(readonly = true)
    public List<LedgerRecord> getRecords(AccountId id, Pagination pagination) {
        StoreTransaction tx = getTx();
        String account = Convert.toHexString(id.getAddressBytes());
        EntityIterable all = paginate(sort(tx.find(entityType, "sender", account).union(
                tx.find(entityType, "recipient", account))), pagination);
        return CollectionUtils.toList(all, this::map);
    }

    @Override
    protected void storeToDbEntity(Entity entity, LedgerRecord record) {
        entity.setProperty("id", record.getId());
        entity.setProperty("type", record.getType());
        if (record.getSender() != null) {
            entity.setProperty("sender", Convert.toHexString(record.getSender().getAddressBytes()));
        }
        if (record.getRecipient() != null) {
            entity.setProperty("recipient", Convert.toHexString(record.getRecipient().getAddressBytes()));
        }
        entity.setProperty("amount", record.getAmount());
    }
}
