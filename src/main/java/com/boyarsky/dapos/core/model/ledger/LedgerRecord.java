package com.boyarsky.dapos.core.model.ledger;

import com.boyarsky.dapos.core.model.BlockchainEntity;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.tx.type.TxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerRecord extends BlockchainEntity {
    private long id;
    private long fee;
    private long amount;
    private TxType type;
    private Type recordType;
    private AccountId sender;
    private AccountId recipient;

    public LedgerRecord(long height, long id, long fee, long amount, TxType type, AccountId sender, AccountId recipient) {
        super(height);
        this.id = id;
        this.fee = fee;
        this.amount = amount;
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
    }

    public LedgerRecord(long height, long amount, Type type, AccountId sender, AccountId recipient) {
        super(height);
        this.id = height;
        this.fee = 0;
        this.amount = amount;
        this.type = TxType.ALL;
        this.recordType = type;
        this.sender = sender;
        this.recipient = recipient;
    }


    @Getter
    public enum Type {
        BLOCK_REWARD(1), FINE(2), ABSENT_VALIDATOR(3), ABSENT_VALIDATOR_FINE(4), VALIDATOR_BYZANTINE_FINE(5),
        ABSENT_VOTER_FINE(6), ABSENT_VOTER_AUTO_REVOCATION(7), VOTER_BYZANTINE_FINE(8), VOTER_BYZANTINE_AUTO_REVOCATION(9),
        VOTE_SUPERSEDED(10);
        private final byte code;

        Type(int code) {
            this.code = (byte) code;
        }

        public static Type fromCode(int code) {
            for (Type value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown code for Type: " + code);
        }
    }
}
