package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.PaymentAttachment;
import com.boyarsky.dapos.core.tx.type.fee.GasCalculationException;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.controller.request.DefaultSendingRequest;
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

@RestController
@RequestMapping(API.REST_ROOT_URL + "/payments")
public class PaymentController {
    @Autowired
    TransactionToolchain txToolchain;


    @PostMapping
    @Transactional(readonly = true, startNew = true)
    public ResponseEntity<?> sendMoney(@RequestBody @Valid DefaultSendingRequest request) throws URISyntaxException, IOException, InterruptedException, InvalidKeyException, GasCalculationException {
        TransactionToolchain.AccountWithWallet accountWithWallet = txToolchain.parseAccount(request);
        return txToolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, accountWithWallet, TxType.PAYMENT, new PaymentAttachment(), 1));
    }
}
