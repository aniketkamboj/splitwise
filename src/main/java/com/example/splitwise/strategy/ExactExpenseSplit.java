package com.example.splitwise.strategy;

import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExactExpenseSplit implements ExpenseSplit {

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
        if (users.size() != splitAmounts.size()) {
            throw new IllegalArgumentException(
                    "Number of users (" + users.size() + ") must match number of exact amounts (" + splitAmounts.size() + ")"
            );
        }
        
        double totalSplitAmount = splitAmounts.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalSplitAmount - totalAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Total split amount " + totalSplitAmount + " doesn't match expense amount " + totalAmount
            );
        }
        
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            splits.add(new Split(users.get(i), splitAmounts.get(i)));
        }
        
        return splits;
    }
}

