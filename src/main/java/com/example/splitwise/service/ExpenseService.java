package com.example.splitwise.service;


import com.example.splitwise.dto.CreateExpenseRequest;
import com.example.splitwise.dto.PaymentDetail;
import com.example.splitwise.dto.SplitDetail;
import com.example.splitwise.entities.Expense;
import com.example.splitwise.entities.ExpensePayment;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import com.example.splitwise.enums.ExpenseSplitType;
import com.example.splitwise.repository.ExpenseRepository;
import com.example.splitwise.strategy.ExpenseSplit;
import com.example.splitwise.strategy.SplitFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SplitFactory splitFactory;
    private final BalanceSheetService balanceSheetService;
    private final UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, SplitFactory splitFactory, BalanceSheetService balanceSheetService, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.splitFactory = splitFactory;
        this.balanceSheetService = balanceSheetService;
        this.userService = userService;
    }

    @Transactional
    public Expense createExpense(CreateExpenseRequest request) {

        String expenseId = request.getExpenseId();
        if (expenseId == null || expenseId.isEmpty()) {
            expenseId = generateExpenseId();
        }
        if (expenseRepository.existsByExpenseId(expenseId)) {
            throw new IllegalArgumentException("Expense with expenseId " + expenseId + " already exists");
        }

        // Create ExpensePayment objects from PaymentDetail DTOs
        List<ExpensePayment> payments = createPayments(request.getPayments());
        ExpenseSplit expenseSplit = splitFactory.getSplitObject(request.getSplitType());
        List<Split> splits = createSplits(request);



        // Validate that total payments match expense amount
        double totalPaid = payments.stream()
                .mapToDouble(ExpensePayment::getAmountPaid)
                .sum();
        
        if (Math.abs(totalPaid - expenseAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Total payments (" + totalPaid + ") must equal expense amount (" + expenseAmount + ")"
            );
        }


        expenseSplit.validateSplitRequest(splits, expenseAmount);

        Expense expense = Expense.builder()
                .expenseId(expenseId)
                .expenseAmount(request.getExpenseAmount())
                .description(request.getDescription())
                .payments(payments)
                .splitType(request.getSplitType())
                .splits(splits)
                .build();

        // Set expense reference in payments
        for (ExpensePayment payment : payments) {
            payment.setExpense(expense);
        }

        // Set expense reference in splits
        for (Split split : splits) {
            split.setExpense(expense);
        }

        Expense savedExpense = expenseRepository.save(expense);

        // Update balance sheets for all payers
        balanceSheetService.updateUserExpenseBalanceSheet(payments, splits, expenseAmount);

        return savedExpense;
    }

    @Transactional
    public Expense createExpenseWithGroup(CreateExpenseRequest request) {
        
        // Validate that total payments match expense amount
        double totalPaid = payments.stream()
                .mapToDouble(ExpensePayment::getAmountPaid)
                .sum();
        
        if (Math.abs(totalPaid - expenseAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Total payments (" + totalPaid + ") must equal expense amount (" + expenseAmount + ")"
            );
        }
        
        ExpenseSplit expenseSplit = splitFactory.getSplitObject(splitType);
        expenseSplit.validateSplitRequest(splits, expenseAmount);

        Expense expense = Expense.builder()
                .expenseId(expenseId)
                .expenseAmount(expenseAmount)
                .description(description)
                .payments(payments)
                .splitType(splitType)
                .splits(splits)
                .group(group)
                .build();

        // Set expense reference in payments
        for (ExpensePayment payment : payments) {
            payment.setExpense(expense);
        }

        // Set expense reference in splits
        for (Split split : splits) {
            split.setExpense(expense);
        }

        Expense savedExpense = expenseRepository.save(expense);

        // Update balance sheets for all payers
        balanceSheetService.updateUserExpenseBalanceSheet(payments, splits, expenseAmount);

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
        return expenseRepository.findByPayments_User_UserId(userId);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public String generateExpenseId() {
        return "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<ExpensePayment> createPayments(List<PaymentDetail> paymentDetails) {
        if (paymentDetails == null || paymentDetails.isEmpty()) {
            throw new IllegalArgumentException("At least one payment is required");
        }

        return paymentDetails.stream()
                .map(paymentDetail -> {
                    User user = userService.getUserById(paymentDetail.getUserId());
                    return new ExpensePayment(user, paymentDetail.getAmountPaid());
                })
                .collect(Collectors.toList());
    }

    private List<Split> createSplits(CreateExpenseRequest request) {
        if (request.getSplits() == null || request.getSplits().isEmpty()) {
            throw new IllegalArgumentException("Split details are required for " + splitType + " split");
        }
        List<User> users = request.getSplits().stream()
                .map(split -> userService.getUserById(split.getUserId()))
                .collect(Collectors.toList());
        ExpenseSplitType splitType = request.getSplitType();
        ExpenseSplit expenseSplit = splitFactory.getSplitObject(splitType);

        List<Split> splits = new ArrayList<>();

        if (splitType == ExpenseSplitType.EQUAL) {
            splits = expenseSplit.validateAndGetSplits(users, null, request.getExpenseAmount());
        } else {
            if (splitType == ExpenseSplitType.PERCENTAGE) {
                List<Double> percentages = request.getSplits().stream()
                        .map(SplitDetail::getAmount)
                        .collect(Collectors.toList());
                splits = expenseSplit.validateAndGetSplits(users, percentages, request.getExpenseAmount());
            } else {
                // UNEQUAL or EXACT
                for (SplitDetail splitDetail : request.getSplits()) {
                    User user = userService.getUserById(splitDetail.getUserId());
                    splits.add(new Split(user, splitDetail.getAmount()));
                }
                expenseSplit.validateSplitRequest(splits, request.getExpenseAmount());
            }
        }

        return splits;
    }
}

