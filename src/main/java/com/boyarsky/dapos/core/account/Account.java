package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account extends BlockchainEntity {
    private AccountId cryptoId;
    private byte[] publicKey;
    private long balance;
    private Type type;

    public enum Type {
        CONTRACT(-1), ORDINARY(1);

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
}
