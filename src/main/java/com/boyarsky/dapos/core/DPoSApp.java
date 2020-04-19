package com.boyarsky.dapos.core;

import com.apollocurrency.aplwallet.apl.util.StringUtils;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.genesis.Genesis;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import types.ABCIApplicationGrpc;
import types.BlockParams;
import types.ConsensusParams;
import types.EvidenceParams;
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
    @Autowired
    private Blockchain blockchain;
    @Autowired
    private TransactionProcessor processor;
    @Autowired
    private TransactionManager manager;
    @Autowired
    private QueryDispatcher dispatcher;
    @Autowired
    private Genesis genesis;
    @Autowired
    private BlockchainConfig config;


    @Override
    public void checkTx(RequestCheckTx req, StreamObserver<ResponseCheckTx> responseObserver) {
        ResponseCheckTx.Builder respBuilder = ResponseCheckTx.newBuilder();
        ProcessingResult result = processor.parseAndValidate(req.getTx().toByteArray());
        respBuilder
                .setCode(result.getCode().getCode())
                .setCodespace(result.getCode().getCodeSpace())
                .setGasWanted(result.getGasData().getWanted())
                .setGasUsed(result.getGasData().getUsed())
                .setLog(result.getMessage());
        responseObserver.onNext(respBuilder.build());
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
            log.info("Tendermint Node blockchain version: {}", request.getBlockVersion());
            log.info("Tendermint Node p2p version: {}", request.getP2PVersion());
            log.info("Application protocol version: {}", protocolVersion);
            log.info("Application version: {}", version);
            LastSuccessBlockData lastBlock = blockchain.getLastBlock();
            ResponseInfo.Builder builder = ResponseInfo.newBuilder();
            if (lastBlock != null) {
                log.info("Current block {}", lastBlock.getHeight());
                config.init(lastBlock.getHeight());
                builder.setLastBlockHeight(lastBlock.getHeight())
                        .setLastBlockAppHash(ByteString.copyFrom(lastBlock.getAppHash()));
            } else {
                log.warn("No block committed yet");
            }
            responseObserver.onNext(builder.setVersion(version)
                    .setAppVersion(protocolVersion).build());
            responseObserver.onCompleted();
        }

        @Override
        public void setOption(RequestSetOption request, StreamObserver<ResponseSetOption> responseObserver) {
            responseObserver.onNext(ResponseSetOption.newBuilder().setCode(0).build());
            responseObserver.onCompleted();
        }

        @Override
        public void initChain(RequestInitChain request, StreamObserver<ResponseInitChain> responseObserver) {
            manager.begin();
            try {
                log.info("Applying genesis");
                genesis.initialize();
                manager.commit();
            } catch (Exception e) {
                log.error("Genesis init error", e);
                manager.rollback();
            }
            log.info("Genesis applied");
            config.init(1);
            responseObserver.onNext(ResponseInitChain.newBuilder()
                    .setConsensusParams(map(config.getCurrentConfig()))
                    .build());
            responseObserver.onCompleted();
        }

    @Override
    public void endBlock(RequestEndBlock request, StreamObserver<ResponseEndBlock> responseObserver) {
        boolean updated = config.tryUpdateForHeight(blockchain.getCurrentBlockHeight() + 1);
        ResponseEndBlock.Builder builder = ResponseEndBlock.newBuilder();
        if (updated) {
            HeightConfig currentConfig = config.getCurrentConfig();
            log.info("Updation config to: " + currentConfig);
            builder.setConsensusParamUpdates(map(currentConfig));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private ConsensusParams map(HeightConfig config) {
        return (ConsensusParams.newBuilder()
                .setBlock(BlockParams.newBuilder()
                        .setMaxGas(config.getMaxGas())
                        .setMaxBytes(config.getMaxSize())
                        .build())
                .setEvidence(EvidenceParams.newBuilder()
                        .setMaxAge(config.getMaxEvidenceAge())
                        .build())
                .build());
    }

    @Override
    public void beginBlock(RequestBeginBlock req, StreamObserver<ResponseBeginBlock> responseObserver) {
        manager.begin();
        blockchain.beginBlock(req.getHeader().getHeight());
        var resp = ResponseBeginBlock.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deliverTx(RequestDeliverTx req, StreamObserver<ResponseDeliverTx> responseObserver) {
        ResponseDeliverTx.Builder respBuilder = ResponseDeliverTx.newBuilder();
        ProcessingResult parsingResult = processor.tryDeliver(req.getTx().toByteArray());
        respBuilder
                .setCode(parsingResult.getCode().getCode())
                .setCodespace(parsingResult.getCode().getCodeSpace())
                .setGasWanted(parsingResult.getGasData().getWanted())
                .setGasUsed(parsingResult.getGasData().getUsed())
                .setLog(parsingResult.getMessage());
        responseObserver.onNext(respBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void commit(RequestCommit req, StreamObserver<ResponseCommit> responseObserver) {
        byte[] hash = new byte[8];
        blockchain.addNewBlock(hash);
        manager.commit();
        ByteString appData = ByteString.copyFrom(hash);
        var resp = ResponseCommit.newBuilder()
                .setData(appData)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void query(RequestQuery req, StreamObserver<ResponseQuery> responseObserver) {
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
