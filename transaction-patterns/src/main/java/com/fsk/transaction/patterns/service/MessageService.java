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

/**
 * 27. Message gönderimi transaction içinde mi olmalı?
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * PROBLEM: Message transaction içinde gönderilirse
     */
    @Transactional
    public void createEmployeeWithMessageProblem(String name, String email) {
        log.info("createEmployeeWithMessageProblem - Message transaction içinde (PROBLEM)");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", employee.getId());
        
        sendMessageDirectly("Employee created: " + employee.getId());
        
        throw new RuntimeException("DB rollback olacak ama message gitmiş olabilir!");
    }
    
    /**
     * ÇÖZÜM: AFTER_COMMIT event kullan
     */
    @Transactional
    public void createEmployeeWithMessageSolution(String name, String email) {
        log.info("createEmployeeWithMessageSolution - AFTER_COMMIT event ile");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        eventPublisher.publishEvent(new EmployeeCreatedEvent(saved.getId(), name, email));
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("handleEmployeeCreated - AFTER_COMMIT - Message gönderiliyor");
        sendMessageSafely("Employee created: " + event.getEmployeeId());
    }
    
    private void sendMessageDirectly(String message) {
        log.warn("Message gönderiliyor (Transaction içinde - YANLIŞ!): {}", message);
    }
    
    private void sendMessageSafely(String message) {
        log.info("Message gönderiliyor (AFTER_COMMIT - DOĞRU): {}", message);
    }
    
    public static class EmployeeCreatedEvent {
        private final Long employeeId;
        private final String name;
        private final String email;
        
        public EmployeeCreatedEvent(Long employeeId, String name, String email) {
            this.employeeId = employeeId;
            this.name = name;
            this.email = email;
        }
        
        public Long getEmployeeId() {
            return employeeId;
        }
        
        public String getName() {
            return name;
        }
        
        public String getEmail() {
            return email;
        }
    }
}


