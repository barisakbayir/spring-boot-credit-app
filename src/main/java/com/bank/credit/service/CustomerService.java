package com.bank.credit.service;

import com.bank.credit.entity.Loan;
import com.bank.credit.entity.LoanEntitySpesification;
import com.bank.credit.exception.CustomerNotFoundException;
import com.bank.credit.repository.CustomerRepository;
import com.bank.credit.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private LoanRepository loanRepository;

    public Page<Loan> getCustomerLoans(Long customerId, Pageable pageable, Boolean paid){
        customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        Specification<Loan> specification = Specification.where(LoanEntitySpesification.hasCustomerId(customerId)).and(LoanEntitySpesification.isPaid(paid));
        return loanRepository.findAll(specification, pageable);
    }
}
