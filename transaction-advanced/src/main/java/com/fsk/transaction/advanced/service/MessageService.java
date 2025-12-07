package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 27. Message gönderimi transaction içinde mi olmalı?
 * 
 * RabbitMQ message send transaction içinde olursa risk nedir?
 * ✅ DB rollback olsa bile message gitmiş olabilir
 * 
 * Senior çözüm:
 * - Outbox pattern
 * - AFTER_COMMIT event
 * - Transactional messaging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * PROBLEM: Message transaction içinde gönderilirse
     * DB rollback olsa bile message gitmiş olabilir
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
        
        // Message gönderiliyor - Transaction içinde (YANLIŞ!)
        sendMessageDirectly("Employee created: " + employee.getId());
        
        // Exception fırlatılıyor - DB rollback olacak
        // Ama message gitmiş olabilir!
        throw new RuntimeException("DB rollback olacak ama message gitmiş olabilir!");
    }
    
    /**
     * ÇÖZÜM 1: AFTER_COMMIT event kullan
     * Message sadece transaction commit olduktan sonra gönderilir
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
        
        // Event yayınla - AFTER_COMMIT'te dinlenecek
        eventPublisher.publishEvent(new EmployeeCreatedEvent(saved.getId(), name, email));
    }
    
    /**
     * AFTER_COMMIT event listener
     * Message sadece transaction commit olduktan sonra gönderilir
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("handleEmployeeCreated - AFTER_COMMIT - Message gönderiliyor");
        log.info("Employee ID: {}, Name: {}, Email: {}", 
            event.getEmployeeId(), event.getName(), event.getEmail());
        
        // Burada gerçek message gönderme işlemi yapılabilir
        // RabbitMQ, Kafka, etc.
        sendMessageSafely("Employee created: " + event.getEmployeeId());
    }
    
    /**
     * Simüle edilmiş message gönderme (YANLIŞ - Transaction içinde)
     */
    private void sendMessageDirectly(String message) {
        log.warn("Message gönderiliyor (Transaction içinde - YANLIŞ!): {}", message);
        // Gerçek implementasyon: rabbitTemplate.send(...)
    }
    
    /**
     * Simüle edilmiş message gönderme (DOĞRU - AFTER_COMMIT'te)
     */
    private void sendMessageSafely(String message) {
        log.info("Message gönderiliyor (AFTER_COMMIT - DOĞRU): {}", message);
        // Gerçek implementasyon: rabbitTemplate.send(...)
    }
    
    // Event class
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



