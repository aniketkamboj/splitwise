package com.example.splitwise.controller;

import com.example.splitwise.dto.ApiResponse;
import com.example.splitwise.dto.BalanceDetail;
import com.example.splitwise.dto.BalanceSheetResponse;
import com.example.splitwise.dto.OutstandingBalanceResponse;
import com.example.splitwise.dto.UserBalanceSummaryResponse;
import com.example.splitwise.entities.User;
import com.example.splitwise.entities.UserExpenseBalanceSheet;
import com.example.splitwise.service.BalanceSheetService;
import com.example.splitwise.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/balance-sheets")
@RequiredArgsConstructor
public class BalanceSheetController {

    private final BalanceSheetService balanceSheetService;
    private final UserService userService;

    @GetMapping("/{userId}/outstanding")
    public ResponseEntity<ApiResponse<OutstandingBalanceResponse>> getOutstandingBalance(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            UserExpenseBalanceSheet balanceSheet = balanceSheetService.getUserBalanceSheet(user);
            
            Double totalOwe = balanceSheet.getTotalYouOwe();
            Double totalReceive = balanceSheet.getTotalYouGetBack();
            Double netOutstanding = totalReceive - totalOwe;
            
            OutstandingBalanceResponse response = OutstandingBalanceResponse.builder()
                    .userId(userId)
                    .totalOutstandingOwe(totalOwe)
                    .totalOutstandingReceive(totalReceive)
                    .netOutstanding(netOutstanding)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<ApiResponse<UserBalanceSummaryResponse>> getUserBalanceSummary(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            UserExpenseBalanceSheet balanceSheet = balanceSheetService.getUserBalanceSheet(user);
            
            Double netBalance = balanceSheet.getTotalYouGetBack() - balanceSheet.getTotalYouOwe();
            
            UserBalanceSummaryResponse response = UserBalanceSummaryResponse.builder()
                    .userId(userId)
                    .userName(user.getName())
                    .netBalance(netBalance)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<BalanceSheetResponse>> getBalanceSheet(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            UserExpenseBalanceSheet balanceSheet = balanceSheetService.getUserBalanceSheet(user);
            BalanceSheetResponse response = mapToBalanceSheetResponse(userId, balanceSheet);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private BalanceSheetResponse mapToBalanceSheetResponse(String userId, UserExpenseBalanceSheet balanceSheet) {
        Map<String, BalanceDetail> userVsBalance = new HashMap<>();
        
        if (balanceSheet.getUserVsBalance() != null) {
            userVsBalance = balanceSheet.getUserVsBalance().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> BalanceDetail.builder()
                                    .amountOwe(entry.getValue().getAmountOwe())
                                    .amountGetBack(entry.getValue().getAmountGetBack())
                                    .build()
                    ));
        }

        return BalanceSheetResponse.builder()
                .userId(userId)
                .totalPayment(balanceSheet.getTotalPayment())
                .totalYourExpense(balanceSheet.getTotalYourExpense())
                .totalYouGetBack(balanceSheet.getTotalYouGetBack())
                .totalYouOwe(balanceSheet.getTotalYouOwe())
                .userVsBalance(userVsBalance)
                .build();
    }
}
