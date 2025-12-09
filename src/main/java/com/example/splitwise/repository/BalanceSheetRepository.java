package com.example.splitwise.repository;

import com.example.splitwise.entities.UserExpenseBalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceSheetRepository extends JpaRepository<UserExpenseBalanceSheet, Long> {
    Optional<UserExpenseBalanceSheet> findByUser_UserId(String userId);
}

