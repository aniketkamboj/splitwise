package com.example.splitwise.strategy;

import com.example.splitwise.dto.SplitDetail;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Split> validateAndGetSplits(List<User> users, Double totalAmount,List<SplitDetail> splitDetails) {
        if (users.size() != splitDetails.size()) {
            throw new IllegalArgumentException(
                    "Number of users (" + users.size() + ") must match number of percentages (" + splitDetails.size() + ")"
            );
        }
        List<Double> percentages = splitDetails.stream()
                .map(SplitDetail::getAmount)
                .toList();

        double totalPercentage = percentages.stream().mapToDouble(Double::doubleValue).sum();

        if (Math.abs(totalPercentage - 100.0) > 0.01) {
            throw new IllegalArgumentException(
                    "Total percentage " + totalPercentage + " must equal 100%"
            );
        }

        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            double amountOwe = (percentages.get(i) / 100.0) * totalAmount;
            splits.add(new Split(users.get(i), amountOwe));
        }

        return splits;
    }
}

