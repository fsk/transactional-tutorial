package com.fsk.transaction.caching.controller;

import com.fsk.transaction.caching.entity.Product;
import com.fsk.transaction.caching.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/caching")
@RequiredArgsConstructor
public class CachingController {
    
    private final CacheService cacheService;
    
    /**
     * Cache tutarsızlığı - PROBLEM
     */
    @PutMapping("/cache-problem/{id}")
    public ResponseEntity<String> testCacheProblem(
            @PathVariable Long id,
            @RequestParam Double newPrice) {
        try {
            cacheService.updateProductWithCacheProblem(id, newPrice);
        } catch (RuntimeException e) {
            return ResponseEntity.ok("Cache problem test edildi - DB rollback oldu ama cache güncellenmiş kalır! " + e.getMessage());
        }
        return ResponseEntity.ok("Beklenmeyen durum");
    }
    
    /**
     * Cache tutarsızlığı - ÇÖZÜM
     */
    @PutMapping("/cache-solution/{id}")
    public ResponseEntity<Product> testCacheSolution(
            @PathVariable Long id,
            @RequestParam Double newPrice) {
        Product product = cacheService.updateProductWithCacheSolution(id, newPrice);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Cache-Aside Pattern
     */
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductWithCache(@PathVariable Long id) {
        Product product = cacheService.getProductWithCache(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Cache invalidation
     */
    @DeleteMapping("/cache/{id}")
    public ResponseEntity<String> evictCache(@PathVariable Long id) {
        cacheService.evictProductFromCache(id);
        return ResponseEntity.ok("Cache invalidated: " + id);
    }
}


