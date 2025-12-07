package com.fsk.transaction.hibernate.controller;

import com.fsk.transaction.hibernate.entity.Employee;
import com.fsk.transaction.hibernate.service.HibernateAdvancedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hibernate")
@RequiredArgsConstructor
public class HibernateController {
    
    private final HibernateAdvancedService hibernateService;
    
    /**
     * 35. Hibernate dirty checking
     * 
     * curl -X PUT http://localhost:8088/api/hibernate/dirty-checking/1
     */
    @PutMapping("/dirty-checking/{id}")
    public ResponseEntity<Employee> testDirtyChecking(@PathVariable Long id) {
        Employee employee = hibernateService.demonstrateDirtyChecking(id);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 36. Detached entity merge
     * 
     * curl -X PUT http://localhost:8088/api/hibernate/detached-entity/1
     */
    @PutMapping("/detached-entity/{id}")
    public ResponseEntity<Employee> testDetachedEntity(@PathVariable Long id) {
        Employee employee = hibernateService.demonstrateDetachedEntity(id);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 37. Lost Update problemi
     * 
     * curl -X PUT http://localhost:8088/api/hibernate/lost-update/1?increment=1000
     */
    @PutMapping("/lost-update/{id}")
    public ResponseEntity<Employee> testLostUpdate(
            @PathVariable Long id,
            @RequestParam Double increment) {
        Employee employee = hibernateService.lostUpdateProblem(id, increment);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * 20. Flush vs Commit testi
     * 
     * curl -X POST http://localhost:8088/api/hibernate/flush-vs-commit
     */
    @PostMapping("/flush-vs-commit")
    public ResponseEntity<String> testFlushVsCommit() {
        try {
            hibernateService.flushVsCommitExample();
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Flush vs Commit test edildi - Flush edilmiş SQL geri alındı! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * 21. Manual flush testi (Batch insert)
     * 
     * curl -X POST http://localhost:8088/api/hibernate/batch-insert
     */
    @PostMapping("/batch-insert")
    public ResponseEntity<String> testBatchInsert() {
        hibernateService.batchInsertWithFlush();
        return ResponseEntity.ok("Batch insert tamamlandı - Her 100 kayıtta flush yapıldı");
    }
}



