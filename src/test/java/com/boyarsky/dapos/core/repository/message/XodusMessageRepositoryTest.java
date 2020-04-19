package com.boyarsky.dapos.core.repository.message;

import com.boyarsky.dapos.core.crypto.EncryptedData;
import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.message.MessageEntity;
import com.boyarsky.dapos.core.repository.RepoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ComponentScan("com.boyarsky.dapos.core.repository.message")
@ContextConfiguration(classes = {RepoTest.Config.class, XodusMessageRepositoryTest.class})
class XodusMessageRepositoryTest extends RepoTest {
    @Autowired
    XodusMessageRepository repos;
    AccountId alice = new AccountId("2500000000000000000000000000000000");
    AccountId bob = new AccountId("2500000000000000000000000000000001");
    AccountId chuck = new AccountId("2500000000000000000000000000000002");
    MessageEntity aliceToBob1 = new MessageEntity(1, new EncryptedData(new byte[64], new byte[32]), alice, bob, false);
    MessageEntity aliceToBob2 = new MessageEntity(2, new EncryptedData(new byte[64], new byte[32]), alice, bob, false);
    MessageEntity aliceToSelf1 = new MessageEntity(3, new EncryptedData(new byte[64], new byte[32]), alice, null, true);
    MessageEntity aliceToChuck1 = new MessageEntity(4, new EncryptedData(new byte[64], new byte[32]), alice, chuck, true);
    MessageEntity bobToAlice1 = new MessageEntity(5, new EncryptedData(new byte[64], new byte[32]), bob, alice, true);
    MessageEntity bobToChuck1 = new MessageEntity(6, new EncryptedData(new byte[64], new byte[32]), bob, chuck, true);
    MessageEntity aliceToSelf2 = new MessageEntity(7, new EncryptedData(new byte[64], new byte[32]), alice, null, true);

    MessageEntity bobToChuck2 = new MessageEntity(8, new EncryptedData(new byte[64], new byte[32]), bob, chuck, false);

    @BeforeEach
    void setUp() {
        manager.begin();
        repos.save(aliceToBob1);
        repos.save(aliceToBob2);
        repos.save(aliceToSelf1);
        repos.save(aliceToChuck1);
        repos.save(bobToAlice1);
        repos.save(bobToChuck1);
        repos.save(aliceToSelf2);
        manager.commit();
    }

    @Test
    void getToSelf() {
        List<MessageEntity> toSelf = repos.getToSelf(alice);
        assertEquals(List.of(aliceToSelf1, aliceToSelf2), toSelf);
        List<MessageEntity> bobToSelf = repos.getToSelf(bob);
        assertEquals(0, bobToSelf.size());
    }

    @Test
    void getWith() {
        List<MessageEntity> aliceBobChat = repos.getWith(alice, bob);
        assertEquals(List.of(aliceToBob1, aliceToBob2), aliceBobChat);

        List<MessageEntity> chuckAliceChat = repos.getWith(chuck, alice);
        assertEquals(0, chuckAliceChat.size());
    }

    @Test
    void getAll() {
        List<MessageEntity> all = repos.getAll(alice);
        assertEquals(List.of(aliceToBob1, aliceToBob2, aliceToSelf1, aliceToChuck1, aliceToSelf2), all);
    }

    @Test
    void save() {
        manager.begin();
        repos.save(bobToChuck2);
        manager.commit();
        List<MessageEntity> all = repos.getAll(bob);
        assertEquals(List.of(bobToAlice1, bobToChuck1, bobToChuck2), all);
    }
}