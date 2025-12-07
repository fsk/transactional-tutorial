package com.fsk.transaction.propagation.service;

import com.fsk.transaction.propagation.entity.AuditLog;
import com.fsk.transaction.propagation.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Audit Service - REQUIRES_NEW kullanımı
 * 
 * Ana işlem rollback olsa bile audit kaydı commit edilsin diye
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    /**
     * REQUIRES_NEW: Mevcut transaction'ı suspend eder, yeni transaction açar
     * Ana işlem rollback olsa bile audit kaydı commit edilir
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, Long entityId) {
        log.info("Audit log kaydediliyor (REQUIRES_NEW) - Yeni transaction");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setTimestamp(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
        log.info("Audit log kaydedildi: {}", auditLog.getId());
    }
}


