package com.boyarsky.dapos.core.repository.validator;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.validator.VoteEntity;
import com.boyarsky.dapos.core.repository.Pagination;

import java.util.List;

public interface VoteRepository {
    void save(VoteEntity v);

    void remove(VoteEntity vote);

    VoteEntity getBy(AccountId validatorId, AccountId voterId);

    List<VoteEntity> getAllVotesForValidator(AccountId validatorId, Pagination pagination);

    List<VoteEntity> getAllVotesForValidator(AccountId validatorId);

    long countAllVotesForValidator(AccountId validatorId);

    VoteEntity minVoteForValidator(AccountId validatorId);

    List<VoteEntity> getAllVotesForVoter(AccountId voterId);

    List<VoteEntity> getAllVotesForVoter(AccountId voterId, Pagination pagination);

    List<VoteEntity> getAll(Pagination pagination);
}
