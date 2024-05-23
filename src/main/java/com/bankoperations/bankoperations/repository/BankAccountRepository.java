package com.bankoperations.bankoperations.repository;

import com.bankoperations.bankoperations.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    BankAccount findByUserId(Long userId);
}
