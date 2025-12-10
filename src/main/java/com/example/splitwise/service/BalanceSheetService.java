package com.example.splitwise.service;

import com.example.splitwise.entities.Balance;
import com.example.splitwise.entities.ExpensePayment;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import com.example.splitwise.entities.UserExpenseBalanceSheet;
import com.example.splitwise.repository.BalanceSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceSheetService {

    private final BalanceSheetRepository balanceSheetRepository;

    @Transactional
    public void updateUserExpenseBalanceSheet(List<ExpensePayment> payments, List<Split> splits, Double totalExpenseAmount) {
        // Create a map of user IDs to their payment amounts
        Map<String, Double> userPayments = payments.stream()
                .collect(Collectors.toMap(
                        payment -> payment.getUser().getUserId(),
                        ExpensePayment::getAmountPaid,
                        (existing, replacement) -> existing + replacement // Sum if same user paid multiple times
                ));

        // Update total payment for all users who paid
        for (ExpensePayment payment : payments) {
            User payingUser = payment.getUser();
            UserExpenseBalanceSheet payerBalanceSheet = payingUser.getUserExpenseBalanceSheet();
            payerBalanceSheet.setTotalPayment(
                    payerBalanceSheet.getTotalPayment() + payment.getAmountPaid()
            );
        }

        // Process each split
        for (Split split : splits) {
            User userOwe = split.getUser();
            UserExpenseBalanceSheet oweUserExpenseSheet = userOwe.getUserExpenseBalanceSheet();
            Double oweAmount = split.getAmountOwe();

            // Check if this user also paid for this expense
            Double amountPaidByOweUser = userPayments.getOrDefault(userOwe.getUserId(), 0.0);

            if (amountPaidByOweUser > 0) {
                // User paid and owes - update their own expense
                oweUserExpenseSheet.setTotalYourExpense(
                        oweUserExpenseSheet.getTotalYourExpense() + oweAmount
                );
            }

            // For each payer, update balances
            for (ExpensePayment payment : payments) {
                User payingUser = payment.getUser();
                
                // Skip if the payer is the same as the user who owes
                if (payingUser.getUserId().equals(userOwe.getUserId())) {
                    continue;
                }

                UserExpenseBalanceSheet payerBalanceSheet = payingUser.getUserExpenseBalanceSheet();

                // Payer gets back money from the user who owes
                payerBalanceSheet.setTotalYouGetBack(
                        payerBalanceSheet.getTotalYouGetBack() + oweAmount
                );

                Balance userOweBalance = getOrCreateBalance(
                        payerBalanceSheet, userOwe.getUserId()
                );
                userOweBalance.setAmountGetBack(userOweBalance.getAmountGetBack() + oweAmount);

                // User who owes has debt to the payer
                oweUserExpenseSheet.setTotalYouOwe(oweUserExpenseSheet.getTotalYouOwe() + oweAmount);
                oweUserExpenseSheet.setTotalYourExpense(
                        oweUserExpenseSheet.getTotalYourExpense() + oweAmount
                );

                Balance userPaidBalance = getOrCreateBalance(
                        oweUserExpenseSheet, payingUser.getUserId()
                );
                userPaidBalance.setAmountOwe(userPaidBalance.getAmountOwe() + oweAmount);
            }
        }

        // Save all payer balance sheets
        for (ExpensePayment payment : payments) {
            UserExpenseBalanceSheet balanceSheet = payment.getUser().getUserExpenseBalanceSheet();
            if (balanceSheet.getId() == null) {
                // New balance sheet, need to save
                balanceSheetRepository.save(balanceSheet);
            }
        }
        
        // Save all owe user balance sheets
        Set<String> oweUserIds = new HashSet<>();
        for (Split split : splits) {
            String userId = split.getUser().getUserId();
            if (!oweUserIds.contains(userId)) {
                oweUserIds.add(userId);
                UserExpenseBalanceSheet balanceSheet = split.getUser().getUserExpenseBalanceSheet();
                if (balanceSheet.getId() == null) {
                    // New balance sheet, need to save
                    balanceSheetRepository.save(balanceSheet);
                }
            }
        }
    }

    private Balance getOrCreateBalance(UserExpenseBalanceSheet balanceSheet, String userId) {
        Map<String, Balance> userVsBalance = balanceSheet.getUserVsBalance();
        Balance balance = userVsBalance.get(userId);

        if (balance == null) {
            balance = new Balance();
            balance.setUserId(userId);
            balance.setBalanceSheet(balanceSheet);
            balance.setAmountOwe(0.0);
            balance.setAmountGetBack(0.0);
            balanceSheet.addBalance(balance);
        }

        return balance;
    }

    public UserExpenseBalanceSheet getUserBalanceSheet(User user) {
        return user.getUserExpenseBalanceSheet();
    }
}

