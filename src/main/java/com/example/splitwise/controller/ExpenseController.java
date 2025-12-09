package com.example.splitwise.controller;

import com.example.splitwise.dto.*;
import com.example.splitwise.entities.Expense;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import com.example.splitwise.enums.ExpenseSplitType;
import com.example.splitwise.service.ExpenseService;
import com.example.splitwise.service.GroupService;
import com.example.splitwise.service.UserService;
import com.example.splitwise.strategy.ExpenseSplit;
import com.example.splitwise.strategy.SplitFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;
    private final GroupService groupService;
    private final SplitFactory splitFactory;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        try {
            String expenseId = request.getExpenseId();
            if (expenseId == null || expenseId.isEmpty()) {
                expenseId = expenseService.generateExpenseId();
            }

            List<Split> splits = createSplits(request);
            Expense expense;

            if (request.getGroupId() != null && !request.getGroupId().isEmpty()) {
                com.example.splitwise.entities.Group group = groupService.getGroupById(request.getGroupId());
                expense = expenseService.createExpenseWithGroup(
                        expenseId,
                        request.getDescription(),
                        request.getExpenseAmount(),
                        splits,
                        request.getSplitType(),
                        request.getPaidByUserId(),
                        group
                );
            } else {
                expense = expenseService.createExpense(
                        expenseId,
                        request.getDescription(),
                        request.getExpenseAmount(),
                        splits,
                        request.getSplitType(),
                        request.getPaidByUserId()
                );
            }

            ExpenseResponse response = mapToExpenseResponse(expense);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Expense created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(@PathVariable String expenseId) {
        try {
            Expense expense = expenseService.getExpenseById(expenseId);
            ExpenseResponse response = mapToExpenseResponse(expense);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpenses() {
        List<ExpenseResponse> expenses = expenseService.getAllExpenses().stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByGroup(@PathVariable String groupId) {
        try {
            List<ExpenseResponse> expenses = expenseService.getExpensesByGroup(groupId).stream()
                    .map(this::mapToExpenseResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(expenses));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByUser(@PathVariable String userId) {
        try {
            List<ExpenseResponse> expenses = expenseService.getExpensesByUser(userId).stream()
                    .map(this::mapToExpenseResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(expenses));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private List<Split> createSplits(CreateExpenseRequest request) {
        ExpenseSplitType splitType = request.getSplitType();
        ExpenseSplit expenseSplit = splitFactory.getSplitObject(splitType);

        List<Split> splits = new ArrayList<>();

        if (splitType == ExpenseSplitType.EQUAL) {
            if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                throw new IllegalArgumentException("User IDs are required for EQUAL split");
            }
            List<User> users = request.getUserIds().stream()
                    .map(userService::getUserById)
                    .collect(Collectors.toList());
            splits = expenseSplit.validateAndGetSplits(users, null, request.getExpenseAmount());
        } else {
            if (request.getSplits() == null || request.getSplits().isEmpty()) {
                throw new IllegalArgumentException("Split details are required for " + splitType + " split");
            }
            
            if (splitType == ExpenseSplitType.PERCENTAGE) {
                List<User> users = request.getSplits().stream()
                        .map(split -> userService.getUserById(split.getUserId()))
                        .collect(Collectors.toList());
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

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        List<SplitDetail> splitDetails = expense.getSplits() != null
                ? expense.getSplits().stream()
                        .map(split -> SplitDetail.builder()
                                .userId(split.getUser().getUserId())
                                .amount(split.getAmountOwe())
                                .build())
                        .collect(Collectors.toList())
                : List.of();

        String groupId = expense.getGroup() != null ? expense.getGroup().getGroupId() : null;

        return ExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .description(expense.getDescription())
                .expenseAmount(expense.getExpenseAmount())
                .paidByUserId(expense.getPaidBy().getUserId())
                .splitType(expense.getSplitType())
                .splits(splitDetails)
                .groupId(groupId)
                .build();
    }
}
