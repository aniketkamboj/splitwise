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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateExpenseRequest {
    
    private String expenseId; // Optional, will be generated if not provided
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Expense amount is required")
    @Positive(message = "Expense amount must be positive")
    private Double expenseAmount;
    
    @NotBlank(message = "Paid by user ID is required")
    private String paidByUserId;
    
    @NotNull(message = "Split type is required")
    private ExpenseSplitType splitType;
    
    private List<SplitDetail> splits; // Required for UNEQUAL, PERCENTAGE, EXACT splits
    
    private List<String> userIds; // Required for EQUAL split
    
    private String groupId; // Optional, if expense is part of a group
}

