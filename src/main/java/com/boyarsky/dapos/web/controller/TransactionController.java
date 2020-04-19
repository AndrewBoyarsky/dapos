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
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.NodeProxyClient;
import com.boyarsky.dapos.web.ValidationUtil;
import com.boyarsky.dapos.web.controller.exception.RestError;
import com.boyarsky.dapos.web.controller.request.DefaultSendingRequest;
import com.boyarsky.dapos.web.controller.request.SendMessageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

@RestController
@RequestMapping(API.REST_ROOT_URL + "/txs")
@Slf4j
public class TransactionController {
    @Autowired
    TxGasCalculator gasCalculator;
    @Autowired
    AccountService accountService;
    @Autowired
    TransactionProcessor processor;
    @Autowired
    Keystore keystore;
    @Autowired
    NodeProxyClient proxyClient;

    @PostMapping
    public ResponseEntity<?> sendMoney(@RequestBody @Valid DefaultSendingRequest request) throws URISyntaxException, IOException, InterruptedException {
        AccountWithWallet accountWithWallet = parseAccount(request);
        return sendTransaction(new TxSendRequest(request, accountWithWallet, TxType.PAYMENT, new PaymentAttachment(), 1));
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody @Valid SendMessageRequest request) throws URISyntaxException, IOException, InterruptedException, InvalidKeyException {
        AccountWithWallet accountWithWallet = parseAccount(request);
        PublicKey recipientPublicKey;
        if (!request.isToSelf()) {
            if (request.getRecipient() == null) {
                return ResponseEntity.unprocessableEntity().body(new RestError("Recipient required for chat message sending", null));
            }
            if (!CryptoUtils.isCompatible(request.getAccount(), request.getRecipient())) {
                return ResponseEntity.unprocessableEntity().body(new RestError("Incompatible sender and recipient accounts: required type - " + (CryptoUtils.isEd25(request.getAccount()) ? "Ed25" : "Secp256k1"), null));
            }
            Account account = accountService.get(request.getRecipient());
            if (account == null) {
                return ResponseEntity.unprocessableEntity().body(new RestError(request.getRecipient().getAppSpecificAccount() + "Account not exist in blockchain", null));
            }
            if (account.getPublicKey() == null) {
                return ResponseEntity.unprocessableEntity().body(new RestError("Recipient account has not assigned public key", null));
            }
            recipientPublicKey = CryptoUtils.getUncompressedPublicKey(CryptoUtils.isEd25(account.getCryptoId()), account.getPublicKey());
        } else {
            recipientPublicKey = accountWithWallet.wallet.getKeyPair().getPublic();
        }
        String data = request.getData();
        boolean isCompressed = false;
        byte[] bytesToEncrypt = data.getBytes();
        byte[] zippedData = CryptoUtils.compress(bytesToEncrypt);
        if (bytesToEncrypt.length > zippedData.length) {
            isCompressed = true;
            bytesToEncrypt = zippedData;
        }
        EncryptedData encryptedData;
        PrivateKey privateKey = accountWithWallet.wallet.getKeyPair().getPrivate();
        if (CryptoUtils.isEd25(request.getAccount())) {
            encryptedData = CryptoUtils.encryptX25519(privateKey, recipientPublicKey, bytesToEncrypt);
        } else {
            encryptedData = CryptoUtils.encryptECDH(privateKey, recipientPublicKey, bytesToEncrypt);
        }

        MessageAttachment messageAttachment = new MessageAttachment((byte) 1, encryptedData, isCompressed, request.isToSelf());
        return sendTransaction(new TxSendRequest(request, accountWithWallet, TxType.MESSAGE, messageAttachment, 1));
    }


    private AccountWithWallet parseAccount(DefaultSendingRequest request) {
        VerifiedWallet wallet = keystore.getWallet(request.getAccount().getAppSpecificAccount(), request.getPass());
        if (wallet.getExtractStatus() != Status.OK) {
            throw new TransactionSendingException("Incorrect password or account");
        }
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), request.getAccount(), wallet.getWallet().getKeyPair(), 1, 2000)
                .amount(request.getAmount());
        if (request.getRecipient() != null) {
            builder.recipient(request.getRecipient());
        }
        Account senderAcc = accountService.get(request.getAccount());
        if (senderAcc == null) {
            throw new TransactionSendingException("Account not exist");
        }
        return new AccountWithWallet(senderAcc, wallet.getWallet());
    }

    private ResponseEntity<?> sendTransaction(TxSendRequest request) throws IOException, URISyntaxException, InterruptedException {
        Account sender = request.accountWithWallet.account;
        Wallet wallet = request.accountWithWallet.wallet;
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, new PaymentAttachment(), sender.getCryptoId(), wallet.getKeyPair(), 1, 2000)
                .amount(request.request.getAmount());
        if (request.request.getRecipient() != null) {
            builder.recipient(request.request.getRecipient());
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
    private static class AccountWithWallet {
        private Account account;
        private Wallet wallet;
    }

    @Data
    @AllArgsConstructor
    private static class TxSendRequest {
        private DefaultSendingRequest request;
        private AccountWithWallet accountWithWallet;
        private TxType type;
        private Attachment attachment;
        private int gasPrice;
    }
}
