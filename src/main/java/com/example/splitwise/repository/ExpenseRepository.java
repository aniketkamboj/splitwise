package com.example.splitwise.repository;

import com.example.splitwise.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByExpenseId(String expenseId);
    boolean existsByExpenseId(String expenseId);
    List<Expense> findByGroup_GroupId(String groupId);
    List<Expense> findByPayments_User_UserId(String userId);
}

