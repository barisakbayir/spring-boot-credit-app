package com.bank.credit.model;

import com.bank.credit.validator.NumberOfInstallments;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLoanRequest {
    @NotNull(message = "Customer id is required")
    private Long customerId;
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Loan amount must be greater than 0")
    private Double amount;
    @NotNull(message = "Loan interest rate is required!")
    @DecimalMin(value = "0.1", inclusive = true, message = "Loan interest rate must be between 0.5 and 1.0")
    @DecimalMax(value = "0.5", inclusive = true, message = "Loan interest rate must be between 0.5 and 1.0")
    private Double interestRate;
    @NotNull(message = "Number of installments is required")
    @NumberOfInstallments(values = {6,9,12,24})
    private Integer numberOfInstallments;
}
