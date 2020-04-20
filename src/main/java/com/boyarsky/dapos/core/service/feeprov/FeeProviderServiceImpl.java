package com.boyarsky.dapos.core.service.feeprov;

import com.boyarsky.dapos.core.model.FeeProvider;
import com.boyarsky.dapos.core.repository.feeprov.FeeProviderRepository;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import org.springframework.beans.factory.annotation.Autowired;

public class FeeProviderServiceImpl implements FeeProviderService {
    FeeProviderRepository repository;
    Blockchain blockchain;

    @Autowired
    public FeeProviderServiceImpl(FeeProviderRepository repository, Blockchain blockchain) {
        this.repository = repository;
        this.blockchain = blockchain;
    }

    @Override
    public void handle(FeeProviderAttachment attachment, Transaction tx) {
        FeeProvider feeProvider = new FeeProvider(tx.getTxId(), tx.getSender(), tx.getAmount(), attachment.getState(), attachment.getFromFeeConfig(), attachment.getToFeeConfig());
        feeProvider.setHeight(blockchain.getCurrentBlockHeight());
        repository.save(feeProvider);
    }
}
