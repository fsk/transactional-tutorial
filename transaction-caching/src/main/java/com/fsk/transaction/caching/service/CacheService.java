package com.fsk.transaction.caching.service;

import com.fsk.transaction.caching.entity.Product;
import com.fsk.transaction.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Transaction + Caching Konuları
 * 
 * Cache Coherence
 * Cache Invalidation Strategies
 * Write-Through vs Write-Behind
 * Cache-Aside Pattern
 * Transaction-aware caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    // In-memory cache (transaction-aware değil)
    private final Map<Long, Product> cache = new HashMap<>();
    
    /**
     * PROBLEM: Cache transaction içinde güncellenirse
     * Transaction rollback olsa bile cache güncellenmiş kalır
     */
    @Transactional
    public Product updateProductWithCacheProblem(Long id, Double newPrice) {
        log.info("updateProductWithCacheProblem - Cache transaction içinde (PROBLEM)");
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product bulunamadı"));
        
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);
        
        // Cache güncelleniyor - Transaction içinde (YANLIŞ!)
        cache.put(id, saved);
        log.info("Cache güncellendi: {}", id);
        
        throw new RuntimeException("DB rollback olacak ama cache güncellenmiş kalır!");
    }
    
    /**
     * ÇÖZÜM: AFTER_COMMIT event kullan
     * Cache sadece transaction commit olduktan sonra güncellenir
     */
    @Transactional
    public Product updateProductWithCacheSolution(Long id, Double newPrice) {
        log.info("updateProductWithCacheSolution - AFTER_COMMIT event ile");
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product bulunamadı"));
        
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);
        
        eventPublisher.publishEvent(new ProductUpdatedEvent(id, saved));
        
        return saved;
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("handleProductUpdated - AFTER_COMMIT - Cache güncelleniyor");
        cache.put(event.getProductId(), event.getProduct());
        log.info("Cache güncellendi: {}", event.getProductId());
    }
    
    /**
     * Cache-Aside Pattern
     */
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public Product getProductWithCache(Long id) {
        log.info("getProductWithCache - Cache-Aside Pattern");
        
        Optional<Product> cached = Optional.ofNullable(cache.get(id));
        if (cached.isPresent()) {
            log.info("Cache hit: {}", id);
            return cached.get();
        }
        
        log.info("Cache miss: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product bulunamadı"));
        
        cache.put(id, product);
        return product;
    }
    
    /**
     * Cache invalidation
     */
    @CacheEvict(value = "products", key = "#id")
    @Transactional
    public void evictProductFromCache(Long id) {
        log.info("evictProductFromCache - Cache invalidation");
        cache.remove(id);
    }
    
    public Optional<Product> getProductFromCache(Long id) {
        return Optional.ofNullable(cache.get(id));
    }
    
    public static class ProductUpdatedEvent {
        private final Long productId;
        private final Product product;
        
        public ProductUpdatedEvent(Long productId, Product product) {
            this.productId = productId;
            this.product = product;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public Product getProduct() {
            return product;
        }
    }
}


