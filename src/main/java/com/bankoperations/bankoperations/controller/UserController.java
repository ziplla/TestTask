package com.bankoperations.bankoperations.controller;

import com.bankoperations.bankoperations.entity.User;
import com.bankoperations.bankoperations.exception.InvalidUserException;
import com.bankoperations.bankoperations.exception.NoContactInfoException;
import com.bankoperations.bankoperations.exception.UserNotFoundException;
import com.bankoperations.bankoperations.dto.UpdateEmailRequest;
import com.bankoperations.bankoperations.dto.UpdatePhoneRequest;
import com.bankoperations.bankoperations.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.createUser(user));
        } catch (InvalidUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user: " + e.getMessage());
        }
    }

    @PutMapping("/updateEmail/{userID}")
    public ResponseEntity<?> updateEmail(@PathVariable Long userID,
                                         @RequestBody UpdateEmailRequest updateEmailRequest) {
        try {
            return ResponseEntity.ok(userService.updateEmail(userID, updateEmailRequest));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/updatePhone/{userID}")
    public ResponseEntity<?> updatePhone(@PathVariable Long userID,
                                         @RequestBody UpdatePhoneRequest updatePhoneRequest) {
        try {
            return ResponseEntity.ok(userService.updatePhoneNumber(userID, updatePhoneRequest));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/deleteEmail/{userId}")
    public ResponseEntity<?> deleteEmail(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.deleteEmail(userId));
        } catch (UserNotFoundException | NoContactInfoException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/deletePhone/{userId}")
    public ResponseEntity<?> deletePhoneNumber(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userService.deletePhoneNumber(userId));
        } catch (UserNotFoundException | NoContactInfoException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Object>> searchUsers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateOfBirth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        String[] sortParts = sort.split(",");
        String sortBy = sortParts[0];
        String sortOrder = sortParts.length > 1 ? sortParts[1] : "asc";

        Sort.Order order = new Sort.Order(Sort.Direction.fromString(sortOrder), sortBy);

        Pageable pageable = PageRequest.of(page, size, Sort.by(order));

        List<Object> users = userService.searchUsers(fullName, email, phoneNumber, dateOfBirth, pageable);

        return ResponseEntity.ok(users);
    }
}
