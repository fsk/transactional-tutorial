package com.fsk.transaction.patterns.service;

import com.fsk.transaction.patterns.entity.Employee;
import com.fsk.transaction.patterns.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/**
 * Transaction Pattern'leri
 * 
 * 16. @Transactional + private method
 * 17. Final class / final method meselesi
 * 22. Nested transaction
 * 29. Read-only transaction içinde write olursa?
 * 30. Transaction + Stream API tuzağı
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionPatternsService {
    
    private final EmployeeRepository employeeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 16. @Transactional + private method
     */
    public void callPrivateMethod() {
        log.info("callPrivateMethod - Public method");
        privateTransactionalMethod();
    }
    
    @Transactional
    private void privateTransactionalMethod() {
        log.info("privateTransactionalMethod - @Transactional çalışmaz (private method)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
    }
    
    /**
     * 17. Final method
     */
    @Transactional
    public final void finalMethod() {
        log.info("finalMethod - @Transactional çalışmayabilir (final method)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
    }
    
    /**
     * 22. Nested transaction
     */
    @Transactional
    public void nestedTransactionExample() {
        log.info("nestedTransactionExample - NESTED propagation testi");
        
        Employee employee = new Employee();
        employee.setName("Nested Test");
        employee.setEmail("nested@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", employee.getId());
        
        try {
            nestedInnerMethod();
        } catch (Exception e) {
            log.error("Nested transaction hatası: {}", e.getMessage());
        }
    }
    
    @org.springframework.transaction.annotation.Transactional(propagation = Propagation.NESTED)
    public void nestedInnerMethod() {
        log.info("nestedInnerMethod - NESTED propagation");
        Employee employee = new Employee();
        employee.setName("Nested Inner");
        employee.setEmail("nested-inner@example.com");
        employee.setDepartment("IT");
        employee.setSalary(6000.0);
        
        employeeRepository.save(employee);
        throw new RuntimeException("Nested transaction rollback");
    }
    
    /**
     * 29. Read-only transaction içinde write
     */
    @Transactional(readOnly = true)
    public void readOnlyWithWrite() {
        log.info("readOnlyWithWrite - Read-only transaction içinde write");
        log.info("Is read-only: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        
        Employee employee = new Employee();
        employee.setName("Read Only Test");
        employee.setEmail("readonly@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
        log.info("Read-only transaction içinde save çağrıldı - Flush edilmeyecek");
    }
    
    /**
     * 30. Stream API tuzağı
     */
    @Transactional
    public List<Employee> streamApiTrap() {
        log.info("streamApiTrap - Stream API transaction tuzağı");
        
        Stream<Employee> employeeStream = employeeRepository.findAll().stream();
        
        return employeeStream
            .filter(e -> e.getSalary() > 1000)
            .toList();
    }
    
    /**
     * Stream API çözümü
     */
    @Transactional
    public List<Employee> streamApiSolution() {
        log.info("streamApiSolution - Stream API çözümü");
        
        List<Employee> employees = employeeRepository.findAll();
        
        return employees.stream()
            .filter(e -> e.getSalary() > 1000)
            .toList();
    }
}


