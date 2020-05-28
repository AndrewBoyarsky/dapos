package com.boyarsky.dapos.web.controller;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.fee.FeeConfig;
import com.boyarsky.dapos.core.model.fee.FeeProvider;
import com.boyarsky.dapos.core.model.fee.PartyFeeConfig;
import com.boyarsky.dapos.core.model.fee.State;
import com.boyarsky.dapos.core.repository.aop.Transactional;
import com.boyarsky.dapos.core.service.feeprov.FeeProviderService;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.core.tx.type.attachment.impl.FeeProviderAttachment;
import com.boyarsky.dapos.web.API;
import com.boyarsky.dapos.web.controller.request.FeeProviderRequest;
import com.boyarsky.dapos.web.exception.RestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(API.REST_ROOT_URL + "/fee-providers")
public class FeeProviderController {
    @Autowired
    TransactionToolchain toolchain;
    @Autowired
    FeeProviderService feeProviderService;

    @PostMapping
    @Transactional(readonly = true, startNew = true)
    public ResponseEntity<?> createFeeProvider(@RequestBody @Valid FeeProviderRequest request) throws URISyntaxException, InvalidKeyException, InterruptedException, IOException {
        TransactionToolchain.AccountWithWallet wallet = toolchain.parseAccount(request);
        FeeProviderRequest.PartyFeeConfigJson fromConfig = request.getFromConfig();
        FeeProviderRequest.PartyFeeConfigJson toFeeConfig = request.getToFeeConfig();
        validateFeeProvConfig(fromConfig, "From");
        validateFeeProvConfig(toFeeConfig, "To");
        return toolchain.sendTransaction(new TransactionToolchain.TxSendRequest(request, wallet, TxType.SET_FEE_PROVIDER, new FeeProviderAttachment((byte) 1, State.ACTIVE, fromJson(fromConfig), fromJson(toFeeConfig)), 1));
    }

    @GetMapping
    public ResponseEntity<List<FeeProvider>> getAll() {
        return ResponseEntity.ok(feeProviderService.getAll());
    }

    private void validateFeeProvConfig(FeeProviderRequest.PartyFeeConfigJson configJson, String side) {
        if (!configJson.whitelistAll() && configJson.getConfigGroups().isEmpty()) {
            throw new RestValidationException(side + " fee config group should be specified for whitelisted fee provider");
        }
        if (!configJson.whitelistAll()) {
            Map<AccountId, Long> accountsMap = configJson
                    .getConfigGroups()
                    .stream()
                    .map(FeeProviderRequest.FeeConfigGroup::getAccounts)
                    .flatMap(List::stream)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            for (Map.Entry<AccountId, Long> entry : accountsMap.entrySet()) {
                if (entry.getValue() > 1) {
                    throw new RestValidationException(side + " fee config contains duplicate account " + entry.getKey().getAppSpecificAccount() + " for " + entry.getValue() + " times", null);
                }
            }
        }
    }

    private PartyFeeConfig fromJson(FeeProviderRequest.PartyFeeConfigJson c) {
        List<FeeProviderRequest.FeeConfigGroup> configGroups = c.getConfigGroups();
        Map<FeeConfig, List<AccountId>> feeConfigListMap = configGroups.stream().collect(Collectors.toMap(e -> fromJson(e.getGroupConfig()), FeeProviderRequest.FeeConfigGroup::getAccounts));
        return new PartyFeeConfig(c.whitelistAll(), fromJson(c.getRootConfig()), feeConfigListMap);
    }

    private FeeConfig fromJson(FeeProviderRequest.FeeConfigJson json) {
        if (json != null) {
            return new FeeConfig(json.getMaxAllowedFee(), json.getOperations(), json.getTotalAllowedFee(), json.getTypes());
        } else {
            return null;
        }
    }
}
