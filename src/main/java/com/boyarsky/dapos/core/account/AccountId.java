package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.utils.CryptoUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
@Data
@NoArgsConstructor
public class AccountId {
    private String origAccount;

    public String getOrigAccount() {
        return origAccount;
    }

    public AccountId(String origAccount) {
        if (origAccount.toLowerCase().startsWith("dab") || origAccount.toLowerCase().startsWith("det")) {
            origAccount = origAccount.substring(3);
        }
        this.origAccount = origAccount;
    }

    public void putBytes(ByteBuffer buffer) {
        if (isBitcoin()) {
            buffer.put((byte) 1);
            buffer.put(CryptoUtils.decodeBitcoinAddress(origAccount));
        } else if (isEth()){
            buffer.put((byte) 2);
            buffer.put(CryptoUtils.decodeEthAddress(origAccount));
        } else {
            throw new RuntimeException("Incorrect address type");
        }
    }

    public byte[] getAddressBytes() {
        if (isBitcoin()) {
            return CryptoUtils.decodeBitcoinAddress(origAccount);
        } else if (isEth()) {
            return CryptoUtils.decodeEthAddress(origAccount);
        } else {
            throw new RuntimeException("Incorrect address type");
        }
    }

    public static AccountId fromBytes(ByteBuffer buffer) {
        byte addressType = buffer.get();
        AccountId accountId = new AccountId();
        if (addressType == 1) {
            byte[] bitcoinBytes = new byte[25];
            buffer.get(bitcoinBytes);
            accountId.origAccount = CryptoUtils.encodeBitcoinAddress(bitcoinBytes);
        } else if (addressType == 2) {
            byte[] ethBytes = new byte[20];
            buffer.get(ethBytes);
            accountId.origAccount = CryptoUtils.encodeEthAddress(ethBytes);
        } else {
            throw new RuntimeException("Incorrect address type");
        }
        return accountId;
    }
    public static AccountId fromBytes(byte[] bytes) {
        AccountId accountId = new AccountId();
        if (bytes.length == 25) {
            accountId.origAccount = CryptoUtils.encodeBitcoinAddress(bytes);
        } else if (bytes.length == 20) {
            accountId.origAccount = CryptoUtils.encodeEthAddress(bytes);
        } else {
            throw new RuntimeException("Incorrect address type");
        }
        return accountId;
    }


    public int size() {
        if (isBitcoin()) {
            return 26;
        } else if (isEth()) {
            return 21;
        } else {
            throw new RuntimeException("Incorrect address type");
        }
    }


    public String getAppSpecificAccount() {
        if (isBitcoin()) {
            return "dab" + origAccount;
        }
        if (isEth()) {
            return "det" + origAccount;
        }
        throw new RuntimeException("Unknown acc type");
    }

    public boolean isBitcoin() {
        return origAccount.startsWith("1");
    }

    public boolean isEth() {
        return origAccount.startsWith("0x");
    }
}
