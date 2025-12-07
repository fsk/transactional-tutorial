package com.fsk.transaction.patterns.service;

import com.fsk.transaction.patterns.entity.Employee;
import com.fsk.transaction.patterns.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 26. Transaction propagation zinciri
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PropagationChainService {
    
    private final EmployeeRepository employeeRepository;
    private final PropagationChainServiceB serviceB;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void methodA(String name, String email) {
        log.info("methodA - REQUIRED propagation");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
        log.info("Employee A kaydedildi: {}", employee.getId());
        
        serviceB.methodB(name + " - B", email + ".b");
        
        throw new RuntimeException("A rollback olacak");
    }
}

@Service
@RequiredArgsConstructor
@Slf4j
class PropagationChainServiceB {
    
    private final EmployeeRepository employeeRepository;
    private final PropagationChainServiceC serviceC;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void methodB(String name, String email) {
        log.info("methodB - REQUIRES_NEW propagation");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(6000.0);
        
        employeeRepository.save(employee);
        log.info("Employee B kaydedildi: {}", employee.getId());
        
        serviceC.methodC(name + " - C", email + ".c");
    }
}

@Service
@RequiredArgsConstructor
@Slf4j
class PropagationChainServiceC {
    
    private final EmployeeRepository employeeRepository;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void methodC(String name, String email) {
        log.info("methodC - REQUIRED propagation (B'nin transaction'ına katılır)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(7000.0);
        
        employeeRepository.save(employee);
        log.info("Employee C kaydedildi: {}", employee.getId());
    }
}



