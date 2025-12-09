package com.example.splitwise.service;

import com.example.splitwise.entities.Expense;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import com.example.splitwise.enums.ExpenseSplitType;
import com.example.splitwise.repository.ExpenseRepository;
import com.example.splitwise.strategy.ExpenseSplit;
import com.example.splitwise.strategy.SplitFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SplitFactory splitFactory;
    private final BalanceSheetService balanceSheetService;
    private final UserService userService;

    @Transactional
    public Expense createExpense(String expenseId, String description, Double expenseAmount,
                                 List<Split> splits, ExpenseSplitType splitType, String paidByUserId) {
        
        if (expenseRepository.existsByExpenseId(expenseId)) {
            throw new IllegalArgumentException("Expense with expenseId " + expenseId + " already exists");
        }

        User paidBy = userService.getUserById(paidByUserId);

        ExpenseSplit expenseSplit = splitFactory.getSplitObject(splitType);
        expenseSplit.validateSplitRequest(splits, expenseAmount);

        Expense expense = Expense.builder()
                .expenseId(expenseId)
                .expenseAmount(expenseAmount)
                .description(description)
                .paidBy(paidBy)
                .splitType(splitType)
                .splits(splits)
                .build();

        // Set expense reference in splits
        for (Split split : splits) {
            split.setExpense(expense);
        }

        Expense savedExpense = expenseRepository.save(expense);

        // Update balance sheets
        balanceSheetService.updateUserExpenseBalanceSheet(paidBy, splits, expenseAmount);

        return savedExpense;
    }

    @Transactional
    public Expense createExpenseWithGroup(String expenseId, String description, Double expenseAmount,
                                         List<Split> splits, ExpenseSplitType splitType, 
                                         String paidByUserId, com.example.splitwise.entities.Group group) {
        
        User paidBy = userService.getUserById(paidByUserId);
        
        ExpenseSplit expenseSplit = splitFactory.getSplitObject(splitType);
        expenseSplit.validateSplitRequest(splits, expenseAmount);

        Expense expense = Expense.builder()
                .expenseId(expenseId)
                .expenseAmount(expenseAmount)
                .description(description)
                .paidBy(paidBy)
                .splitType(splitType)
                .splits(splits)
                .group(group)
                .build();

        // Set expense reference in splits
        for (Split split : splits) {
            split.setExpense(expense);
        }

        Expense savedExpense = expenseRepository.save(expense);

        // Update balance sheets
        balanceSheetService.updateUserExpenseBalanceSheet(paidBy, splits, expenseAmount);

        return savedExpense;
    }

    public Expense getExpenseById(String expenseId) {
        return expenseRepository.findByExpenseId(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found with expenseId: " + expenseId));
    }

    public List<Expense> getExpensesByGroup(String groupId) {
        return expenseRepository.findByGroup_GroupId(groupId);
    }

    public List<Expense> getExpensesByUser(String userId) {
        return expenseRepository.findByPaidBy_UserId(userId);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public String generateExpenseId() {
        return "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

