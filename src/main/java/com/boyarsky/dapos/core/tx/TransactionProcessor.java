package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.tx.type.fee.GasCalculationException;
import com.boyarsky.dapos.core.tx.type.handler.TxHandlingException;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import com.boyarsky.dapos.utils.Convert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionProcessor {
    private final TransactionParser parser;
    private final TransactionValidator validator;
    private final TransactionHandler handler;
    private final TxGasCalculator gasCalculator;

    @Autowired
    public TransactionProcessor(TransactionParser parser, TransactionValidator validator, TransactionHandler handler, TxGasCalculator gasCalculator) {
        this.parser = parser;
        this.validator = validator;
        this.handler = handler;
        this.gasCalculator = gasCalculator;
    }

    public ProcessingResult parseAndValidate(byte[] tx) {
        ProcessingResult parsingResult = parseTx(tx);
        if (!parsingResult.getCode().isOk()) {
            logErrorResult(parsingResult, tx, "Parsing error");
            return parsingResult;
        }
        ProcessingResult validationResult = checkTx(parsingResult.getTx());
        if (!validationResult.getCode().isOk()) {
            logErrorResult(validationResult, tx, "Validation error");
        }
        return validationResult;
    }

    public ProcessingResult tryDeliver(byte[] tx) {
        ProcessingResult validationResult = parseAndValidate(tx);
        if (!validationResult.getCode().isOk()) {
            return validationResult;
        }
        validationResult.getTx().setGasUsed(validationResult.getGasData().getUsed());
        ProcessingResult deliverResult = deliverTx(validationResult.getTx());
        if (!deliverResult.getCode().isOk()) {
            logErrorResult(deliverResult, tx, "Deliver error");
        }
        deliverResult.setGasData(validationResult.getGasData());
        return deliverResult;
    }

    public ProcessingResult checkTx(Transaction tx) {
        try {
            validator.validate(tx);
        } catch (TxNotValidException e) {
            return new ProcessingResult("Validation failed: " + e.getMessage(), e.getCode(), tx, e);
        } catch (Exception e) {
            return new ProcessingResult("Unknown error during validation: " + e.getMessage(), ErrorCodes.UNKNOWN_VALIDATION_ERROR, tx, e);
        }
        ProcessingResult estimationResult = estimateTx(tx);
        if (!estimationResult.getCode().isOk()) {
            return estimationResult;
        }

        ProcessingResult okResult = new ProcessingResult("Validation OK", ErrorCodes.OK, tx, null);
        okResult.setGasData(estimationResult.getGasData());
        return okResult;
    }

    public ProcessingResult estimateTx(Transaction tx) {
        int gasUsed;
        try {
            gasUsed = gasCalculator.calculateGas(tx);
        } catch (GasCalculationException e) {
            return new ProcessingResult("Gas calculation error: " + e.getMessage(), ErrorCodes.FAILED_GAS_CALC, tx, e);
        } catch (Exception e) {
            return new ProcessingResult("Unknown gas calculation error: " + e.getMessage(), ErrorCodes.UNKNOWN_GAS_CALC_ERROR, tx, e);
        }

        if (gasUsed < tx.getGasPrice()) {
            return new ProcessingResult("Not enough gas: required - " + gasUsed + ", provided - " + tx.getMaxGas(), ErrorCodes.NOT_ENOUGH_GAS, tx, null);
        }
        ProcessingResult ok = new ProcessingResult("Estimation OK", ErrorCodes.OK, tx, null);
        ok.getGasData().setWanted(tx.getMaxGas());
        ok.getGasData().setUsed(gasUsed);
        return ok;
    }


    public ProcessingResult parseTx(byte[] tx) {
        Transaction transaction;
        try {
            transaction = parser.parseTx(tx);
        } catch (Exception e) {
            return new ProcessingResult("Unable to parse transaction: " + e.getMessage(), ErrorCodes.GENERAL_PARSING_ERROR, null, e);
        }
        return new ProcessingResult("Parsed OK", ErrorCodes.OK, transaction, null);
    }

    public ProcessingResult deliverTx(Transaction transaction) {
        try {
            handler.handleTx(transaction);
            return new ProcessingResult("Handled ok", ErrorCodes.OK, transaction, null);
        } catch (TxHandlingException e) {
            return new ProcessingResult("Handling exception: " + e.getMessage(), ErrorCodes.HANDLING_ERROR, transaction, e);
        } catch (Exception e) {
            return new ProcessingResult("Unknown tx handling exception", ErrorCodes.UNKNOWN_HANDLING_ERROR, transaction, e);
        }
    }

    private void logErrorResult(ProcessingResult result, byte[] additionalData, String initialMessage) {
        log.error(initialMessage + ": " + result.getMessage() + ", data: " + Convert.toHexString(additionalData) + ", code " + result.getCode() + ", tx " + result.getTx(), result.getE());
    }
}
