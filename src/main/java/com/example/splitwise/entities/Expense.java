package com.example.splitwise.entities;

import com.example.splitwise.enums.ExpenseSplitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String expenseId;

    @Column 
    private Double expenseAmount;

    @Column 
    private String description;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpensePayment> payments;

    @Enumerated(EnumType.STRING)
    @Column 
    private ExpenseSplitType splitType;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Split> splits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column 
    private LocalDate date;

    public Expense(String expenseId, Double expenseAmount, String description, 
                   List<ExpensePayment> payments, ExpenseSplitType splitType, List<Split> splits) {
        this.expenseId = expenseId;
        this.expenseAmount = expenseAmount;
        this.description = description;
        this.payments = payments != null ? payments : new ArrayList<>();
        this.splitType = splitType;
        this.splits = splits != null ? splits : new ArrayList<>();
        this.date = LocalDate.now();
        
        // Set expense reference in payments
        for (ExpensePayment payment : this.payments) {
            payment.setExpense(this);
        }
        
        // Set expense reference in splits
        for (Split split : this.splits) {
            split.setExpense(this);
        }
    }
}

