package com.fsk.transaction.basics.service;

import com.fsk.transaction.basics.entity.Employee;
import com.fsk.transaction.basics.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Temel Transaction Konuları
 * 
 * 8. Read-only transaction ne işe yarar?
 * Hibernate flush kapatılır, dirty checking devre dışı, performans artar
 * 
 * 9. Transaction ne zaman commit edilir?
 * Method return edince (proxy dışına çıkınca)
 * 
 * 10. Transaction timeout ne işe yarar?
 * Long-running transaction'lar DB lock'ları uzun süre tutar, deadlock riski
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * Read-only transaction
     * Hibernate flush kapatılır, dirty checking devre dışı
     * Performans artar, yanlış write'ları önler
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        log.info("getAllEmployees - Read-only transaction");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("Is read-only: {}", TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        
        return employeeRepository.findAll();
    }
    
    /**
     * Normal transactional write işlemi
     * Transaction ne zaman commit edilir?
     * Method return edince (proxy dışına çıkınca)
     */
    @Transactional
    public Employee createEmployee(String name, String email, String department, Double salary) {
        log.info("createEmployee - Transaction başladı");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Method return edince transaction commit edilir (proxy dışına çıkınca)
        log.info("Method return ediyor - Transaction commit edilecek");
        return saved;
    }
    
    /**
     * Transaction timeout
     * Long-running transaction'ları önlemek için
     */
    @Transactional(timeout = 5) // 5 saniye timeout
    public Employee createEmployeeWithTimeout(String name, String email, String department, Double salary) {
        log.info("createEmployeeWithTimeout - 5 saniye timeout ile");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        
        Employee saved = employeeRepository.save(employee);
        
        // Simüle edilmiş uzun işlem - timeout'u test etmek için
        try {
            Thread.sleep(6000); // 6 saniye bekle - timeout olacak
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return saved;
    }
    
    /**
     * Exception ile rollback testi
     * Method return etmeden exception fırlatılırsa rollback olur
     */
    @Transactional
    public Employee createEmployeeWithException(String name, String email, String department, Double salary) {
        log.info("createEmployeeWithException - Exception ile rollback testi");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Exception fırlatılıyor - rollback olacak
        throw new RuntimeException("Exception - Transaction rollback olacak!");
    }
}


