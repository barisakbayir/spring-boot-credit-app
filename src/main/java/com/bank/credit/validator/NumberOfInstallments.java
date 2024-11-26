package com.bank.credit.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = { NumberOfInstallmentsValidator.class })
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberOfInstallments {

    int[] values();

    String message() default "Number of Installments must be 6,9,12 or 24";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
