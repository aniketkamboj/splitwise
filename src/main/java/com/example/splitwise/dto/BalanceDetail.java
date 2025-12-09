package com.example.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceDetail {
    private Double amountOwe;
    private Double amountGetBack;
}

