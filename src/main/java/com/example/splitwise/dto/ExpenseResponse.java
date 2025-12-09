package com.example.splitwise.dto;

import com.example.splitwise.enums.ExpenseSplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private String expenseId;
    private String description;
    private Double expenseAmount;
    private String paidByUserId;
    private ExpenseSplitType splitType;
    private List<SplitDetail> splits;
    private String groupId;
}

