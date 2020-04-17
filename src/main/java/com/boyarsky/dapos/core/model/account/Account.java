package com.boyarsky.dapos.core.model.account;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.utils.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account extends BlockchainEntity {
    private AccountId cryptoId;
    private byte[] publicKey;
    private long balance;
    private Type type;

    public enum Type {
        CONTRACT(-1), ORDINARY(1), NODE(2);

        Type(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

        private final byte code;

        public static Type fromCode(int code) {
            for (Type value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            throw new RuntimeException("unable to find type for code " + code);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Account account = (Account) o;
        return balance == account.balance &&
                Objects.equals(cryptoId, account.cryptoId) &&
                Arrays.equals(publicKey, account.publicKey) &&
                type == account.type;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), cryptoId, balance, type);
        result = 31 * result + Arrays.hashCode(publicKey);
        return result;
    }

    @Override
    public String toString() {
        return "Account{" +
                "cryptoId=" + cryptoId.getOrigAccount() +
                ", publicKey=" + Convert.toHexString(publicKey) +
                ", balance=" + balance +
                ", type=" + type +
                ", height=" + getHeight() +
                '}';
    }
}
