package com.example.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSheetResponse {
    private String userId;
    private Double totalPayment;
    private Double totalYouGetBack;
    private Double totalYouOwe;
    private Map<String, BalanceDetail> userVsBalance;
}

