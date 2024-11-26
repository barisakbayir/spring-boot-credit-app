package com.bank.credit.entity;

import org.springframework.data.jpa.domain.Specification;

public class LoanEntitySpesification {
    public static Specification<Loan> hasCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) ->
                customerId == null ? null : criteriaBuilder.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Loan> isPaid(Boolean paid) {
        return (root, query, criteriaBuilder) ->
                paid == null ? null : criteriaBuilder.equal(root.get("isPaid"), paid);
    }
}
