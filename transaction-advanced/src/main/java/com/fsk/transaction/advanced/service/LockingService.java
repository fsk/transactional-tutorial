package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.entity.EmployeeWithVersion;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import com.fsk.transaction.advanced.repository.EmployeeWithVersionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 38. Optimistic locking transaction'ı nasıl etkiler?
 * 
 * @Version alanı hangi aşamada kontrol edilir?
 * ✅ Flush / commit sırasında
 * ✅ Exception ne zaman fırlatılır? → commit'e çok yakın
 * 
 * 39. Pessimistic lock transaction scope'u dışında kalır mı?
 * 
 * PESSIMISTIC_WRITE lock ne zaman bırakılır?
 * ✅ Transaction commit / rollback
 * ❌ method return ettiğinde değil
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LockingService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeWithVersionRepository employeeWithVersionRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Optimistic locking örneği
     */
    @Transactional
    public EmployeeWithVersion optimisticLockUpdate(Long id, Double newSalary) {
        log.info("optimisticLockUpdate - Optimistic locking");
        
        EmployeeWithVersion employee = employeeWithVersionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        log.info("Employee yüklendi - Version: {}", employee.getVersion());
        
        // Simüle edilmiş gecikme - başka bir transaction bu sırada değişiklik yapabilir
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        employee.setSalary(newSalary);
        
        try {
            // Flush / commit sırasında version kontrol edilir
            EmployeeWithVersion saved = employeeWithVersionRepository.save(employee);
            log.info("Employee güncellendi - Yeni version: {}", saved.getVersion());
            return saved;
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            log.error("Optimistic lock exception - Version conflict!");
            throw new RuntimeException("Optimistic lock failed - başka bir transaction değişiklik yapmış", e);
        }
    }
    
    /**
     * Pessimistic lock örneği
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Employee pessimisticLockUpdate(Long id, Double newSalary) {
        log.info("pessimisticLockUpdate - Pessimistic locking");
        
        // Pessimistic lock alınıyor
        Employee employee = entityManager.find(Employee.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (employee == null) {
            throw new RuntimeException("Employee bulunamadı");
        }
        
        log.info("Pessimistic lock alındı - Employee: {}", employee.getName());
        log.info("Lock transaction commit/rollback'e kadar tutulacak");
        
        // Simüle edilmiş uzun işlem
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        employee.setSalary(newSalary);
        Employee saved = employeeRepository.save(employee);
        
        log.info("Employee güncellendi - Lock commit/rollback'te bırakılacak");
        
        return saved;
    }
    
    /**
     * Serialization failure örneği
     * 
     * 40. Serialization failure nedir?
     * 
     * SERIALIZABLE isolation'da neden random rollback olur?
     * ✅ DB conflict detection
     * ✅ Phantom read prevention
     * ✅ Retry zorunluluğu
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Employee serializationFailureExample(Long id, Double newSalary) {
        log.info("serializationFailureExample - SERIALIZABLE isolation");
        log.info("SERIALIZABLE isolation - Conflict detection riski yüksek");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        
        try {
            Employee saved = employeeRepository.save(employee);
            log.info("Employee güncellendi");
            return saved;
        } catch (Exception e) {
            log.error("Serialization failure - Retry gerekli!");
            throw new RuntimeException("Serialization failure - Retry loop olmadan kullanılmamalı", e);
        }
    }
}


