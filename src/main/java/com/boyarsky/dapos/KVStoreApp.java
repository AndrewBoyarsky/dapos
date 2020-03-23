package com.boyarsky.dapos;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import types.ABCIApplicationGrpc;
import types.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class KVStoreApp extends ABCIApplicationGrpc.ABCIApplicationImplBase {
    private Environment env;
    private Transaction txn = null;
    private Store store = null;

    @Autowired
    public KVStoreApp(Environment env) {
        this.env = env;
    }

    @Override
    public void checkTx(RequestCheckTx req, StreamObserver<ResponseCheckTx> responseObserver) {
        var tx = req.getTx();
        int code = validate(tx);
        var resp = ResponseCheckTx.newBuilder()
                .setCode(code)
                .setGasWanted(1)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    private int validate(ByteString tx) {
        try {
            List<byte[]> parts = split(tx, '_');
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
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return 1;
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
    public void echo(RequestEcho request, StreamObserver<ResponseEcho> responseObserver) {
        responseObserver.onNext(ResponseEcho.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void flush(RequestFlush request, StreamObserver<ResponseFlush> responseObserver) {
        responseObserver.onNext(ResponseFlush.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void info(RequestInfo request, StreamObserver<ResponseInfo> responseObserver) {
        responseObserver.onNext(ResponseInfo.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void setOption(RequestSetOption request, StreamObserver<ResponseSetOption> responseObserver) {
        responseObserver.onNext(ResponseSetOption.newBuilder().setCode(0).build());
        responseObserver.onCompleted();
    }

    @Override
    public void initChain(RequestInitChain request, StreamObserver<ResponseInitChain> responseObserver) {
        responseObserver.onNext(ResponseInitChain.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void endBlock(RequestEndBlock request, StreamObserver<ResponseEndBlock> responseObserver) {
        responseObserver.onNext(ResponseEndBlock.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void beginBlock(RequestBeginBlock req, StreamObserver responseObserver) {
        txn = env.beginTransaction();
        store = env.openStore("store", StoreConfig.WITHOUT_DUPLICATES, txn);
        var resp = ResponseBeginBlock.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deliverTx(RequestDeliverTx req, StreamObserver responseObserver) {
        var tx = req.getTx();
        int code = validate(tx);
        if (code == 0) {
            List<byte[]> parts = split(tx, '_');
            ByteIterable key = new ArrayByteIterable(parts.get(0));
            ByteIterable value = new ArrayByteIterable(parts.get(1));
            store.put(txn, key, value);
        }
        var resp = ResponseDeliverTx.newBuilder()
                .setCode(code)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void commit(RequestCommit req, StreamObserver responseObserver) {
        txn.commit();
        var resp = ResponseCommit.newBuilder()
                .setData(ByteString.copyFrom(new byte[8]))
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void query(RequestQuery req, StreamObserver responseObserver) {
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
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}