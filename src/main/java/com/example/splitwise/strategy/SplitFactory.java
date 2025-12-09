package com.example.splitwise.strategy;

import com.example.splitwise.enums.ExpenseSplitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SplitFactory {

    private final Map<ExpenseSplitType, ExpenseSplit> splitStrategies;

    @Autowired
    public SplitFactory(EqualExpenseSplit equalExpenseSplit,
                       UnequalExpenseSplit unequalExpenseSplit,
                       PercentageExpenseSplit percentageExpenseSplit,
                       ExactExpenseSplit exactExpenseSplit) {
        this.splitStrategies = new HashMap<>();
        this.splitStrategies.put(ExpenseSplitType.EQUAL, equalExpenseSplit);
        this.splitStrategies.put(ExpenseSplitType.UNEQUAL, unequalExpenseSplit);
        this.splitStrategies.put(ExpenseSplitType.PERCENTAGE, percentageExpenseSplit);
        this.splitStrategies.put(ExpenseSplitType.EXACT, exactExpenseSplit);
    }

    public ExpenseSplit getSplitObject(ExpenseSplitType splitType) {
        ExpenseSplit splitStrategy = splitStrategies.get(splitType);
        if (splitStrategy == null) {
            throw new IllegalArgumentException("Invalid split type: " + splitType);
        }
        return splitStrategy;
    }
}

