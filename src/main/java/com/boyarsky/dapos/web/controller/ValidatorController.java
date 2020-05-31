package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.Pagination;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.service.validator.VoterService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RegisterValidatorAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.RevokeAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.ValidatorControlAttachment;
import com.boyarsky.dapos.core.tx.type.attachment.impl.VoteAttachment;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.controller.request.RegisterValidatorRequest;
import com.boyarsky.dapos.web.controller.request.ValidatorToggleRequest;
import com.boyarsky.dapos.web.controller.request.VoteForRequest;
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
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

@RequestMapping(value = API.REST_ROOT_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class ValidatorController {
    private static final String VALIDATOR_URL = "/validators";
    private static final String VOTERS_URL = "/voters";
    private final ValidatorService service;
    private final VoterService voterService;
    private final TransactionToolchain toolchain;

    @Autowired
    public ValidatorController(ValidatorService service, VoterService voterService, TransactionToolchain toolchain) {
        this.service = service;
        this.voterService = voterService;
        this.toolchain = toolchain;
    }

    @GetMapping(value = VALIDATOR_URL)
    public List<ValidatorEntity> getAll(@RequestParam(value = "enabled", required = false) Boolean enabled,
                                        @Valid Pagination pagination
    ) {
        return service.getAll(enabled, API.initPagination(pagination));
    }

    @GetMapping(VALIDATOR_URL + "/{id}")
    public ValidatorEntity getById(@PathParam("id") @ValidAccount(allowedTypes = "VALIDATOR") AccountId id) {
        ValidatorEntity entity = service.get(id);
        API.throwNotFoundExceptionIfNull(entity, "Validator with id " + id + " was not found");
        return entity;
    }

    @GetMapping(VALIDATOR_URL + "/{id}/voters")
    public List<VoteEntity> getValidatorVoters(@PathParam("id") @ValidAccount(allowedTypes = "VALIDATOR") AccountId id, @Valid Pagination pagination) {
        return voterService.getValidatorVoters(id, API.initPagination(pagination));
    }

    @GetMapping(VALIDATOR_URL + "/{id}/voters/{voterId}")
    public VoteEntity getValidatorVote(@PathParam("id") @ValidAccount(allowedTypes = "VALIDATOR") AccountId id, @PathParam("voterId") @ValidAccount(allowedTypes = {"ED25", "BTC", "ETH"}) AccountId voterId) {
        VoteEntity voteEntity = voterService.get(id, voterId);
        API.throwNotFoundExceptionIfNull(voteEntity, "Voter with accountId " + voterId + " for validator " + id + " was not found");
        return voteEntity;
    }

    @GetMapping(VOTERS_URL)
    public List<VoteEntity> getAllVoters(@Valid Pagination pagination) {
        return voterService.getAll(API.initPagination(pagination));
    }

    @GetMapping(VOTERS_URL + "/{id}")
    public List<VoteEntity> getVotesByAccount(@PathParam("id") @ValidAccount(allowedTypes = {"BTC", "ETH", "ED25"}) AccountId voterId, @Valid Pagination pagination) {
        return voterService.getByVoter(voterId, API.initPagination(pagination));
    }

    @PostMapping(value = VALIDATOR_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerValidator(@RequestBody @Valid RegisterValidatorRequest request) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        TransactionToolchain.AccountWithWallet wallet = toolchain.parseAccount(request);
        RegisterValidatorAttachment attchment = new RegisterValidatorAttachment((byte) 1, request.getEnable(), request.getFee(), request.getPublicKey(), request.getRewardId());
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, wallet, TxType.REGISTER_VALIDATOR, attchment, 1));
    }

    @PostMapping(value = VALIDATOR_URL + "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> toggleValidator(
            @PathParam("id") @ValidAccount(allowedTypes = "VALIDATOR") AccountId validatorId,
            @RequestBody @Valid ValidatorToggleRequest request) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        TransactionToolchain.AccountWithWallet wallet = toolchain.parseAccount(request);
        ValidatorControlAttachment attchment = new ValidatorControlAttachment((byte) 1);
        request.setRecipient(validatorId);
        TxType type;
        if (request.getEnable()) {
            type = TxType.START_VALIDATOR;
        } else {
            type = TxType.STOP_VALIDATOR;
        }
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, wallet, type, attchment, 1));
    }

    @PostMapping(value = VALIDATOR_URL + "/{id}/voters")
    public ResponseEntity<?> voteFor(@PathParam("id") @ValidAccount(allowedTypes = "VALIDATOR") AccountId validatorId,
                                     @RequestBody @Valid VoteForRequest request
    ) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        TransactionToolchain.AccountWithWallet wallet = toolchain.parseAccount(request);
        VoteAttachment voteAttachment = new VoteAttachment((byte) 1);
        request.setRecipient(validatorId);
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, wallet, TxType.VOTE, voteAttachment, 1));
    }

    @PostMapping(value = VALIDATOR_URL + "/{id}/voters/revoke")
    public ResponseEntity<?> revoke(@PathParam("id") @ValidAccount(allowedTypes = "VALIDATOR") AccountId validatorId,
                                    @RequestBody @Valid VoteForRequest request
    ) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        TransactionToolchain.AccountWithWallet wallet = toolchain.parseAccount(request);
        RevokeAttachment voteAttachment = new RevokeAttachment((byte) 1);
        request.setRecipient(validatorId);
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, wallet, TxType.REVOKE, voteAttachment, 1));
    }
}
