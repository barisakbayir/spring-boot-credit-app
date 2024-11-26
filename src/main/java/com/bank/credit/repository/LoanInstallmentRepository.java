package com.bank.credit.repository;

import com.bank.credit.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long>, JpaSpecificationExecutor<LoanInstallment> {

}
