package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.TxHandlingException;
import com.boyarsky.dapos.utils.Convert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionProcessor {
    private TransactionParser parser;
    private TransactionValidator validator;
    private TransactionHandler handler;

    @Autowired
    public TransactionProcessor(TransactionParser parser, TransactionValidator validator, TransactionHandler handler) {
        this.parser = parser;
        this.validator = validator;
        this.handler = handler;
    }

    public ProcessingResult parseAndValidate(byte[] tx) {
        ProcessingResult parsingResult = parseTx(tx);
        if (parsingResult.getCode() != 0) {
            logErrorResult(parsingResult, tx, "Parsing error");
            return parsingResult;
        }
        ProcessingResult validationResult = checkTx(parsingResult.getTx());
        if (validationResult.getCode() != 0) {
            logErrorResult(validationResult, tx, "Validation error");
        }
        return validationResult;
    }

    public ProcessingResult tryDeliver(byte[] tx) {
        ProcessingResult validationResult = parseAndValidate(tx);
        if (validationResult.getCode() != 0) {
            return validationResult;
        }
        ProcessingResult deliverResult = deliverTx(validationResult.getTx());
        if (deliverResult.getCode() != 0) {
            logErrorResult(deliverResult, tx, "Deliver error");
        }
        return deliverResult;
    }

    public ProcessingResult checkTx(Transaction tx) {
        return validator.validate(tx);
    }


    public ProcessingResult parseTx(byte[] tx) {
        Transaction transaction;
        try {
            transaction = parser.parseTx(tx);
        } catch (Exception e) {
            return new ProcessingResult("Unable to parse transaction", 255, null, e);
        }
        return new ProcessingResult("Parsed OK", 0, transaction, null);
    }

    public ProcessingResult deliverTx(Transaction transaction) {
        try {
            handler.handleTx(transaction);
            return new ProcessingResult("Handled ok", 0, transaction, null);
        } catch (TxHandlingException e) {
            return new ProcessingResult("Handling exception: " + e.getMessage(), 127, transaction, e);
        } catch (Exception e) {
            return new ProcessingResult("Unknown tx handling exception", 128, transaction, e);
        }
    }

    private void logErrorResult(ProcessingResult result, byte[] additionalData, String initialMessage) {
        log.error(initialMessage + ": " + result.getMessage() + ", data: " + Convert.toHexString(additionalData) + ", code " + result.getCode() + ", tx " + result.getTx(), result.getE());
    }
}
