package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.keystore.Status;
import com.boyarsky.dapos.core.model.keystore.VerifiedWallet;
import com.boyarsky.dapos.core.model.keystore.Wallet;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.keystore.KeyStoreService;
import com.boyarsky.dapos.core.service.message.MessageService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculationException;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.controller.request.MessageRequest;
import com.boyarsky.dapos.web.exception.RestError;
import com.boyarsky.dapos.web.validation.ValidAccount;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = API.REST_ROOT_URL + "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class MessageController {
    MessageService messageService;
    KeyStoreService keyStoreService;
    AccountService accountService;
    TransactionToolchain txToolchain;

    @Autowired
    public MessageController(MessageService messageService, KeyStoreService keyStoreService, AccountService accountService, TransactionToolchain txToolchain) {
        this.messageService = messageService;
        this.keyStoreService = keyStoreService;
        this.accountService = accountService;
        this.txToolchain = txToolchain;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readonly = true, startNew = true)
    public ResponseEntity<?> sendMessage(@RequestBody @Valid MessageRequest request) throws URISyntaxException, IOException, InterruptedException, InvalidKeyException, GasCalculationException {
        TransactionToolchain.AccountWithWallet accountWithWallet = txToolchain.parseAccount(request);
        TransactionToolchain.MessageWithResponse messageWithError = txToolchain.createMessageAttachment(request, accountWithWallet.getWallet());
        if (messageWithError.getErrorResponse() != null) {
            return messageWithError.getErrorResponse();
        }
        return txToolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, accountWithWallet, TxType.MESSAGE, messageWithError.getAttachment(), 1));
    }

    @GetMapping
    public ResponseEntity<?> getAllMessages(@RequestParam @ValidAccount @Parameter(schema = @Schema(implementation = String.class)) AccountId sender,
                                            @RequestParam @NotBlank String pass,
                                            @RequestParam(required = false) @ValidAccount @Parameter(schema = @Schema(implementation = String.class)) AccountId recipient) throws InvalidKeyException, IOException {
        List<MessageEntity> entities = new ArrayList<>();
        VerifiedWallet wallet = keyStoreService.getWallet(sender.getAppSpecificAccount(), pass);
        if (wallet.getExtractStatus() != Status.OK) {
            return ResponseEntity.badRequest().body(new RestError("Bad credentials", null));
        }
        Wallet wal = wallet.getWallet();
        PublicKey key;
        if (recipient != null) {
            if (!CryptoUtils.isCompatible(sender, recipient)) {
                return ResponseEntity.unprocessableEntity().body(new RestError("Incompatible account keys", null));
            }
            Account recipientAcc = accountService.get(recipient);
            if (recipientAcc == null) {
                return ResponseEntity.badRequest().body(new RestError("Recipient account does not exist", null));
            }
            if (recipientAcc.getPublicKey() == null) {
                return ResponseEntity.badRequest().body(new RestError("Recipient public key does not exist", null));
            }
            key = CryptoUtils.getUncompressedPublicKey(wal.getKeyPair().getPublic().getAlgorithm(), recipientAcc.getPublicKey());
            entities.addAll(messageService.getChat(sender, recipient));
        } else {
            key = wal.getKeyPair().getPublic();
            entities.addAll(messageService.getToSelfNotes(sender));
        }
        List<Message> ms = new ArrayList<>();
        for (MessageEntity entity : entities) {
            byte[] bytes;
            if (CryptoUtils.isEd25(sender)) {
                bytes = CryptoUtils.decryptX25519WithEd25519(wal.getKeyPair().getPrivate(), key, entity.getData());
            } else {
                bytes = CryptoUtils.decryptECDH(wal.getKeyPair().getPrivate(), key, entity.getData());
            }
            if (entity.isCompressed()) {
                bytes = CryptoUtils.uncompress(bytes);
            }
            ms.add(new Message(new String(bytes), sender.getAppSpecificAccount(), recipient == null ? null : recipient.getAppSpecificAccount(), entity.getHeight()));
        }
        return ResponseEntity.ok(ms);
    }

    @GetMapping("/chats")
    public ResponseEntity<?> getChats(@RequestParam @ValidAccount @Parameter(schema = @Schema(implementation = String.class)) AccountId account) {
        List<MessageEntity> chats = messageService.getChats(account);
        return ResponseEntity.ok(chats);
    }

    @Data
    @AllArgsConstructor
    private static class Message {
        private String message;
        private String sender;
        private String recipient;
        private long height;
    }
}
