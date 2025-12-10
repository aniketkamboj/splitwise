package com.example.splitwise.controller;

import com.example.splitwise.dto.*;
import com.example.splitwise.entities.*;
import com.example.splitwise.service.ExpenseService;
import com.example.splitwise.service.GroupService;
import com.example.splitwise.service.UserService;
import com.example.splitwise.strategy.SplitFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final GroupService groupService;

    public ExpenseController(ExpenseService expenseService, GroupService groupService) {
        this.expenseService = expenseService;
        this.groupService = groupService;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        try {
            Expense expense;

            if (request.getGroupId() != null && !request.getGroupId().isEmpty()) {
                Group group = groupService.getGroupById(request.getGroupId());
                expense = expenseService.createExpenseWithGroup(request);
            } else {
                expense = expenseService.createExpense(request);
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

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        List<SplitDetail> splitDetails = expense.getSplits() != null
                ? expense.getSplits().stream()
                        .map(split -> SplitDetail.builder()
                                .userId(split.getUser().getUserId())
                                .amount(split.getAmountOwe())
                                .build())
                        .collect(Collectors.toList())
                : List.of();

        List<PaymentDetail> paymentDetails = expense.getPayments() != null
                ? expense.getPayments().stream()
                        .map(payment -> PaymentDetail.builder()
                                .userId(payment.getUser().getUserId())
                                .amountPaid(payment.getAmountPaid())
                                .build())
                        .collect(Collectors.toList())
                : List.of();

        String groupId = expense.getGroup() != null ? expense.getGroup().getGroupId() : null;

        return ExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .description(expense.getDescription())
                .expenseAmount(expense.getExpenseAmount())
                .payments(paymentDetails)
                .splitType(expense.getSplitType())
                .splits(splitDetails)
                .groupId(groupId)
                .build();
    }
}
