package com.fsk.transaction.locking.controller;

import com.fsk.transaction.locking.entity.Employee;
import com.fsk.transaction.locking.entity.EmployeeWithVersion;
import com.fsk.transaction.locking.service.DeadlockService;
import com.fsk.transaction.locking.service.LockingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locking")
@RequiredArgsConstructor
public class LockingController {
    
    private final LockingService lockingService;
    private final DeadlockService deadlockService;
    
    /**
     * 38. Optimistic locking
     * 
     * curl -X PUT http://localhost:8089/api/locking/optimistic/1?newSalary=10000
     */
    @PutMapping("/optimistic/{id}")
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
     * curl -X PUT http://localhost:8089/api/locking/pessimistic/1?newSalary=10000
     */
    @PutMapping("/pessimistic/{id}")
    public ResponseEntity<Employee> testPessimisticLocking(
            @PathVariable Long id,
            @RequestParam Double newSalary) {
        Employee employee = lockingService.pessimisticLockUpdate(id, newSalary);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 40. Serialization failure
     * 
     * curl -X PUT http://localhost:8089/api/locking/serialization/1?newSalary=10000
     */
    @PutMapping("/serialization/{id}")
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
     * 25. Deadlock testi
     * 
     * curl -X POST http://localhost:8089/api/locking/deadlock-test?idA=1&idB=2
     */
    @PostMapping("/deadlock-test")
    public ResponseEntity<String> testDeadlock(
            @RequestParam Long idA,
            @RequestParam Long idB) {
        try {
            deadlockService.updateEmployeeAThenB(idA, idB, 5000.0, 6000.0);
            return ResponseEntity.ok("Deadlock test edildi");
        } catch (Exception e) {
            return ResponseEntity.ok("Deadlock test edildi: " + e.getMessage());
        }
    }
    
    /**
     * 25. Deadlock çözümü - Consistent lock ordering
     * 
     * curl -X POST http://localhost:8089/api/locking/deadlock-solution?id1=1&id2=2
     */
    @PostMapping("/deadlock-solution")
    public ResponseEntity<String> testDeadlockSolution(
            @RequestParam Long id1,
            @RequestParam Long id2) {
        deadlockService.updateEmployeesSafely(id1, id2, 5000.0, 6000.0);
        return ResponseEntity.ok("Deadlock çözümü test edildi - Consistent lock ordering");
    }
}


