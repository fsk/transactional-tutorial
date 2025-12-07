package com.fsk.transaction.security.service;

import com.fsk.transaction.security.entity.AuditLog;
import com.fsk.transaction.security.entity.Employee;
import com.fsk.transaction.security.repository.AuditLogRepository;
import com.fsk.transaction.security.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Transaction + Security Konuları
 * 
 * Audit Trails
 * Row-Level Security
 * Data Encryption
 * Access Control
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;
    
    /**
     * Audit trail ile employee oluşturma
     */
    @Transactional
    public Employee createEmployeeWithAudit(String name, String email, String department, Double salary, String userId) {
        log.info("createEmployeeWithAudit - Audit trail ile");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setSalary(salary);
        employee.setCreatedBy(userId);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Audit log kaydet (aynı transaction içinde)
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE");
        auditLog.setEntityType("Employee");
        auditLog.setEntityId(saved.getId());
        auditLog.setUserId(userId);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails("Employee created: " + name);
        
        auditLogRepository.save(auditLog);
        log.info("Audit log kaydedildi");
        
        return saved;
    }
    
    /**
     * Audit trail ile employee güncelleme
     */
    @Transactional
    public Employee updateEmployeeWithAudit(Long id, Double newSalary, String userId) {
        log.info("updateEmployeeWithAudit - Audit trail ile");
        
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee bulunamadı"));
        
        Double oldSalary = employee.getSalary();
        employee.setSalary(newSalary);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee güncellendi: {}", saved.getId());
        
        // Audit log kaydet
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("UPDATE");
        auditLog.setEntityType("Employee");
        auditLog.setEntityId(saved.getId());
        auditLog.setUserId(userId);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails("Salary changed from " + oldSalary + " to " + newSalary);
        
        auditLogRepository.save(auditLog);
        log.info("Audit log kaydedildi");
        
        return saved;
    }
}


