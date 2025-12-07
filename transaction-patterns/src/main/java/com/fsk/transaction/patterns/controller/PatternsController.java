package com.fsk.transaction.patterns.controller;

import com.fsk.transaction.patterns.entity.Employee;
import com.fsk.transaction.patterns.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternsController {
    
    private final TransactionPatternsService patternsService;
    private final RetryAndRemoteService retryService;
    private final BatchProcessingService batchService;
    private final SilentRollbackService silentRollbackService;
    private final MessageService messageService;
    private final CacheService cacheService;
    private final PropagationChainService propagationChainService;
    
    /**
     * 16. @Transactional + private method testi
     */
    @PostMapping("/private-method")
    public ResponseEntity<String> testPrivateMethod() {
        patternsService.callPrivateMethod();
        return ResponseEntity.ok("Private method test edildi - @Transactional çalışmaz!");
    }
    
    /**
     * 17. Final method testi
     */
    @PostMapping("/final-method")
    public ResponseEntity<String> testFinalMethod() {
        patternsService.finalMethod();
        return ResponseEntity.ok("Final method test edildi - @Transactional çalışmayabilir!");
    }
    
    /**
     * 22. Nested transaction testi
     */
    @PostMapping("/nested-transaction")
    public ResponseEntity<String> testNestedTransaction() {
        try {
            patternsService.nestedTransactionExample();
        } catch (Exception e) {
            return ResponseEntity.ok("Nested transaction test edildi - PostgreSQL'de çalışmayabilir: " + e.getMessage());
        }
        return ResponseEntity.ok("Nested transaction test edildi");
    }
    
    /**
     * 29. Read-only transaction içinde write testi
     */
    @PostMapping("/read-only-write")
    public ResponseEntity<String> testReadOnlyWrite() {
        try {
            patternsService.readOnlyWithWrite();
        } catch (Exception e) {
            return ResponseEntity.ok("Read-only write test edildi - Hibernate flush etmez: " + e.getMessage());
        }
        return ResponseEntity.ok("Read-only write test edildi");
    }
    
    /**
     * 30. Stream API tuzağı testi
     */
    @GetMapping("/stream-trap")
    public ResponseEntity<List<Employee>> testStreamTrap() {
        List<Employee> employees = patternsService.streamApiTrap();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * 30. Stream API çözümü
     */
    @GetMapping("/stream-solution")
    public ResponseEntity<List<Employee>> testStreamSolution() {
        List<Employee> employees = patternsService.streamApiSolution();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * 41. Retry transaction içinde (YANLIŞ)
     */
    @PutMapping("/retry/inside-transaction/{id}")
    public ResponseEntity<String> testRetryInsideTransaction(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        try {
            retryService.retryInsideTransaction(id, newSalary);
            return ResponseEntity.ok("Retry inside transaction (YANLIŞ)");
        } catch (Exception e) {
            return ResponseEntity.ok("Retry failed: " + e.getMessage());
        }
    }
    
    /**
     * 42. Remote call transaction içinde (YANLIŞ)
     */
    @PutMapping("/remote/inside-transaction/{id}")
    public ResponseEntity<String> testRemoteCallInsideTransaction(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        try {
            retryService.remoteCallInsideTransaction(id, newSalary);
            return ResponseEntity.ok("Remote call inside transaction (YANLIŞ)");
        } catch (Exception e) {
            return ResponseEntity.ok("Remote call failed: " + e.getMessage());
        }
    }
    
    /**
     * 44. Time-sensitive logic
     */
    @PutMapping("/time-sensitive/{id}")
    public ResponseEntity<Employee> testTimeSensitiveLogic(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        Employee employee = retryService.timeSensitiveLogicProblem(id, newSalary);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 45. Silent rollback
     */
    @PostMapping("/silent-rollback")
    public ResponseEntity<String> testSilentRollback(@RequestBody EmployeeRequest request) {
        try {
            silentRollbackService.silentRollbackExample(request.getName(), request.getEmail());
            return ResponseEntity.ok("Silent rollback test edildi - Exception yok ama rollback olacak");
        } catch (Exception e) {
            return ResponseEntity.ok("Silent rollback exception: " + e.getMessage());
        }
    }
    
    /**
     * 49. Batch processing - YANLIŞ
     */
    @PostMapping("/batch/wrong")
    public ResponseEntity<String> testBatchWrong(@RequestParam int count) {
        batchService.largeBatchWrong(count);
        return ResponseEntity.ok("Büyük batch (YANLIŞ) - Persistence context şişti");
    }
    
    /**
     * 49. Batch processing - DOĞRU
     */
    @PostMapping("/batch/correct")
    public ResponseEntity<String> testBatchCorrect(
            @RequestParam int totalCount,
            @RequestParam int chunkSize) {
        batchService.largeBatchCorrect(totalCount, chunkSize);
        return ResponseEntity.ok("Batch processing (DOĞRU) - Chunk'lar halinde commit edildi");
    }
    
    /**
     * 27. Message gönderimi - PROBLEM
     */
    @PostMapping("/message-problem")
    public ResponseEntity<String> testMessageProblem(@RequestBody EmployeeRequest request) {
        try {
            messageService.createEmployeeWithMessageProblem(request.getName(), request.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Message problem test edildi - DB rollback oldu ama message gitmiş olabilir! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * 27. Message gönderimi - ÇÖZÜM
     */
    @PostMapping("/message-solution")
    public ResponseEntity<String> testMessageSolution(@RequestBody EmployeeRequest request) {
        messageService.createEmployeeWithMessageSolution(request.getName(), request.getEmail());
        return ResponseEntity.ok("Message solution test edildi - AFTER_COMMIT event ile message gönderilecek");
    }
    
    /**
     * 24. Cache tutarsızlığı - PROBLEM
     */
    @PutMapping("/cache-problem/{id}")
    public ResponseEntity<String> testCacheProblem(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        try {
            cacheService.updateEmployeeWithCacheProblem(id, newSalary);
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Cache problem test edildi - DB rollback oldu ama cache güncellenmiş kalır! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * 24. Cache tutarsızlığı - ÇÖZÜM
     */
    @PutMapping("/cache-solution/{id}")
    public ResponseEntity<Employee> testCacheSolution(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        Employee employee = cacheService.updateEmployeeWithCacheSolution(id, newSalary);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 26. Transaction propagation zinciri
     */
    @PostMapping("/propagation-chain")
    public ResponseEntity<String> testPropagationChain(@RequestBody EmployeeRequest request) {
        try {
            propagationChainService.methodA(request.getName(), request.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Propagation chain test edildi - A rollback oldu ama B commit edildi! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    // DTO
    public record EmployeeRequest(String name, String email) {}
}



