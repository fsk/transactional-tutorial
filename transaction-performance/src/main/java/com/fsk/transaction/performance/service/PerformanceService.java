package com.fsk.transaction.performance.service;

import com.fsk.transaction.performance.entity.Product;
import com.fsk.transaction.performance.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transaction + Performance Konuları
 * 
 * Connection Pooling
 * Query Optimization
 * Batch Operations
 * N+1 Problem
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {
    
    private final ProductRepository productRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * N+1 Problem - YANLIŞ
     * Her product için ayrı category sorgusu
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsWithNPlusOneProblem() {
        log.info("getProductsWithNPlusOneProblem - N+1 Problem");
        
        List<Product> products = productRepository.findAll();
        
        // N+1 Problem: Her product için category lazy load edilir
        products.forEach(product -> {
            log.info("Product: {}, Category: {}", product.getName(), product.getCategory().getName());
        });
        
        return products;
    }
    
    /**
     * N+1 Problem - ÇÖZÜM: Fetch Join
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsWithFetchJoin() {
        log.info("getProductsWithFetchJoin - Fetch Join ile N+1 çözümü");
        
        // Fetch join ile tek sorguda category'ler de yüklenir
        List<Product> products = productRepository.findAllWithCategory();
        
        products.forEach(product -> {
            log.info("Product: {}, Category: {}", product.getName(), product.getCategory().getName());
        });
        
        return products;
    }
    
    /**
     * Batch insert optimization
     */
    @Transactional
    public void batchInsertOptimized(int count) {
        log.info("batchInsertOptimized - Optimized batch insert: {} kayıt", count);
        
        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(100.0 + i);
            product.setStock(100);
            
            productRepository.save(product);
            
            // Her 50 kayıtta bir flush
            if (i % 50 == 0 && i > 0) {
                entityManager.flush();
                entityManager.clear();
                log.info("Flush yapıldı: {} kayıt", i);
            }
        }
        
        log.info("Batch insert tamamlandı");
    }
}



