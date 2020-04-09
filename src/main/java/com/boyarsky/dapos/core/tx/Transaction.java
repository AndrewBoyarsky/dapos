package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.account.Account;
import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.utils.CryptoUtils;
import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;
import java.nio.ByteBuffer;

@ToString
@Getter
public class Transaction {
    private static final byte TX_VERSION = 1;
    private String rawTransaction;
    private byte version;
    private long txId;
    private TxType type;
    private AccountId sender;
    private AccountId recipient;
    private byte[] data = new byte[0];
    private long amount;
    private long fee;
    private long gasPrice;
    private byte[] signature;

    public String getRawTransaction() {
        return rawTransaction;
    }

    public byte getVersion() {
        return version;
    }

    public long getTxId() {
        return txId;
    }


    public byte[] getData() {
        return data;
    }

    public long getAmount() {
        return amount;
    }

    public byte[] getSignature() {
        return signature;
    }

    public Transaction(byte[] rawTransaction) {
        this.rawTransaction = Convert.toHexString(rawTransaction);
        ByteBuffer buffer = ByteBuffer.wrap(rawTransaction);
        version = buffer.get();
        txId = buffer.getLong();
        type = TxType.ofCode(buffer.get());
        sender = AccountId.fromBytes(buffer);
        byte recKeyExist = buffer.get();
        if (recKeyExist == 0) {
            recipient = AccountId.fromBytes(buffer);
        }
        int dataLength = buffer.getInt();
        if (dataLength != 0) {
            data = new byte[dataLength];
            buffer.get(data);
        }
        byte amountExist = buffer.get();
        if (amountExist == 0) {
            amount = buffer.getLong();
        }
        byte feeExist = buffer.get();
        if (feeExist == 0) {
            fee = buffer.getLong();
        }
        signature = new byte[74];
        buffer.get(signature);
        if (buffer.position() != buffer.capacity()) {
            throw new RuntimeException("Incorrect deserialization procedure");
        }
    }

    public byte[] bytes(boolean forSigning) {
        ByteBuffer buffer = ByteBuffer.allocate(size(forSigning));
        buffer.put(version);
        if (!forSigning) {
            buffer.putLong(txId);
        }
        buffer.put(type.getCode());
        sender.putBytes(buffer);
        if (recipient != null) {
            buffer.put((byte) 0);
            recipient.putBytes(buffer);
        } else {
            buffer.put((byte) -1);
        }
        buffer.putInt(data.length);
        buffer.put(data);
        if (amount == 0) {
            buffer.put((byte) -1);
        } else {
            buffer.put((byte) 0);
            buffer.putLong(amount);
        }
        if (fee == 0) {
            buffer.put((byte) -1);
        } else {
            buffer.put((byte) 0);
            buffer.putLong(fee);
        }
        if (!forSigning) {
            buffer.put(signature);
        }
        return buffer.array();
    }

    public int size(boolean forSigning) {
        return 1 + (forSigning ? 0 : 8) + 1 + sender.size() + (recipient == null ? 0 : recipient.size()) + 1 + 4 + data.length + (amount == 0 ? 0 : 8) + 1 + (fee == 0 ? 0 : 8) + 1 + (forSigning ? 0 : 74);
    }


    public Transaction(byte version, long txId, TxType type, AccountId sender, AccountId recipient, byte[] data, long amount, long fee, byte[] signature) {
        this.version = version;
        this.txId = txId;
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.data = data;
        this.amount = amount;
        this.fee = fee;
        this.signature = signature;
    }

    public static class TransactionBuilder {
        private byte version = TX_VERSION;
        private TxType type;
        private AccountId sender;
        private AccountId recipient;
        private byte[] data = new byte[0];
        private long amount = 0;
        private long fee;
        private byte[] privateKey;

        public TransactionBuilder(TxType type, AccountId sender, byte[] privateKey, long fee) {
            this.type = type;
            this.sender = sender;
            this.privateKey = privateKey;
            this.fee = fee;
        }

        public TransactionBuilder recipientPublicKey(AccountId recipient) {
            this.recipient = recipient;
            return this;
        }

        public TransactionBuilder data(byte[] data) {
            this.data = data;
            return this;
        }

        public TransactionBuilder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public TransactionBuilder version(byte version) {
            this.version = version;
            return this;
        }

        public Transaction build() {
            Transaction transaction = new Transaction(version, 0, type, sender, recipient, data, amount, fee, null);
            byte[] bytes = transaction.bytes(true);
            transaction.signature = CryptoUtils.sign(privateKey, bytes);
            transaction.txId = new BigInteger(transaction.signature, 0, 8).longValueExact();
            return transaction;
        }
    }


}
