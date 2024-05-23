package com.bankoperations.bankoperations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.bankoperations.bankoperations.entity.BankAccount;
import com.bankoperations.bankoperations.entity.User;
import com.bankoperations.bankoperations.exception.InsufficientBalanceException;
import com.bankoperations.bankoperations.exception.InvalidTransferException;
import com.bankoperations.bankoperations.exception.UserNotFoundException;
import com.bankoperations.bankoperations.repository.TransferRepository;
import com.bankoperations.bankoperations.repository.UserRepository;
import com.bankoperations.bankoperations.service.TransferService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = InvalidTransferException.class)
    public void testTransferMoneyToSameAccount() throws InsufficientBalanceException, UserNotFoundException,
            InvalidTransferException {
        Long senderId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);
        transferService.transferMoney(senderId, senderId, amount);
    }

    @Test(expected = UserNotFoundException.class)
    public void testTransferMoneySenderNotFound() throws InsufficientBalanceException, UserNotFoundException,
            InvalidTransferException {
        Long senderId = 1L;
        Long recipientId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100.0);
        when(userRepository.findById(senderId)).thenReturn(Optional.empty());
        transferService.transferMoney(senderId, recipientId, amount);
    }

    @Test(expected = UserNotFoundException.class)
    public void testTransferMoneyRecipientNotFound() throws InsufficientBalanceException, UserNotFoundException,
            InvalidTransferException {
        Long senderId = 1L;
        Long recipientId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100.0);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(recipientId)).thenReturn(Optional.empty());
        transferService.transferMoney(senderId, recipientId, amount);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testTransferMoneyInsufficientBalance() throws InsufficientBalanceException, UserNotFoundException,
            InvalidTransferException {
        Long senderId = 1L;
        Long recipientId = 2L;
        BigDecimal senderBalance = BigDecimal.valueOf(50.0);
        BigDecimal amount = BigDecimal.valueOf(100.0);
        User sender = new User();
        sender.setId(senderId);
        BankAccount senderAccount = new BankAccount();
        senderAccount.setBalance(senderBalance);
        sender.setBankAccount(senderAccount);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(new User()));
        transferService.transferMoney(senderId, recipientId, amount);
    }

    @Test
    public void testTransferMoneySuccessful() throws InsufficientBalanceException, UserNotFoundException,
            InvalidTransferException {
        Long senderId = 1L;
        Long recipientId = 2L;
        BigDecimal senderBalance = BigDecimal.valueOf(200.0);
        BigDecimal recipientBalance = BigDecimal.valueOf(100.0);
        BigDecimal amount = BigDecimal.valueOf(50.0);
        User sender = new User();
        sender.setId(senderId);
        BankAccount senderAccount = new BankAccount();
        senderAccount.setBalance(senderBalance);
        sender.setBankAccount(senderAccount);
        User recipient = new User();
        recipient.setId(recipientId);
        BankAccount recipientAccount = new BankAccount();
        recipientAccount.setBalance(recipientBalance);
        recipient.setBankAccount(recipientAccount);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        transferService.transferMoney(senderId, recipientId, amount);
        assertEquals(senderBalance.subtract(amount), sender.getBankAccount().getBalance());
        assertEquals(recipientBalance.add(amount), recipient.getBankAccount().getBalance());
    }
}
