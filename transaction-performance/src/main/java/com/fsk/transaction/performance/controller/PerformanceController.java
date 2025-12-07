package com.fsk.transaction.performance.controller;

import com.fsk.transaction.performance.entity.Product;
import com.fsk.transaction.performance.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    
    private final PerformanceService performanceService;
    
    /**
     * N+1 Problem - YANLIŞ
     */
    @GetMapping("/products/n-plus-one")
    public ResponseEntity<List<Product>> getProductsWithNPlusOne() {
        List<Product> products = performanceService.getProductsWithNPlusOneProblem();
        return ResponseEntity.ok(products);
    }
    
    /**
     * N+1 Problem - ÇÖZÜM
     */
    @GetMapping("/products/fetch-join")
    public ResponseEntity<List<Product>> getProductsWithFetchJoin() {
        List<Product> products = performanceService.getProductsWithFetchJoin();
        return ResponseEntity.ok(products);
    }
    
    /**
     * Batch insert optimization
     */
    @PostMapping("/batch-insert")
    public ResponseEntity<String> batchInsert(@RequestParam int count) {
        performanceService.batchInsertOptimized(count);
        return ResponseEntity.ok("Batch insert tamamlandı: " + count + " kayıt");
    }
}



