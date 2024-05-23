package com.bankoperations.bankoperations.controller;

import com.bankoperations.bankoperations.exception.InsufficientBalanceException;
import com.bankoperations.bankoperations.exception.InvalidTransferException;
import com.bankoperations.bankoperations.exception.UserNotFoundException;
import com.bankoperations.bankoperations.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping("/sendMoney/{senderId}/{recipientId}/{amount}")
    public ResponseEntity<?> sendMoney(@PathVariable Long senderId, @PathVariable Long recipientId,
                                       @PathVariable BigDecimal amount) {
        try {
            return ResponseEntity.ok(transferService.transferMoney(senderId, recipientId, amount));
        } catch (UserNotFoundException | InsufficientBalanceException | InvalidTransferException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
