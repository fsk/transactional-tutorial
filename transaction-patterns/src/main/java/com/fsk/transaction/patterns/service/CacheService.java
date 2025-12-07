package com.fsk.transaction.patterns.service;

import com.fsk.transaction.patterns.entity.Employee;
import com.fsk.transaction.patterns.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    private final Map<Long, Employee> cache = new HashMap<>();
    
    /**
     * PROBLEM: Cache transaction içinde güncellenirse
     */
    @Transactional
    public Employee updateEmployeeWithCacheProblem(Long id, Double newSalary) {
        log.info("updateEmployeeWithCacheProblem - Cache transaction içinde (PROBLEM)");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        Employee saved = employeeRepository.save(employee);
        
        cache.put(id, saved);
        log.info("Cache güncellendi: {}", id);
        
        throw new RuntimeException("DB rollback olacak ama cache güncellenmiş kalır!");
    }
    
    /**
     * ÇÖZÜM: AFTER_COMMIT event kullan
     */
    @Transactional
    public Employee updateEmployeeWithCacheSolution(Long id, Double newSalary) {
        log.info("updateEmployeeWithCacheSolution - AFTER_COMMIT event ile");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        employee.setSalary(newSalary);
        Employee saved = employeeRepository.save(employee);
        
        eventPublisher.publishEvent(new EmployeeUpdatedEvent(id, saved));
        
        return saved;
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeUpdated(EmployeeUpdatedEvent event) {
        log.info("handleEmployeeUpdated - AFTER_COMMIT - Cache güncelleniyor");
        cache.put(event.getEmployeeId(), event.getEmployee());
        log.info("Cache güncellendi: {}", event.getEmployeeId());
    }
    
    public Optional<Employee> getEmployeeFromCache(Long id) {
        return Optional.ofNullable(cache.get(id));
    }
    
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



