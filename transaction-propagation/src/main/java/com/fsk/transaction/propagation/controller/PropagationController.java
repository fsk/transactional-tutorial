package com.fsk.transaction.propagation.controller;

import com.fsk.transaction.propagation.entity.Account;
import com.fsk.transaction.propagation.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/propagation")
@RequiredArgsConstructor
public class PropagationController {
    
    private final AccountService accountService;
    
    /**
     * REQUIRED: Varsa mevcut transaction'a katılır
     * 
     * curl -X POST http://localhost:8084/api/propagation/required \
     *   -H "Content-Type: application/json" \
     *   -d '{"accountNumber":"ACC-001","balance":1000.0,"ownerName":"John Doe"}'
     */
    @PostMapping("/required")
    public ResponseEntity<Account> testRequired(@RequestBody AccountRequest request) {
        Account account = accountService.createAccountRequired(request.accountNumber(), request.balance(), request.ownerName());
        return ResponseEntity.ok(account);
    }
    
    /**
     * REQUIRES_NEW: Yeni transaction açar
     * 
     * Senaryo:
     * 1. Ana transaction başlar → Account kaydedilir (henüz commit yok)
     * 2. REQUIRES_NEW çağrılır → Ana transaction SUSPEND edilir
     * 3. Yeni transaction açılır → Balance güncellenir → COMMIT EDİLİR ✅
     * 4. Ana transaction devam eder (RESUME)
     * 5. Exception fırlatılır → Ana transaction ROLLBACK olur ❌
     * 
     * Sonuç:
     * - Balance güncellemesi KALIR (REQUIRES_NEW commit edildi)
     * - Account kaydı SİLİNİR (Ana transaction rollback oldu)
     * 
     * curl -X POST http://localhost:8084/api/propagation/requires-new \
     *   -H "Content-Type: application/json" \
     *   -d '{"accountNumber":"ACC-002","balance":2000.0,"ownerName":"Jane Doe"}'
     */
    @PostMapping("/requires-new")
    public ResponseEntity<String> testRequiresNew(@RequestBody AccountRequest request) {
        try {
            accountService.createAccountWithRequiresNew(
                    request.accountNumber(),
                    request.balance(),
                    request.ownerName()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok(
                "✅ REQUIRES_NEW başarılı: Balance güncellemesi commit edildi!\n" +
                "❌ Ana transaction rollback: Account kaydı silindi!\n" +
                "📝 Mesaj: " + e.getMessage()
            );
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * SUPPORTS: Varsa katılır, yoksa transaction olmadan
     * 
     * curl -X POST http://localhost:8084/api/propagation/supports \
     *   -H "Content-Type: application/json" \
     *   -d '{"accountNumber":"ACC-003","balance":3000.0,"ownerName":"Bob Smith"}'
     */
    @PostMapping("/supports")
    public ResponseEntity<Account> testSupports(@RequestBody AccountRequest request) {
        Account account = accountService.createAccountSupports(request.accountNumber(), request.balance(), request.ownerName());
        return ResponseEntity.ok(account);
    }
    
    /**
     * NOT_SUPPORTED: Transaction'ı suspend eder
     * 
     * curl -X POST http://localhost:8084/api/propagation/not-supported \
     *   -H "Content-Type: application/json" \
     *   -d '{"accountNumber":"ACC-004","balance":4000.0,"ownerName":"Alice Brown"}'
     */
    @PostMapping("/not-supported")
    public ResponseEntity<Account> testNotSupported(@RequestBody AccountRequest request) {
        Account account = accountService.createAccountNotSupported(
                request.accountNumber(),
                request.balance(),
                request.ownerName()
        );
        return ResponseEntity.ok(account);
    }
    
    /**
     * MANDATORY: Mevcut TX olmalı (transaction içinden çağrılmalı)
     * 
     * curl -X POST http://localhost:8084/api/propagation/mandatory \
     *   -H "Content-Type: application/json" \
     *   -d '{"accountNumber":"ACC-005","balance":5000.0,"ownerName":"Charlie Wilson"}'
     */
    @PostMapping("/mandatory")
    public ResponseEntity<Account> testMandatory(@RequestBody AccountRequest request) {
        // Bu çağrı transaction içinden yapılıyor (AccountService.createAccountRequired içinden)
        Account account = accountService.createAccountRequired(request.accountNumber(), request.balance(), request.ownerName());
        return ResponseEntity.ok(account);
    }
    
    /**
     * REQUIRES_NEW - Mevcut transaction OLMADAN test
     * 
     * ÖNEMLİ: REQUIRES_NEW mevcut transaction'ın olmasını BEKLEMEZ!
     * Controller'dan direkt çağrılıyor (transaction yok)
     * REQUIRES_NEW direkt yeni transaction açacak
     * 
     * curl -X POST http://localhost:8084/api/propagation/requires-new-direct \
     *   -H "Content-Type: application/json" \
     *   -d '{"accountNumber":"ACC-006","balance":6000.0,"ownerName":"Test User"}'
     */
    @PostMapping("/requires-new-direct")
    public ResponseEntity<Account> testRequiresNewDirect(@RequestBody AccountRequest request) {
        // Controller'da transaction YOK - REQUIRES_NEW direkt yeni transaction açacak
        Account account = accountService.createAccountRequiresNewDirect(
                request.accountNumber(),
                request.balance(),
                request.ownerName()
        );
        return ResponseEntity.ok(account);
    }

    public record AccountRequest(String accountNumber, Double balance, String ownerName) {}
}


