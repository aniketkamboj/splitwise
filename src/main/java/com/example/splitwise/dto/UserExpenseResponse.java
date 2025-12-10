package com.example.splitwise.dto;

import com.example.splitwise.enums.ExpenseSplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserExpenseResponse {
    private String expenseId;
    private String description;
    private Double expenseAmount;
    private List<PaymentDetail> payments;
    private ExpenseSplitType splitType;
    private List<SplitDetail> splits;
    private String groupId;
    private LocalDate date;
    
    // User's share in this expense
    // Positive value = user gets back this amount
    // Negative value = user owes this amount
    private Double userShare;
}
