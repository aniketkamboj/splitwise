package com.example.splitwise.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column
    private String name;

    @Column(unique = true)
    private String email;

    @Column
    private String mobileNumber;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Split> splits;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserExpenseBalanceSheet userExpenseBalanceSheet;

    public UserExpenseBalanceSheet getUserExpenseBalanceSheet() {
        if (userExpenseBalanceSheet == null) {
            userExpenseBalanceSheet = new UserExpenseBalanceSheet();
            userExpenseBalanceSheet.setUser(this);
        }
        return userExpenseBalanceSheet;
    }
}

