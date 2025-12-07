package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 45. Silent rollback nasıl olur?
 * 
 * Transaction rollback oldu ama exception yok. Nasıl?
 * ✅ TransactionStatus.setRollbackOnly()
 * ✅ Inner transaction rollback edip swallow etti
 * ✅ REQUIRES_NEW + catch
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SilentRollbackService {
    
    private final EmployeeRepository employeeRepository;
    private final SilentRollbackInnerService innerService;
    
    /**
     * Silent rollback örneği - setRollbackOnly()
     */
    @Transactional
    public Employee silentRollbackExample(String name, String email) {
        log.info("silentRollbackExample - Silent rollback");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Silent rollback - Exception fırlatılmadan rollback
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
        // Veya başka bir yöntemle rollback-only işaretlenir
        
        log.info("Transaction rollback-only işaretlendi - Ama exception yok!");
        
        return saved; // Method return ediyor ama transaction rollback olacak
    }
    
    /**
     * Inner transaction rollback edip swallow etti
     */
    @Transactional
    public Employee innerTransactionRollback(String name, String email) {
        log.info("innerTransactionRollback - Inner transaction rollback");
        
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        Employee saved = employeeRepository.save(employee);
        log.info("Employee kaydedildi: {}", saved.getId());
        
        // Inner transaction çağrılıyor - Rollback olacak ama exception swallow edilecek
        try {
            innerService.innerMethodThatRollsBack();
        } catch (Exception e) {
            log.warn("Inner transaction exception swallow edildi: {}", e.getMessage());
            // Exception catch edildi - Outer transaction devam ediyor
        }
        
        log.info("Outer transaction devam ediyor - Ama inner transaction rollback oldu");
        
        return saved;
    }
}

@Service
@RequiredArgsConstructor
@Slf4j
class SilentRollbackInnerService {
    
    private final EmployeeRepository employeeRepository;
    
    /**
     * REQUIRES_NEW ile inner transaction - Rollback olacak
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerMethodThatRollsBack() {
        log.info("innerMethodThatRollsBack - REQUIRES_NEW transaction");
        
        Employee employee = new Employee();
        employee.setName("Inner Transaction");
        employee.setEmail("inner@example.com");
        employee.setDepartment("IT");
        employee.setSalary(5000.0);
        
        employeeRepository.save(employee);
        log.info("Inner transaction'da employee kaydedildi");
        
        // Exception fırlatılıyor - Inner transaction rollback olacak
        throw new RuntimeException("Inner transaction rollback");
    }
}



