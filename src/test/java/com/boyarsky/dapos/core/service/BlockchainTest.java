package com.boyarsky.dapos.core.service;

import com.boyarsky.dapos.TestUtil;
import com.boyarsky.dapos.core.TransactionManager;
import com.boyarsky.dapos.core.config.BlockchainConfig;
import com.boyarsky.dapos.core.config.HeightConfig;
import com.boyarsky.dapos.core.crypto.CryptoUtils;
import com.boyarsky.dapos.core.genesis.Genesis;
import com.boyarsky.dapos.core.genesis.GenesisInitResult;
import com.boyarsky.dapos.core.model.LastSuccessBlockData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.ValidatorEntity;
import com.boyarsky.dapos.core.repository.block.BlockRepository;
import com.boyarsky.dapos.core.service.validator.ValidatorService;
import com.boyarsky.dapos.core.tx.ErrorCode;
import com.boyarsky.dapos.core.tx.ProcessingResult;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.TransactionProcessor;
import com.boyarsky.dapos.utils.Convert;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import types.Evidence;
import types.Validator;
import types.VoteInfo;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlockchainTest {

    @Mock
    BlockchainConfig config;
    @Mock
    BlockRepository blockRepository;
    @Mock
    Genesis genesis;
    @Mock
    TransactionProcessor processor;
    @Mock
    TransactionManager manager;
    @Mock
    ValidatorService validatorService;

    AccountId validatorId1 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validatorId2 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validatorId3 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validatorId4 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validatorId5 = TestUtil.generateValidatorAcc().getCryptoId();
    AccountId validatorId6 = TestUtil.generateValidatorAcc().getCryptoId();

    Blockchain blockchain;

    @BeforeEach
    void setUp() {
        blockchain = new Blockchain(blockRepository, config, genesis, processor, manager, validatorService);
    }

    @Test
    void getLastBlock() {
        LastSuccessBlockData data = new LastSuccessBlockData(new byte[32], 120);
        doReturn(data).when(blockRepository).getLastBlock();
        LastSuccessBlockData lastBlock = blockchain.getLastBlock();
        assertEquals(data, lastBlock);
    }

    @Test
    void beginBlock() {
        VoteInfo v1 = vote(true, validatorId1);
        VoteInfo v2 = vote(true, validatorId2);
        VoteInfo v3 = vote(true, validatorId3);
        VoteInfo v4 = vote(false, validatorId4);
        VoteInfo v5 = vote(false, validatorId5);
        Evidence evidence1 = evidence(validatorId1);
        Evidence evidence2 = evidence(validatorId4);
        Evidence evidence3 = evidence(validatorId6);
        doReturn(100L).when(config).getBlockReward();
        doReturn(222L).when(validatorService).punishAbsents(Set.of(validatorId5), 10);
        doReturn(111L).when(validatorService).punishByzantines(Set.of(validatorId1, validatorId4, validatorId6), 10);

        blockchain.beginBlock(10, List.of(v1, v2, v3, v4, v5), List.of(evidence1, evidence2, evidence3));

        verify(validatorService).distributeReward(Set.of(validatorId2, validatorId3), 333, 10);
        verify(manager).begin();
        assertEquals(10, blockchain.getCurrentBlockHeight());
        assertEquals(100L, blockchain.getRewardAmount().get());
    }

    private VoteInfo vote(boolean signedLast, AccountId validatorAddress) {
        return VoteInfo.newBuilder()
                .setSignedLastBlock(signedLast)
                .setValidator(Validator.newBuilder()
                        .setPower(0)
                        .setAddress(ByteString.copyFrom(CryptoUtils.decodeValidatorAddress(validatorAddress.getOrigAccount())))
                        .build())
                .build();
    }

    private Evidence evidence(AccountId validatorAddress) {
        return Evidence.newBuilder()
                .setValidator(Validator.newBuilder()
                        .setPower(0)
                        .setAddress(ByteString.copyFrom(CryptoUtils.decodeValidatorAddress(validatorAddress.getOrigAccount())))
                        .build())
                .build();
    }

    @Test
    void addNewBlock() {
        blockchain.addNewBlock(new byte[32]);

        verify(blockRepository).insert(new LastSuccessBlockData(new byte[32], 0));
    }

    @Test
    void getCurrentHeight() {
        long currentBlockHeight = blockchain.getCurrentBlockHeight();
        assertEquals(0, currentBlockHeight);
    }

    @Test
    void deliverTx_unsuccessful() {
        byte[] tx = new byte[0];
        ProcessingResult result = new ProcessingResult("ERROR", new ErrorCode(-1), mock(Transaction.class), null);
        doReturn(result).when(processor).tryDeliver(tx, 0);

        ProcessingResult r = blockchain.deliverTx(tx);

        assertEquals(r, result);
        assertEquals(0, blockchain.getRewardAmount().get());
    }

    @Test
    void deliverTx() {
        byte[] txBytes = new byte[120];
        Transaction tx = mock(Transaction.class);
        doReturn(100L).when(tx).getFee();
        ProcessingResult result = new ProcessingResult("OK", new ErrorCode(0), tx, null);
        doReturn(result).when(processor).tryDeliver(txBytes, 0);

        ProcessingResult r = blockchain.deliverTx(txBytes);

        assertEquals(r, result);
        assertEquals(100, blockchain.getRewardAmount().get());
    }

    @Test
    void checkTx() {
        byte[] tx = new byte[0];
        ProcessingResult result = new ProcessingResult("OK", new ErrorCode(0), mock(Transaction.class), null);
        doReturn(result).when(processor).parseAndValidate(tx);

        ProcessingResult r = blockchain.checkTx(tx);

        assertEquals(result, r);
    }

    @Test
    void onInitChain() {
        HeightConfig heightConfig = mock(HeightConfig.class);
        doReturn(heightConfig).when(config).init(1);
        GenesisInitResult genesisResult = new GenesisInitResult(List.of(mock(ValidatorEntity.class), mock(ValidatorEntity.class)), 5);

        doReturn(genesisResult).when(genesis).initialize();

        InitChainResponse response = blockchain.onInitChain();
        assertEquals(heightConfig, response.getConfig());
        assertEquals(genesisResult, response.getGenesisInitResult());
    }

    @Test
    void commitBlock() {
        byte[] bytes = blockchain.commitBlock();

        assertEquals(Convert.toHexString(new byte[8]), Convert.toHexString(bytes));
        verify(manager).commit();
        verify(blockRepository).insert(new LastSuccessBlockData(bytes, 0));
    }

    @Test
    void endBlock() {
        HeightConfig heightConfig = mock(HeightConfig.class);
        doReturn(heightConfig).when(config).tryUpdateForHeight(1);
        List<ValidatorEntity> updatedValidators = List.of(mock(ValidatorEntity.class), mock(ValidatorEntity.class));
        doReturn(updatedValidators).when(validatorService).getAll(0);

        EndBlockResponse endBlockResponse = blockchain.endBlock();

        assertEquals(heightConfig, endBlockResponse.getNewConfig());
        assertEquals(updatedValidators, endBlockResponse.getValidators());
    }
}