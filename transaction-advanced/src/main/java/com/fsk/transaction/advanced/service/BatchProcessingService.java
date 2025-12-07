package com.fsk.transaction.advanced.service;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 49. Transaction + Batch Processing tuzağı
 * 
 * Neden tek transaction ile 1M insert yapılmaz?
 * ✅ Persistence context şişer
 * ✅ Memory leak
 * ✅ Lock duration
 * 
 * Çözüm: Chunk + intermediate commit
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
            
            // Problem: Persistence context şişiyor
            // Memory leak riski
            // Lock duration çok uzun
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
            
            // Her 100 kayıtta bir flush
            if ((i - start) % 100 == 0) {
                entityManager.flush();
                entityManager.clear(); // Persistence context temizleniyor
            }
        }
        
        // Chunk commit ediliyor - Persistence context temizleniyor
        log.info("Chunk commit edildi - Persistence context temizlendi");
    }
}


