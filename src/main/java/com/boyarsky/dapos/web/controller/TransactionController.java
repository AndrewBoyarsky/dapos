package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.keystore.Status;
import com.boyarsky.dapos.core.model.keystore.VerifiedWallet;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.keystore.Keystore;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TxGasCalculator;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.NodeProxyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping(API.REST_ROOT_URL + "/txs")
public class TransactionController {
    @Autowired
    TxGasCalculator gasCalculator;
    @Autowired
    AccountService accountService;
    @Autowired
    Keystore keystore;
    @Autowired
    NodeProxyClient proxyClient;

    @PostMapping
    public ResponseEntity<?> sendMoney(@RequestBody @Valid SendMoneyRequest request) throws URISyntaxException, IOException, InterruptedException {
        AccountId sender = new AccountId(request.getAccount());
        VerifiedWallet wallet = keystore.getWallet(request.getAccount(), request.getPass());
        if (wallet.getExtractStatus() != Status.OK) {
            return ResponseEntity.badRequest().body(new Error("Incorrect password or account"));
        }
        Transaction.TransactionBuilder builder = new Transaction.TransactionBuilder(TxType.PAYMENT, sender, wallet.getWallet().getKeyPair(), 1, 2000)
                .amount(request.getAmount());
        if (request.getRecipient() != null) {
            builder.recipient(new AccountId(request.getRecipient()));
        }
        Account senderAcc = accountService.get(sender);
        if (senderAcc == null) {
            return ResponseEntity.unprocessableEntity().body(new Error("Account not exist"));
        }
        Transaction tx = builder.build(senderAcc.getPublicKey() == null);
        byte[] txBytes = tx.bytes(false);
        String result = proxyClient.sendRequest("/broadcast_tx_commit", Map.of("tx", "0x" + Convert.toHexString(txBytes)));
        return ResponseEntity.ok(result);
    }
}
