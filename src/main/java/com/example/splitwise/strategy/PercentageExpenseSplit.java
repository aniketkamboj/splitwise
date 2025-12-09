package com.example.splitwise.strategy;

import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PercentageExpenseSplit implements ExpenseSplit {

    @Override
    public void validateSplitRequest(List<Split> splits, Double totalAmount) {
        double totalPercentage = splits.stream()
                .mapToDouble(split -> (split.getAmountOwe() / totalAmount) * 100)
                .sum();
        
        if (Math.abs(totalPercentage - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                    "Total percentage " + totalPercentage + " doesn't equal 100%"
            );
        }
    }

    @Override
    public List<Split> validateAndGetSplits(List<User> users, List<Double> splitAmounts, Double totalAmount) {
        if (users.size() != splitAmounts.size()) {
            throw new IllegalArgumentException(
                    "Number of users (" + users.size() + ") must match number of percentages (" + splitAmounts.size() + ")"
            );
        }
        
        double totalPercentage = splitAmounts.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalPercentage - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                    "Total percentage " + totalPercentage + " must equal 100%"
            );
        }
        
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            double amountOwe = (splitAmounts.get(i) / 100.0) * totalAmount;
            splits.add(new Split(users.get(i), amountOwe));
        }
        
        return splits;
    }
}

