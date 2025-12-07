package com.fsk.transaction.propagation.service;

import com.fsk.transaction.propagation.entity.Account;
import com.fsk.transaction.propagation.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inner Service - Farklı propagation türlerini test etmek için
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountInnerService {
    
    private final AccountRepository accountRepository;
    
    /**
     * REQUIRES_NEW: Yeni transaction açar
     * 
     * ÖNEMLİ:
     * - Mevcut transaction VARSA → SUSPEND edilir (beklemede kalır)
     * - Mevcut transaction YOKSA → Direkt yeni transaction açılır
     * - Bu transaction COMMIT edilir (ana transaction'dan bağımsız)
     * - Ana transaction varsa sonra devam eder (RESUME)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAccountBalanceRequiresNew(Long accountId, Double newBalance) {
        boolean hasTransaction = org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive();
        
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│  YENİ TRANSACTION BAŞLADI (REQUIRES_NEW)                │");
        if (hasTransaction) {
            log.info("│  ⚠️  Mevcut transaction VAR - SUSPEND edildi          │");
        } else {
            log.info("│  ✅ Mevcut transaction YOK - Direkt yeni açıldı      │");
        }
        log.info("└─────────────────────────────────────────────────────────┘");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account bulunamadı"));
        
        log.info("📝 Balance güncelleniyor: {} → {}", account.getBalance(), newBalance);
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│  ✅ YENİ TRANSACTION COMMIT EDİLİYOR                    │");
        log.info("│  Balance güncellemesi kalıcı olacak!                    │");
        log.info("│  (Ana transaction'dan bağımsız)                         │");
        log.info("└─────────────────────────────────────────────────────────┘");
        // Bu transaction commit edilir, ana transaction'dan bağımsız
    }
    
    /**
     * REQUIRED: Mevcut transaction'a katılır
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateAccountBalanceRequired(Long accountId, Double newBalance) {
        log.info("updateAccountBalanceRequired - REQUIRED (Mevcut transaction'a katılır)");
        log.info("Current transaction: {}", 
            org.springframework.transaction.support.TransactionSynchronizationManager.getCurrentTransactionName());
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account bulunamadı"));
        
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Balance güncellendi: {}", newBalance);
    }
}


