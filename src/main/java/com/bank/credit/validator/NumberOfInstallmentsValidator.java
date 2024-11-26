package com.bank.credit.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class NumberOfInstallmentsValidator implements ConstraintValidator<NumberOfInstallments, Integer> {
    private int[] allowedNumberOfInstallments;

    @Override
    public void initialize(NumberOfInstallments constraintAnnotation) {
        this.allowedNumberOfInstallments = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(allowedNumberOfInstallments).anyMatch(i -> i == value);
    }
}
