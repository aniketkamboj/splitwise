package com.example.splitwise.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column  
    @Builder.Default
    private Double amountOwe = 0.0;

    @Column  
    @Builder.Default
    private Double amountGetBack = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_sheet_id", nullable = false)
    private UserExpenseBalanceSheet balanceSheet;

    @Column  
    private String userId; // The other user ID this balance is for
}

