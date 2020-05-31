package com.boyarsky.dapos.web.controller.request;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.web.validation.ValidAccount;
import com.boyarsky.dapos.web.validation.ValidHexadecimalArray;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegisterValidatorRequest extends DefaultSendingRequest {

    @NotNull
    private Boolean enable;
    @NotNull
    @Max(10000)
    @Min(0)
    private Short fee;
    @Size(min = 32, max = 32)
    @ValidHexadecimalArray
    @NotNull
    private String publicKey;
    @ValidAccount
    @NotNull
    private AccountId rewardId;

    @Override
    public @Null Long getAmount() {
        return super.getAmount();
    }

    @Override
    public @ValidAccount @Null AccountId getRecipient() {
        return super.getRecipient();
    }

    public byte[] getPublicKey() {
        return Convert.parseHexString(publicKey);
    }
}
