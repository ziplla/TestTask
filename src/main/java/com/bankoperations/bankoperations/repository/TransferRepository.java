package com.bankoperations.bankoperations.repository;

import com.bankoperations.bankoperations.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
