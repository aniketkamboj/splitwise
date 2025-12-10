package com.example.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutstandingBalanceResponse {
    private String userId;
    private Double totalOutstandingOwe;     // Total amount user owes to others
    private Double totalOutstandingReceive; // Total amount others owe to user
    private Double netOutstanding;          // Positive: to receive, Negative: to pay
}
