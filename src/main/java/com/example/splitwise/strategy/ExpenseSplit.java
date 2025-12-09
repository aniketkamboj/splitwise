package com.example.splitwise.strategy;

import com.example.splitwise.entities.Split;
import com.example.splitwise.entities.User;

import java.util.List;

public interface ExpenseSplit {
    void validateSplitRequest(List<Split> splits, Double totalAmount);
    List<Split> validateAndGetSplits(List<User> users, List<Double> splitAmounts, Double totalAmount);
}

