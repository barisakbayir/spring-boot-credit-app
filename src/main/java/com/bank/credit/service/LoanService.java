package com.bank.credit.service;

import com.bank.credit.entity.*;
import com.bank.credit.exception.CustomerNotFoundException;
import com.bank.credit.exception.LoanNotFoundException;
import com.bank.credit.model.CreateLoanRequest;
import com.bank.credit.model.PayLoanRequest;
import com.bank.credit.model.PayLoanResponse;
import com.bank.credit.repository.CustomerRepository;
import com.bank.credit.repository.LoanInstallmentRepository;
import com.bank.credit.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private LoanInstallmentRepository loanInstallmentRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private final static int MAX_NUMBER_OF_INSTALLMENT_PAYMENT = 3;

    public Page<LoanInstallment> getLoanInstallments(Long loanId, Pageable pageable, Boolean paid){
        loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + loanId));
        Specification<LoanInstallment> specification = Specification.where(LoanInstallmentEntitySpesification.hasLoanId(loanId))
                .and(LoanInstallmentEntitySpesification.isPaid(paid));
        return loanInstallmentRepository.findAll(specification, pageable);
    }

    public void createLoan(CreateLoanRequest request) {
        Customer customer = getCustomer(request.getCustomerId());
        BigDecimal remainingCustomerLimit = customer.getCreditLimit().subtract(customer.getUsedCreditLimit());
        BigDecimal loanAmount = BigDecimal.valueOf(request.getAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal interestRate = BigDecimal.valueOf(request.getInterestRate()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal loanAmountWithInterest = loanAmount.multiply(BigDecimal.ONE.add(interestRate)).setScale(2, RoundingMode.HALF_UP);
        if (loanAmountWithInterest.compareTo(remainingCustomerLimit) > 0) {
            throw new IllegalArgumentException("Customer credit limit exceeded!");
        }

        LocalDateTime now = LocalDateTime.now();
        Loan loan = prepareLoan(loanAmount, loanAmountWithInterest, interestRate, request.getNumberOfInstallments(), now, customer);
        List<LoanInstallment> loanInstallments = prepareLoanInstallments(now.toLocalDate(), request.getNumberOfInstallments(), loan);
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(loanAmountWithInterest));
        saveCreateLoanEntities(loan, loanInstallments, customer);
    }

    private Customer getCustomer(Long customerId){
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }

    private Loan prepareLoan(BigDecimal loanAmount, BigDecimal loanAmountWithInterest, BigDecimal interestRate,
                                  Integer numberOfInstallments, LocalDateTime now, Customer customer){
        BigDecimal installmentAmount = loanAmountWithInterest.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
        Loan loan = new Loan();
        loan.setInstallmentAmountWithInterest(installmentAmount);
        loan.setInterestRate(interestRate);
        loan.setLoanAmount(loanAmount);
        loan.setLoanAmountWithInterest(loanAmountWithInterest);
        loan.setNumberOfInstallments(numberOfInstallments);
        loan.setCreateDate(now);
        loan.setCustomer(customer);
        return loan;
    }

    private List<LoanInstallment> prepareLoanInstallments(LocalDate now, int numberOfInstallments, Loan loan){
        List<LoanInstallment> loanInstallments = new ArrayList<>();
        List<LocalDate> firstDays = getFirstDaysOfNextMonths(now, numberOfInstallments);
        BigDecimal totalInstallmentsAmount = BigDecimal.ZERO;
        for (int i = 0; i < numberOfInstallments; i++) {
            LoanInstallment loanInstallment = new LoanInstallment();
            loanInstallment.setLoan(loan);
            loanInstallment.setAmount(loan.getInstallmentAmountWithInterest());
            loanInstallment.setDueDate(firstDays.get(i));
            loanInstallments.add(loanInstallment);
            totalInstallmentsAmount = totalInstallmentsAmount.add(loan.getInstallmentAmountWithInterest());
        }
        adjustLastInstallmentIfNecessary(totalInstallmentsAmount, loan.getLoanAmountWithInterest(), loanInstallments);
        return loanInstallments;
    }

    private void adjustLastInstallmentIfNecessary(BigDecimal totalInstallmentsAmount, BigDecimal loanAmountWithInterest, List<LoanInstallment> loanInstallments) {
        if (totalInstallmentsAmount.compareTo(loanAmountWithInterest) != 0) {
            LoanInstallment loanInstallment = loanInstallments.get(loanInstallments.size() - 1);
            if (totalInstallmentsAmount.compareTo(loanAmountWithInterest) > 0) {
                BigDecimal diffAmount = totalInstallmentsAmount.subtract(loanAmountWithInterest);
                loanInstallment.setAmount(loanInstallment.getAmount().subtract(diffAmount));
            } else {
                BigDecimal diffAmount = loanAmountWithInterest.subtract(totalInstallmentsAmount);
                loanInstallment.setAmount(loanInstallment.getAmount().add(diffAmount));
            }
        }
    }

    private void saveCreateLoanEntities(Loan loan, List<LoanInstallment> loanInstallments, Customer customer) {
        loanRepository.save(loan);
        loanInstallmentRepository.saveAll(loanInstallments);
        customerRepository.save(customer);
    }

    private static List<LocalDate> getFirstDaysOfNextMonths(LocalDate startDate, int numberOfMonths) {
        List<LocalDate> firstDays = new ArrayList<>();
        LocalDate nextMonth = startDate.plusMonths(1).withDayOfMonth(1);

        for (int i = 0; i < numberOfMonths; i++) {
            firstDays.add(nextMonth);
            nextMonth = nextMonth.plusMonths(1);
        }

        return firstDays;
    }

    public PayLoanResponse payLoan(Long loanId, PayLoanRequest request) {
        Loan loan = getLoan(loanId);
        BigDecimal paymentAmount = BigDecimal.valueOf(request.getAmount()).setScale(2, RoundingMode.HALF_UP);
        PayLoanResponse response = new PayLoanResponse();
        if (loan.isPaid()) {
            throw new IllegalArgumentException("Loan has already been paid");
        }
        if (loan.getInstallmentAmountWithInterest().compareTo(paymentAmount) > 0) {
            throw new IllegalArgumentException("Payment amount is not enough to pay an installment");
        }
        LocalDate now = LocalDate.now();
        List<LoanInstallment> payableInstallments = getPayableInstallments(now, loan);
        if (payableInstallments.isEmpty()) {
            throw new IllegalArgumentException("There are no unpaid installments in the next 3 calendar months for the loan with ID: " + loanId);
        }

        List<LoanInstallment> paidInstallments = preparePaidInstallments(paymentAmount, loan.getInstallmentAmountWithInterest(), payableInstallments, response, now);
        if (loan.getLoanInstallments().stream().allMatch(LoanInstallment::isPaid)){
            loan.setPaid(true);
            response.setLoanCompleted(true);
        }
        BigDecimal totalPaidAmount = paidInstallments.stream().map(LoanInstallment::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Customer customer = loan.getCustomer();
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(totalPaidAmount));
        savePayLoanEntities(paidInstallments, loan, customer);
        response.setPaidAmount(totalPaidAmount.doubleValue());
        return response;
    }

    private Loan getLoan(Long loanId){
        return loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException("Loan not found with ID: " + loanId));
    }

    private List<LoanInstallment> getPayableInstallments(LocalDate now, Loan loan) {
        LocalDate secondDayOfThirdMonth = now.plusMonths(3).withDayOfMonth(2);
        return loan.getLoanInstallments().stream().filter(l -> !l.isPaid() && l.getDueDate().isBefore(secondDayOfThirdMonth))
                .sorted(Comparator.comparing(LoanInstallment::getDueDate)).toList();
    }

    private List<LoanInstallment> preparePaidInstallments(BigDecimal paymentAmount, BigDecimal installmentAmountWithInterest,
                                                          List<LoanInstallment> payableInstallments, PayLoanResponse response, LocalDate now){
        int i = 0;
        List<LoanInstallment> paidInstallments = new ArrayList<>();
        while(paymentAmount.compareTo(installmentAmountWithInterest) > 0 && payableInstallments.size() > i) {
            LoanInstallment paidInstallment = payableInstallments.get(i);
            paidInstallment.setPaid(true);
            paidInstallment.setPaymentDate(now);
            paidInstallment.setPaidAmount(installmentAmountWithInterest);
            paidInstallments.add(paidInstallment);
            paymentAmount = paymentAmount.subtract(installmentAmountWithInterest);
            i++;
        }
        response.setNumberOfPaidInstallments(i);
        return paidInstallments;
    }

    private void savePayLoanEntities(List<LoanInstallment> paidInstallments, Loan loan, Customer customer){
        loanInstallmentRepository.saveAll(paidInstallments);
        loanRepository.save(loan);
        customerRepository.save(customer);
    }
}
