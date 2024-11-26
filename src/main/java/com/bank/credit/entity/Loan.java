package com.bank.credit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(nullable = false, scale = 2, precision = 10)
    private BigDecimal loanAmount;
    private BigDecimal loanAmountWithInterest;
    @Column(nullable = false, scale = 1, precision = 2)
    private BigDecimal interestRate;
    @Column(nullable = false)
    private int numberOfInstallments;
    @Column(nullable = false)
    private LocalDateTime createDate;
    private boolean isPaid;
    private BigDecimal installmentAmountWithInterest;
    @JsonIgnore
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanInstallment> loanInstallments;

}
