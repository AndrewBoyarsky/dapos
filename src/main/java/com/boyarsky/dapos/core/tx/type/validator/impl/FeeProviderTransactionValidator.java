package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.FeeConfig;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TransactionTypeValidator;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FeeProviderTransactionValidator implements TransactionTypeValidator {
    @Override
    public void validate(Transaction tx) throws TxNotValidException {
        if (tx.getRecipient() != null) {
            throw new TxNotValidException("Fee provider tx should not has recipient.", null, tx, ErrorCodes.RECIPIENT_EXIST);
        }
        long amount = tx.getAmount();
        if (amount == 0) {
            throw new TxNotValidException("Fee provider require non-zero tx amount", null, tx, ErrorCodes.ZERO_BALANCE);
        }
        FeeProviderAttachment attachment = tx.getAttachment(FeeProviderAttachment.class);
        Set<AccountId> allAccounts = new HashSet<>();
        Map<FeeConfig, List<AccountId>> fromConfigs = attachment.getFromFeeConfig().getConfigs();
        Map<FeeConfig, List<AccountId>> toConfigs = attachment.getToFeeConfig().getConfigs();
        validateFeeConfig(tx, fromConfigs, allAccounts);
        validateFeeConfig(tx, toConfigs, allAccounts);
    }

    private void validateFeeConfig(Transaction tx, Map<FeeConfig, List<AccountId>> configs, Set<AccountId> allAccounts) {
        configs.forEach((e, v) -> {
            if (e.getMaxAllowedFee() != -1 && e.getMaxAllowedTotalFee() != -1 && e.getMaxAllowedTotalFee() < e.getMaxAllowedFee()) {
                throw new TxNotValidException("Total fee is less than max fee for one operation", null, tx, ErrorCodes.TOTAL_FEE_LESS_OP_FEE);
            }
            v.forEach(acc -> {
                if (allAccounts.contains(acc)) {
                    throw new TxNotValidException("Duplicate account found in fee config: " + acc, null, tx, ErrorCodes.DUPLICATE_FEE_ACCOUNT);
                }
                allAccounts.add(acc);
            });
        });
    }

    @Override
    public TxType type() {
        return TxType.SET_FEE_PROVIDER;
    }
}
