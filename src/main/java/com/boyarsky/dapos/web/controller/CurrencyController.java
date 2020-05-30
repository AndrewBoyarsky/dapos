package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.service.account.NotFoundException;
import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIssuanceAttachment;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.controller.request.CurrencyClaimRequest;
import com.boyarsky.dapos.web.controller.request.CurrencyIssueRequest;
import com.boyarsky.dapos.web.controller.request.CurrencyTransferRequest;
import com.boyarsky.dapos.web.validation.ValidAccount;
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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

@RestController
@RequestMapping(value = API.REST_ROOT_URL + "/currencies", produces = MediaType.APPLICATION_JSON_VALUE)
public class CurrencyController {
    @Autowired
    CurrencyService currencyService;
    @Autowired
    TransactionToolchain toolchain;

    @GetMapping
    public List<Currency> getAll(@Valid Pagination pagination) {
        return currencyService.getAllCurrencies(pagination);
    }

    @GetMapping(value = "/{id}")
    public Currency getById(@PathParam("id") @NotNull Long id) {
        Currency currency = currencyService.getById(id);
        if (currency == null) {
            throw new NotFoundException("Currency with id '" + id + "' was not found");
        }
        return currency;
    }

    @GetMapping(value = "/by-code")
    public Currency getById(@RequestParam("code") @NotEmpty String code) {
        Currency currency = currencyService.getByCode(code);
        if (currency == null) {
            throw new NotFoundException("Currency with code '" + code + "' was not found");
        }
        return currency;
    }

    @GetMapping(params = "account")
    public List<CurrencyHolder> getAccountCurrencies(@RequestParam("account") @ValidAccount @NotNull AccountId accountId, @Valid Pagination pagination) {
        return currencyService.accountCurrencies(accountId, pagination);
    }

    @GetMapping("/{id}/holders")
    public List<CurrencyHolder> getCurrencyHolders(@PathParam("id") @NotNull Long currencyId, @Valid Pagination pagination) {
        return currencyService.holders(currencyId, pagination);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> issueCurrency(@RequestBody @Valid CurrencyIssueRequest request) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        CurrencyIssuanceAttachment attachment = new CurrencyIssuanceAttachment((byte) 1, request.getCode(), request.getName(), request.getDescription(), request.getSupply(), request.getDecimals());
        TransactionToolchain.AccountWithWallet accountWithWallet = toolchain.parseAccount(request);
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, accountWithWallet, TxType.CURRENCY_ISSUANCE, attachment, 1));
    }

    @PostMapping(value = "/claims", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> claimReserve(@RequestBody @Valid CurrencyClaimRequest request) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, request.getCurrencyId());
        TransactionToolchain.AccountWithWallet accountWithWallet = toolchain.parseAccount(request);
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, accountWithWallet, TxType.CURRENCY_CLAIM_RESERVE, attachment, 1));
    }

    @PostMapping(value = "/transfers", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> transfer(@RequestBody @Valid CurrencyTransferRequest request) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        CurrencyIdAttachment attachment = new CurrencyIdAttachment((byte) 1, request.getCurrencyId());
        TransactionToolchain.AccountWithWallet accountWithWallet = toolchain.parseAccount(request);
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, accountWithWallet, TxType.CURRENCY_TRANSFER, attachment, 1));
    }
}
