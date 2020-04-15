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
        if (origAccount.toLowerCase().startsWith("dbt") || origAccount.toLowerCase().startsWith("det") || origAccount.toLowerCase().startsWith("dap") || origAccount.toLowerCase().startsWith("dav")) {
            origAccount = origAccount.substring(3);
        }
        this.origAccount = origAccount;
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
        } else if (addressType == 3) {
            byte[] ed25Bytes = new byte[16];
            buffer.get(ed25Bytes);
            accountId.origAccount = CryptoUtils.encodeEd25Address(ed25Bytes);
        } else if (addressType == 4) {
            byte[] validatorBytes = new byte[21];
            buffer.get(validatorBytes);
            accountId.origAccount = CryptoUtils.encodeValidatorAddress(validatorBytes);
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
        } else if (bytes.length == 16) {
            accountId.origAccount = CryptoUtils.encodeEd25Address(bytes);
        } else if (bytes.length == 21) {
            accountId.origAccount = CryptoUtils.encodeValidatorAddress(bytes);
        } else {
            throw new RuntimeException("Incorrect address type");
        }
        return accountId;
    }

    public void putBytes(ByteBuffer buffer) {
        if (isBitcoin()) {
            buffer.put((byte) 1);
            buffer.put(CryptoUtils.decodeBitcoinAddress(origAccount));
        } else if (isEth()) {
            buffer.put((byte) 2);
            buffer.put(CryptoUtils.decodeEthAddress(origAccount));
        } else if (isEd25()) {
            buffer.put((byte) 3);
            buffer.put(CryptoUtils.decodeEd25Address(origAccount));
        } else if (isVal()) {
            buffer.put((byte) 4);
            buffer.put(CryptoUtils.decodeValidatorAddress(origAccount));
        } else {
            throw new RuntimeException("Incorrect address type");
        }
    }

    public byte[] getAddressBytes() {
        if (isBitcoin()) {
            return CryptoUtils.decodeBitcoinAddress(origAccount);
        } else if (isEth()) {
            return CryptoUtils.decodeEthAddress(origAccount);
        } else if (isEd25()) {
            return CryptoUtils.decodeEd25Address(origAccount);
        } else if (isVal()) {
            return CryptoUtils.decodeValidatorAddress(origAccount);
        } else {
            throw new RuntimeException("Incorrect address type");
        }
    }

    public int size() {
        if (isBitcoin()) {
            return 26;
        } else if (isEth()) {
            return 21;
        } else if (isEd25()) {
            return 17;
        } else if (isVal()) {
            return 22;
        } else {
            throw new RuntimeException("Incorrect address type");
        }
    }


    public String getAppSpecificAccount() {
        if (isBitcoin()) {
            return "dbt" + origAccount;
        }
        if (isEth()) {
            return "det" + origAccount;
        }
        if (isEd25()) {
            return "dap" + origAccount;
        }
        if (isVal()) {
            return "dav" + origAccount;
        }
        throw new RuntimeException("Unknown acc type");
    }

    public boolean isBitcoin() {
        return origAccount.startsWith("1");
    }

    public boolean isEth() {
        return origAccount.startsWith("0x") && origAccount.length() == 42;
    }

    public boolean isEd25() {
        return origAccount.startsWith("25") && origAccount.length() == 34;
    }

    public boolean isVal() {
        return origAccount.startsWith("nn11") && origAccount.length() == 44;
    }
}
