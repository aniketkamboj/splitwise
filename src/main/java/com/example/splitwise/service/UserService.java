package com.example.splitwise.service;

import com.example.splitwise.entities.User;
import com.example.splitwise.entities.UserExpenseBalanceSheet;
import com.example.splitwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String userId, String name, String email, String mobileNumber) {
        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User with userId " + userId + " already exists");
        }
        
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        User user = User.builder()
                .userId(userId)
                .name(name)
                .email(email)
                .mobileNumber(mobileNumber)
                .build();

        // Initialize balance sheet
        UserExpenseBalanceSheet balanceSheet = new UserExpenseBalanceSheet();
        balanceSheet.setUser(user);
        user.setUserExpenseBalanceSheet(balanceSheet);

        return userRepository.save(user);
    }

    public User getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with userId: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(String userId, String name, String email, String mobileNumber) {
        User user = getUserById(userId);
        
        if (name != null) {
            user.setName(name);
        }
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("User with email " + email + " already exists");
            }
            user.setEmail(email);
        }
        if (mobileNumber != null) {
            user.setMobileNumber(mobileNumber);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}

