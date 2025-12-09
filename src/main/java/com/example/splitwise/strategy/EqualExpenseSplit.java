package com.example.splitwise.strategy;

import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EqualExpenseSplit implements ExpenseSplit {

    @Override
    public void validateSplitRequest(List<Split> splits, Double totalAmount) {
        double totalSplitAmount = splits.stream()
                .mapToDouble(Split::getAmountOwe)
                .sum();
        
        if (Math.abs(totalSplitAmount - totalAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Total split amount " + totalSplitAmount + " doesn't match expense amount " + totalAmount
            );
        }
    }

    @Override
    public List<Split> validateAndGetSplits(List<User> users, List<Double> splitAmounts, Double totalAmount) {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Users list cannot be empty");
        }
        
        double amountPerUser = totalAmount / users.size();
        List<Split> splits = new ArrayList<>();
        
        for (User user : users) {
            splits.add(new Split(user, amountPerUser));
        }
        
        return splits;
    }
}

