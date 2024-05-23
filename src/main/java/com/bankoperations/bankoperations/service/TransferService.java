package com.bankoperations.bankoperations.service;

import com.bankoperations.bankoperations.entity.Transfer;
import com.bankoperations.bankoperations.entity.User;
import com.bankoperations.bankoperations.exception.InsufficientBalanceException;
import com.bankoperations.bankoperations.exception.InvalidTransferException;
import com.bankoperations.bankoperations.exception.UserNotFoundException;
import com.bankoperations.bankoperations.repository.TransferRepository;
import com.bankoperations.bankoperations.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Transactional
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;

    private final static Logger log = Logger.getLogger(TransferService.class);

    @Transactional(propagation = Propagation.REQUIRED)
    public Transfer transferMoney(Long senderId, Long recipientId, BigDecimal amount) throws InsufficientBalanceException,
            UserNotFoundException, InvalidTransferException {

        if (senderId.equals(recipientId)) {
            throw new InvalidTransferException("You cannot transfer money to your account");
        }

        User sender = userRepository.findByIdForUpdate(senderId).orElse(null);
        User recipient = userRepository.findByIdForUpdate(recipientId).orElse(null);

        if (sender == null) {
            throw new UserNotFoundException("Sender not found");
        }

        if (recipient == null) {
            throw new UserNotFoundException("Recipient not found");
        }

        BigDecimal senderBalance = sender.getBankAccount().getBalance();
        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("There are not enough funds in the account to complete the transfer");
        }

        BigDecimal newSenderBalance = senderBalance.subtract(amount);
        sender.getBankAccount().setBalance(newSenderBalance);
        userRepository.save(sender);

        BigDecimal recipientBalance = recipient.getBankAccount().getBalance();
        BigDecimal newRecipientBalance = recipientBalance.add(amount);
        recipient.getBankAccount().setBalance(newRecipientBalance);
        userRepository.save(recipient);

        Transfer transfer = new Transfer();
        transfer.setSender(sender);
        transfer.setRecipient(recipient);
        transfer.setAmount(amount);
        transfer.setTimestamp(new Date());
        transferRepository.save(transfer);

        log.info("User with ID: " + senderId + " sent " + amount + " to user with ID: " + recipientId);

        return transfer;
    }
}


