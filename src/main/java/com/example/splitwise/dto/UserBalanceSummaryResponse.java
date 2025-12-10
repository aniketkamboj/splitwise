package com.example.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalanceSummaryResponse {
    private String userId;
    private String userName;
    private Double netBalance; // Positive: gets back, Negative: owes
}
