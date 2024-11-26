package com.bank.credit.controller;

import com.bank.credit.entity.LoanInstallment;
import com.bank.credit.model.CreateLoanRequest;
import com.bank.credit.model.PayLoanRequest;
import com.bank.credit.model.PayLoanResponse;
import com.bank.credit.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<String> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        loanService.createLoan(request);
        return ResponseEntity.ok("Loan has ben created successfully");
    }

    @GetMapping("/{loanId}/loan-installments")
    public ResponseEntity<Page<LoanInstallment>> getLoanInstallmentsByLoanId(@PathVariable Long loanId,
                                                                             @RequestParam(required = false, name = "is_paid") Boolean paid,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("dueDate").ascending());
        Page<LoanInstallment> loanInstallments = loanService.getLoanInstallments(loanId, pageable, paid);
        return ResponseEntity.ok(loanInstallments);
    }

    @PostMapping("/{id}/pay-loan")
    public ResponseEntity<PayLoanResponse> payLoan(@PathVariable Long id, @Valid @RequestBody PayLoanRequest request) {
        PayLoanResponse response = loanService.payLoan(id, request);
        return ResponseEntity.ok(response);
    }
}
