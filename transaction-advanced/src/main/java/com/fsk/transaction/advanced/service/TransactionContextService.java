package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CompletableFuture;

/**
 * 33. Transaction context propagation nasıl taşınır?
 * 
 * Spring transaction'ı nasıl "taşır"?
 * ✅ ThreadLocal üzerinden
 * ✅ TransactionSynchronizationManager
 * ✅ Thread değişirse → context kaybolur
 * 
 * Bu yüzden:
 * - @Async
 * - CompletableFuture
 * - Reactor
 * hepsi transaction düşmanıdır (varsayılan hâliyle)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionContextService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Transaction context ThreadLocal'da tutulur
     */
    @Transactional
    public void demonstrateThreadLocal() {
        log.info("demonstrateThreadLocal - Transaction context ThreadLocal'da");
        log.info("Current thread: {}", Thread.currentThread().getName());
        log.info("Has active transaction: {}", 
            TransactionSynchronizationManager.isActualTransactionActive());
        log.info("Current transaction name: {}", 
            TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Transaction isolation level: {}", 
            TransactionSynchronizationManager.getCurrentIsolationLevel());
        
        Employee employee = new Employee();
        employee.setName("ThreadLocal Test");
        employee.setEmail("threadlocal@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
    }
    
    /**
     * PROBLEM: @Async ile thread değişir, transaction context kaybolur
     */
    @Transactional
    @Async
    public CompletableFuture<Employee> asyncMethodProblem() {
        log.info("asyncMethodProblem - Farklı thread'de çalışıyor");
        log.info("Current thread: {}", Thread.currentThread().getName());
        log.info("Has active transaction: {}", 
            TransactionSynchronizationManager.isActualTransactionActive());
        
        // Transaction context kayboldu!
        Employee employee = new Employee();
        employee.setName("Async Problem");
        employee.setEmail("async-problem@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        // Bu işlem transaction dışında çalışır
        Employee saved = employeeRepository.save(employee);
        
        return CompletableFuture.completedFuture(saved);
    }
    
    /**
     * ÇÖZÜM: Async method kendi transaction'ına sahip olmalı
     */
    @Async
    @Transactional
    public CompletableFuture<Employee> asyncMethodSolution() {
        log.info("asyncMethodSolution - Kendi transaction'ı var");
        log.info("Current thread: {}", Thread.currentThread().getName());
        log.info("Has active transaction: {}", 
            TransactionSynchronizationManager.isActualTransactionActive());
        
        // Kendi transaction'ı içinde çalışır
        Employee employee = new Employee();
        employee.setName("Async Solution");
        employee.setEmail("async-solution@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        Employee saved = employeeRepository.save(employee);
        
        return CompletableFuture.completedFuture(saved);
    }
    
    /**
     * CompletableFuture ile transaction context kaybolur
     */
    @Transactional
    public CompletableFuture<Employee> completableFutureProblem() {
        log.info("completableFutureProblem - CompletableFuture transaction düşmanı");
        
        return CompletableFuture.supplyAsync(() -> {
            log.info("CompletableFuture thread: {}", Thread.currentThread().getName());
            log.info("Has active transaction: {}", 
                TransactionSynchronizationManager.isActualTransactionActive());
            
            // Transaction context kayboldu!
            Employee employee = new Employee();
            employee.setName("CompletableFuture Problem");
            employee.setEmail("cf-problem@example.com");
            employee.setDepartment("IT");
            employee.setSalary(5000.0);
            
            // Transaction dışında çalışır
            return employeeRepository.save(employee);
        });
    }
}


