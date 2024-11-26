package com.bank.credit.entity;

import org.springframework.data.jpa.domain.Specification;

public class LoanInstallmentEntitySpesification {
    public static Specification<LoanInstallment> hasLoanId(Long loanId) {
        return (root, query, criteriaBuilder) ->
                loanId == null ? null : criteriaBuilder.equal(root.get("loan").get("id"), loanId);
    }

    public static Specification<LoanInstallment> isPaid(Boolean paid) {
        return (root, query, criteriaBuilder) ->
                paid == null ? null : criteriaBuilder.equal(root.get("isPaid"), paid);
    }
}
