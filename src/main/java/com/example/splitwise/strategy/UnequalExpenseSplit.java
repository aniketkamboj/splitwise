package com.example.splitwise.strategy;

import com.example.splitwise.dto.SplitDetail;
import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnequalExpenseSplit implements ExpenseSplit {

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
    public List<Split> validateAndGetSplits(List<User> users, Double totalAmount,List<SplitDetail> splitDetails) {
        if (users.size() != splitDetails.size()) {
            throw new IllegalArgumentException(
                    "Number of users (" + users.size() + ") must match number of split amounts (" + splitDetails.size() + ")"
            );
        }

        double totalSplitAmount = splitDetails.stream()
                .mapToDouble(SplitDetail::getAmount)
                .sum();

        if (Math.abs(totalSplitAmount - totalAmount) > 0.01) {
            throw new IllegalArgumentException(
                    "Total split amount " + totalSplitAmount + " doesn't match expense amount " + totalAmount
            );
        }
        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            splits.add(new Split(users.get(i), splitDetails.get(i).getAmount()));
        }

        return splits;
    }
}

