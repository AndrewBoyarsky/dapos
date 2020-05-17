package com.boyarsky.dapos.core;

import com.apollocurrency.aplwallet.apl.util.StringUtils;
import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.service.Blockchain;
import com.boyarsky.dapos.core.service.EndBlockEnvelope;
import com.boyarsky.dapos.core.service.ValidatorUpdate;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import types.ABCIApplicationGrpc;
import types.BlockParams;
import types.ConsensusParams;
import types.EvidenceParams;
import types.PubKey;
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
import java.util.List;

@Component
@Slf4j
public class DPoSApp extends ABCIApplicationGrpc.ABCIApplicationImplBase {
    public static final String version = "1.0.0.PRE-ALPHA";
    public static final long protocolVersion = 2;
    public static final String PUBLIC_KEY_TYPE = "ed25519";
    @Autowired
    private Blockchain blockchain;
    @Autowired
    private QueryDispatcher dispatcher;

    private boolean acceptRequest = true;

    public boolean isAcceptRequest() {
        return acceptRequest;
    }

    public void setAcceptRequest(boolean acceptRequest) {
        this.acceptRequest = acceptRequest;
    }

    @Override
    public void checkTx(RequestCheckTx req, StreamObserver<ResponseCheckTx> responseObserver) {
        ResponseCheckTx.Builder respBuilder = ResponseCheckTx.newBuilder();
        ProcessingResult result = blockchain.checkTx(req.getTx().toByteArray());
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
            HeightConfig config = blockchain.onInitChain();

            responseObserver.onNext(ResponseInitChain.newBuilder()
                    .setConsensusParams(map(config))
                    .build());
            responseObserver.onCompleted();
        }

    @Override
    public void endBlock(RequestEndBlock request, StreamObserver<ResponseEndBlock> responseObserver) {
        ResponseEndBlock.Builder builder = ResponseEndBlock.newBuilder();
        EndBlockEnvelope endBlockEnvelope = blockchain.endBlock();
        HeightConfig newConfig = endBlockEnvelope.getNewConfig();
        if (newConfig != null) {
            builder.setConsensusParamUpdates(map(newConfig));
        }
        List<ValidatorUpdate> validators = endBlockEnvelope.getValidators();
        for (ValidatorUpdate validator : validators) {
            builder.addValidatorUpdates(types.ValidatorUpdate.newBuilder()
                    .setPower(validator.getPower())
                    .setPubKey(PubKey.newBuilder()
                            .setType(PUBLIC_KEY_TYPE)
                            .setData(ByteString.copyFrom(validator.getPublicKey()))
                            .build()));

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
        if (!acceptRequest) {
            throw new RuntimeException("Dapos app was stopped");
        }
        blockchain.beginBlock(req.getHeader().getHeight(), req.getLastCommitInfo().getVotesList(), req.getByzantineValidatorsList(), req.getHeader().getProposerAddress().toByteArray());
        var resp = ResponseBeginBlock.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deliverTx(RequestDeliverTx req, StreamObserver<ResponseDeliverTx> responseObserver) {
        ResponseDeliverTx.Builder respBuilder = ResponseDeliverTx.newBuilder();
        ProcessingResult parsingResult = blockchain.deliverTx(req.getTx().toByteArray());
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
        byte[] hash = blockchain.commitBlock();
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
