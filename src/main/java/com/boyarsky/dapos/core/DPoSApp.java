package com.boyarsky.dapos.core;

import com.apollocurrency.aplwallet.apl.util.StringUtils;
import com.boyarsky.dapos.core.dao.BlockchainDao;
import com.boyarsky.dapos.core.tx.TransactionParser;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import types.ABCIApplicationGrpc;
import types.RequestBeginBlock;
import types.RequestCheckTx;
import types.RequestCommit;
import types.RequestDeliverTx;
import types.RequestEcho;
import types.RequestEndBlock;
import types.RequestFlush;
import types.RequestInfo;
import types.RequestInitChain;
import types.RequestQuery;
import types.RequestSetOption;
import types.ResponseBeginBlock;
import types.ResponseCheckTx;
import types.ResponseCommit;
import types.ResponseDeliverTx;
import types.ResponseEcho;
import types.ResponseEndBlock;
import types.ResponseFlush;
import types.ResponseInfo;
import types.ResponseInitChain;
import types.ResponseQuery;
import types.ResponseSetOption;

import java.nio.charset.StandardCharsets;
@Component
@Slf4j
public class DPoSApp  extends ABCIApplicationGrpc.ABCIApplicationImplBase {
    public static final String version = "1.0.0.PRE-ALPHA";
    public static final long protocolVersion = 2;
    private final BlockchainDao dao;
    private final TransactionParser parser;
    private final TransactionManager manager;
    private final QueryDispatcher dispatcher;

    @Autowired
    public DPoSApp(BlockchainDao dao, TransactionParser parser, TransactionManager manager, QueryDispatcher dispatcher) {
        this.dao = dao;
        this.parser = parser;
        this.manager = manager;
        this.dispatcher = dispatcher;
    }

    @Override
        public void checkTx(RequestCheckTx req, StreamObserver<ResponseCheckTx> responseObserver) {
            ByteString tx = req.getTx(); // validate transactions here
        parser.parseTx(tx.toByteArray());
//            int code = validate(tx);
            var resp = ResponseCheckTx.newBuilder()
                    .setCode(0)
                    .setGasWanted(1)
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
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
            log.info("Node blockchain version: {}", request.getBlockVersion());
            log.info("Node p2p version: {}", request.getP2PVersion());
            log.info("Application protocol version: {}", protocolVersion);
            log.info("Application version: {}", version);
            responseObserver.onNext(ResponseInfo.newBuilder()
                    .setVersion(version)
                    .setAppVersion(protocolVersion)

                    .build());
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
            responseObserver.onNext(ResponseEndBlock.newBuilder()

                    .build());
            responseObserver.onCompleted();
        }

        @Override
        public void beginBlock(RequestBeginBlock req, StreamObserver responseObserver) {
            manager.begin();
            var resp = ResponseBeginBlock.newBuilder().build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }

        @Override
        public void deliverTx(RequestDeliverTx req, StreamObserver responseObserver) {
            var tx = req.getTx(); // validate txs here
//            int code = validate(tx);
            int code = 0;
            if (code == 0) {
               // persist transactions here
            }
            var resp = ResponseDeliverTx.newBuilder()
                    .setCode(code)
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }

        @Override
        public void commit(RequestCommit req, StreamObserver responseObserver) {
            manager.commit();
            var resp = ResponseCommit.newBuilder()
                    .setData(ByteString.copyFrom(new byte[8]))
                    .build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }

        @Override
        public void query(RequestQuery req, StreamObserver responseObserver) {
            var k = req.getData().toByteArray();
            var builder = ResponseQuery.newBuilder();
            String result = dispatcher.dispatch(req.getPath(), k);
            if (StringUtils.isBlank(result)) {
                builder.setLog("does not exist");
            } else {
                builder.setLog("exists");
                builder.setKey(ByteString.copyFrom(k));
                builder.setValue(ByteString.copyFrom(result, StandardCharsets.UTF_8));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }

}
