package com.example.splitwise.dto;

import com.example.splitwise.enums.ExpenseSplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateExpenseRequest {
    
    private String expenseId; // Optional, will be generated if not provided
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Expense amount is required")
    @Positive(message = "Expense amount must be positive")
    private Double expenseAmount;
    
    @NotNull(message = "Payments are required")
    private List<PaymentDetail> payments; // List of users who paid and their amounts
    
    @NotNull(message = "Split type is required")
    private ExpenseSplitType splitType;
    
    private List<SplitDetail> splits; // Required for UNEQUAL, PERCENTAGE, EXACT splits
    
    private String groupId; // Optional, if expense is part of a group

}

