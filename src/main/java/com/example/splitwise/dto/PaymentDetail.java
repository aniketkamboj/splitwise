package com.example.splitwise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetail {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Amount paid is required")
    @PositiveOrZero(message = "Amount paid must be positive or zero")
    private Double amountPaid;
}

