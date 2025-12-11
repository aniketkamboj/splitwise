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

        // Calculate total payment amount for proportional distribution
        double totalPaid = payments.stream()
                .mapToDouble(ExpensePayment::getAmountPaid)
                .sum();

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

            // Update total owe for the user who owes (full amount, not proportional)
            oweUserExpenseSheet.setTotalYouOwe(
                    oweUserExpenseSheet.getTotalYouOwe() + oweAmount
            );

            // For each payer, update balances proportionally
            for (ExpensePayment payment : payments) {
                User payingUser = payment.getUser();
                
                // Skip if the payer is the same as the user who owes
                if (payingUser.getUserId().equals(userOwe.getUserId())) {
                    continue;
                }

                // Calculate proportional amount this payer should get back from the owe user
                // Based on how much this payer contributed to the total payment
                double payerProportion = payment.getAmountPaid() / totalPaid;
                double proportionalOweAmount = oweAmount * payerProportion;

                UserExpenseBalanceSheet payerBalanceSheet = payingUser.getUserExpenseBalanceSheet();

                // Payer gets back proportional money from the user who owes
                payerBalanceSheet.setTotalYouGetBack(
                        payerBalanceSheet.getTotalYouGetBack() + proportionalOweAmount
                );

                // Update individual balance: payer gets back proportional amount from owe user
                Balance userOweBalance = getOrCreateBalance(
                        payerBalanceSheet, userOwe.getUserId()
                );
                userOweBalance.setAmountGetBack(userOweBalance.getAmountGetBack() + proportionalOweAmount);

                // Update individual balance: owe user owes proportional amount to payer
                Balance userPaidBalance = getOrCreateBalance(
                        oweUserExpenseSheet, payingUser.getUserId()
                );
                userPaidBalance.setAmountOwe(userPaidBalance.getAmountOwe() + proportionalOweAmount);
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

