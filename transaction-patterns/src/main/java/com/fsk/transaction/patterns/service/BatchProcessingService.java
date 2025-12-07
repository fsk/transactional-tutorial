package com.fsk.transaction.patterns.service;

import com.fsk.transaction.patterns.entity.Employee;
import com.fsk.transaction.patterns.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 49. Transaction + Batch Processing tuzağı
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {
    
    private final EmployeeRepository employeeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * YANLIŞ: Tek transaction ile büyük batch
     */
    @Transactional
    public void largeBatchWrong(int count) {
        log.info("largeBatchWrong - YANLIŞ: Tek transaction ile {} kayıt", count);
        
        for (int i = 0; i < count; i++) {
            Employee employee = new Employee();
            employee.setName("Batch Employee " + i);
            employee.setEmail("batch" + i + "@example.com");
            employee.setDepartment("IT");
            employee.setSalary(5000.0 + i);
            
            employeeRepository.save(employee);
        }
        
        log.warn("Büyük batch tamamlandı - Ama persistence context şişti!");
    }
    
    /**
     * DOĞRU: Chunk processing + intermediate commit
     */
    public void largeBatchCorrect(int totalCount, int chunkSize) {
        log.info("largeBatchCorrect - DOĞRU: Chunk processing");
        
        for (int chunk = 0; chunk < totalCount; chunk += chunkSize) {
            processChunk(chunk, Math.min(chunk + chunkSize, totalCount));
            log.info("Chunk işlendi: {} - {}", chunk, Math.min(chunk + chunkSize, totalCount));
        }
        
        log.info("Batch processing tamamlandı - Chunk'lar halinde commit edildi");
    }
    
    @Transactional
    private void processChunk(int start, int end) {
        log.info("processChunk - Chunk işleniyor: {} - {}", start, end);
        
        for (int i = start; i < end; i++) {
            Employee employee = new Employee();
            employee.setName("Chunk Employee " + i);
            employee.setEmail("chunk" + i + "@example.com");
            employee.setDepartment("IT");
            employee.setSalary(5000.0 + i);
            
            employeeRepository.save(employee);
            
            if ((i - start) % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        log.info("Chunk commit edildi - Persistence context temizlendi");
    }
}


