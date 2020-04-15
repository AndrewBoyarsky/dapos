package com.boyarsky.dapos.core.tx;

import com.boyarsky.dapos.core.account.AccountId;
import com.boyarsky.dapos.core.tx.type.TxType;
import com.boyarsky.dapos.utils.Convert;
import com.boyarsky.dapos.utils.CryptoUtils;
import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;

@ToString
@Getter
public class Transaction {
    private static final byte TX_VERSION = 1;
    private String rawTransaction;
    private byte version;
    private long txId;
    private TxType type;
    private AccountId sender;
    private byte[] senderPublicKey;
    private AccountId recipient;
    private byte[] data = new byte[0];
    private long amount;
    private byte[] signature;
    private int gasPrice;
    private int maxGas;

    private int gasUsed;

    public Transaction(byte[] rawTransaction) {
        this.rawTransaction = Convert.toHexString(rawTransaction);
        ByteBuffer buffer = ByteBuffer.wrap(rawTransaction);
        version = buffer.get();
        type = TxType.ofCode(buffer.get());
        if (isFirst()) {
            int pubSize = 33; // secp compressed
            if (isEd()) {
                pubSize = 32;
            }
            senderPublicKey = new byte[pubSize];
            buffer.get(senderPublicKey);
            sender = CryptoUtils.fromPublicKey(senderPublicKey, isBitcoin());
        } else {
            sender = AccountId.fromBytes(buffer);
        }
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
        gasPrice = buffer.getInt();
        maxGas = buffer.getInt();
        signature = new byte[64];
        buffer.get(signature);
        idFromSignature();
        if (buffer.position() != buffer.capacity()) {
            throw new RuntimeException("Incorrect deserialization procedure");
        }
    }

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

    public Transaction(byte version, TxType type, AccountId sender, byte[] senderPublicKey, AccountId recipient, byte[] data, long amount, int gasPrice, int maxGas, byte[] signature) {
        this.version = version;
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.data = data;
        this.amount = amount;
        this.gasPrice = gasPrice;
        this.maxGas = maxGas;
        this.signature = signature;
        this.senderPublicKey = senderPublicKey;
    }

    public void setGasUsed(int gasUsed) {
        this.gasUsed = gasUsed;
    }

    public byte[] bytes(boolean forSigning) {
        ByteBuffer buffer = ByteBuffer.allocate(size(forSigning));
        buffer.put(version);
        buffer.put(type.getCode());
        if (!isFirst()) {
            sender.putBytes(buffer);
        } else {
            buffer.put(senderPublicKey);
        }
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
        buffer.putInt(gasPrice);
        buffer.putInt(maxGas);
        if (!forSigning) {
            buffer.put(signature);
        }
        return buffer.array();
    }

    public int size(boolean forSigning) {
        return 1 + 1 + (isFirst() ? senderPublicKey.length : sender.size()) +
                (recipient == null ? 0 : recipient.size()) + 1 + 4 + data.length + (amount == 0 ? 0 : 8) + 1 +
                4 + 4 + (forSigning ? 0 : 64);
    }

    private void idFromSignature() {
        txId = new BigInteger(CryptoUtils.sha256().digest(signature), 0, 8).longValueExact();
    }

    public long getFee() {
        return gasUsed * gasPrice;
    }

    public boolean isEd() {
        return (version & 2) == 2;
    }

    public boolean isFirst() {
        return (version & 1) == 1;
    }

    public boolean isBitcoin() {
        return (version & 4) == 4;
    }

    public static class TransactionBuilder {
        private TxType type;
        private AccountId sender;
        private KeyPair keyPair;
        private AccountId recipient;
        private byte[] data = new byte[0];
        private long amount = 0;
        private int maxGas;
        private int gasPrice;

        public TransactionBuilder(TxType type, AccountId accountId, KeyPair keyPair, int gasPrice, int maxGas) {
            this.type = type;
            this.sender = accountId;
            this.gasPrice = gasPrice;
            this.maxGas = maxGas;
            this.keyPair = keyPair;
        }

        public TransactionBuilder data(byte[] data) {
            this.data = data;
            return this;
        }

        public TransactionBuilder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public TransactionBuilder recipient(AccountId id) {
            this.recipient = id;
            return this;
        }

        public Transaction build(boolean first) {
            byte version = 0;
            if (first) {
                version |= 1;
            }
            if (sender.isEd25()) {
                version |= 2;
            }
            if (first && sender.isBitcoin()) {
                version |= 4;
            }
            Transaction transaction = new Transaction(version, type, sender, CryptoUtils.compress(keyPair.getPublic()), recipient, data, amount, gasPrice, maxGas, null);
            byte[] bytes = transaction.bytes(true);
            transaction.signature = CryptoUtils.compressSignature(CryptoUtils.sign(keyPair.getPrivate(), bytes));
            transaction.idFromSignature();
            return transaction;
        }
    }
}
