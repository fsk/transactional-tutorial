package com.fsk.transaction.propagation.service;

import com.fsk.transaction.propagation.entity.Account;
import com.fsk.transaction.propagation.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Transaction Propagation Türleri
 * 
 * REQUIRED: Varsa mevcut transaction'a katılır, yoksa yeni açar (DEFAULT)
 * REQUIRES_NEW: Mevcut TX'i suspend eder, yeni TX açar
 * NESTED: Nested transaction açar (savepoint)
 * SUPPORTS: Varsa katılır, yoksa transaction olmadan çalışır
 * NOT_SUPPORTED: Mevcut TX'i suspend eder, transaction olmadan çalışır
 * MANDATORY: Mevcut TX olmalı, yoksa exception
 * NEVER: Transaction olmamalı, varsa exception
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AuditService auditService;
    private final AccountInnerService accountInnerService;
    
    /**
     * REQUIRED (DEFAULT): Varsa mevcut transaction'a katılır
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Account createAccountRequired(String accountNumber, Double balance, String ownerName) {
        log.info("createAccountRequired - REQUIRED propagation");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwnerName(ownerName);

        Account saved = accountRepository.save(account);
        log.info("Account saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());

        // Audit log - REQUIRES_NEW ile ayrı transaction
        auditService.logAction("CREATE", "Account", saved.getId());
        
        return saved;
    }

    /**
     * REQUIRES_NEW ile çağrı - Yeni transaction
     * 
     * Senaryo:
     * 1. Ana transaction başlar (REQUIRED)
     * 2. Account kaydedilir (henüz commit yok)
     * 3. REQUIRES_NEW ile balance güncellenir → YENİ TRANSACTION → COMMIT EDİLİR
     * 4. Ana transaction'da exception → ROLLBACK (Account silinir)
     * 5. Sonuç: Balance güncellemesi kalır, Account silinir
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void createAccountWithRequiresNew(String accountNumber, Double balance, String ownerName) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("  MAIN TRANSACTION STARTED (REQUIRED propagation)");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("═══════════════════════════════════════════════════════════");

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwnerName(ownerName);

        Account saved = accountRepository.save(account);
        log.info("Account saved with ID: {} (NOTE: ID assigned but not yet committed!)", saved.getId());

        log.info("═══════════════════════════════════════════════════════════");
        log.info("   REQUIRES_NEW METHOD CALLED");
        log.info("   → Ana transaction WILL BE SUSPENDED");
        log.info("   → New transaction WILL BE OPENED");
        log.info("═══════════════════════════════════════════════════════════");

        // Inner service REQUIRES_NEW ile çağrılıyor
        accountInnerService.updateAccountBalanceRequiresNew(saved.getId(), balance + 100);
        
        log.info("═══════════════════════════════════════════════════════════");
        log.info("REQUIRES_NEW COMPLETED - Ana transaction WILL CONTINUE");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("═══════════════════════════════════════════════════════════");
        
        // Exception fırlatılıyor - Ana transaction rollback olacak
        // Ama REQUIRES_NEW ile yapılan işlem commit edilmiş olacak
        log.error("Exception thrown - Ana transaction WILL BE ROLLBACKED!");
        throw new RuntimeException("Ana transaction WILL BE ROLLBACKED but REQUIRES_NEW commit will be committed!");
    }
    
    /**
     * SUPPORTS: Varsa katılır, yoksa transaction olmadan çalışır
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public Account createAccountSupports(String accountNumber, Double balance, String ownerName) {
        log.info("createAccountSupports - SUPPORTS propagation");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwnerName(ownerName);
        
        return accountRepository.save(account);
    }
    
    /**
     * NOT_SUPPORTED: Mevcut TX'i suspend eder, transaction olmadan çalışır
     * 
     * ÖNEMLİ:
     * - Mevcut transaction VARSA → Suspend eder (beklemede kalır)
     * - YENİ TRANSACTION AÇMAZ
     * - Transaction olmadan çalışır (non-transactional)
     * - Her işlem otomatik commit edilir (auto-commit mode)
     * - Mevcut transaction YOKSA → Direkt transaction olmadan çalışır
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Account createAccountNotSupported(String accountNumber, Double balance, String ownerName) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("NOT_SUPPORTED - Transaction without new transaction");
        log.info("═══════════════════════════════════════════════════════════");
        
        boolean hasTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Is current transaction present? {}", hasTransaction);
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        if (hasTransaction) {
            log.info("Current transaction present - WILL BE SUSPENDED");
            log.info("New transaction WILL NOT BE OPENED - WILL RUN WITHOUT TRANSACTION");
        } else {
            log.info("Current transaction NOT present - WILL RUN WITHOUT TRANSACTION");
        }
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwnerName(ownerName);
        
        Account saved = accountRepository.save(account);
        log.info("Account saved with ID: {} - Auto-commit mode (transaction yok!)", saved.getId());
        
        return saved;
    }
    
    /**
     * MANDATORY: Mevcut TX olmalı
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Account createAccountMandatory(String accountNumber, Double balance, String ownerName) {
        log.info("createAccountMandatory - MANDATORY propagation");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwnerName(ownerName);
        
        return accountRepository.save(account);
    }
    
    /**
     * REQUIRES_NEW - Mevcut transaction OLMADAN çağrılıyor
     * 
     * ÖNEMLİ: REQUIRES_NEW mevcut transaction'ın olmasını BEKLEMEZ!
     * - Mevcut transaction VARSA → Suspend eder, yeni açar
     * - Mevcut transaction YOKSA → Direkt yeni transaction açar
     * 
     * Bu metod Controller'dan direkt çağrılacak (transaction yok)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Account createAccountRequiresNewDirect(String accountNumber, Double balance, String ownerName) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("REQUIRES_NEW - Mevcut transaction NOT present!");
        log.info("Current transaction: {}", TransactionSynchronizationManager.getCurrentTransactionName());
        log.info("═══════════════════════════════════════════════════════════");
        
        boolean hasTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("Is current transaction present? {}", hasTransaction);
        
        if (!hasTransaction) {
            log.info("Current transaction NOT present - REQUIRES_NEW will open a new transaction directly!");
        } else {
            log.info("Current transaction present - REQUIRES_NEW will suspend and open a new transaction!");
        }
        
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwnerName(ownerName);
        
        Account saved = accountRepository.save(account);
        log.info("Account saved with ID: {} - Transaction WILL BE COMMITTED", saved.getId());
        
        return saved;
    }
}


