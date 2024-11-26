package com.bank.credit.controller;

import com.bank.credit.entity.Loan;
import com.bank.credit.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    private final static int MAX_SIZE = 50;

    @GetMapping("/{customerId}/loans")
    public ResponseEntity<Page<Loan>> getCustomerLoans(@PathVariable Long customerId, @RequestParam(required = false, name = "is_paid") Boolean paid,
                                                       @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10", name = "page_size") int pageSize) {
        if (pageSize > MAX_SIZE) {
            pageSize = MAX_SIZE;
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createDate").ascending());
        Page<Loan> loans = customerService.getCustomerLoans(customerId, pageable, paid);
        return ResponseEntity.ok(loans);
    }

}
