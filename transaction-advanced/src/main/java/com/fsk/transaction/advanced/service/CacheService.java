package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 24. Transaction + Cache tutarsızlığı
 * 
 * Transaction rollback oldu ama cache güncellendi. Neden?
 * Cache transaction-aware değilse hemen set edilir
 * 
 * Çözüm: AFTER_COMMIT event veya Tx-aware cache
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    // Basit in-memory cache (transaction-aware değil)
    private final Map<Long, Employee> cache = new HashMap<>();
    
    /**
     * PROBLEM: Cache transaction içinde güncellenirse
     * Transaction rollback olsa bile cache güncellenmiş kalır
     */
    @Transactional
    public Employee updateEmployeeWithCacheProblem(Long id, Double newSalary) {
        log.info("updateEmployeeWithCacheProblem - Cache transaction içinde (PROBLEM)");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        Employee saved = employeeRepository.save(employee);
        
        // Cache güncelleniyor - Transaction içinde (YANLIŞ!)
        cache.put(id, saved);
        log.info("Cache güncellendi: {}", id);
        
        // Exception fırlatılıyor - DB rollback olacak
        // Ama cache güncellenmiş kalır!
        throw new RuntimeException("DB rollback olacak ama cache güncellenmiş kalır!");
    }
    
    /**
     * ÇÖZÜM 1: AFTER_COMMIT event kullan
     * Cache sadece transaction commit olduktan sonra güncellenir
     */
    @Transactional
    public Employee updateEmployeeWithCacheSolution(Long id, Double newSalary) {
        log.info("updateEmployeeWithCacheSolution - AFTER_COMMIT event ile");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        Employee saved = employeeRepository.save(employee);
        
        // Event yayınla - AFTER_COMMIT'te cache güncellenecek
        eventPublisher.publishEvent(new EmployeeUpdatedEvent(id, saved));
        
        return saved;
    }
    
    /**
     * AFTER_COMMIT event listener
     * Cache sadece transaction commit olduktan sonra güncellenir
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeUpdated(EmployeeUpdatedEvent event) {
        log.info("handleEmployeeUpdated - AFTER_COMMIT - Cache güncelleniyor");
        log.info("Employee ID: {}", event.getEmployeeId());
        
        // Cache güncelleniyor - Transaction commit olduktan sonra (DOĞRU!)
        cache.put(event.getEmployeeId(), event.getEmployee());
        log.info("Cache güncellendi: {}", event.getEmployeeId());
    }
    
    /**
     * Cache'den okuma
     */
    public Optional<Employee> getEmployeeFromCache(Long id) {
        return Optional.ofNullable(cache.get(id));
    }
    
    // Event class
    public static class EmployeeUpdatedEvent {
        private final Long employeeId;
        private final Employee employee;
        
        public EmployeeUpdatedEvent(Long employeeId, Employee employee) {
            this.employeeId = employeeId;
            this.employee = employee;
        }
        
        public Long getEmployeeId() {
            return employeeId;
        }
        
        public Employee getEmployee() {
            return employee;
        }
    }
}



