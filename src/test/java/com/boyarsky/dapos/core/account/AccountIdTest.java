package com.boyarsky.dapos.core.account;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.utils.Base58;
import com.boyarsky.dapos.utils.Convert;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountIdTest {
    AccountId encodedEd25 = new AccountId("dap25" + hexBytes(16));
    AccountId plainEd25 = new AccountId("25" + hexBytes(16));
    AccountId encodedValidator = new AccountId("davnn11" + hexBytes(20));
    AccountId plainValidator = new AccountId("nn11" + hexBytes(20));
    AccountId encodedEth = new AccountId("det0x" + hexBytes(20));
    AccountId plainEth = new AccountId("0x" + hexBytes(20));
    AccountId encodedBtc = new AccountId("dbt" + bitcoin());
    AccountId plainBtc = new AccountId(bitcoin());

    @Test
    void types() {
        assertTrue(encodedEd25.isEd25());
        assertTrue(plainEd25.isEd25());
        assertTrue(encodedValidator.isVal());
        assertTrue(plainValidator.isVal());
        assertTrue(encodedEth.isEth());
        assertTrue(plainEth.isEth());
        assertTrue(encodedBtc.isBitcoin());
        assertTrue(plainBtc.isBitcoin());
    }

    @Test
    void appSpecificId() {
        assertEquals(encodedEd25.getAppSpecificAccount(), "dap" + encodedEd25.getOrigAccount());
        assertEquals(plainEd25.getAppSpecificAccount(), "dap" + plainEd25.getOrigAccount());
        assertEquals(encodedBtc.getAppSpecificAccount(), "dbt" + encodedBtc.getOrigAccount());
        assertEquals(plainBtc.getAppSpecificAccount(), "dbt" + plainBtc.getOrigAccount());
        assertEquals(plainEth.getAppSpecificAccount(), "det" + plainEth.getOrigAccount());
        assertEquals(encodedEth.getAppSpecificAccount(), "det" + encodedEth.getOrigAccount());
        assertEquals(encodedValidator.getAppSpecificAccount(), "dav" + encodedValidator.getOrigAccount());
        assertEquals(plainValidator.getAppSpecificAccount(), "dav" + plainValidator.getOrigAccount());
    }

    @Test
    void serialize() {
        serialize(encodedEd25, plainEd25, 17);
        serialize(encodedValidator, plainValidator, 22);
        serialize(encodedBtc, plainBtc, 26);
        serialize(encodedEth, plainEth, 21);
    }

    private void serialize(AccountId encoded, AccountId plain, int size) {
        int encodedSize = encoded.size();
        int plainSize = plain.size();
        assertEquals(size, encodedSize);
        assertEquals(size, plainSize);
        ByteBuffer buff = ByteBuffer.allocate(size);
        plain.putBytes(buff);
        buff.flip();
        AccountId deserialized = AccountId.fromBytes(buff);
        assertEquals(plain, deserialized);

        buff.flip();
        byte[] rawBytes = new byte[size - 1];
        buff.get();
        buff.get(rawBytes);
        assertArrayEquals(plain.getAddressBytes(), rawBytes);
        assertEquals(plain, AccountId.fromBytes(rawBytes));
    }

    byte[] randomBytes(int num) {
        byte[] bytes = new byte[num];
        new Random().nextBytes(bytes);
        return bytes;
    }

    String hexBytes(int num) {
        byte[] bytes = randomBytes(num);
        return Convert.toHexString(bytes);
    }

    String bitcoin() {
        byte[] bytes = randomBytes(25);
        bytes[0] = 0;
        return Base58.encode(bytes);
    }
}