package com.example.splitwise.entities;

import com.example.splitwise.enums.ExpenseSplitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private Double expenseAmount;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_user_id", nullable = false)
    private User paidBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseSplitType splitType;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Split> splits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public Expense(String expenseId, Double expenseAmount, String description, 
                   User paidBy, ExpenseSplitType splitType, List<Split> splits) {
        this.expenseId = expenseId;
        this.expenseAmount = expenseAmount;
        this.description = description;
        this.paidBy = paidBy;
        this.splitType = splitType;
        this.splits = splits != null ? splits : new ArrayList<>();
        
        // Set expense reference in splits
        for (Split split : this.splits) {
            split.setExpense(this);
        }
    }
}

