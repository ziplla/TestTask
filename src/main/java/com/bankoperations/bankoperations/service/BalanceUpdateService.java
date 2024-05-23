package com.bankoperations.bankoperations.service;

import com.bankoperations.bankoperations.entity.BankAccount;
import com.bankoperations.bankoperations.entity.User;
import com.bankoperations.bankoperations.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@EnableScheduling
public class BalanceUpdateService {

    @Autowired
    private UserRepository userRepository;

    private final static Logger log = Logger.getLogger(BalanceUpdateService.class);

    @Scheduled(fixedRate = 60000)
    public void updateBalances() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            BankAccount bankAccount = user.getBankAccount();
            BigDecimal currentBalance = bankAccount.getBalance();
            BigDecimal initialDeposit = user.getInitialDeposit();
            BigDecimal interestRate = new BigDecimal("1.05");
            BigDecimal newBalance = currentBalance.multiply(interestRate);
            BigDecimal maxBalance = initialDeposit.multiply(new BigDecimal("2.07"));

            if (newBalance.compareTo(maxBalance) > 0) {
                newBalance = maxBalance;
            }

            bankAccount.setBalance(newBalance);
            log.info("Balance " + bankAccount.getId() + " increased by " + newBalance.subtract(currentBalance));
            userRepository.save(user);
        }
    }
}

