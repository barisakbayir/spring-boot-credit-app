package com.bank.credit.test;

import com.bank.credit.exception.CustomerNotFoundException;
import com.bank.credit.repository.CustomerRepository;
import com.bank.credit.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCustomerLoansWhenCustomerNotFound() {
        when(customerRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerLoans(1L, null, null);
        });
        assertEquals("Customer not found with id: 1", exception.getMessage());
    }
}
