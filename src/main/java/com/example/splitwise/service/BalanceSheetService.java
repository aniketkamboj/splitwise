package com.example.splitwise.service;

import com.example.splitwise.entities.Balance;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import com.example.splitwise.entities.UserExpenseBalanceSheet;
import com.example.splitwise.repository.BalanceSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BalanceSheetService {

    private final BalanceSheetRepository balanceSheetRepository;

    @Transactional
    public void updateUserExpenseBalanceSheet(User expensePaidBy, java.util.List<Split> splits, Double totalExpenseAmount) {
        // Update the total amount paid of the expense paid by user
        UserExpenseBalanceSheet paidByUserExpenseSheet = expensePaidBy.getUserExpenseBalanceSheet();
        paidByUserExpenseSheet.setTotalPayment(paidByUserExpenseSheet.getTotalPayment() + totalExpenseAmount);

        for (Split split : splits) {
            User userOwe = split.getUser();
            UserExpenseBalanceSheet oweUserExpenseSheet = userOwe.getUserExpenseBalanceSheet();
            Double oweAmount = split.getAmountOwe();

            if (expensePaidBy.getUserId().equals(userOwe.getUserId())) {
                paidByUserExpenseSheet.setTotalYourExpense(
                        paidByUserExpenseSheet.getTotalYourExpense() + oweAmount
                );
            } else {
                // Update the balance of paid user
                paidByUserExpenseSheet.setTotalYouGetBack(
                        paidByUserExpenseSheet.getTotalYouGetBack() + oweAmount
                );

                Balance userOweBalance = getOrCreateBalance(
                        paidByUserExpenseSheet, userOwe.getUserId()
                );
                userOweBalance.setAmountGetBack(userOweBalance.getAmountGetBack() + oweAmount);

                // Update the balance sheet of owe user
                oweUserExpenseSheet.setTotalYouOwe(oweUserExpenseSheet.getTotalYouOwe() + oweAmount);
                oweUserExpenseSheet.setTotalYourExpense(
                        oweUserExpenseSheet.getTotalYourExpense() + oweAmount
                );

                Balance userPaidBalance = getOrCreateBalance(
                        oweUserExpenseSheet, expensePaidBy.getUserId()
                );
                userPaidBalance.setAmountOwe(userPaidBalance.getAmountOwe() + oweAmount);
            }
        }

        balanceSheetRepository.save(paidByUserExpenseSheet);
        for (Split split : splits) {
            if (!expensePaidBy.getUserId().equals(split.getUser().getUserId())) {
                balanceSheetRepository.save(split.getUser().getUserExpenseBalanceSheet());
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

