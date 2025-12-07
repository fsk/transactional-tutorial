package com.fsk.transaction.monitoring.service;

import com.fsk.transaction.monitoring.entity.TransactionMetrics;
import com.fsk.transaction.monitoring.repository.TransactionMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Transaction + Monitoring Konuları
 * 
 * Metrics Collection
 * Distributed Tracing
 * Observability
 * Performance Monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {
    
    private final TransactionMetricsRepository metricsRepository;
    
    /**
     * Transaction metrics kaydetme
     */
    @Transactional
    public void recordTransactionMetrics(String transactionName, long durationMillis, boolean success, String errorMessage) {
        log.info("recordTransactionMetrics - Transaction metrics kaydediliyor");
        
        TransactionMetrics metrics = new TransactionMetrics();
        metrics.setTransactionName(transactionName);
        metrics.setDurationMillis(durationMillis);
        metrics.setSuccess(success);
        metrics.setTimestamp(LocalDateTime.now());
        metrics.setErrorMessage(errorMessage);
        
        metricsRepository.save(metrics);
        log.info("Transaction metrics kaydedildi: {}", transactionName);
    }
    
    /**
     * Transaction performance monitoring
     */
    @Transactional
    public void performMonitoredOperation(String operationName) {
        long startTime = System.currentTimeMillis();
        log.info("performMonitoredOperation - Operation başladı: {}", operationName);
        
        try {
            // Simüle edilmiş işlem
            Thread.sleep(100);
            
            long duration = System.currentTimeMillis() - startTime;
            recordTransactionMetrics(operationName, duration, true, null);
            log.info("Operation tamamlandı: {} ms", duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            recordTransactionMetrics(operationName, duration, false, e.getMessage());
            log.error("Operation başarısız: {}", operationName, e);
            throw new RuntimeException("Operation failed", e);
        }
    }
}



