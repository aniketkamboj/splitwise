package com.example.splitwise.repository;

import com.example.splitwise.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByExpenseId(String expenseId);
    boolean existsByExpenseId(String expenseId);
    List<Expense> findByGroup_GroupId(String groupId);
    
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.splits s WHERE s.user.userId = :userId")
    List<Expense> findExpensesByUserInSplits(@Param("userId") String userId);

    @Query("SELECT DISTINCT e FROM Expense e JOIN e.payments p WHERE p.user.userId = :userId")
    List<Expense> findExpensesByUserInPayments(@Param("userId") String userId);
}

