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

    @Column(nullable = false)
    @Builder.Default
    private Double amountOwe = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double amountGetBack = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balance_sheet_id", nullable = false)
    private UserExpenseBalanceSheet balanceSheet;

    @Column(nullable = false)
    private String userId; // The other user ID this balance is for
}

