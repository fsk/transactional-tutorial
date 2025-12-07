package com.fsk.transaction.theory.controller;

import com.fsk.transaction.theory.service.TransactionBestPracticesService;
import com.fsk.transaction.theory.service.TransactionTheoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/theory")
@RequiredArgsConstructor
public class TheoryController {
    
    private final TransactionTheoryService theoryService;
    private final TransactionBestPracticesService bestPracticesService;
    
    /**
     * 18. Interface vs Class Annotation
     */
    @GetMapping("/interface-vs-class")
    public ResponseEntity<String> getInterfaceVsClassExplanation() {
        return ResponseEntity.ok(theoryService.interfaceVsClassAnnotation());
    }
    
    /**
     * 23. Transaction boundary neden Controller'a kadar uzatılmaz? (OSIV)
     */
    @GetMapping("/transaction-boundary")
    public ResponseEntity<String> getTransactionBoundaryExplanation() {
        return ResponseEntity.ok(theoryService.transactionBoundaryExplanation());
    }
    
    /**
     * 28. @Transactional testlerde neden farklı davranır?
     */
    @GetMapping("/transactional-in-tests")
    public ResponseEntity<String> getTransactionalInTestsExplanation() {
        return ResponseEntity.ok(theoryService.transactionalInTestsExplanation());
    }
    
    /**
     * 31. Distributed transaction neden önerilmez?
     */
    @GetMapping("/distributed-transaction")
    public ResponseEntity<String> getDistributedTransactionExplanation() {
        return ResponseEntity.ok(theoryService.distributedTransactionExplanation());
    }
    
    /**
     * 32. Transaction vs Eventual Consistency karar kriterleri
     */
    @GetMapping("/transaction-vs-consistency")
    public ResponseEntity<String> getTransactionVsConsistencyDecision() {
        return ResponseEntity.ok(theoryService.transactionVsEventualConsistencyDecision());
    }
    
    /**
     * 34. Transaction + Reactor (WebFlux) neden birlikte çalışmaz?
     */
    @GetMapping("/reactor-transaction")
    public ResponseEntity<String> getReactorTransactionExplanation() {
        return ResponseEntity.ok(bestPracticesService.reactorAndTransaction());
    }
    
    /**
     * 43. Event publishing transaction'dan ayrılır mı?
     */
    @GetMapping("/event-publishing")
    public ResponseEntity<String> getEventPublishingExplanation() {
        return ResponseEntity.ok(bestPracticesService.eventPublishingExplanation());
    }
    
    /**
     * 46. TransactionState neden loglarda görünmez?
     */
    @GetMapping("/transaction-logging")
    public ResponseEntity<String> getTransactionLoggingExplanation() {
        return ResponseEntity.ok(bestPracticesService.transactionStateLogging());
    }
    
    /**
     * 47. Transaction + JVM crash senaryosu
     */
    @GetMapping("/jvm-crash")
    public ResponseEntity<String> getJvmCrashExplanation() {
        return ResponseEntity.ok(bestPracticesService.jvmCrashScenario());
    }
    
    /**
     * 48. Exactly-once semantics neden zordur?
     */
    @GetMapping("/exactly-once")
    public ResponseEntity<String> getExactlyOnceExplanation() {
        return ResponseEntity.ok(bestPracticesService.exactlyOnceSemantics());
    }
    
    /**
     * 50. Transaction'da olmaması gerekenler
     */
    @GetMapping("/what-not-in-transaction")
    public ResponseEntity<String> getWhatNotInTransaction() {
        return ResponseEntity.ok(bestPracticesService.whatShouldNotBeInTransaction());
    }
}



