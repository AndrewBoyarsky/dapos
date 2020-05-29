package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.keystore.Status;
import com.boyarsky.dapos.core.model.keystore.VerifiedWallet;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.keystore.Keystore;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import com.boyarsky.dapos.core.tx.TxGasCalculator;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.Attachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.MessageAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.NoFeeAttachment;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculationException;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.web.NodeProxyClient;
import com.boyarsky.dapos.web.ValidationUtil;
import com.boyarsky.dapos.web.controller.request.DefaultSendingRequest;
import com.boyarsky.dapos.web.exception.RestError;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

@Service
public class TransactionToolchain {
    private TxGasCalculator gasCalculator;
    private AccountService accountService;
    private TransactionProcessor processor;
    private Keystore keystore;
    private NodeProxyClient proxyClient;

    @Autowired
    public TransactionToolchain(TxGasCalculator gasCalculator, AccountService accountService, TransactionProcessor processor, Keystore keystore, NodeProxyClient proxyClient) {
        this.gasCalculator = gasCalculator;
        this.accountService = accountService;
        this.processor = processor;
        this.keystore = keystore;
        this.proxyClient = proxyClient;
    }

    public MessageWithResponse createMessageAttachment(DefaultSendingRequest request, Wallet wallet) throws InvalidKeyException, IOException {
        PublicKey recipientPublicKey;
        DefaultSendingRequest.MessageData messageData = request.getMessageData();
        if (messageData.getIsToSelf()) {
            if (request.getRecipient() == null) {
                return new MessageWithResponse(ResponseEntity.unprocessableEntity().body(new RestError("Recipient required for chat message sending", null)));
            }
            if (!CryptoUtils.isCompatible(request.getAccount(), request.getRecipient())) {
                return new MessageWithResponse(ResponseEntity.unprocessableEntity().body(new RestError("Incompatible sender and recipient accounts: required type - " + (CryptoUtils.isEd25(request.getAccount()) ? "Ed25" : "Secp256k1"), null)));
            }
            Account account = accountService.get(request.getRecipient());
            if (account == null) {
                return new MessageWithResponse(ResponseEntity.unprocessableEntity().body(new RestError(request.getRecipient().getAppSpecificAccount() + "Account not exist in blockchain", null)));
            }
            if (account.getPublicKey() == null) {
                return new MessageWithResponse(ResponseEntity.unprocessableEntity().body(new RestError("Recipient account has not assigned public key", null)));
            }
            recipientPublicKey = CryptoUtils.getUncompressedPublicKey(CryptoUtils.isEd25(account.getCryptoId()), account.getPublicKey());
        } else {
            recipientPublicKey = wallet.getKeyPair().getPublic();
        }
        String data = messageData.getMessage();
        boolean isCompressed = false;
        byte[] bytesToEncrypt = data.getBytes();
        byte[] zippedData = CryptoUtils.compress(bytesToEncrypt);
        if (bytesToEncrypt.length > zippedData.length) {
            isCompressed = true;
            bytesToEncrypt = zippedData;
        }
        EncryptedData encryptedData;
        PrivateKey privateKey = wallet.getKeyPair().getPrivate();
        if (CryptoUtils.isEd25(request.getAccount())) {
            encryptedData = CryptoUtils.encryptX25519WithEd25519(privateKey, recipientPublicKey, bytesToEncrypt);
        } else {
            encryptedData = CryptoUtils.encryptECDH(privateKey, recipientPublicKey, bytesToEncrypt);
        }
        MessageAttachment messageAttachment = new MessageAttachment((byte) 1, encryptedData, isCompressed, messageData.getIsToSelf());
        return new MessageWithResponse(messageAttachment);
    }


    public AccountWithWallet parseAccount(DefaultSendingRequest request) {
        VerifiedWallet wallet = keystore.getWallet(request.getAccount().getAppSpecificAccount(), request.getPass());
        if (wallet.getExtractStatus() != Status.OK) {
            throw new TransactionSendingException("Incorrect password or account");
        }
        Account senderAcc = accountService.get(request.getAccount());
        if (senderAcc == null) {
            throw new TransactionSendingException("Account not exist");
        }
        return new AccountWithWallet(senderAcc, wallet.getWallet());
    }

    public ResponseEntity<?> sendTransaction(TxSendRequest request) throws IOException, URISyntaxException, InterruptedException, InvalidKeyException, GasCalculationException {
        Account sender = request.accountWithWallet.account;
        Wallet wallet = request.accountWithWallet.wallet;

        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(request.getType(), request.getAttachment(), sender.getCryptoId(), wallet.getKeyPair(), 1, 2000);
        if (request.request.getAmount() != null) {
            builder.amount(request.request.getAmount());
        }

        if (request.request.getRecipient() != null) {
            builder.recipient(request.request.getRecipient());
        }
        if (request.request.getFeeProvider() != null) {
            builder.noFee(new NoFeeAttachment((byte) 1, request.request.getFeeProvider()));
        }
        if (TxType.MESSAGE != request.getType() && request.request.getMessageData() != null) {
            MessageWithResponse messageWithResponse = createMessageAttachment(request.request, wallet);
            if (messageWithResponse.errorResponse != null) {
                return messageWithResponse.errorResponse;
            }
            builder.message(messageWithResponse.attachment);
        }
        Transaction tx = builder.build(sender.getPublicKey() == null);
        int gas = gasCalculator.calculateGas(tx);
        Transaction gasAppliedTx = builder.gas(gas).build(tx.isFirst());
        byte[] txBytes = gasAppliedTx.bytes(false);
        ProcessingResult processingResult = processor.parseAndValidate(txBytes);
        if (!processingResult.getCode().isOk()) {
            String stacktrace = ValidationUtil.dumpException(processingResult.getE());
            return ResponseEntity.unprocessableEntity().body(new RestError(processingResult.getMessage() + ", code - " + processingResult.getCode(), stacktrace));
        }
        String result = proxyClient.sendRequest("/broadcast_tx_commit", Map.of("tx", "0x" + Convert.toHexString(txBytes)));
        return ResponseEntity.ok(result);
    }

    @Data
    @AllArgsConstructor
    public static class AccountWithWallet {
        private Account account;
        private Wallet wallet;
    }

    @Data
    public static class MessageWithResponse {
        private ResponseEntity<?> errorResponse;
        private MessageAttachment attachment;

        public MessageWithResponse(ResponseEntity<?> errorResponse) {
            this.errorResponse = errorResponse;
        }

        public MessageWithResponse(MessageAttachment attachment) {
            this.attachment = attachment;
        }
    }

    @Data
    @AllArgsConstructor
    public static class TxSendRequest {
        private DefaultSendingRequest request;
        private AccountWithWallet accountWithWallet;
        private TxType type;
        private Attachment attachment;
        private int gasPrice;
    }
}
