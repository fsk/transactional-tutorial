package com.fsk.transaction.context.controller;

import com.fsk.transaction.context.entity.Employee;
import com.fsk.transaction.context.service.TransactionContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/context")
@RequiredArgsConstructor
public class ContextController {
    
    private final TransactionContextService contextService;
    
    /**
     * 33. Transaction context propagation (ThreadLocal)
     * 
     * curl -X POST http://localhost:8087/api/context/threadlocal
     */
    @PostMapping("/threadlocal")
    public ResponseEntity<String> testThreadLocal() {
        contextService.demonstrateThreadLocal();
        return ResponseEntity.ok("ThreadLocal transaction context test edildi");
    }
    
    /**
     * 33. @Async transaction context kaybı
     * 
     * curl -X POST http://localhost:8087/api/context/async-problem
     */
    @PostMapping("/async-problem")
    public ResponseEntity<String> testAsyncProblem() {
        contextService.asyncMethodProblem();
        return ResponseEntity.ok("Async transaction context kaybı test edildi");
    }
    
    /**
     * 33. @Async çözümü
     * 
     * curl -X POST http://localhost:8087/api/context/async-solution
     */
    @PostMapping("/async-solution")
    public ResponseEntity<String> testAsyncSolution() {
        CompletableFuture<Employee> future = contextService.asyncMethodSolution();
        return ResponseEntity.ok("Async çözüm test edildi - Kendi transaction'ı var");
    }
    
    /**
     * 33. CompletableFuture transaction context kaybı
     * 
     * curl -X POST http://localhost:8087/api/context/completable-future
     */
    @PostMapping("/completable-future")
    public ResponseEntity<String> testCompletableFuture() {
        CompletableFuture<Employee> future = contextService.completableFutureProblem();
        return ResponseEntity.ok("CompletableFuture transaction context kaybı test edildi");
    }
}



