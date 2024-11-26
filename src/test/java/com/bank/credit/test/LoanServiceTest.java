package com.bank.credit.test;

import com.bank.credit.entity.Customer;
import com.bank.credit.entity.Loan;
import com.bank.credit.entity.LoanInstallment;
import com.bank.credit.exception.CustomerNotFoundException;
import com.bank.credit.exception.LoanNotFoundException;
import com.bank.credit.model.CreateLoanRequest;
import com.bank.credit.model.PayLoanRequest;
import com.bank.credit.model.PayLoanResponse;
import com.bank.credit.repository.CustomerRepository;
import com.bank.credit.repository.LoanInstallmentRepository;
import com.bank.credit.repository.LoanRepository;
import com.bank.credit.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private Customer customer;
    private CreateLoanRequest createLoanRequest;
    private PayLoanRequest payLoanRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);
        customer.setCreditLimit(new BigDecimal("5000.0000001"));
        customer.setUsedCreditLimit(new BigDecimal("1000.0000023"));

        createLoanRequest = new CreateLoanRequest();
        createLoanRequest.setCustomerId(1L);
        createLoanRequest.setAmount(2000.00123);
        createLoanRequest.setInterestRate(0.2);
        createLoanRequest.setNumberOfInstallments(12);

        loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setInstallmentAmountWithInterest(new BigDecimal("100"));
        loan.setPaid(false);

        LoanInstallment installment1 = new LoanInstallment();
        installment1.setPaid(false);
        installment1.setDueDate(LocalDate.now().minusMonths(2));
        installment1.setLoan(loan);
        installment1.setAmount(new BigDecimal("100"));

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setPaid(false);
        installment2.setDueDate(LocalDate.now().minusMonths(1));
        installment2.setLoan(loan);
        installment2.setAmount(new BigDecimal("100"));

        loan.setLoanInstallments(new ArrayList<>());
        loan.getLoanInstallments().add(installment1);
        loan.getLoanInstallments().add(installment2);

        payLoanRequest = new PayLoanRequest();
        payLoanRequest.setAmount(250.0);
    }

    @Test
    void testCreateLoan_CustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> loanService.createLoan(createLoanRequest));
    }

    @Test
    void testCreateLoan_CreditLimitExceeded() {
        customer.setCreditLimit(new BigDecimal("2500.00"));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        assertThrows(IllegalArgumentException.class, () -> loanService.createLoan(createLoanRequest));
    }

    @Test
    void testCreateLoan_LastInstallmentAdjustment() {
        customer.setCreditLimit(new BigDecimal("5000.00"));
        createLoanRequest.setAmount(2500.00);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        loanService.createLoan(createLoanRequest);

        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanInstallmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetFirstDaysOfNextMonthsWithReflection() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 1, 1);

        Method method = LoanService.class.getDeclaredMethod("getFirstDaysOfNextMonths", LocalDate.class, int.class);
        method.setAccessible(true);

        List<LocalDate> result = (List<LocalDate>) method.invoke(loanService, startDate, 3);
        assertEquals(LocalDate.of(2025, 2, 1), result.get(0));
        assertEquals(LocalDate.of(2025, 3, 1), result.get(1));
        assertEquals(LocalDate.of(2025, 4, 1), result.get(2));
    }

    @Test
    void testCreateLoan_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        loanService.createLoan(createLoanRequest);
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(customer);
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanInstallmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testPayLoan_AlreadyPaidLoan() {
        loan.setPaid(true);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> loanService.payLoan(loan.getId(), payLoanRequest));
        assertEquals("Loan has already been paid", exception.getMessage());
    }

    @Test
    void testPayLoan_InsufficientPayment() {
        payLoanRequest.setAmount(50.0);
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> loanService.payLoan(loan.getId(), payLoanRequest));
        assertEquals("Payment amount is not enough to pay an installment", exception.getMessage());
    }

    @Test
    void testPayLoan_NoPayableInstallments() {
        loan.getLoanInstallments().clear();
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> loanService.payLoan(loan.getId(), payLoanRequest));
        assertEquals("There are no unpaid installments in the next 3 calendar months for the loan with ID: 1", exception.getMessage());
    }

    @Test
    void testPayLoan_LoanNotFound() {
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.empty());
        Exception exception = assertThrows(LoanNotFoundException.class, () -> loanService.payLoan(loan.getId(), payLoanRequest));
        assertEquals("Loan not found with ID: 1", exception.getMessage());
    }

    @Test
    void testPayLoan_Success() {
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        PayLoanResponse response = loanService.payLoan(loan.getId(), payLoanRequest);
        assertNotNull(response);
        assertTrue(response.isLoanCompleted());
        assertEquals(200.00, response.getPaidAmount());
        assertEquals(2, response.getNumberOfPaidInstallments());
        verify(loanInstallmentRepository, times(1)).saveAll(anyList());
        verify(loanRepository, times(1)).save(loan);
        verify(customerRepository, times(1)).save(customer);
    }
}
