package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.model.account.Account;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.ledger.LedgerRecord;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.service.account.AccountService;
import com.boyarsky.dapos.core.service.keystore.KeyStoreService;
import com.boyarsky.dapos.core.service.keystore.PassphraseProtectedWallet;
import com.boyarsky.dapos.core.service.ledger.LedgerService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.controller.request.CreateAccRequest;
import com.boyarsky.dapos.web.validation.ValidAccount;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(API.REST_ROOT_URL + "/accounts")
public class AccountController {
    @Autowired
    private AccountService service;
    @Autowired
    private KeyStoreService keystore;
    @Autowired
    private LedgerService ledgerService;

    @GetMapping
    public List<Account> getAccounts() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable("id") String account) {
        return service.get(new AccountId(account));
    }

    @PostMapping
    public PassphraseProtectedWallet createAccount(@Valid @RequestBody CreateAccRequest request) {
        PassphraseProtectedWallet wallet;
        Integer type = request.getType();
        String passphrase = request.getPass();
        if (type == 1) {
            wallet = keystore.createBitcoin(passphrase);
        } else if (type == 2) {
            wallet = keystore.createEthereum(passphrase);
        } else if (type == 3) {
            wallet = keystore.createEd25(passphrase);
        } else if (type == 4) {
            wallet = keystore.createVal(passphrase);
        } else {
            throw new UnsupportedOperationException("Fatal error");
        }
        return wallet;
    }

    @GetMapping("/{id}/ledger")
    public ResponseEntity<?> getLedger(@ValidAccount @PathVariable("id") AccountId accountId,
                                       @RequestParam(required = false) @Parameter(schema = @Schema(allOf = {TxType.class, LedgerRecord.Type.class})) String type,
                                       @RequestParam(value = "page", required = false, defaultValue = "0") @PositiveOrZero Integer page,
                                       @RequestParam(value = "limit", required = false, defaultValue = "15") @Positive @Max(100) Integer limit) {
        Pagination pagination = new Pagination(page, limit);
        if (type == null) {
            return ResponseEntity.ok(ledgerService.records(accountId, pagination));
        } else {
            return ResponseEntity.ok(ledgerService.records(accountId, type, pagination));
        }
    }
}
