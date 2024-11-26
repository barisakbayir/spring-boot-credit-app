package com.bank.credit.model;

import lombok.Data;

@Data
public class PayLoanResponse {
    private int numberOfPaidInstallments;
    private Double paidAmount;
    private boolean loanCompleted;
}
