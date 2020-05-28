package com.boyarsky.dapos.core.tx.type.validator.impl;

import com.boyarsky.dapos.core.model.account.AccountId;
import com.boyarsky.dapos.core.model.currency.Currency;
import com.boyarsky.dapos.core.model.currency.CurrencyHolder;
import com.boyarsky.dapos.core.service.currency.CurrencyService;
import com.boyarsky.dapos.core.tx.ErrorCodes;
import com.boyarsky.dapos.core.tx.Transaction;
import com.boyarsky.dapos.core.tx.type.attachment.impl.CurrencyIdAttachment;
import com.boyarsky.dapos.core.tx.type.validator.TxNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CurrencyClaimTransactionValidatorTest {
    @Mock
    CurrencyService currencyService;

    CurrencyClaimTransactionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CurrencyClaimTransactionValidator(currencyService);
    }

    @Test
    void validate() {
//        recipient for claim reserve
        AccountId recipient = mock(AccountId.class);
        Transaction tx = mock(Transaction.class);
        doReturn(recipient).when(tx).getRecipient();

        TxNotValidException exception = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.CURRENCY_CLAIM_RECIPIENT_NOT_ALLOWED, exception.getCode());

//      no currency hold
        AccountId sender = mock(AccountId.class);
        doReturn(sender).when(tx).getSender();
        doReturn(null).when(tx).getRecipient();
        doReturn(new CurrencyIdAttachment((byte) 1, 3)).when(tx).getAttachment(CurrencyIdAttachment.class);

        TxNotValidException noCurrencyEx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.CURRENCY_CLAIM_NO_CURRENCY, noCurrencyEx.getCode());
//       not enough holding
        doReturn(new CurrencyHolder(2, sender, 3, 90)).when(currencyService).getCurrencyHolder(sender, 3);
        doReturn(91L).when(tx).getAmount();

        TxNotValidException notEnoughEx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.CURRENCY_CLAIM_NOT_ENOUGH_CURRENCY, notEnoughEx.getCode());

        //        currency without reserve cannot be claimed partially
        doReturn(89L).when(tx).getAmount();
        doReturn(new Currency(1, 3, "", "", "", recipient, 90, 0, (byte) 2)).when(currencyService).getById(3);

        TxNotValidException zeroReserveEx = assertThrows(TxNotValidException.class, () -> validator.validate(tx));

        assertEquals(ErrorCodes.CURRENCY_CLAIM_ZERO_RESERVE, zeroReserveEx.getCode());
        //        ok
        doReturn(90L).when(tx).getAmount();

        validator.validate(tx);
    }
}