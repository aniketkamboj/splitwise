package com.example.splitwise.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "user_expense_balance_sheets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExpenseBalanceSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column
    private Double totalPayment = 0.0;

    @Column
    private Double totalYouGetBack = 0.0;

    @Column
    private Double totalYouOwe = 0.0;

    @OneToMany(mappedBy = "balanceSheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Balance> balances;

    @Transient
    private Map<String, Balance> userVsBalance;


    public Map<String, Balance> getUserVsBalance() {
        if (userVsBalance == null) {
            userVsBalance = new HashMap<>();
            if (balances != null) {
                for (Balance balance : balances) {
                    userVsBalance.put(balance.getUserId(), balance);
                }
            }
        }
        return userVsBalance;
    }

    public void addBalance(Balance balance) {
        if (balances == null) {
            balances = new ArrayList<>();
        }
        balances.add(balance);
        getUserVsBalance().put(balance.getUserId(), balance);
    }
}

