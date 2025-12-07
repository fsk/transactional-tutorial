package com.fsk.transaction.advanced.controller;

import com.fsk.transaction.advanced.entity.Employee;
import com.fsk.transaction.advanced.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/advanced")
@RequiredArgsConstructor
public class AdvancedController {
    
    private final EmployeeService employeeService;
    private final NotificationService notificationService;
    private final TransactionAdvancedService transactionAdvancedService;
    private final DeadlockService deadlockService;
    private final PropagationChainService propagationChainService;
    private final MessageService messageService;
    private final CacheService cacheService;
    private final TransactionTheoryService theoryService;
    private final TransactionContextService contextService;
    private final HibernateAdvancedService hibernateService;
    private final LockingService lockingService;
    private final RetryAndRemoteService retryService;
    private final SilentRollbackService silentRollbackService;
    private final BatchProcessingService batchService;
    private final TransactionBestPracticesService bestPracticesService;
    
    /**
     * Read-only transaction testi
     * 
     * curl -X GET http://localhost:8086/api/advanced/employees
     */
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Normal transactional write
     * 
     * curl -X POST http://localhost:8086/api/advanced/employee \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"John Doe","email":"john@example.com","department":"IT","salary":5000.0}'
     */
    @PostMapping("/employee")
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeRequest request) {
        Employee employee = employeeService.createEmployee(
            request.getName(),
            request.getEmail(),
            request.getDepartment(),
            request.getSalary()
        );
        return ResponseEntity.ok(employee);
    }
    
    /**
     * Transaction timeout testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/employee-timeout \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Timeout Test","email":"timeout@example.com","department":"IT","salary":6000.0}'
     */
    @PostMapping("/employee-timeout")
    public ResponseEntity<String> createEmployeeWithTimeout(@RequestBody EmployeeRequest request) {
        try {
            employeeService.createEmployeeWithTimeout(
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                request.getSalary()
            );
            return ResponseEntity.ok("Employee kaydedildi");
        } catch (Exception e) {
            return ResponseEntity.ok("Timeout hatası: " + e.getMessage());
        }
    }
    
    /**
     * @Async + @Transactional PROBLEM
     * 
     * curl -X POST http://localhost:8086/api/advanced/employee-async-problem \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Async Problem","email":"async@example.com","department":"IT","salary":7000.0}'
     */
    @PostMapping("/employee-async-problem")
    public ResponseEntity<String> createEmployeeAsyncProblem(@RequestBody EmployeeRequest request) {
        CompletableFuture<Employee> future = employeeService.createEmployeeAsyncProblem(
            request.getName(),
            request.getEmail(),
            request.getDepartment(),
            request.getSalary()
        );
        return ResponseEntity.ok("Async işlem başlatıldı (PROBLEM: Transaction context kaybolur)");
    }
    
    /**
     * @Async + @Transactional ÇÖZÜM
     * 
     * curl -X POST http://localhost:8086/api/advanced/employee-async-solution \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Async Solution","email":"async2@example.com","department":"IT","salary":8000.0}'
     */
    @PostMapping("/employee-async-solution")
    public ResponseEntity<String> createEmployeeAsyncSolution(@RequestBody EmployeeRequest request) {
        CompletableFuture<Employee> future = employeeService.createEmployeeAsyncSolution(
            request.getName(),
            request.getEmail(),
            request.getDepartment(),
            request.getSalary()
        );
        return ResponseEntity.ok("Async işlem başlatıldı (ÇÖZÜM: Kendi transaction'ı var)");
    }
    
    /**
     * Exception ile rollback testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/employee-exception \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Exception Test","email":"exception@example.com","department":"IT","salary":9000.0}'
     */
    @PostMapping("/employee-exception")
    public ResponseEntity<String> createEmployeeWithException(@RequestBody EmployeeRequest request) {
        try {
            employeeService.createEmployeeWithException(
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                request.getSalary()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Exception fırlatıldı - Transaction ROLLBACK oldu! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * @TransactionalEventListener testi - AFTER_COMMIT
     * 
     * curl -X POST http://localhost:8086/api/advanced/notification \
     *   -H "Content-Type: application/json" \
     *   -d '{"message":"Test Notification","recipient":"user@example.com"}'
     */
    @PostMapping("/notification")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendNotification(request.getMessage(), request.getRecipient());
        return ResponseEntity.ok("Notification kaydedildi - Transaction commit olduktan sonra event dinlenecek");
    }
    
    /**
     * 16. @Transactional + private method testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/private-method
     */
    @PostMapping("/private-method")
    public ResponseEntity<String> testPrivateMethod() {
        transactionAdvancedService.callPrivateMethod();
        return ResponseEntity.ok("Private method test edildi - @Transactional çalışmaz!");
    }
    
    /**
     * 17. Final method testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/final-method
     */
    @PostMapping("/final-method")
    public ResponseEntity<String> testFinalMethod() {
        transactionAdvancedService.finalMethod();
        return ResponseEntity.ok("Final method test edildi - @Transactional çalışmayabilir!");
    }
    
    /**
     * 20. Flush vs Commit testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/flush-vs-commit
     */
    @PostMapping("/flush-vs-commit")
    public ResponseEntity<String> testFlushVsCommit() {
        try {
            transactionAdvancedService.flushVsCommitExample();
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Flush vs Commit test edildi - Flush edilmiş SQL geri alındı! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * 21. Manual flush testi (Batch insert)
     * 
     * curl -X POST http://localhost:8086/api/advanced/batch-insert
     */
    @PostMapping("/batch-insert")
    public ResponseEntity<String> testBatchInsert() {
        transactionAdvancedService.batchInsertWithFlush();
        return ResponseEntity.ok("Batch insert tamamlandı - Her 100 kayıtta flush yapıldı");
    }
    
    /**
     * 22. Nested transaction testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/nested-transaction
     */
    @PostMapping("/nested-transaction")
    public ResponseEntity<String> testNestedTransaction() {
        try {
            transactionAdvancedService.nestedTransactionExample();
        } catch (Exception e) {
            return ResponseEntity.ok("Nested transaction test edildi - PostgreSQL'de çalışmayabilir: " + e.getMessage());
        }
        return ResponseEntity.ok("Nested transaction test edildi");
    }
    
    /**
     * 25. Deadlock testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/deadlock-test?idA=1&idB=2
     */
    @PostMapping("/deadlock-test")
    public ResponseEntity<String> testDeadlock(
            @RequestParam Long idA,
            @RequestParam Long idB) {
        try {
            // İki thread'de farklı sıralamada lock alınarak deadlock oluşturulabilir
            deadlockService.updateEmployeeAThenB(idA, idB, 5000.0, 6000.0);
        } catch (Exception e) {
            return ResponseEntity.ok("Deadlock test edildi: " + e.getMessage());
        }
        return ResponseEntity.ok("Deadlock test edildi");
    }
    
    /**
     * 25. Deadlock çözümü - Consistent lock ordering
     * 
     * curl -X POST http://localhost:8086/api/advanced/deadlock-solution?id1=1&id2=2
     */
    @PostMapping("/deadlock-solution")
    public ResponseEntity<String> testDeadlockSolution(
            @RequestParam Long id1,
            @RequestParam Long id2) {
        deadlockService.updateEmployeesSafely(id1, id2, 5000.0, 6000.0);
        return ResponseEntity.ok("Deadlock çözümü test edildi - Consistent lock ordering");
    }
    
    /**
     * 26. Transaction propagation zinciri testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/propagation-chain \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Chain Test","email":"chain@example.com"}'
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
    
    /**
     * 27. Message gönderimi - PROBLEM
     * 
     * curl -X POST http://localhost:8086/api/advanced/message-problem \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Message Problem","email":"message@example.com"}'
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
     * 27. Message gönderimi - ÇÖZÜM (AFTER_COMMIT)
     * 
     * curl -X POST http://localhost:8086/api/advanced/message-solution \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Message Solution","email":"message2@example.com"}'
     */
    @PostMapping("/message-solution")
    public ResponseEntity<String> testMessageSolution(@RequestBody EmployeeRequest request) {
        messageService.createEmployeeWithMessageSolution(request.getName(), request.getEmail());
        return ResponseEntity.ok("Message solution test edildi - AFTER_COMMIT event ile message gönderilecek");
    }
    
    /**
     * 24. Cache tutarsızlığı - PROBLEM
     * 
     * curl -X PUT http://localhost:8086/api/advanced/cache-problem/1?newSalary=10000
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
     * 24. Cache tutarsızlığı - ÇÖZÜM (AFTER_COMMIT)
     * 
     * curl -X PUT http://localhost:8086/api/advanced/cache-solution/1?newSalary=10000
     */
    @PutMapping("/cache-solution/{id}")
    public ResponseEntity<Employee> testCacheSolution(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        Employee employee = cacheService.updateEmployeeWithCacheSolution(id, newSalary);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * Cache'den okuma
     * 
     * curl -X GET http://localhost:8086/api/advanced/cache/{id}
     */
    @GetMapping("/cache/{id}")
    public ResponseEntity<Employee> getFromCache(@PathVariable Long id) {
        Optional<Employee> employee = cacheService.getEmployeeFromCache(id);
        return employee.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 29. Read-only transaction içinde write testi
     * 
     * curl -X POST http://localhost:8086/api/advanced/read-only-write
     */
    @PostMapping("/read-only-write")
    public ResponseEntity<String> testReadOnlyWrite() {
        try {
            transactionAdvancedService.readOnlyWithWrite();
        } catch (Exception e) {
            return ResponseEntity.ok("Read-only write test edildi - Hibernate flush etmez: " + e.getMessage());
        }
        return ResponseEntity.ok("Read-only write test edildi");
    }
    
    /**
     * 30. Stream API tuzağı testi
     * 
     * curl -X GET http://localhost:8086/api/advanced/stream-trap
     */
    @GetMapping("/stream-trap")
    public ResponseEntity<List<Employee>> testStreamTrap() {
        List<Employee> employees = transactionAdvancedService.streamApiTrap();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * 30. Stream API çözümü
     * 
     * curl -X GET http://localhost:8086/api/advanced/stream-solution
     */
    @GetMapping("/stream-solution")
    public ResponseEntity<List<Employee>> testStreamSolution() {
        List<Employee> employees = transactionAdvancedService.streamApiSolution();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * 18. Interface vs Class Annotation açıklaması
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/interface-vs-class
     */
    @GetMapping("/theory/interface-vs-class")
    public ResponseEntity<String> getInterfaceVsClassExplanation() {
        return ResponseEntity.ok(theoryService.interfaceVsClassAnnotation());
    }
    
    /**
     * 23. Transaction boundary açıklaması (OSIV)
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/transaction-boundary
     */
    @GetMapping("/theory/transaction-boundary")
    public ResponseEntity<String> getTransactionBoundaryExplanation() {
        return ResponseEntity.ok(theoryService.transactionBoundaryExplanation());
    }
    
    /**
     * 28. @Transactional testlerde neden farklı davranır?
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/transactional-in-tests
     */
    @GetMapping("/theory/transactional-in-tests")
    public ResponseEntity<String> getTransactionalInTestsExplanation() {
        return ResponseEntity.ok(theoryService.transactionalInTestsExplanation());
    }
    
    /**
     * 31. Distributed transaction neden önerilmez?
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/distributed-transaction
     */
    @GetMapping("/theory/distributed-transaction")
    public ResponseEntity<String> getDistributedTransactionExplanation() {
        return ResponseEntity.ok(theoryService.distributedTransactionExplanation());
    }
    
    /**
     * 32. Transaction vs Eventual Consistency karar kriterleri
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/transaction-vs-consistency
     */
    @GetMapping("/theory/transaction-vs-consistency")
    public ResponseEntity<String> getTransactionVsConsistencyDecision() {
        return ResponseEntity.ok(theoryService.transactionVsEventualConsistencyDecision());
    }
    
    /**
     * 33. Transaction context propagation (ThreadLocal)
     * 
     * curl -X POST http://localhost:8086/api/advanced/context/threadlocal
     */
    @PostMapping("/context/threadlocal")
    public ResponseEntity<String> testThreadLocal() {
        contextService.demonstrateThreadLocal();
        return ResponseEntity.ok("ThreadLocal transaction context test edildi");
    }
    
    /**
     * 33. @Async transaction context kaybı
     * 
     * curl -X POST http://localhost:8086/api/advanced/context/async-problem
     */
    @PostMapping("/context/async-problem")
    public ResponseEntity<String> testAsyncProblem() {
        contextService.asyncMethodProblem();
        return ResponseEntity.ok("Async transaction context kaybı test edildi");
    }
    
    /**
     * 35. Hibernate dirty checking
     * 
     * curl -X PUT http://localhost:8086/api/advanced/hibernate/dirty-checking/1
     */
    @PutMapping("/hibernate/dirty-checking/{id}")
    public ResponseEntity<Employee> testDirtyChecking(@PathVariable Long id) {
        Employee employee = hibernateService.demonstrateDirtyChecking(id);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 36. Detached entity merge
     * 
     * curl -X PUT http://localhost:8086/api/advanced/hibernate/detached-entity/1
     */
    @PutMapping("/hibernate/detached-entity/{id}")
    public ResponseEntity<Employee> testDetachedEntity(@PathVariable Long id) {
        Employee employee = hibernateService.demonstrateDetachedEntity(id);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 37. Lost Update problemi
     * 
     * curl -X PUT http://localhost:8086/api/advanced/hibernate/lost-update/1?increment=1000
     */
    @PutMapping("/hibernate/lost-update/{id}")
    public ResponseEntity<Employee> testLostUpdate(
            @PathVariable Long id,
            @RequestParam Double increment) {
        Employee employee = hibernateService.lostUpdateProblem(id, increment);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 38. Optimistic locking
     * 
     * curl -X PUT http://localhost:8086/api/advanced/locking/optimistic/1?newSalary=10000
     */
    @PutMapping("/locking/optimistic/{id}")
    public ResponseEntity<String> testOptimisticLocking(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        try {
            lockingService.optimisticLockUpdate(id, newSalary);
            return ResponseEntity.ok("Optimistic locking başarılı");
        } catch (Exception e) {
            return ResponseEntity.ok("Optimistic lock exception: " + e.getMessage());
        }
    }
    
    /**
     * 39. Pessimistic locking
     * 
     * curl -X PUT http://localhost:8086/api/advanced/locking/pessimistic/1?newSalary=10000
     */
    @PutMapping("/locking/pessimistic/{id}")
    public ResponseEntity<Employee> testPessimisticLocking(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        Employee employee = lockingService.pessimisticLockUpdate(id, newSalary);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 40. Serialization failure
     * 
     * curl -X PUT http://localhost:8086/api/advanced/locking/serialization/1?newSalary=10000
     */
    @PutMapping("/locking/serialization/{id}")
    public ResponseEntity<String> testSerializationFailure(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        try {
            lockingService.serializationFailureExample(id, newSalary);
            return ResponseEntity.ok("Serialization başarılı");
        } catch (Exception e) {
            return ResponseEntity.ok("Serialization failure: " + e.getMessage());
        }
    }
    
    /**
     * 41. Retry transaction içinde (YANLIŞ)
     * 
     * curl -X PUT http://localhost:8086/api/advanced/retry/inside-transaction/1?newSalary=10000
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
     * 
     * curl -X PUT http://localhost:8086/api/advanced/remote/inside-transaction/1?newSalary=10000
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
     * 
     * curl -X PUT http://localhost:8086/api/advanced/time-sensitive/1?newSalary=10000
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
     * 
     * curl -X POST http://localhost:8086/api/advanced/silent-rollback \
     *   -H "Content-Type: application/json" \
     *   -d '{"name":"Silent Test","email":"silent@example.com"}'
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
     * 
     * curl -X POST http://localhost:8086/api/advanced/batch/wrong?count=1000
     */
    @PostMapping("/batch/wrong")
    public ResponseEntity<String> testBatchWrong(@RequestParam int count) {
        batchService.largeBatchWrong(count);
        return ResponseEntity.ok("Büyük batch (YANLIŞ) - Persistence context şişti");
    }
    
    /**
     * 49. Batch processing - DOĞRU
     * 
     * curl -X POST http://localhost:8086/api/advanced/batch/correct?totalCount=1000&chunkSize=100
     */
    @PostMapping("/batch/correct")
    public ResponseEntity<String> testBatchCorrect(
            @RequestParam int totalCount,
            @RequestParam int chunkSize) {
        batchService.largeBatchCorrect(totalCount, chunkSize);
        return ResponseEntity.ok("Batch processing (DOĞRU) - Chunk'lar halinde commit edildi");
    }
    
    /**
     * 34. Reactor + Transaction açıklaması
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/reactor-transaction
     */
    @GetMapping("/theory/reactor-transaction")
    public ResponseEntity<String> getReactorTransactionExplanation() {
        return ResponseEntity.ok(bestPracticesService.reactorAndTransaction());
    }
    
    /**
     * 43. Event publishing açıklaması
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/event-publishing
     */
    @GetMapping("/theory/event-publishing")
    public ResponseEntity<String> getEventPublishingExplanation() {
        return ResponseEntity.ok(bestPracticesService.eventPublishingExplanation());
    }
    
    /**
     * 46. TransactionState logging açıklaması
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/transaction-logging
     */
    @GetMapping("/theory/transaction-logging")
    public ResponseEntity<String> getTransactionLoggingExplanation() {
        return ResponseEntity.ok(bestPracticesService.transactionStateLogging());
    }
    
    /**
     * 47. JVM crash senaryosu açıklaması
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/jvm-crash
     */
    @GetMapping("/theory/jvm-crash")
    public ResponseEntity<String> getJvmCrashExplanation() {
        return ResponseEntity.ok(bestPracticesService.jvmCrashScenario());
    }
    
    /**
     * 48. Exactly-once semantics açıklaması
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/exactly-once
     */
    @GetMapping("/theory/exactly-once")
    public ResponseEntity<String> getExactlyOnceExplanation() {
        return ResponseEntity.ok(bestPracticesService.exactlyOnceSemantics());
    }
    
    /**
     * 50. Transaction'da olmaması gerekenler
     * 
     * curl -X GET http://localhost:8086/api/advanced/theory/what-not-in-transaction
     */
    @GetMapping("/theory/what-not-in-transaction")
    public ResponseEntity<String> getWhatNotInTransaction() {
        return ResponseEntity.ok(bestPracticesService.whatShouldNotBeInTransaction());
    }
    
    // DTOs
    public record EmployeeRequest(String name, String email, String department, Double salary) {}
    public record NotificationRequest(String message, String recipient) {}
}

