package com.fsk.transaction.locking.service;

import com.fsk.transaction.locking.entity.Employee;
import com.fsk.transaction.locking.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 25. Deadlock'lar transaction ile nasıl ilişkilidir?
 * 
 * Transaction isolation deadlock'a neden olabilir mi?
 * ✅ Yes
 * - Lock ordering yanlış
 * - Long transaction
 * - REPEATABLE_READ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeadlockService {
    
    private final EmployeeRepository employeeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Deadlock örneği - Yanlış lock ordering
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateEmployeeAThenB(Long idA, Long idB, Double newSalaryA, Double newSalaryB) {
        log.info("updateEmployeeAThenB - A'yı kilitle, sonra B'yi kilitle");
        
        // A'yı kilitle
        Employee employeeA = entityManager.find(Employee.class, idA, LockModeType.PESSIMISTIC_WRITE);
        if (employeeA != null) {
            employeeA.setSalary(newSalaryA);
            employeeRepository.save(employeeA);
            log.info("Employee A güncellendi: {}", idA);
        }
        
        // Simüle edilmiş gecikme - Deadlock için
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // B'yi kilitle
        Employee employeeB = entityManager.find(Employee.class, idB, LockModeType.PESSIMISTIC_WRITE);
        if (employeeB != null) {
            employeeB.setSalary(newSalaryB);
            employeeRepository.save(employeeB);
            log.info("Employee B güncellendi: {}", idB);
        }
    }
    
    /**
     * Deadlock örneği - Ters sıralama
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateEmployeeBThenA(Long idA, Long idB, Double newSalaryA, Double newSalaryB) {
        log.info("updateEmployeeBThenA - B'yi kilitle, sonra A'yı kilitle");
        
        // B'yi kilitle
        Employee employeeB = entityManager.find(Employee.class, idB, LockModeType.PESSIMISTIC_WRITE);
        if (employeeB != null) {
            employeeB.setSalary(newSalaryB);
            employeeRepository.save(employeeB);
            log.info("Employee B güncellendi: {}", idB);
        }
        
        // Simüle edilmiş gecikme - Deadlock için
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // A'yı kilitle
        Employee employeeA = entityManager.find(Employee.class, idA, LockModeType.PESSIMISTIC_WRITE);
        if (employeeA != null) {
            employeeA.setSalary(newSalaryA);
            employeeRepository.save(employeeA);
            log.info("Employee A güncellendi: {}", idA);
        }
    }
    
    /**
     * Deadlock çözümü - Consistent lock ordering
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateEmployeesSafely(Long id1, Long id2, Double newSalary1, Double newSalary2) {
        log.info("updateEmployeesSafely - Consistent lock ordering");
        
        // Her zaman küçük ID'den büyük ID'ye doğru kilitle
        Long firstId = id1 < id2 ? id1 : id2;
        Long secondId = id1 < id2 ? id2 : id1;
        Double firstSalary = id1 < id2 ? newSalary1 : newSalary2;
        Double secondSalary = id1 < id2 ? newSalary2 : newSalary1;
        
        // İlk ID'yi kilitle
        Employee employee1 = entityManager.find(Employee.class, firstId, LockModeType.PESSIMISTIC_WRITE);
        if (employee1 != null) {
            employee1.setSalary(firstSalary);
            employeeRepository.save(employee1);
            log.info("Employee {} güncellendi", firstId);
        }
        
        // İkinci ID'yi kilitle
        Employee employee2 = entityManager.find(Employee.class, secondId, LockModeType.PESSIMISTIC_WRITE);
        if (employee2 != null) {
            employee2.setSalary(secondSalary);
            employeeRepository.save(employee2);
            log.info("Employee {} güncellendi", secondId);
        }
    }
}



