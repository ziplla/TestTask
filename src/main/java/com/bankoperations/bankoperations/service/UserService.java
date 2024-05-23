package com.bankoperations.bankoperations.service;

import com.bankoperations.bankoperations.entity.BankAccount;
import com.bankoperations.bankoperations.entity.User;
import com.bankoperations.bankoperations.exception.*;
import com.bankoperations.bankoperations.dto.UpdateEmailRequest;
import com.bankoperations.bankoperations.dto.UpdatePhoneRequest;
import com.bankoperations.bankoperations.repository.BankAccountRepository;
import com.bankoperations.bankoperations.repository.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private final static Logger log = Logger.getLogger(UserService.class);

    public User createUser(User request) throws InvalidUserException {

        try {
            validateUser(request);
        } catch (InvalidUserException e) {
            throw new InvalidUserException(e.getMessage());
        }

        User user = new User();

        user.setUsername(request.getUsername());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        user.setInitialDeposit(request.getInitialDeposit());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUsername(request.getUsername());

        user.setDateOfBirth(request.getDateOfBirth());
        user.setFullName(request.getFullName());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUser(user);
        bankAccount.setBalance(request.getInitialDeposit());

        user = userRepository.save(user);
        bankAccountRepository.save(bankAccount);

        log.info("User with username: " + user.getUsername() + " was created");

        return user;

    }

    public void validateUser(User user) throws InvalidUserException {
        try {
            validateInitialDeposit(user);
        } catch (InvalidInitialDepositException e) {
            throw new InvalidUserException(e.getMessage());
        }

        try {
            validateUsername(user);
        } catch (InvalidUsernameException e) {
            throw new InvalidUserException(e.getMessage());
        }

        try {
            validateEmail(user);
        } catch (InvalidEmailException e) {
            throw new InvalidUserException(e.getMessage());
        }

        try {
            validatePhoneNumber(user);
        } catch (InvalidPhoneNumberException e) {
            throw new InvalidUserException(e.getMessage());
        }
    }

    public static void validateInitialDeposit(User user) throws InvalidInitialDepositException {
        BigDecimal balance = user.getInitialDeposit();
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInitialDepositException("Initial deposit can't be 0 or less");
        }
    }

    public void validateUsername(User user) throws InvalidUsernameException {
        User checkUsername = userRepository.findByUsername(user.getUsername());
        if (checkUsername != null) {
            throw new InvalidUsernameException("This username is already in use");
        }
    }

    public void validatePhoneNumber(User user) throws InvalidPhoneNumberException {
        User checkPhoneNumber = userRepository.findByPhoneNumber(user.getPhoneNumber());
        if (checkPhoneNumber != null) {
            throw new InvalidPhoneNumberException("This phone number is already in use");
        }
    }

    public void validateEmail(User user) throws InvalidEmailException {
        User checkEmail = userRepository.findByEmail(user.getEmail());
        if (checkEmail != null) {
            throw new InvalidEmailException("This email is already in use");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateEmail(Long userId, UpdateEmailRequest request) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (request.getNewEmail() != null) {
                user.setEmail(request.getNewEmail());
            }
            log.info("User with ID:" + userId + " update email " + user.getEmail() + " to " + request.getNewEmail());
            return userRepository.save(user);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    public User updatePhoneNumber(Long userId, UpdatePhoneRequest request) throws UserNotFoundException {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (request.getNewPhoneNumber() != null) {
                user.setPhoneNumber(request.getNewPhoneNumber());
            }
            log.info("User with ID:" + userId + " update phone " + user.getPhoneNumber() + " to " + request.getNewPhoneNumber());
            return userRepository.save(user);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    public boolean deleteEmail(Long userId) throws UserNotFoundException, NoContactInfoException {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (user.getPhoneNumber() != null) {
                user.setEmail(null);
                log.info("User with ID:" + userId + " delete email");
                userRepository.save(user);
                return true;
            } else {
                throw new NoContactInfoException("You can't delete all contact info");
            }
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    public boolean deletePhoneNumber(Long userId) throws UserNotFoundException, NoContactInfoException {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (user.getEmail() != null) {
                user.setPhoneNumber(null);
                log.info("User with ID:" + userId + " delete email");
                userRepository.save(user);
                return true;
            } else {
                throw new NoContactInfoException("You can't delete all contact info");
            }
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    public List<Object> searchUsers(String fullName, String email, String phoneNumber, Date dateOfBirth, Pageable pageable) {
        Specification<User> specification = Specification.where(null);

        if (fullName != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("fullName"), fullName + "%"));
        }

        if (email != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("email"), email));
        }

        if (phoneNumber != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("phoneNumber"), phoneNumber));
        }

        if (dateOfBirth != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("dateOfBirth"), dateOfBirth));
        }

        return userRepository.findAll(specification, pageable).getContent();
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                try {
                    return getByUsername(username);
                } catch (UserNotFoundException e) {
                    throw new UsernameNotFoundException("User not found", e);
                }
            }
        };
    }


    public User getByUsername(String username) throws UserNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return user;
    }

    public User getCurrentUser() throws UserNotFoundException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }


}
