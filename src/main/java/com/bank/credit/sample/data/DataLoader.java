package com.bank.credit.sample.data;

import com.bank.credit.entity.Customer;
import com.bank.credit.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    public DataLoader(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        Customer customer1 = new Customer();
        customer1.setName("John");
        customer1.setSurname("Doe");
        customer1.setCreditLimit(BigDecimal.valueOf(5000.0));
        customer1.setUsedCreditLimit(BigDecimal.ZERO);

        Customer customer2 = new Customer();
        customer2.setName("Jack");
        customer2.setSurname("Smith");
        customer2.setCreditLimit(BigDecimal.valueOf(10_000.0));
        customer2.setUsedCreditLimit(BigDecimal.ZERO);

        Customer customer3 = new Customer();
        customer3.setName("Jennifer");
        customer3.setSurname("Smith");
        customer3.setCreditLimit(BigDecimal.valueOf(13_000.0));
        customer3.setUsedCreditLimit(BigDecimal.ZERO);

        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);
    }
}
