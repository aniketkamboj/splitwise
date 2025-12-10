package com.example.splitwise.service;


import com.example.splitwise.dto.CreateExpenseRequest;
import com.example.splitwise.dto.PaymentDetail;
import com.example.splitwise.dto.SplitDetail;
import com.example.splitwise.dto.UserExpenseResponse;
import com.example.splitwise.entities.*;
import com.example.splitwise.enums.ExpenseSplitType;
import com.example.splitwise.repository.ExpenseRepository;
import com.example.splitwise.strategy.ExpenseSplit;
import com.example.splitwise.strategy.SplitFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SplitFactory splitFactory;
    private final BalanceSheetService balanceSheetService;
    private final UserService userService;
    private final GroupService groupService;

    public ExpenseService(ExpenseRepository expenseRepository, SplitFactory splitFactory, BalanceSheetService balanceSheetService, UserService userService, GroupService groupService) {
        this.expenseRepository = expenseRepository;
        this.splitFactory = splitFactory;
        this.balanceSheetService = balanceSheetService;
        this.userService = userService;
        this.groupService = groupService;
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

        double expenseAmount = request.getExpenseAmount();
        // Create ExpensePayment objects from PaymentDetail DTOs
        List<ExpensePayment> payments = createPayments(request.getPayments());

        // Validate that total payments match expense amount
        double totalPaid = payments.stream()
                .mapToDouble(ExpensePayment::getAmountPaid)
                .sum();

        if (Math.abs(totalPaid - expenseAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Total payments (" + totalPaid + ") must equal expense amount (" + expenseAmount + ")"
            );
        }

        List<Split> splits = createSplits(request);

        Expense expense = Expense.builder()
                .expenseId(expenseId)
                .expenseAmount(request.getExpenseAmount())
                .description(request.getDescription())
                .payments(payments)
                .splitType(request.getSplitType())
                .splits(splits)
                .date(LocalDate.now())
                .build();

        if(request.getGroupId() != null) {
            Group group = groupService.getGroupById(request.getGroupId());
            expense.setGroup(group);
        }

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

    public List<UserExpenseResponse> getExpensesByUser(String userId) {
        
        // Find list of all expenses where user is in split
        List<Expense> expensesInSplits = expenseRepository.findExpensesByUserInSplits(userId);

        // Find list of all expenses where user is in payment
        List<Expense> expensesInPayments = expenseRepository.findExpensesByUserInPayments(userId);

        // Consolidate expenses removing duplicates
        Set<Expense> consolidatedExpenses = new LinkedHashSet<>();
        consolidatedExpenses.addAll(expensesInSplits);
        consolidatedExpenses.addAll(expensesInPayments);

        // Calculate user's share for each expense and return sorted by date
        return consolidatedExpenses.stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .map(expense -> mapToUserExpenseResponse(expense, userId))
                .collect(Collectors.toList());
    }

    private UserExpenseResponse mapToUserExpenseResponse(Expense expense, String userId) {
        // Calculate amount paid by user in this expense
        double amountPaid = expense.getPayments().stream()
                .filter(payment -> payment.getUser().getUserId().equals(userId))
                .mapToDouble(ExpensePayment::getAmountPaid)
                .sum();

        // Calculate amount owed by user in this expense
        double amountOwed = expense.getSplits().stream()
                .filter(split -> split.getUser().getUserId().equals(userId))
                .mapToDouble(Split::getAmountOwe)
                .sum();

        // userShare = amountPaid - amountOwed
        // Positive: user gets back this amount
        // Negative: user owes this amount
        double userShare = amountPaid - amountOwed;

        return UserExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .description(expense.getDescription())
                .expenseAmount(expense.getExpenseAmount())
                .payments(expense.getPayments().stream()
                        .map(p -> new PaymentDetail(p.getUser().getUserId(), p.getAmountPaid()))
                        .collect(Collectors.toList()))
                .splitType(expense.getSplitType())
                .splits(expense.getSplits().stream()
                        .map(s -> new SplitDetail(s.getUser().getUserId(), s.getAmountOwe()))
                        .collect(Collectors.toList()))
                .groupName(expense.getGroup() != null ? expense.getGroup().getGroupName() : null)
                .date(expense.getDate())
                .userShare(userShare)
                .build();
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
        ExpenseSplitType splitType = request.getSplitType();
        if (request.getSplits() == null || request.getSplits().isEmpty()) {
            throw new IllegalArgumentException("Split details are required for " + splitType + " split");
        }
        List<User> users = request.getSplits().stream()
                .map(split -> userService.getUserById(split.getUserId()))
                .collect(Collectors.toList());
        Double totalAmount = request.getExpenseAmount();
        List<SplitDetail> splitDetails = request.getSplits();

        ExpenseSplit expenseSplit = splitFactory.getSplitObject(splitType);
        List<Split> splits = new ArrayList<>();
        splits = expenseSplit.validateAndGetSplits(users, totalAmount, splitDetails);
        return splits;
    }
}

