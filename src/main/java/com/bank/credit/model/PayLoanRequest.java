package com.bank.credit.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayLoanRequest {
    @NotNull(message = "Amount is required!")
    @DecimalMin(value = "0.00", inclusive = false, message = "Loan amount must be greater than 0")
    private Double amount;
}
