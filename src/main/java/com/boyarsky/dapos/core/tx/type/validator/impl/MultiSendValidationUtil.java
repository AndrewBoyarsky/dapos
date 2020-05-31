package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MultiAccountAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;

import java.util.Map;

public class MultiSendValidationUtil {
    public static void commonValidation(Transaction tx, MultiAccountAttachment attachment, long balance, int maxAccounts) {
        if (tx.getRecipient() != null) {
            throw new TxNotValidException("MultiSend tx does not allow recipient", null, tx, ErrorCodes.MULTI_SEND_HAS_RECIPIENT);
        }
        if (tx.getAmount() != 0) {
            throw new TxNotValidException("MultiSend tx should not contain amount in body, but got " + tx.getAmount(), null, tx, ErrorCodes.MULTI_SEND_CONTAINS_AMOUNT);
        }
        if (attachment.getTransfers().size() > maxAccounts) {
            throw new TxNotValidException("At most " + maxAccounts + " accounts allowed for MultiSend", null, tx, ErrorCodes.MULTI_SEND_TOO_MANY_ACCOUNTS);
        }
        if (attachment.getTransfers().size() < 2) {
            throw new TxNotValidException("More than 1 accounts should be specified for MultiSend", null, tx, ErrorCodes.MULTI_SEND_NOT_ENOUGH_RECIPIENTS);
        }
        long amount = 0;
        for (Map.Entry<AccountId, Long> entry : attachment.getTransfers().entrySet()) {
            if (entry.getValue() <= 0) {
                throw new TxNotValidException("Amount for sending should be more than zero, but got " + entry.getValue() + " for account " + entry.getKey(), null, tx, ErrorCodes.MULTI_SEND_ZERO_AMOUNT);
            }
            if (entry.getKey().isVal()) {
                throw new TxNotValidException("Unable to make transfers to validator address: " + entry.getKey(), null, tx, ErrorCodes.MULTI_SEND_RECIPIENT_VALIDATOR_ADDRESS);
            }
            amount += entry.getValue();
        }
        long shouldPay = amount + tx.getMaxFee();
        if (balance < shouldPay) {
            throw new TxNotValidException("Not enough funds on account to pay, required " + shouldPay + " but has only " + balance, null, tx, ErrorCodes.MULTI_SEND_NOT_ENOUGH_FUNDS);
        }
    }
}
