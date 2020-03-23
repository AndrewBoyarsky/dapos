package io.example;

import com.github.jtendermint.jabci.api.IBeginBlock;
import com.github.jtendermint.jabci.api.ICheckTx;
import com.github.jtendermint.jabci.api.ICommit;
import com.github.jtendermint.jabci.api.IDeliverTx;
import com.github.jtendermint.jabci.api.IQuery;
import com.github.jtendermint.jabci.types.RequestBeginBlock;
import com.github.jtendermint.jabci.types.RequestCheckTx;
import com.github.jtendermint.jabci.types.RequestCommit;
import com.github.jtendermint.jabci.types.RequestDeliverTx;
import com.github.jtendermint.jabci.types.RequestQuery;
import com.github.jtendermint.jabci.types.ResponseBeginBlock;
import com.github.jtendermint.jabci.types.ResponseCheckTx;
import com.github.jtendermint.jabci.types.ResponseCommit;
import com.github.jtendermint.jabci.types.ResponseDeliverTx;
import com.github.jtendermint.jabci.types.ResponseQuery;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class KVStoreApp implements IDeliverTx, ICheckTx, ICommit, IQuery, IBeginBlock {
    private Environment env;
    private Transaction txn = null;
    private Store store = null;

    KVStoreApp(Environment env) {
        this.env = env;
    }

    private int validate(ByteString tx) {
        List<byte[]> parts = split(tx, '=');
        if (parts.size() != 2) {
            return 1;
        }
        byte[] key = parts.get(0);
        byte[] value = parts.get(1);

        // check if the same key=value already exists
        var stored = getPersistedValue(key);
        if (stored != null && Arrays.equals(stored, value)) {
            return 2;
        }

        return 0;
    }

    private List<byte[]> split(ByteString tx, char separator) {
        var arr = tx.toByteArray();
        int i;
        for (i = 0; i < tx.size(); i++) {
            if (arr[i] == (byte)separator) {
                break;
            }
        }
        if (i == tx.size()) {
            return Collections.emptyList();
        }
        return List.of(
                tx.substring(0, i).toByteArray(),
                tx.substring(i + 1).toByteArray()
        );
    }

    private byte[] getPersistedValue(byte[] k) {
        return env.computeInReadonlyTransaction(txn -> {
            var store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn);
            ByteIterable byteIterable = store.get(txn, new ArrayByteIterable(k));
            if (byteIterable == null) {
                return null;
            }
            return byteIterable.getBytesUnsafe();
        });
    }



    @Override
    public ResponseCheckTx requestCheckTx(RequestCheckTx req) {
        var tx = req.getTx();
        int code = validate(tx);
        return ResponseCheckTx.newBuilder()
                .setCode(code)
                .setGasWanted(1)
                .build();
    }

    @Override
    public ResponseCommit requestCommit(RequestCommit requestCommit) {
        txn.commit();
        return ResponseCommit.newBuilder()
                .setData(ByteString.copyFrom(new byte[8]))
                .build();
    }

    @Override
    public ResponseDeliverTx receivedDeliverTx(RequestDeliverTx req) {
        var tx = req.getTx();
        int code = validate(tx);
        if (code == 0) {
            List<byte[]> parts = split(tx, '=');
            ByteIterable key = new ArrayByteIterable(parts.get(0));
            ByteIterable value = new ArrayByteIterable(parts.get(1));
            store.put(txn, key, value);
        }
        return ResponseDeliverTx.newBuilder()
                .setCode(code)
                .build();
    }

    @Override
    public ResponseQuery requestQuery(RequestQuery req) {
        var k = req.getData().toByteArray();
        var v = getPersistedValue(k);
        var builder = ResponseQuery.newBuilder();
        if (v == null) {
            builder.setLog("does not exist");
        } else {
            builder.setLog("exists");
            builder.setKey(ByteString.copyFrom(k));
            builder.setValue(ByteString.copyFrom(v));
        }
        return builder.build();
    }

    @Override
    public ResponseBeginBlock requestBeginBlock(RequestBeginBlock req) {
        try {
            txn = env.beginTransaction();
            store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn);
            return ResponseBeginBlock.newBuilder().build();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ResponseBeginBlock.newBuilder().build();
    }
}